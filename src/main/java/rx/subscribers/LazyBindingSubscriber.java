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

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Binding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import rx.Observable;
import rx.Subscription;


final class LazyBindingSubscriber<T> implements ObservableValue<T>, Binding<T> {

    private final Observable<T> observable;
    private final BindingSubscriber<T> binding;
    private Subscription subscription;

    LazyBindingSubscriber(Observable<T> observable, BindingSubscriber<T> binding) {
        this.observable = observable;
        this.binding = binding;
    }
    @Override
    public T getValue() {
        if (subscription == null) {
            subscription = observable.subscribe(binding);
        }
        return binding.getValue();
    }
    @Override
    public boolean isValid() {
        return binding.isValid();
    }

    @Override
    public void invalidate() {
        binding.invalidate();
    }

    @Override
    public ObservableList<?> getDependencies() {
        return binding.getDependencies();
    }

    @Override
    public void dispose() {
        binding.dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(InvalidationListener listener) {
        binding.addListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(ChangeListener<? super T> listener) {
        binding.addListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(InvalidationListener listener) {
        binding.removeListener(listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        binding.removeListener(listener);
    }

    /**
     * Notify the currently registered observers of a value change.
     *
     * This implementation will ignore all adds and removes of observers that
     * are done while a notification is processed. The changes take effect in
     * the following call to fireValueChangedEvent.
     */
    protected void fireValueChangedEvent() {
        binding.fireValueChangedEvent();
    }
}
