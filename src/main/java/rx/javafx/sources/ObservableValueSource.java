package rx.javafx.sources;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.JavaFxSubscriptions;

public class ObservableValueSource {

    /**
     * @see rx.observables.JavaFxObservable#fromObservableValue
     */
    public static <T> Observable<T> fromObservableValue(final ObservableValue<T> fxObservable) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> subscriber) {
                subscriber.onNext(fxObservable.getValue());

                final ChangeListener<T> listener = (observableValue, prev, current) -> subscriber.onNext(current);

                fxObservable.addListener(listener);

                subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> fxObservable.removeListener(listener)));

            }
        });
    }
    /**
     * @see rx.observables.JavaFxObservable#fromObservableValue
     */
    public static <T> Observable<Change<T>> fromObservableValueChanges(final ObservableValue<T> fxObservable) {
        return Observable.create(new Observable.OnSubscribe<Change<T>>() {
            @Override
            public void call(final Subscriber<? super Change<T>> subscriber) {

                final ChangeListener<T> listener = (observableValue, prev, current) -> subscriber.onNext(new Change<>(prev,current));

                fxObservable.addListener(listener);

                subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> fxObservable.removeListener(listener)));

            }
        });
    }

}
