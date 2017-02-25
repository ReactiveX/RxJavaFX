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
package rx.javafx.sources;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import rx.schedulers.JavaFxScheduler;
import rx.subscriptions.JavaFxSubscriptions;

public final class ObservableSetSource {
    private ObservableSetSource() {}

    public static <T> Observable<ObservableSet<T>> fromObservableSet(final ObservableSet<T> source) {

        return Observable.create((ObservableOnSubscribe<ObservableSet<T>>) subscriber -> {
            SetChangeListener<T> listener = c -> subscriber.onNext(source);
            source.addListener(listener);
            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));
        }).startWith(source).subscribeOn(JavaFxScheduler.platform());
    }

    public static <T> Observable<T> fromObservableSetAdds(final ObservableSet<T> source) {

        return Observable.create((ObservableOnSubscribe<T>) subscriber -> {

            SetChangeListener<T> listener = c -> {
                if (c.wasAdded()) {
                    subscriber.onNext(c.getElementAdded());
                }
            };
            source.addListener(listener);
            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));

        }).subscribeOn(JavaFxScheduler.platform());
    }

    public static <T> Observable<T> fromObservableSetRemovals(final ObservableSet<T> source) {

        return Observable.create((ObservableOnSubscribe<T>) subscriber -> {

            SetChangeListener<T> listener = c -> {
                if (c.wasRemoved()) {
                    subscriber.onNext(c.getElementRemoved());
                }
            };
            source.addListener(listener);
            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));

        }).subscribeOn(JavaFxScheduler.platform());
    }

    public static <T> Observable<SetChange<T>> fromObservableSetChanges(final ObservableSet<T> source) {

        return Observable.create((ObservableOnSubscribe<SetChange<T>>) subscriber -> {

            SetChangeListener<T> listener = c -> {
                if (c.wasRemoved()) {
                    subscriber.onNext(new SetChange<T>(c.getElementRemoved(), Flag.REMOVED));
                }
                if (c.wasAdded()) {
                    subscriber.onNext(new SetChange<T>(c.getElementAdded(), Flag.ADDED));
                }
            };
            source.addListener(listener);
            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));

        }).subscribeOn(JavaFxScheduler.platform());
    }
}
