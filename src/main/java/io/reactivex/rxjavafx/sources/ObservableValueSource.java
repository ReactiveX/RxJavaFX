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
import io.reactivex.rxjavafx.subscriptions.JavaFxSubscriptions;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.Optional;

public class ObservableValueSource {


    public static <T> Observable<T> fromObservableValue(final ObservableValue<T> fxObservable) {
        return Observable.create((ObservableEmitter<T> emitter) -> {
            if (fxObservable.getValue() != null)
                emitter.onNext(fxObservable.getValue());

            final ChangeListener<T> listener = (observableValue, prev, current) -> {
                emitter.onNext(current);
            };

            fxObservable.addListener(listener);

            emitter.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> fxObservable.removeListener(listener)));
        });
    }

    public static <T> Observable<T> fromObservableValue(final ObservableValue<T> fxObservable, final T nullSentinel) {
        if (nullSentinel == null) {
            throw new NullPointerException("The null value sentinel must not be null.");
        }
        return Observable.create((ObservableEmitter<T> emitter) -> {
            if (fxObservable.getValue() != null) {
                emitter.onNext(fxObservable.getValue());
            }

            final ChangeListener<T> listener = (observableValue, prev, current) -> {
                if (current != null) {
                    emitter.onNext(current);
                } else {
                    emitter.onNext(nullSentinel);
                }
            };

            fxObservable.addListener(listener);

            emitter.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> fxObservable.removeListener(listener)));
        });
    }

    public static <T> Observable<Optional<T>> fromNullableObservableValue(final ObservableValue<T> fxObservable) {
        return Observable.create((ObservableEmitter<Optional<T>> emitter) -> {
            emitter.onNext(Optional.ofNullable(fxObservable.getValue()));

            final ChangeListener<T> listener = (observableValue, prev, current) -> emitter.onNext(Optional.ofNullable(current));

            fxObservable.addListener(listener);

            emitter.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> fxObservable.removeListener(listener)));
        });
    }

    public static <T> Observable<Change<T>> fromObservableValueChanges(final ObservableValue<T> fxObservable) {
        return Observable.create((ObservableEmitter<Change<T>> emitter) -> {
            final ChangeListener<T> listener = (observableValue, prev, current) -> {
                emitter.onNext(new Change<>(prev,current));
            };

            fxObservable.addListener(listener);

            emitter.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> fxObservable.removeListener(listener)));
        });
    }


    public static Observable<javafx.beans.Observable> fromInvalidations(javafx.beans.Observable fxObservable) {
        return Observable.create(emitter -> {
            final InvalidationListener listener = emitter::onNext;
            fxObservable.addListener(listener);
            emitter.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> fxObservable.removeListener(listener)));
        });
    }
}
