package rx.javafx.sources;

import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import rx.Observable;
import rx.schedulers.JavaFxScheduler;
import rx.subscriptions.JavaFxSubscriptions;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

public final class ObservableMapSource {

    private ObservableMapSource() {}

    public static <K,T> Observable<ObservableMap<K,T>> fromObservableMap(final ObservableMap<K,T> source) {
        return Observable.create((Observable.OnSubscribe<ObservableMap<K,T>>) subscriber -> {
            MapChangeListener<K,T> listener = c -> subscriber.onNext(source);
            source.addListener(listener);
            subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));
        }).subscribeOn(JavaFxScheduler.getInstance());
    }

    public static <K,T> Observable<Entry<K,T>> fromObservableMapAdds(final ObservableMap<K,T> source) {

        return Observable.create((Observable.OnSubscribe<Entry<K,T>>) subscriber -> {

            MapChangeListener<K,T> listener = c -> {

                if (c.wasAdded()) {
                   subscriber.onNext(new SimpleEntry<>(c.getKey(),c.getValueAdded()));
                }

            };
            source.addListener(listener);
            subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));

        }).subscribeOn(JavaFxScheduler.getInstance());
    }

    public static <K,T> Observable<Entry<K,T>> fromObservableMapRemovals(final ObservableMap<K,T> source) {

        return Observable.create((Observable.OnSubscribe<Entry<K,T>>) subscriber -> {

            MapChangeListener<K,T> listener = c -> {

                if (c.wasRemoved()) {
                    subscriber.onNext(new SimpleEntry<>(c.getKey(),c.getValueRemoved()));
                }

            };
            source.addListener(listener);
            subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));

        }).subscribeOn(JavaFxScheduler.getInstance());
    }

    public static <K,T> Observable<MapChange<K,T>> fromObservableMapChanges(final ObservableMap<K,T> source) {

        return Observable.create((Observable.OnSubscribe<MapChange<K,T>>) subscriber -> {

            MapChangeListener<K,T> listener = c -> {

                if (c.wasRemoved()) {
                    subscriber.onNext(new MapChange<>(c.getKey(),c.getValueRemoved(),Flag.REMOVED));
                }
                if (c.wasAdded()) {
                    subscriber.onNext(new MapChange<>(c.getKey(),c.getValueAdded(),Flag.ADDED));
                }

            };
            source.addListener(listener);
            subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeListener(listener)));

        }).subscribeOn(JavaFxScheduler.getInstance());
    }
}
