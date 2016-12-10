package rx.javafx.sources;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import rx.schedulers.JavaFxScheduler;
import rx.subscriptions.JavaFxSubscriptions;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

public final class ObservableMapSource {

    private ObservableMapSource() {}

    public static <K,T> Observable<ObservableMap<K,T>> fromObservableMap(final ObservableMap<K,T> source) {
        return Observable.create((ObservableOnSubscribe<ObservableMap<K,T>>) subscriber -> {
            MapChangeListener<K,T> listener = c -> subscriber.onNext(source);
            source.addListener(listener);
            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));
        }).startWith(source).subscribeOn(JavaFxScheduler.getInstance());
    }

    public static <K,T> Observable<Entry<K,T>> fromObservableMapAdds(final ObservableMap<K,T> source) {

        return Observable.create((ObservableOnSubscribe<Entry<K,T>>) subscriber -> {

            MapChangeListener<K,T> listener = c -> {

                if (c.wasAdded()) {
                   subscriber.onNext(new SimpleEntry<K,T>(c.getKey(),c.getValueAdded()));
                }

            };
            source.addListener(listener);
            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));

        }).subscribeOn(JavaFxScheduler.getInstance());
    }

    public static <K,T> Observable<Entry<K,T>> fromObservableMapRemovals(final ObservableMap<K,T> source) {

        return Observable.create((ObservableOnSubscribe<Entry<K,T>>) subscriber -> {

            MapChangeListener<K,T> listener = c -> {

                if (c.wasRemoved()) {
                    subscriber.onNext(new SimpleEntry<K,T>(c.getKey(),c.getValueRemoved()));
                }

            };
            source.addListener(listener);
            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));

        }).subscribeOn(JavaFxScheduler.getInstance());
    }

    public static <K,T> Observable<MapChange<K,T>> fromObservableMapChanges(final ObservableMap<K,T> source) {

        return Observable.create((ObservableOnSubscribe<MapChange<K,T>>) subscriber -> {

            MapChangeListener<K,T> listener = c -> {

                if (c.wasRemoved()) {
                    subscriber.onNext(new MapChange<K,T>(c.getKey(),c.getValueRemoved(),Flag.REMOVED));
                }
                if (c.wasAdded()) {
                    subscriber.onNext(new MapChange<K,T>(c.getKey(),c.getValueAdded(),Flag.ADDED));
                }

            };
            source.addListener(listener);
            subscriber.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));

        }).subscribeOn(JavaFxScheduler.getInstance());
    }
}
