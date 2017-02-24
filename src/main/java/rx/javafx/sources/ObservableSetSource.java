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
