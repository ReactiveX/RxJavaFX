package rx.schedulers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.BooleanSubscription;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.SerialSubscription;
import rx.subscriptions.Subscriptions;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * Executes work on the JavaFx UI thread.
 * This scheduler should only be used with actions that execute quickly.
 */
public final class JavaFxScheduler extends Scheduler {
    private static final JavaFxScheduler INSTANCE = new JavaFxScheduler();

    /* package for unit test */JavaFxScheduler() {
    }

    public static JavaFxScheduler getInstance() {
        return INSTANCE;
    }

    private static void assertThatTheDelayIsValidForTheJavaFxTimer(long delay) {
        if (delay < 0 || delay > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(String.format("The JavaFx timer only accepts non-negative delays up to %d milliseconds.", Integer.MAX_VALUE));
        }
    }

    @Override
    public Worker createWorker() {
        return new InnerJavaFxScheduler();
    }

    private static class InnerJavaFxScheduler extends Worker implements Runnable {

        private final CompositeSubscription tracking = new CompositeSubscription();

        /** Allows cheaper trampolining than invokeLater(). Accessed from EDT only. */
        private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
        /** Allows cheaper trampolining than invokeLater(). Accessed from EDT only. */
        private int wip;

        @Override
        public void unsubscribe() {
            tracking.unsubscribe();
        }

        @Override
        public boolean isUnsubscribed() {
            return tracking.isUnsubscribed();
        }

        @Override
        public Subscription schedule(final Action0 action, long delayTime, TimeUnit unit) {
            long delay = Math.max(0,unit.toMillis(delayTime));
            assertThatTheDelayIsValidForTheJavaFxTimer(delay);

            class DualAction implements EventHandler<ActionEvent>, Subscription, Runnable {
                private Timeline timeline;
                final SerialSubscription subs = new SerialSubscription();
                boolean nonDelayed;

                private void setTimer(Timeline timeline) {
                    this.timeline = timeline;
                }

                @Override
                public void handle(ActionEvent event) {
                    run();
                }

                @Override
                public void run() {
                    if (nonDelayed) {
                        try {
                            if (tracking.isUnsubscribed() || isUnsubscribed()) {
                                return;
                            }
                            action.call();
                        } finally {
                            subs.unsubscribe();
                        }
                    } else {
                        timeline.stop();
                        timeline = null;
                        nonDelayed = true;
                        trampoline(this);
                    }
                }

                @Override
                public boolean isUnsubscribed() {
                    return subs.isUnsubscribed();
                }

                @Override
                public void unsubscribe() {
                    subs.unsubscribe();
                }
                public void set(Subscription s) {
                    subs.set(s);
                }
            }

            final DualAction executeOnce = new DualAction();
            tracking.add(executeOnce);

            final Timeline timer = new Timeline(new KeyFrame(Duration.millis(delay), executeOnce));
            executeOnce.setTimer(timer);
            timer.play();

            executeOnce.set(Subscriptions.create(() -> {
                timer.stop();
                tracking.remove(executeOnce);
            }));

            return executeOnce;
        }

        @Override
        public Subscription schedule(final Action0 action) {
            final BooleanSubscription s = BooleanSubscription.create();
            Runnable runnable = () -> {
                try {
                    if (tracking.isUnsubscribed() || s.isUnsubscribed()) {
                        return;
                    }
                    action.call();
                } finally {
                    tracking.remove(s);
                }
            };
            tracking.add(s);

            if (Platform.isFxApplicationThread()) {
                if (trampoline(runnable)) {
                    return Subscriptions.unsubscribed();
                }
                else {
                    queue.offer(runnable);
                    Platform.runLater(this);
                }
            }

            // wrap for returning so it also removes it from the 'innerSubscription'
            return Subscriptions.create(() -> tracking.remove(s));
        }
        /**
         * Uses a fast-path/slow path trampolining and tries to run
         * the given runnable directly.
         * @param runnable
         * @return true if the fast path was taken
         */
        boolean trampoline(Runnable runnable) {
            // fast path: if wip increments from 0 to 1
            if (wip == 0) {
                wip = 1;
                runnable.run();
                // but a recursive schedule happened
                if (--wip > 0) {
                    do {
                        Runnable r = queue.poll();
                        r.run();
                    } while (--wip > 0);
                }
                return true;
            }
            queue.offer(runnable);
            run();
            return false;
        }
        @Override
        public void run() {
            if (wip++ == 0) {
                do {
                    Runnable r = queue.poll();
                    r.run();
                } while (--wip > 0);
            }
        }
    }
}