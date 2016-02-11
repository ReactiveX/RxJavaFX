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

import rx.Observable;
import rx.functions.Action1;

import java.util.Optional;

public enum JavaFxSubscriber {
    ;//no instances

    public static <T> BindingSubscriber<T> toBinding(Observable<T> observable) {
        return new BindingSubscriber<T>(observable, Optional.empty(), e -> {});
    }
    public static <T> BindingSubscriber<T> toBinding(Observable<T> observable,Action1<Throwable> onErrorAction ) {
        return new BindingSubscriber<T>(observable, Optional.empty(), onErrorAction);
    }
    public static <T> BindingSubscriber<T> toBinding(Observable<T> observable, T initialValue, Action1<Throwable> onErrorAction ) {
        return new BindingSubscriber<T>(observable, Optional.of(initialValue), onErrorAction);
    }
    public static <T> BindingSubscriber<T> toBinding(Observable<T> observable, T initialValue) {
        return new BindingSubscriber<T>(observable, Optional.of(initialValue), e -> {});
    }
}
