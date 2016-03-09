/**
 * Copyright 2014 Netflix, Inc.
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

import javafx.application.Platform;
import rx.Scheduler;
import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.BooleanSubscription;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.SerialSubscription;
import rx.subscriptions.Subscriptions;

import java.awt.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;


public final class JavaFxScheduler extends Scheduler {
    private static final JavaFxScheduler INSTANCE = new JavaFxScheduler();

    public static JavaFxScheduler getInstance() {
        return INSTANCE;
    }

    /* package for unit test */JavaFxScheduler() {
    }

    @Override
    public Worker createWorker() {
        return new InnerPlatformScheduler();
    }

    private static class InnerPlatformScheduler extends Worker implements Runnable {

        private final CompositeSubscription tracking = new CompositeSubscription();

        /**
         * Allows cheaper trampolining than invokeLater(). Accessed from EDT only.
         */
        private final Queue<Runnable> queue = new ConcurrentLinkedQueue<Runnable>();
        /**
         * Allows cheaper trampolining than invokeLater(). Accessed from EDT only.
         */
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
            long delay = Math.max(0, unit.toMillis(delayTime));
            assertThatTheDelayIsValidForTheSwingTimer(delay);

            class DualAction implements ActionListener, Subscription, Runnable {
                private Timer timer;
                final SerialSubscription subs = new SerialSubscription();
                boolean nonDelayed;

                private void setTimer(Timer timer) {
                    this.timer = timer;
                }

                @Override
                public void actionPerformed(ActionEvent e) {
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
                        timer.stop();
                        timer = null;
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

            final Timer timer = new Timer((int) delay, executeOnce);
            executeOnce.setTimer(timer);
            timer.start();

            executeOnce.set(Subscriptions.create(new Action0() {
                @Override
                public void call() {
                    timer.stop();
                    tracking.remove(executeOnce);
                }
            }));

            return executeOnce;
        }

        @Override
        public Subscription schedule(final Action0 action) {
            final BooleanSubscription s = BooleanSubscription.create();

            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        if (tracking.isUnsubscribed() || s.isUnsubscribed()) {
                            return;
                        }
                        action.call();
                    } finally {
                        tracking.remove(s);
                    }
                }
            };
            tracking.add(s);

            if (Platform.isFxApplicationThread()) {
                if (trampoline(runnable)) {
                    return Subscriptions.unsubscribed();
                }
            } else {
                queue.offer(runnable);
                EventQueue.invokeLater(this);
            }

            // wrap for returning so it also removes it from the 'innerSubscription'
            return Subscriptions.create(new Action0() {

                @Override
                public void call() {
                    tracking.remove(s);
                }

            });
        }

        /**
         * Uses a fast-path/slow path trampolining and tries to run
         * the given runnable directly.
         *
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


    private static void assertThatTheDelayIsValidForTheSwingTimer(long delay) {
        if (delay < 0 || delay > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(String.format("The swing timer only accepts non-negative delays up to %d milliseconds.", Integer.MAX_VALUE));
        }
    }
}