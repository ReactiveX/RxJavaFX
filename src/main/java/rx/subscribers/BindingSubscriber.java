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

package rx.subscribers;

import javafx.beans.binding.Binding;
import javafx.beans.value.ObservableValueBase;
import javafx.collections.ObservableList;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;

import java.util.Optional;


public final class BindingSubscriber<T> extends ObservableValueBase<T> implements Binding<T> {

    private final Subscriber<T> subscriber;

    private T value;

    BindingSubscriber(Observable<T> observable, Optional<T> initialValue, final Action1<Throwable> onError) {

        this.subscriber = new Subscriber<T>() {
            @Override
            public void onCompleted() {
                //do nothing
            }

            @Override
            public void onError(Throwable e) {
                onError.call(e);
            }

            @Override
            public void onNext(T item) {
                value = item;
                fireValueChangedEvent();
            }
        };

        observable.subscribe(subscriber);

    }
    @Override
    public T getValue() {
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
        subscriber.unsubscribe();
    }
}
