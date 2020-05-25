/**
 * Copyright 2017 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.reactivex.rxjavafx.observers;

import java.util.Optional;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.observables.ConnectableObservable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import javafx.beans.binding.Binding;
import javafx.beans.value.ObservableValue;

public enum JavaFxObserver {
    ;//no instances

    /**
     * Turns an Observable into an eager JavaFX Binding that subscribes immediately to the Observable. Calling the Binding's dispose() method will handle the unsubscription.
     */
    public static <T> Binding<T> toBinding(Observable<T> obs) {
        return toBinding(obs, JavaFxObserver::onError);
    }

    /**
     * Turns an Observable into an eager JavaFX Binding that subscribes immediately to the Observable. Calling the Binding's dispose() method will handle the unsubscription.
     */
    public static <T> Binding<T> toBinding(Observable<T> obs, Consumer<Throwable> onErrorAction) {
        BindingObserver<T, T> bindingObserver = new BindingObserver<>(t -> t, onErrorAction);
        obs.subscribe(bindingObserver);
        return bindingObserver;
    }

    /**
     * Turns an Observable into an eager JavaFX Binding that subscribes immediately to the Observable. Calling the Binding's dispose() method will handle the unsubscription.
     * This variant unmasks a nullable value as in {@link JavaFxObservable#valuesOf(ObservableValue, Object)} and emits null when the sentinel is encountered.
     */
    public static <T> Binding<T> toNullBinding(Observable<T> obs, T nullSentinel) {
        return toNullBinding(obs, nullSentinel, JavaFxObserver::onError);
    }

    /**
     * Turns an Observable into an eager JavaFX Binding that subscribes immediately to the Observable. Calling the Binding's dispose() method will handle the unsubscription.
     * This variant unmasks a nullable value as in {@link JavaFxObservable#valuesOf(ObservableValue, Object)} and emits null when the sentinel is encountered.
     */
    public static <T> Binding<T> toNullBinding(Observable<T> obs, T nullSentinel, Consumer<Throwable> onErrorAction) {
        if (nullSentinel == null) {
            throw new NullPointerException("The null value sentinel must not be null.");
        }
        BindingObserver<T, T> bindingObserver = new BindingObserver<>(t -> t == nullSentinel ? null : t, onErrorAction);
        obs.subscribe(bindingObserver);
        return bindingObserver;
    }

    /**
     * Turns an Observable into an eager JavaFX Binding that subscribes immediately to the Observable. Calling the Binding's dispose() method will handle the unsubscription.
     * This variant unmasks a nullable value as in {@link JavaFxObservable#nullableValuesOf(ObservableValue)} and emits null when the value is not present.
     */
    public static <T> Binding<T> toNullableBinding(Observable<Optional<T>> obs) {
        return toNullableBinding(obs, JavaFxObserver::onError);
    }

    /**
     * Turns an Observable into an eager JavaFX Binding that subscribes immediately to the Observable. Calling the Binding's dispose() method will handle the unsubscription.
     * This variant unmasks a nullable value as in {@link JavaFxObservable#nullableValuesOf(ObservableValue)} and emits null when the value is not present.
     */
    public static <T> Binding<T> toNullableBinding(Observable<Optional<T>> obs, Consumer<Throwable> onErrorAction) {
        BindingObserver<Optional<T>, T> bindingObserver = new BindingObserver<>(o -> o.orElse(null), onErrorAction);
        obs.subscribe(bindingObserver);
        return bindingObserver;
    }

    /**
     * Turns an Observable into an lazy JavaFX Binding that subscribes to the Observable when its getValue() is called. Calling the Binding's dispose() method will handle the unsubscription.
     */
    public static <T> Binding<T> toLazyBinding(Observable<T> obs) {
        return toLazyBinding(obs, JavaFxObserver::onError);
    }

    /**
     * Turns an Observable into an eager JavaFX Binding that subscribes to the Observable when its getValue() is called. Calling the Binding's dispose() method will handle the unsubscription.
     */
    public static <T> Binding<T> toLazyBinding(Observable<T> obs, Consumer<Throwable> onErrorAction) {
        ConnectableObservable<T> published = obs.publish();
        BindingObserver<T, T> bindingObserver = new BindingObserver<>(t -> t, published, onErrorAction);
        published.subscribe(bindingObserver);
        return bindingObserver;
    }

    /**
     * Turns an Observable into an eager JavaFX Binding that subscribes to the Observable when its getValue() is called. Calling the Binding's dispose() method will handle the unsubscription.
     * This variant unmasks a nullable value as in {@link JavaFxObservable#valuesOf(ObservableValue, Object)} and emits null when the sentinel is encountered.
     */
    public static <T> Binding<T> toLazyNullBinding(Observable<T> obs, T nullSentinel) {
        return toLazyNullBinding(obs, nullSentinel, JavaFxObserver::onError);
    }

    /**
     * Turns an Observable into an eager JavaFX Binding that subscribes to the Observable when its getValue() is called. Calling the Binding's dispose() method will handle the unsubscription.
     * This variant unmasks a nullable value as in {@link JavaFxObservable#valuesOf(ObservableValue, Object)} and emits null when the sentinel is encountered.
     */
    public static <T> Binding<T> toLazyNullBinding(Observable<T> obs, T nullSentinel, Consumer<Throwable> onErrorAction) {
        if (nullSentinel == null) {
            throw new NullPointerException("The null value sentinel must not be null.");
        }
        ConnectableObservable<T> published = obs.publish();
        BindingObserver<T, T> bindingObserver = new BindingObserver<>(t -> t == nullSentinel ? null : t, published, onErrorAction);
        published.subscribe(bindingObserver);
        return bindingObserver;
    }

    /**
     * Turns an Observable into an lazy JavaFX Binding that subscribes to the Observable when its getValue() is called. Calling the Binding's dispose() method will handle the unsubscription.
     * This variant unmasks a nullable value as in {@link JavaFxObservable#nullableValuesOf(ObservableValue)} and emits null when the value is not present.
     */
    public static <T> Binding<T> toLazyNullableBinding(Observable<Optional<T>> obs) {
        return toLazyNullableBinding(obs, JavaFxObserver::onError);
    }

    /**
     * Turns an Observable into an lazy JavaFX Binding that subscribes to the Observable when its getValue() is called. Calling the Binding's dispose() method will handle the unsubscription.
     * This variant unmasks a nullable value as in {@link JavaFxObservable#nullableValuesOf(ObservableValue)} and emits null when the value is not present.
     */
    public static <T> Binding<T> toLazyNullableBinding(Observable<Optional<T>> obs, Consumer<Throwable> onErrorAction) {
        ConnectableObservable<Optional<T>> published = obs.publish();
        BindingObserver<Optional<T>, T> bindingObserver = new BindingObserver<>(o -> o.orElse(null), published, onErrorAction);
        published.subscribe(bindingObserver);
        return bindingObserver;
    }

    private static void onError(Throwable t) {
        RxJavaPlugins.onError(t);
    }
}
