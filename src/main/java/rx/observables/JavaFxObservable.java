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
package rx.observables;


import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import rx.Observable;
import rx.functions.Func1;
import rx.javafx.sources.*;


public enum JavaFxObservable {
    ; // no instances


    /**
     * Creates an observable corresponding to javafx Node events.
     *
     * @param node      The target of the UI events.
     * @param eventType The type of the observed UI events
     * @return An Observable of UI events, appropriately typed
     */
    public static <T extends Event> Observable<T> fromNodeEvents(final Node node, final EventType<T> eventType) {
        return NodeEventSource.fromNodeEvents(node, eventType);
    }

    /**
     * Create an rx Observable from a javafx ObservableValue
     *
     * @param fxObservable the observed ObservableValue
     * @param <T>          the type of the observed value
     * @return an Observable emitting values as the wrapped ObservableValue changes
     */
    public static <T> Observable<T> fromObservableValue(final ObservableValue<T> fxObservable) {
        return ObservableValueSource.fromObservableValue(fxObservable);
    }

    /**
     * Create an rx Observable from a javafx ObservableValue, and emits changes with old and new value pairs
     *
     * @param fxObservable the observed ObservableValue
     * @param <T>          the type of the observed value
     * @return an Observable emitting values as the wrapped ObservableValue changes
     */
    public static <T> Observable<Change<T>> fromObservableValueChanges(final ObservableValue<T> fxObservable) {
        return ObservableValueSource.fromObservableValueChanges(fxObservable);
    }
    /**
     * Creates an observable corresponding to javafx Scene events.
     *
     * @param scene      The target of the UI events.
     * @param eventType The type of the observed UI events
     * @return An Observable of UI events, appropriately typed
     */
    public static <T extends Event> Observable<T> fromSceneEvents(final Scene scene, final EventType<T> eventType) {
        return SceneEventSource.fromSceneEvents(scene,eventType);
    }

    /**
     * Creates an observable corresponding to javafx Window events.
     *
     * @param window      The target of the UI events.
     * @param eventType The type of the observed UI events
     * @return An Observable of UI events, appropriately typed
     */
    public static <T extends WindowEvent> Observable<T> fromWindowEvents(final Window window, final EventType<T> eventType) {
        return WindowEventSource.fromWindowEvents(window,eventType);
    }

    /**
     * Creates an observable corresponding to javafx Node action events.
     *
     * @param node      The target of the ActionEvents
     * @return An Observable of UI ActionEvents
     */
    public static Observable<ActionEvent> fromActionEvents(final Node node) {
        return ActionEventSource.fromActionEvents(node);
    }

    /**
     * Creates an observable corresponding to javafx ContextMenu action events.
     *
     * @param contextMenu      The target of the ActionEvents
     * @return An Observable of UI ActionEvents
     */
    public static Observable<ActionEvent> fromActionEvents(final ContextMenu contextMenu) {
        return ActionEventSource.fromActionEvents(contextMenu);
    }

    /**
     * Creates an observable corresponding to javafx MenuItem action events.
     *
     * @param menuItem      The target of the ActionEvents
     * @return An Observable of UI ActionEvents
     */
    public static Observable<ActionEvent> fromActionEvents(final MenuItem menuItem) {
        return ActionEventSource.fromActionEvents(menuItem);
    }

    public static <T> Observable<ObservableList<T>> fromObservableList(final ObservableList<T> source) {
        return ObservableListSource.fromObservableList(source);
    }

    public static <T> Observable<T> fromObservableListAdds(final ObservableList<T> source) {
        return ObservableListSource.fromObservableListAdds(source);
    }

    public static <T> Observable<T> fromObservableListRemovals(final ObservableList<T> source) {
        return ObservableListSource.fromObservableListRemovals(source);
    }

    public static <T> Observable<T> fromObservableListUpdates(final ObservableList<T> source) {
        return ObservableListSource.fromObservableListUpdates(source);
    }

    public static <T> Observable<ListChange<T>> fromObservableListChanges(final ObservableList<T> source) {
        return ObservableListSource.fromObservableListChanges(source);
    }

    public static <T> Observable<ListChange<T>> fromObservableListDistinctChanges(final ObservableList<T> source) {
        return ObservableListSource.fromObservableListDistinctChanges(source);
    }
    public static <T,R> Observable<ListChange<R>> fromObservableListDistinctMappings(final ObservableList<T> source, Func1<T,R> mapper) {
        return ObservableListSource.fromObservableListDistinctMappings(source,mapper);
    }
}
