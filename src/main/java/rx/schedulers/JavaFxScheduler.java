/**
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.schedulers;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.disposables.SerialDisposable;
import io.reactivex.internal.subscriptions.BooleanSubscription;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;
import org.reactivestreams.Subscription;

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

        private final CompositeDisposable tracking = new CompositeDisposable();

        /** Allows cheaper trampolining than invokeLater(). Accessed from EDT only. */
        private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
        /** Allows cheaper trampolining than invokeLater(). Accessed from EDT only. */
        private int wip;

        @Override
        public void dispose() {
            tracking.dispose();
        }

        @Override
        public boolean isDisposed() {
            return tracking.isDisposed();
        }

        @Override
        public Disposable schedule(final Runnable action, long delayTime, TimeUnit unit) {
            long delay = Math.max(0,unit.toMillis(delayTime));
            assertThatTheDelayIsValidForTheJavaFxTimer(delay);

            class DualAction implements EventHandler<ActionEvent>, Subscription, Runnable {
                private Timeline timeline;
                final SerialDisposable subs = new SerialDisposable();
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
                            if (tracking.isDisposed() || isUnsubscribed()) {
                                return;
                            }
                            action.run();
                        } finally {
                            subs.dispose();
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
                    return subs.isDisposed();
                }

                @Override
                public void request(long n) {
                    //not quite sure what to do here
                }

                @Override
                public void cancel() {
                    subs.dispose();
                }
                public void set(Disposable s) {
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
        public Disposable schedule(final Runnable action) {
            final BooleanSubscription s = new BooleanSubscription();
            Runnable runnable = () -> {
                try {
                    if (tracking.isDisposed() || s.isCancelled()) {
                        return;
                    }
                    action.run();
                } finally {
                    tracking.remove(s);
                }
            };
            tracking.add(s);

            if (Platform.isFxApplicationThread()) {
                if (trampoline(runnable)) {
                    return Disposables.disposed();
                }
            }else {
                queue.offer(runnable);
                Platform.runLater(this);
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
