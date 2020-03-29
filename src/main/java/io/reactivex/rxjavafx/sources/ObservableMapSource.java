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
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import io.reactivex.rxjavafx.subscriptions.JavaFxSubscriptions;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

public final class ObservableMapSource {

    private ObservableMapSource() {}

    public static <K,T> Observable<ObservableMap<K,T>> fromObservableMap(final ObservableMap<K,T> source) {
        Observable<ObservableMap<K,T>> mutations = Observable.create((ObservableOnSubscribe<ObservableMap<K,T>>) subscriber -> {
            MapChangeListener<K,T> listener = c -> subscriber.onNext(source);
            source.addListener(listener);
            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));
        });


        if (source instanceof MapProperty<?,?>) {
            return JavaFxObservable.valuesOf((MapProperty<K,T>) source);
        } else {
            return mutations.startWithArray(source);
        }
    }

    public static <K,T> Observable<Entry<K,T>> fromObservableMapAdds(final ObservableMap<K,T> source) {

        return Observable.create((ObservableOnSubscribe<Entry<K,T>>) subscriber -> {

            MapChangeListener<K,T> listener = c -> {

                if (c.wasAdded()) {
                   subscriber.onNext(new SimpleEntry<K,T>(c.getKey(),c.getValueAdded()));
                }

            };
            source.addListener(listener);
            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));

        });
    }

    public static <K,T> Observable<Entry<K,T>> fromObservableMapRemovals(final ObservableMap<K,T> source) {

        return Observable.create((ObservableOnSubscribe<Entry<K,T>>) subscriber -> {

            MapChangeListener<K,T> listener = c -> {

                if (c.wasRemoved()) {
                    subscriber.onNext(new SimpleEntry<K,T>(c.getKey(),c.getValueRemoved()));
                }

            };
            source.addListener(listener);
            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));

        });
    }

    public static <K,T> Observable<MapChange<K,T>> fromObservableMapChanges(final ObservableMap<K,T> source) {

        return Observable.create((ObservableOnSubscribe<MapChange<K,T>>) subscriber -> {

            MapChangeListener<K,T> listener = c -> {

                if (c.wasRemoved()) {
                    subscriber.onNext(new MapChange<K,T>(c.getKey(),c.getValueRemoved(),Flag.REMOVED));
                }
                if (c.wasAdded()) {
                    subscriber.onNext(new MapChange<K,T>(c.getKey(),c.getValueAdded(),Flag.ADDED));
                }

            };
            source.addListener(listener);
            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));

        });
    }
}
