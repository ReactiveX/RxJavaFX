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
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.rxjavafx.subscriptions.JavaFxSubscriptions;
import javafx.beans.property.ListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.function.Function;

public final class ObservableListSource {
    private ObservableListSource() {}

    public static <T> Observable<ObservableList<T>> fromObservableList(final ObservableList<T> source) {

        Observable<ObservableList<T>> mutations = Observable.create((ObservableOnSubscribe<ObservableList<T>>) subscriber -> {
            ListChangeListener<T> listener = c -> subscriber.onNext(source);
            source.addListener(listener);
            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));
        });


        if (source instanceof ListProperty<?>) {
            return JavaFxObservable.valuesOf((ListProperty<T>) source);
        } else {
            return mutations.startWithArray(source);
        }
    }

    public static <T> Observable<T> fromObservableListAdds(final ObservableList<T> source) {

        return Observable.create((ObservableOnSubscribe<T>) subscriber -> {

            ListChangeListener<T> listener = c -> {
                while (c.next()) {
                    if (c.wasAdded()) {
                        c.getAddedSubList().forEach(subscriber::onNext);
                    }
                }
            };
            source.addListener(listener);
            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));

        }).subscribeOn(JavaFxScheduler.platform());
    }
    public static <T> Observable<T> fromObservableListRemovals(final ObservableList<T> source) {

        return Observable.create((ObservableOnSubscribe<T>) subscriber -> {

            ListChangeListener<T> listener = c -> {
                while (c.next()) {
                    if (c.wasRemoved()) {
                        c.getRemoved().forEach(subscriber::onNext);
                    }
                }
            };

            source.addListener(listener);
            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));

        });
    }
    public static <T> Observable<T> fromObservableListUpdates(final ObservableList<T> source) {

        return Observable.create((ObservableOnSubscribe<T>) subscriber -> {

            ListChangeListener<T> listener = c -> {
                while (c.next()) {
                    if (c.wasUpdated()) {
                        for (int i = c.getFrom(); i < c.getTo(); i++) {
                            subscriber.onNext(c.getList().get(i));
                        }
                    }
                }
            };
            source.addListener(listener);
            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));

        });
    }
    public static <T> Observable<ListChange<T>> fromObservableListChanges(final ObservableList<T> source) {
        return Observable.create((ObservableOnSubscribe<ListChange<T>>) subscriber -> {

            ListChangeListener<T> listener = c -> {
                while (c.next()) {
                    if (c.wasAdded()) {
                        c.getAddedSubList().forEach(v -> subscriber.onNext(ListChange.of(v,Flag.ADDED)));
                    }
                    if (c.wasRemoved()) {
                        c.getRemoved().forEach(v -> subscriber.onNext(ListChange.of(v,Flag.REMOVED)));
                    }
                    if (c.wasUpdated()) {
                        for (int i = c.getFrom(); i < c.getTo(); i++) {
                            subscriber.onNext(ListChange.of(c.getList().get(i),Flag.UPDATED));
                        }
                    }
                }
            };
            source.addListener(listener);

            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));
        });
    }

    public static <T> Observable<ListChange<T>> fromObservableListDistinctChanges(final ObservableList<T> source) {

        return Observable.create((ObservableOnSubscribe<ListChange<T>>) subscriber -> {

            final DupeCounter<T> dupeCounter = new DupeCounter<>();
            source.forEach(dupeCounter::add);

            ListChangeListener<T> listener = c -> {

                while (c.next()) {
                    if (c.wasAdded()) {
                        c.getAddedSubList().stream().filter(v -> dupeCounter.add(v) == 1)
                                .forEach(v -> subscriber.onNext(ListChange.of(v,Flag.ADDED)));
                    }
                    if (c.wasRemoved()) {
                        c.getRemoved().stream().filter(v -> dupeCounter.remove(v) == 0)
                                .forEach(v -> subscriber.onNext(ListChange.of(v,Flag.REMOVED)));
                    }
                }
            };
            source.addListener(listener);

            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));
        });
    }
    public static <T,R> Observable<ListChange<T>> fromObservableListDistinctChanges(final ObservableList<T> source, Function<T,R> mapper) {

        return Observable.create((ObservableOnSubscribe<ListChange<T>>) subscriber -> {

            final DupeCounter<R> dupeCounter = new DupeCounter<>();
            source.stream().map(mapper).forEach(dupeCounter::add);

            ListChangeListener<T> listener = c -> {

                while (c.next()) {
                    if (c.wasAdded()) {
                        c.getAddedSubList().stream().filter(v -> dupeCounter.add(mapper.apply(v)) == 1)
                                .forEach(v -> subscriber.onNext(ListChange.of(v,Flag.ADDED)));
                    }
                    if (c.wasRemoved()) {
                        c.getRemoved().stream().filter(v -> dupeCounter.remove(mapper.apply(v)) == 0)
                                .forEach(v -> subscriber.onNext(ListChange.of(v,Flag.REMOVED)));
                    }
                }
            };
            source.addListener(listener);
            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));
        });
    }
    public static <T,R> Observable<ListChange<R>> fromObservableListDistinctMappings(final ObservableList<T> source, Function<T,R> mapper) {

        return Observable.create((ObservableOnSubscribe<ListChange<R>>) subscriber -> {

            final DupeCounter<R> dupeCounter = new DupeCounter<>();
            source.stream().map(mapper).forEach(dupeCounter::add);

            ListChangeListener<T> listener = c -> {

                while (c.next()) {
                    if (c.wasAdded()) {
                        c.getAddedSubList().stream().map(mapper)
                                .filter(v -> dupeCounter.add(v) == 1)
                                .forEach(v -> subscriber.onNext(ListChange.of(v,Flag.ADDED)));
                    }
                    if (c.wasRemoved()) {
                        c.getRemoved().stream().map(mapper)
                                .filter(v -> dupeCounter.remove(v) == 0)
                                .forEach(v -> subscriber.onNext(ListChange.of(v,Flag.REMOVED)));
                    }
                }
            };
            source.addListener(listener);
            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));

        });
    }

    private static final class DupeCounter<T> {
        private final HashMap<T,Integer> counts = new HashMap<>();

        public int add(T value) {
            Integer prev = counts.get(value);
            int newVal = 0;
            if (prev == null) {
                newVal = 1;
                counts.put(value, newVal);
            }  else {
                newVal = prev + 1;
                counts.put(value, newVal);
            }
            return newVal;
        }
        public int remove(T value) {
            Integer prev = counts.get(value);
            if (prev != null && prev > 0) {
                int newVal = prev - 1;
                if (newVal == 0) {
                    counts.remove(value);
                } else {
                    counts.put(value, newVal);
                }
                return newVal;
            }
            else {
                throw new IllegalStateException();
            }
        }
    }

}
