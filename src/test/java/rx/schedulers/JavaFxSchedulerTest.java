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

import io.reactivex.Scheduler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Executes work on the JavaFx UI thread.
 * This scheduler should only be used with actions that execute quickly.
 */
public final class JavaFxSchedulerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static void waitForEmptyEventQueue() throws Exception {
        FXUtilities.runAndWait(() -> {
            // nothing to do, we're just waiting here for the event queue to be emptied
        });
    }

    public static class AsNonApp extends Application {
        @Override
        public void start(Stage primaryStage) throws Exception {
            // noop
        }
    }

    @BeforeClass
    public static void initJFX() {
        //use panel to initialize JavaFX thread
        JFXPanel panel = new JFXPanel();
    }

    @Test
    public void testPeriodicScheduling() throws Exception {
        final JavaFxScheduler scheduler = new JavaFxScheduler();
        final Scheduler.Worker inner = scheduler.createWorker();

        final CountDownLatch latch = new CountDownLatch(4);

        final Runnable innerAction = mock(Runnable.class);
        final Runnable action = () -> {
            try {
                innerAction.run();
                assertTrue(Platform.isFxApplicationThread());
            } finally {
                latch.countDown();
            }
        };

        inner.schedulePeriodically(action, 100, 400, TimeUnit.MILLISECONDS);

        if (!latch.await(5, TimeUnit.SECONDS)) {
            fail("timed out waiting for tasks to execute");
        }

        inner.dispose();
        waitForEmptyEventQueue();
        verify(innerAction, times(4)).run();
    }

    @Test
    public void testNestedActions() throws Exception {
        final JavaFxScheduler scheduler = new JavaFxScheduler();
        final Scheduler.Worker inner = scheduler.createWorker();

        final Runnable firstStepStart = mock(Runnable.class);
        final Runnable firstStepEnd = mock(Runnable.class);

        final Runnable secondStepStart = mock(Runnable.class);
        final Runnable secondStepEnd = mock(Runnable.class);

        final Runnable thirdStepStart = mock(Runnable.class);
        final Runnable thirdStepEnd = mock(Runnable.class);

        final Runnable firstAction = () -> {
            assertTrue(Platform.isFxApplicationThread());
            firstStepStart.run();
            firstStepEnd.run();
        };
        final Runnable secondAction = () -> {
            assertTrue(Platform.isFxApplicationThread());
            secondStepStart.run();
            inner.schedule(firstAction);
            secondStepEnd.run();
        };
        final Runnable thirdAction = () -> {
            assertTrue(Platform.isFxApplicationThread());
            thirdStepStart.run();
            inner.schedule(secondAction);
            thirdStepEnd.run();
        };

        InOrder inOrder = inOrder(firstStepStart, firstStepEnd, secondStepStart, secondStepEnd, thirdStepStart, thirdStepEnd);

        inner.schedule(thirdAction);
        waitForEmptyEventQueue();

        inOrder.verify(thirdStepStart, times(1)).run();
        inOrder.verify(thirdStepEnd, times(1)).run();
        inOrder.verify(secondStepStart, times(1)).run();
        inOrder.verify(secondStepEnd, times(1)).run();
        inOrder.verify(firstStepStart, times(1)).run();
        inOrder.verify(firstStepEnd, times(1)).run();
    }

    /*
     * based on http://www.guigarage.com/2013/01/invokeandwait-for-javafx/
     * by hendrikebbers
     */
    static public class FXUtilities {

        /**
         * Simple helper class.
         *
         * @author hendrikebbers
         */
        private static class ThrowableWrapper {
            Throwable t;
        }

        /**
         * Invokes a Runnable in JFX Thread and waits while it's finished. Like
         * SwingUtilities.invokeAndWait does for EDT.
         *
         * @param run The Runnable that has to be called on JFX thread.
         * @throws InterruptedException f the execution is interrupted.
         * @throws ExecutionException   If a exception is occurred in the run method of the Runnable
         */
        public static void runAndWait( final Runnable run) throws InterruptedException, ExecutionException {
            if (Platform.isFxApplicationThread()) {
                try {
                    run.run();
                } catch (Exception e) {
                    throw new ExecutionException(e);
                }
            } else {
                final Lock lock = new ReentrantLock();
                final Condition condition = lock.newCondition();
                final ThrowableWrapper throwableWrapper = new ThrowableWrapper();
                lock.lock();
                try {
                    Platform.runLater(() -> {
                        lock.lock();
                        try {
                            run.run();
                        } catch (Throwable e) {
                            throwableWrapper.t = e;
                        } finally {
                            try {
                                condition.signal();
                            } finally {
                                lock.unlock();
                            }
                        }
                    });
                    condition.await();
                    if (throwableWrapper.t != null) {
                        throw new ExecutionException(throwableWrapper.t);
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
