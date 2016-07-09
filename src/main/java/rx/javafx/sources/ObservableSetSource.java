package rx.javafx.sources;

import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import rx.Observable;
import rx.schedulers.JavaFxScheduler;
import rx.subscriptions.JavaFxSubscriptions;

public final class ObservableSetSource {
    private ObservableSetSource() {}

    public static <T> Observable<ObservableSet<T>> fromObservableSet(final ObservableSet<T> source) {

        return Observable.create((Observable.OnSubscribe<ObservableSet<T>>) subscriber -> {
            subscriber.onNext(source);
            SetChangeListener<T> listener = c -> subscriber.onNext(source);
            subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));
        }).startWith(source).subscribeOn(JavaFxScheduler.getInstance());
    }

    public static <T> Observable<T> fromObservableSetAdds(final ObservableSet<T> source) {

        return Observable.create((Observable.OnSubscribe<T>) subscriber -> {

            SetChangeListener<T> listener = c -> {
                if (c.wasAdded()) {
                    subscriber.onNext(c.getElementAdded());
                }
            };
            source.addListener(listener);
            subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));

        }).subscribeOn(JavaFxScheduler.getInstance());
    }

    public static <T> Observable<T> fromObservableSetRemovals(final ObservableSet<T> source) {

        return Observable.create((Observable.OnSubscribe<T>) subscriber -> {

            SetChangeListener<T> listener = c -> {
                if (c.wasRemoved()) {
                    subscriber.onNext(c.getElementRemoved());
                }
            };
            source.addListener(listener);
            subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));

        }).subscribeOn(JavaFxScheduler.getInstance());
    }

    public static <T> Observable<SetChange<T>> fromObservableSetChanges(final ObservableSet<T> source) {

        return Observable.create((Observable.OnSubscribe<SetChange<T>>) subscriber -> {

            SetChangeListener<T> listener = c -> {
                if (c.wasRemoved()) {
                    subscriber.onNext(new SetChange<T>(c.getElementRemoved(), Flag.REMOVED));
                }
                if (c.wasAdded()) {
                    subscriber.onNext(new SetChange<T>(c.getElementAdded(), Flag.ADDED));
                }
            };
            source.addListener(listener);
            subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));

        }).subscribeOn(JavaFxScheduler.getInstance());
    }
}
