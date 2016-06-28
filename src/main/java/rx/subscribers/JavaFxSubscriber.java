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
     * Turns an Observable into an eager JavaFX Binding that subscribes immediately to the Observable. Calling the Binding's dispose() method will handle the unsubscription.
     */
    public static <T> Binding<T> toBinding(Observable<T> obs) {
        return BindingSubscriber.forObservable(obs, e -> {}, false);
    }
    /**
     * Turns an Observable into an eager JavaFX Binding that subscribes immediately to the Observable. Calling the Binding's dispose() method will handle the unsubscription.
     */
    public static <T> Binding<T> toBinding(Observable<T> obs, Action1<Throwable> onErrorAction ) {
        return BindingSubscriber.forObservable(obs, onErrorAction, false);
    }

    /**
     * Turns an Observable into a lazy JavaFX Binding that subscribes to the Observable the first time the Binding's getValue() method is called. Calling the Binding's dispose() method will handle the unsubscription.
     */
    public static <T> Binding<T> toLazyBinding(Observable<T> obs) {
        return toLazyBinding(obs, e -> {});
    }

    /**
     * Turns an Observable into a lazy JavaFX Binding that subscribes to the Observable the first time the Binding's getValue() method is called. Calling the Binding's dispose() method will handle the unsubscription.
     */
    public static <T> Binding<T> toLazyBinding(Observable<T> obs, Action1<Throwable> onErrorAction ) {
        return BindingSubscriber.forObservable(obs, onErrorAction, true);
    }
}
