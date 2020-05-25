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
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public final class ActionEventSource {
    private ActionEventSource() {}

    public static Observable<ActionEvent> fromActionEvents(final Node node) {
        return NodeEventSource.fromNodeEvents(node, ActionEvent.ACTION);
    }
    public static Observable<ActionEvent> fromActionEvents(final ContextMenu source) {

        return Observable.create((ObservableEmitter<ActionEvent> subscriber) ->  {
            final EventHandler<ActionEvent> handler = subscriber::onNext;

            source.addEventHandler(ActionEvent.ANY, handler);

            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeEventHandler(ActionEvent.ANY, handler)));
        }).subscribeOn(JavaFxScheduler.platform());
    }
    public static Observable<ActionEvent> fromActionEvents(final MenuItem source) {
        return Observable.create((ObservableEmitter<ActionEvent> subscriber) -> {
            final EventHandler<ActionEvent> handler = subscriber::onNext;

            source.addEventHandler(ActionEvent.ANY, handler);

            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeEventHandler(ActionEvent.ANY, handler)));
        }).subscribeOn(JavaFxScheduler.platform());
    }

}
