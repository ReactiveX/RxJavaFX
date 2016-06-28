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

import com.sun.javafx.binding.ExpressionHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Binding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;


final class BindingSubscriber<T> extends Subscriber<T> implements ObservableValue<T>, Binding<T> {

    private final Observable<T> observable;
    private final Action1<Throwable> onError;
    private Subscription subscription;
    private ExpressionHelper<T> helper;
    private T value;

    private BindingSubscriber(Observable<T> observable, Action1<Throwable> onError) {
        this.observable = observable;
        this.onError = onError;
    }

    public static <T> BindingSubscriber<T> forObservable(Observable<T> observable, Action1<Throwable> onError, boolean isLazy) {
        BindingSubscriber<T> bindingSubscriber = new BindingSubscriber<>(observable, onError);

        if (!isLazy) {
            bindingSubscriber.connect();
        }

        return bindingSubscriber;
    }
    private void connect() {
        subscription = observable.subscribe(this);
    }
    @Override
    public void onCompleted() {
        //do nothing
    }

    @Override
    public void onError(Throwable e) {
        onError.call(e);
    }

    @Override
    public void onNext(T t) {
        value = t;
        fireValueChangedEvent();
    }
    @Override
    public T getValue() {
        if (subscription == null) {
            connect();
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
            subscription.unsubscribe();
        }
        this.unsubscribe();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(InvalidationListener listener) {
        helper = ExpressionHelper.addListener(helper, this, listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addListener(ChangeListener<? super T> listener) {
        helper = ExpressionHelper.addListener(helper, this, listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(InvalidationListener listener) {
        helper = ExpressionHelper.removeListener(helper, listener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        helper = ExpressionHelper.removeListener(helper, listener);
    }

    /**
     * Notify the currently registered observers of a value change.
     *
     * This implementation will ignore all adds and removes of observers that
     * are done while a notification is processed. The changes take effect in
     * the following call to fireValueChangedEvent.
     */
    protected void fireValueChangedEvent() {
        ExpressionHelper.fireValueChangedEvent(helper);
    }
}
