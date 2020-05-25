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
package io.reactivex.rxjavafx.observables;


import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjavafx.sources.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;


public enum JavaFxObservable {
    ; // no instances


    /**
     * Creates an observable corresponding to JavaFX Node events.
     *
     * @param node      The target of the UI events.
     * @param eventType The type of the observed UI events
     * @return An Observable of UI events, appropriately typed
     */
    public static <T extends Event> Observable<T> eventsOf(final Node node, final EventType<T> eventType) {
        return NodeEventSource.fromNodeEvents(node, eventType);
    }

  /**
     * Create an rx Observable from a JavaFX ObservableValue
     *
     * @param fxObservable the observed ObservableValue
     * @param <T>          the type of the observed value
     * @return an Observable emitting non-null values as the wrapped ObservableValue changes
     */
    public static <T> Observable<T> valuesOf(final ObservableValue<T> fxObservable) {
        return ObservableValueSource.fromObservableValue(fxObservable);
    }

    /**
     * Create an rx Observable from a JavaFX ObservableValue
     *
     * @param fxObservable the observed ObservableValue
     * @param nullSentinel the default sentinel value emitted when the observable is null
     * @param <T>          the type of the observed value
     * @return an Observable emitting values as the wrapped ObservableValue changes, null will be replaces with nullSentinel
     */
    public static <T> Observable<T> valuesOf(final ObservableValue<T> fxObservable, final T nullSentinel) {
        return ObservableValueSource.fromObservableValue(fxObservable, nullSentinel);
    }

    /**
     * Create an rx Observable from a JavaFX ObservableValue
     *
     * @param fxObservable the observed ObservableValue
     * @param <T>          the type of the observed value
     * @return an Observable emitting nullable values as the wrapped ObservableValue changes
     */
    public static <T> Observable<Optional<T>> nullableValuesOf(final ObservableValue<T> fxObservable) {
        return ObservableValueSource.fromNullableObservableValue(fxObservable);
    }

    /**
     * Create an rx Observable from a javafx ObservableValue, and emits changes with old and new value pairs
     *
     * @param fxObservable the observed ObservableValue
     * @param <T>          the type of the observed value
     * @return an Observable emitting values as the wrapped ObservableValue changes
     */
    public static <T> Observable<Change<T>> changesOf(final ObservableValue<T> fxObservable) {
        return ObservableValueSource.fromObservableValueChanges(fxObservable);
    }
    /**
     * Create an rx Observable from a javafx ObservableValue, and emits changes with non-null old and new value pairs
     *
     * @param fxObservable the observed ObservableValue
     * @param <T>          the type of the observed value
     * @return an Observable emitting values as the wrapped ObservableValue changes
     */
    public static <T> Observable<Change<T>> nonNullChangesOf(final ObservableValue<T> fxObservable) {
        return changesOf(fxObservable).filter(t -> t.getOldVal() != null && t.getNewVal() != null);
    }

    /**
     * Emits a JavaFX Observable every time it is invalidated.
     * @param fxObservable
     * @return
     */
    public static Observable<javafx.beans.Observable> invalidationsOf(final javafx.beans.Observable fxObservable) {
        return ObservableValueSource.fromInvalidations(fxObservable);
    }


    /**
     * Creates an observable corresponding to javafx Scene events.
     *
     * @param scene      The target of the UI events.
     * @param eventType The type of the observed UI events
     * @return An Observable of UI events, appropriately typed
     */
    public static <T extends Event> Observable<T> eventsOf(final Scene scene, final EventType<T> eventType) {
        return SceneEventSource.fromSceneEvents(scene,eventType);
    }



    /**
     * Creates an observable corresponding to javafx Window events.
     *
     * @param window      The target of the UI events.
     * @param eventType The type of the observed UI events
     * @return An Observable of UI events, appropriately typed
     */
    public static <T extends WindowEvent> Observable<T> eventsOf(final Window window, final EventType<T> eventType) {
        return WindowEventSource.fromWindowEvents(window,eventType);
    }


    /**
     * Creates an observable corresponding to javafx Node action events.
     *
     * @param node      The target of the ActionEvents
     * @return An Observable of UI ActionEvents
     */
    public static Observable<ActionEvent> actionEventsOf(final Node node) {
        return ActionEventSource.fromActionEvents(node);
    }


    /**
     * Creates an observable corresponding to javafx ContextMenu action events.
     *
     * @param contextMenu      The target of the ActionEvents
     * @return An Observable of UI ActionEvents
     */
    public static Observable<ActionEvent> actionEventsOf(final ContextMenu contextMenu) {
        return ActionEventSource.fromActionEvents(contextMenu);
    }



    /**
     * Creates an observable corresponding to javafx MenuItem action events.
     *
     * @param menuItem      The target of the ActionEvents
     * @return An Observable of UI ActionEvents
     */
    public static Observable<ActionEvent> actionEventsOf(final MenuItem menuItem) {
        return ActionEventSource.fromActionEvents(menuItem);
    }


    /**
     * Creates an observable that emits an ObservableList every time it is modified
     *
     * @param source      The target ObservableList of the ListChange events
     * @return An Observable emitting the ObservableList each time it changes
     */
    public static <T> Observable<ObservableList<T>> emitOnChanged(final ObservableList<T> source) {
        return ObservableListSource.fromObservableList(source);
    }


    /**
     * Creates an observable that emits all additions to an ObservableList
     *
     * @param source      The target ObservableList for the item add events
     * @return An Observable emitting items added to the ObservableList
     */
    public static <T> Observable<T> additionsOf(final ObservableList<T> source) {
        return ObservableListSource.fromObservableListAdds(source);
    }



    /**
     * Creates an observable that emits all removal items from an ObservableList
     *
     * @param source      The target ObservableList for the item removal events
     * @return An Observable emitting items removed from the ObservableList
     */
    public static <T> Observable<T> removalsOf(final ObservableList<T> source) {
        return ObservableListSource.fromObservableListRemovals(source);
    }


    /**
     * Creates an observable that emits all updated items from an ObservableList.
     * If you declare an ObservableList that listens to one or more properties of each element,
     * you can emit the changed items every time these properties are modified
     * @param source      The target ObservableList for the item update events
     *
     * @return An Observable emitting items updated in the ObservableList
     */
    public static <T> Observable<T> updatesOf(final ObservableList<T> source) {
        return ObservableListSource.fromObservableListUpdates(source);
    }


    /**
     * Emits all added, removed, and updated items from an ObservableList
     * @param source
     * @return An Observable emitting changed items with an ADDED, REMOVED, or UPDATED flag
     */
    public static <T> Observable<ListChange<T>> changesOf(final ObservableList<T> source) {
        return ObservableListSource.fromObservableListChanges(source);
    }


    /**
     * Emits distinctly  added and removed items from an ObservableList.
     * If dupe items with identical hashcode/equals evaluations are added to an ObservableList, only the first one will fire an ADDED item.
     * When the last dupe is removed, only then will it fire a REMOVED item.
     * @param source
     * @return An Observable emitting changed items with an ADDED, REMOVED, or UPDATED flag
     */
    public static <T> Observable<ListChange<T>> distinctChangesOf(final ObservableList<T> source) {
        return ObservableListSource.fromObservableListDistinctChanges(source);
    }


    /**
     * Emits distinctly added and removed T items from an ObservableList based on a mapping to an R value.
     * If dupe mapped R items with identical hashcode/equals evaluations are added to an ObservableList, only the first one will fire an ADDED T item.
     * When the last R dupe is removed, only then will it fire a REMOVED T item.
     * @param source
     * @return An Observable emitting changed items with an ADDED, REMOVED, or UPDATED flag
     */
    public static <T,R> Observable<ListChange<T>> distinctChangesOf(final ObservableList<T> source, Function<T,R> mapper) {
        return ObservableListSource.fromObservableListDistinctChanges(source,mapper);
    }



    /**
     * Emits distinctly added and removed mappings to each R item from an ObservableList.
     * If dupe mapped R items with identical hashcode/equals evaluations are added to an ObservableList, only the first one will fire an ADDED R item.
     * When the last dupe is removed, only then will it fire a REMOVED R item.
     * @param source
     * @return An Observable emitting changed mapped items with an ADDED, REMOVED, or UPDATED flag
     */
    public static <T,R> Observable<ListChange<R>> distinctMappingsOf(final ObservableList<T> source, Function<T,R> mapper) {
        return ObservableListSource.fromObservableListDistinctMappings(source,mapper);
    }


    /**
     * Creates an observable that emits an ObservableMap every time it is modified
     *
     * @param source      The target ObservableMap of the MapChange events
     * @return An Observable emitting the ObservableMap each time it changes
     */
    public static <K,T> Observable<ObservableMap<K,T>> emitOnChanged(final ObservableMap<K,T> source) {
        return ObservableMapSource.fromObservableMap(source);
    }

    /**
     * Creates an observable that emits all additions to an ObservableMap
     *
     * @param source      The target ObservableMap for the item add events
     * @return An Observable emitting Entry items added to the ObservableMap
     */
    public static <K,T> Observable<Map.Entry<K,T>> additionsOf(final ObservableMap<K,T> source) {
        return ObservableMapSource.fromObservableMapAdds(source);
    }


    /**
     * Creates an observable that emits all removal items from an ObservableMap
     *
     * @param source      The target ObservableMap for the item removal events
     * @return An Observable emitting items removed Entry items from the ObservableMap
     */
    public static <K,T> Observable<Map.Entry<K,T>> removalsOf(final ObservableMap<K,T> source) {
        return ObservableMapSource.fromObservableMapRemovals(source);
    }



    /**
     * Emits all added and removed items (including swaps) from an ObservableMap
     * @param source
     * @return An Observable emitting changed entries with an ADDED or REMOVED flag
     */
    public static <K,T> Observable<MapChange<K,T>> changesOf(final ObservableMap<K,T> source) {
        return ObservableMapSource.fromObservableMapChanges(source);
    }


    /**
     * Creates an observable that emits an ObservableSet every time it is modified
     *
     * @param source      The target ObservableSet of the SetChange events
     * @return An Observable emitting the ObservableSet each time it changes
     */
    public static <T> Observable<ObservableSet<T>> emitOnChanged(final ObservableSet<T> source) {
        return ObservableSetSource.fromObservableSet(source);
    }


    /**
     * Creates an observable that emits all additions to an ObservableSet
     *
     * @param source      The target ObservableSet for the item add events
     * @return An Observable emitting items added to the ObservableSet
     */
    public static <T> Observable<T> additionsOf(final ObservableSet<T> source) {
        return ObservableSetSource.fromObservableSetAdds(source);
    }



    /**
     * Creates an observable that emits all removal items from an ObservableSet
     *
     * @param source      The target ObservableSet for the item removal events
     * @return An Observable emitting items removed items from the ObservableSet
     */
    public static <T> Observable<T> removalsOf(final ObservableSet<T> source) {
        return ObservableSetSource.fromObservableSetRemovals(source);
    }


    /**
     * Emits all added and removed items (including swaps) from an ObservableSet
     * @param source
     * @return An Observable emitting changed entries with an ADDED or REMOVED flag
     */
    public static <T> Observable<SetChange<T>> changesOf(final ObservableSet<T> source) {
        return ObservableSetSource.fromObservableSetChanges(source);
    }

    /**
     * Returns an Observable that emits a 0L  and ever increasing numbers after each duration of time thereafter
     */
    public static Observable<Long> interval(final Duration duration) {
        return TimerSource.interval(duration);
    }

    /**
     * Returns a Maybe that emits the T response  of a Dialog. If no response was given then the Maybe will be empty.
     */
    public static <T> Maybe<T> fromDialog(Dialog<T> dialog) {
        return DialogSource.fromDialogSource(dialog);
    }
}
