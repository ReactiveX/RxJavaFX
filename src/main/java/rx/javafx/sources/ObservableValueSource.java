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

import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import rx.Observable;
import rx.subscriptions.JavaFxSubscriptions;

public class ObservableValueSource {

    public static <T> Observable<T> fromObservableValue(final ObservableValue<T> fxObservable) {
        return Observable.create(subscriber -> {
            subscriber.onNext(fxObservable.getValue());

            final ChangeListener<T> listener = (observableValue, prev, current) -> subscriber.onNext(current);

            fxObservable.addListener(listener);

            subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> fxObservable.removeListener(listener)));

        });
    }

    public static <T> Observable<Change<T>> fromObservableValueChanges(final ObservableValue<T> fxObservable) {
        return Observable.create(subscriber -> {

            final ChangeListener<T> listener = (observableValue, prev, current) -> subscriber.onNext(new Change<>(prev,current));

            fxObservable.addListener(listener);

            subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> fxObservable.removeListener(listener)));

        });
    }

    public static <T> Observable<ObservableValue<T>> fromInvalidations(final ObservableValue<T> fxObservable) {
        return Observable.create(subscriber -> {
            final InvalidationListener listener = s -> subscriber.onNext(fxObservable);
            fxObservable.addListener(listener);
            subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> fxObservable.removeListener(listener)));
        });
    }

    public static <T> Observable<Property<T>> fromInvalidations(final Property<T> fxProperty) {
        return Observable.create(subscriber -> {
            final InvalidationListener listener = s -> subscriber.onNext(fxProperty);
            fxProperty.addListener(listener);
            subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> fxProperty.removeListener(listener)));
        });
    }
}
