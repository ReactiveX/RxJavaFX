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
package io.reactivex.rxjavafx.sources;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.rxjavafx.subscriptions.JavaFxSubscriptions;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;

public class NodeEventSource {
    public static <T extends Event> Observable<T> fromNodeEvents(final Node source, final EventType<T> eventType) {

        return Observable.create((ObservableEmitter<T> emitter) -> {
            final EventHandler<T> handler = emitter::onNext;

            source.addEventHandler(eventType, handler);

            emitter.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeEventHandler(eventType, handler)));
        }).subscribeOn(JavaFxScheduler.platform());
    }
}
