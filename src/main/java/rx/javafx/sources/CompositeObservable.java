/**
 * Copyright 2016 Netflix, Inc.
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
package rx.javafx.sources;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import rx.Observable;
import rx.annotations.Beta;
import rx.observables.JavaFxObservable;

import java.util.Arrays;
import java.util.HashSet;


/**
 * A CompositeObservable can merge multiple Observables that can be added/removed at any time,
 * affecting all Subscribers regardless of when they subscribed. This is especially helpful for merging
 * multiple UI event sources. You can also pass a Transformer to perform
 * further operations on the combined Observable that is returned
 *
 * @param <T>
 */
@Beta
public final class CompositeObservable<T> {

    private final ObservableSet<Observable<T>> sources;
    private final Observable<T> output;

    /**
     *  Creates a new CompositeObservable
     */
    public CompositeObservable() {
        this(null);
    }

    /**
     * Creates a new CompositeObservable with the provided transformations applied to the returned Observable
     * yield from `toObservable()`. For instance, you can pass `obs -> obs.replay(1).refCount()` to make this CompositeObservable
     * replay one emission or `obs -> obs.share()` to multicast it.
     * @param transformer
     */
    public CompositeObservable(Observable.Transformer<T,T> transformer) {
        sources = FXCollections.synchronizedObservableSet(FXCollections.observableSet(new HashSet<>()));

        Observable<T> updatingSource = Observable.merge(
                Observable.from(sources).flatMap(obs -> obs.takeWhile(v -> sources.contains(obs))),
                JavaFxObservable.fromObservableSetAdds(sources).flatMap(obs -> obs.takeWhile(v -> sources.contains(obs)))
        );

        if (transformer == null) {
            output = updatingSource;
        } else {
            output = updatingSource.compose(transformer);
        }
    }

    /**
     * Returns the `Observable` combining all the source Observables, with any transformations that were specified
     * on construction.
     * @return
     */
    public Observable<T> toObservable() {
        return output;
    }
    public void add(Observable<T> observable) {
        sources.add(observable);
    }
    public void addAll(Observable<T>... observables) {
        Arrays.stream(observables).forEach(this::add);
    }
    public void remove(Observable<T> observable) {
        sources.remove(observable);
    }
    public void removeAll(Observable<T>... observables) {
        Arrays.stream(observables).forEach(this::remove);
    }
    public void clear() {
        sources.clear();
    }
    public ObservableSet<Observable<T>> getSources() {
        return FXCollections.unmodifiableObservableSet(sources);
    }
}
