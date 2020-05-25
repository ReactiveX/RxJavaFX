/**
 * Copyright 2017 Netflix, Inc.
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
package io.reactivex.rxjavafx.subscriptions;


import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.Disposable;
import javafx.application.Platform;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;


public final class JavaFxSubscriptions {

    private JavaFxSubscriptions() {
        // no instance
    }

    /**
     * Create a Disposable that always runs <code>unsubscribe</code> in the event dispatch thread.
     *
     * @param unsubscribe the runnable to be performed in the ui thread at un-subscription
     * @return an Disposable that always runs <code>unsubscribe</code> in the event dispatch thread.
     */
    public static Disposable unsubscribeInEventDispatchThread(final Runnable unsubscribe) {
        return Disposable.fromRunnable(() -> {
            if (Platform.isFxApplicationThread()) {
                unsubscribe.run();
            } else {
                final Scheduler.Worker inner = JavaFxScheduler.platform().createWorker();
                inner.schedule(() -> {
                    unsubscribe.run();
                    inner.dispose();
                });
            }
        });
    }
}
