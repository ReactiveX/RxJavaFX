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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.JavaFxSubscriptions;

public class ObservableValueSource {

    /**
     * @see rx.observables.JavaFxObservable#fromObservableValue
     */
    public static <T> Observable<T> fromObservableValue(final ObservableValue<T> fxObservable) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> subscriber) {
                subscriber.onNext(fxObservable.getValue());

                final ChangeListener<T> listener = (observableValue, prev, current) -> subscriber.onNext(current);

                fxObservable.addListener(listener);

                subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> fxObservable.removeListener(listener)));

            }
        });
    }
    /**
     * @see rx.observables.JavaFxObservable#fromObservableValue
     */
    public static <T> Observable<Change<T>> fromObservableValueChanges(final ObservableValue<T> fxObservable) {
        return Observable.create(new Observable.OnSubscribe<Change<T>>() {
            @Override
            public void call(final Subscriber<? super Change<T>> subscriber) {

                final ChangeListener<T> listener = (observableValue, prev, current) -> subscriber.onNext(new Change<>(prev,current));

                fxObservable.addListener(listener);

                subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> fxObservable.removeListener(listener)));

            }
        });
    }

}
