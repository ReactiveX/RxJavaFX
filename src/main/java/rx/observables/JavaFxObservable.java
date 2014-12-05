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


import java.util.List;
import java.util.Map;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Node;
import rx.Observable;
import rx.javafx.sources.NodeEventSource;
import rx.javafx.sources.ObservableListSource;
import rx.javafx.sources.ObservableMapSource;
import rx.javafx.sources.ObservableValueSource;


public enum JavaFxObservable {
    ; // no instances


    /**
     * Creates an observable corresponding to javafx ui events.
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
     * Create an rx Observable from a javafx ObservableList.
     * 
     * @param list the observed ObservableList
     * @param <T>          the type of the elements in the list
     * @return an Observable emitting list versions as the wrapped ObservableList changes
     */
    public static <T> Observable<List<? extends T>> fromObservableList(final ObservableList<T> list) {
        return ObservableListSource.fromObservableList(list);
    }

    /**
     * Create an rx Observable from a javafx ObservableMap.
     * 
     * @param map the observed ObservableMap
     * @param <K>          the type of the keys in the map
     * @param <V>          the type of the values in the map
     * @return an Observable emitting map versions as the wrapped ObservableMap changes
     */
    public static <K, V> Observable<Map<? extends K, ? extends V>> fromObservableMap(final ObservableMap<K, V> map) {
        return ObservableMapSource.fromObservableMap(map);
    }
}
