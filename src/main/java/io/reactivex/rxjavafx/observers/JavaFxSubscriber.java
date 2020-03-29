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

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.flowables.ConnectableFlowable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import javafx.beans.binding.Binding;
import javafx.beans.value.ObservableValue;

import java.util.Optional;

public enum JavaFxSubscriber {
    ;//no instances

    /**
     * Turns an Flowable into an eager JavaFX Binding that subscribes immediately to the Flowable. Calling the Binding's dispose() method will handle the unsubscription.
     */
    public static <T> Binding<T> toBinding(Flowable<T> flowable) {
        return toBinding(flowable, JavaFxSubscriber::onError);
    }

    /**
     * Turns an Flowable into an eager JavaFX Binding that subscribes immediately to the Flowable. Calling the Binding's dispose() method will handle the unsubscription.
     */
    public static <T> Binding<T> toBinding(Flowable<T> flowable, Consumer<Throwable> onErrorAction) {
        BindingSubscriber<T, T> bindingSubscriber = new BindingSubscriber<>(t -> t, onErrorAction);
        flowable.subscribe(bindingSubscriber);
        return bindingSubscriber;
    }

    /**
     * Turns an Flowable into an eager JavaFX Binding that subscribes immediately to the Flowable. Calling the Binding's dispose() method will handle the unsubscription.
     * This variant unmasks a nullable value as in {@link JavaFxObservable#valuesOf(ObservableValue, Object)} and emits null when the sentinel is encountered.
     */
    public static <T> Binding<T> toNullBinding(Flowable<T> flowable, T nullSentinel) {
        return toNullBinding(flowable, nullSentinel, JavaFxSubscriber::onError);
    }

    /**
     * Turns an Flowable into an eager JavaFX Binding that subscribes immediately to the Flowable. Calling the Binding's dispose() method will handle the unsubscription.
     * This variant unmasks a nullable value as in {@link JavaFxObservable#valuesOf(ObservableValue, Object)} and emits null when the sentinel is encountered.
     */
    public static <T> Binding<T> toNullBinding(Flowable<T> flowable, T nullSentinel, Consumer<Throwable> onErrorAction) {
        if (nullSentinel == null) {
            throw new NullPointerException("The null value sentinel must not be null.");
        }
        BindingSubscriber<T, T> bindingSubscriber = new BindingSubscriber<>(t -> t == nullSentinel ? null : t, onErrorAction);
        flowable.subscribe(bindingSubscriber);
        return bindingSubscriber;
    }

    /**
     * Turns an Flowable into an eager JavaFX Binding that subscribes immediately to the Flowable. Calling the Binding's dispose() method will handle the unsubscription.
     * This variant unmasks a nullable value as in {@link JavaFxObservable#nullableValuesOf(ObservableValue)} and emits null when the value is not present.
     */
    public static <T> Binding<T> toNullableBinding(Flowable<Optional<T>> flowable) {
        return toNullableBinding(flowable, JavaFxSubscriber::onError);
    }

    /**
     * Turns an Flowable into an eager JavaFX Binding that subscribes immediately to the Flowable. Calling the Binding's dispose() method will handle the unsubscription.
     * This variant unmasks a nullable value as in {@link JavaFxObservable#nullableValuesOf(ObservableValue)} and emits null when the value is not present.
     */
    public static <T> Binding<T> toNullableBinding(Flowable<Optional<T>> flowable, Consumer<Throwable> onErrorAction) {
        BindingSubscriber<Optional<T>, T> bindingSubscriber = new BindingSubscriber<>(o -> o.orElse(null), onErrorAction);
        flowable.subscribe(bindingSubscriber);
        return bindingSubscriber;
    }

    /**
     * Turns an Flowable into an lazy JavaFX Binding that subscribes to the Flowable when its getValue() is called. Calling the Binding's dispose() method will handle the unsubscription.
     */
    public static <T> Binding<T> toLazyBinding(Flowable<T> flowable) {
        return toLazyBinding(flowable, JavaFxSubscriber::onError);
    }

    /**
     * Turns an Flowable into an eager JavaFX Binding that subscribes to the Flowable when its getValue() is called. Calling the Binding's dispose() method will handle the unsubscription.
     */
    public static <T> Binding<T> toLazyBinding(Flowable<T> flowable, Consumer<Throwable> onErrorAction) {
        ConnectableFlowable<T> published = flowable.publish();
        BindingSubscriber<T, T> bindingSubscriber = new BindingSubscriber<>(t -> t, published, onErrorAction);
        published.subscribe(bindingSubscriber);
        return bindingSubscriber;
    }

    /**
     * Turns an Flowable into an eager JavaFX Binding that subscribes to the Flowable when its getValue() is called. Calling the Binding's dispose() method will handle the unsubscription.
     * This variant unmasks a nullable value as in {@link JavaFxObservable#valuesOf(ObservableValue, Object)} and emits null when the sentinel is encountered.
     */
    public static <T> Binding<T> toLazyNullBinding(Flowable<T> flowable, T nullSentinel) {
        return toLazyNullBinding(flowable, nullSentinel, JavaFxSubscriber::onError);
    }

    /**
     * Turns an Flowable into an eager JavaFX Binding that subscribes to the Flowable when its getValue() is called. Calling the Binding's dispose() method will handle the unsubscription.
     * This variant unmasks a nullable value as in {@link JavaFxObservable#valuesOf(ObservableValue, Object)} and emits null when the sentinel is encountered.
     */
    public static <T> Binding<T> toLazyNullBinding(Flowable<T> flowable, T nullSentinel, Consumer<Throwable> onErrorAction) {
        if (nullSentinel == null) {
            throw new NullPointerException("The null value sentinel must not be null.");
        }
        ConnectableFlowable<T> published = flowable.publish();
        BindingSubscriber<T, T> bindingSubscriber = new BindingSubscriber<>(t -> t == nullSentinel ? null : t, published, onErrorAction);
        published.subscribe(bindingSubscriber);
        return bindingSubscriber;
    }

    /**
     * Turns an Flowable into an lazy JavaFX Binding that subscribes to the Flowable when its getValue() is called. Calling the Binding's dispose() method will handle the unsubscription.
     * This variant unmasks a nullable value as in {@link JavaFxObservable#nullableValuesOf(ObservableValue)} and emits null when the value is not present.
     */
    public static <T> Binding<T> toLazyNullableBinding(Flowable<Optional<T>> flowable) {
        return toLazyNullableBinding(flowable, JavaFxSubscriber::onError);
    }

    /**
     * Turns an Flowable into an lazy JavaFX Binding that subscribes to the Flowable when its getValue() is called. Calling the Binding's dispose() method will handle the unsubscription.
     * This variant unmasks a nullable value as in {@link JavaFxObservable#nullableValuesOf(ObservableValue)} and emits null when the value is not present.
     */
    public static <T> Binding<T> toLazyNullableBinding(Flowable<Optional<T>> flowable, Consumer<Throwable> onErrorAction) {
        ConnectableFlowable<Optional<T>> published = flowable.publish();
        BindingSubscriber<Optional<T>, T> bindingSubscriber = new BindingSubscriber<>(o -> o.orElse(null), published, onErrorAction);
        published.subscribe(bindingSubscriber);
        return bindingSubscriber;
    }

    private static void onError(Throwable t) {
        RxJavaPlugins.onError(t);
    }
}
