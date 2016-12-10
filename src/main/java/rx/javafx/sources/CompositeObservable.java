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



import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import java.util.Arrays;


/**
 * A CompositeObservable can merge multiple event source Observables that can be added/removed at any time,
 * affecting all Subscribers regardless of when they subscribed. This is especially helpful for merging
 * multiple UI event sources. You can also pass a Transformer to perform
 * further operations on the combined Observable that is returned
 *
 * @param <T>
 */
public final class CompositeObservable<T> {

    private final Subject<T> subject;
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
     * @param transformer
     */
    public CompositeObservable(Observable.Transformer<T,T> transformer) {
        subject = PublishSubject.<T>create().toSerialized();

        Observable<T> updatingSource = subject.asObservable();

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
    public Disposable add(Observable<T> observable) {
        return observable.subscribe(subject);
    }
    public CompositeDisposable addAll(Observable<T>... observables) {
        final CompositeDisposable subscriptions = new CompositeDisposable();
        Arrays.stream(observables).map(this::add).forEach(subscriptions::add);
        return subscriptions;
    }
}
