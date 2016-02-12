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
import rx.Observable;
import rx.functions.Action1;

public enum JavaFxSubscriber {
    ;//no instances
    /**
     * Turns an Observable into a JavaFX Binding. Calling the dispose() method will handle the unsubscription. 
     */
    public static <T> Binding<T> toBinding(Observable<T> obs) {
        BindingSubscriber<T> bindingSubscriber = new BindingSubscriber<>(e -> {});
        obs.subscribe(bindingSubscriber);
        return bindingSubscriber;
    }
    /**
     * Turns an Observable into a JavaFX Binding. Calling the dispose() method will handle the unsubscription. 
     */
    public static <T> Binding<T> toBinding(Observable<T> obs, Action1<Throwable> onErrorAction ) {
        BindingSubscriber<T> bindingSubscriber = new BindingSubscriber<>(onErrorAction);
        obs.subscribe(bindingSubscriber);
        return bindingSubscriber;
    }
}
