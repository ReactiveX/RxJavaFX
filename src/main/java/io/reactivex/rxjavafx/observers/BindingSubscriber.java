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

import io.reactivex.rxjava3.flowables.ConnectableFlowable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import javafx.beans.binding.Binding;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

final class BindingSubscriber<T, S> extends ObservableListenerHelper<S> implements Subscriber<T>, ObservableValue<S>, Binding<S> {

    private final Function<T, S>         unmaskingFunction;
    private final Consumer<Throwable>    onError;
    private final ConnectableFlowable<T> obs;
    private boolean connected = false;
    private Subscription        subscription;
    private S                   value;

    BindingSubscriber(Function<T, S> unmaskingFunction, Consumer<Throwable> onError) {
        this.unmaskingFunction = unmaskingFunction;
        this.onError = onError;
        this.obs = null;
    }

    BindingSubscriber(Function<T, S> unmaskingFunction, ConnectableFlowable<T> obs, Consumer<Throwable> onError) {
        this.unmaskingFunction = unmaskingFunction;
        this.onError = onError;
        this.obs = obs;
    }

    @Override
    public void onSubscribe(Subscription s) {
        this.subscription = s;
        this.subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onComplete() {
        //do nothing
    }

    @Override
    public void onError(Throwable e) {
        try {
            onError.accept(e);
        } catch (Throwable e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void onNext(T t) {
        try {
            value = unmaskingFunction.apply(t);
            fireChange();
        } catch (Throwable e) {
            onError(e);
        }
    }

    @Override
    public S getValue() {
        if (!connected && obs != null) {
            obs.connect();
            connected = true;
        }
        return value;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void invalidate() {
        //does nothing
    }

    @Override
    public ObservableList<?> getDependencies() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dispose() {
        if (subscription != null) {
            subscription.cancel();
        }
    }
}
