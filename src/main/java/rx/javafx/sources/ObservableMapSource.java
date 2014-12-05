package rx.javafx.sources;

import java.util.Map;

import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.JavaFxSubscriptions;

public class ObservableMapSource {

    /**
     * @see rx.observables.JavaFxObservable#fromObservableMap
     */
    public static <K, V> Observable<Map<? extends K, ? extends V>> fromObservableMap(ObservableMap<K, V> map) {
        return Observable.create(new Observable.OnSubscribe<Map<? extends K, ? extends V>>() {
            @Override
            public void call(Subscriber<? super Map<? extends K, ? extends V>> subscriber) {
                subscriber.onNext(map);
                
                final MapChangeListener<K, V> listener = new MapChangeListener<K, V>() {
                    @Override
                    public void onChanged(Change<? extends K, ? extends V> change) {
                        subscriber.onNext(change.getMap());
                    }
                };
                
                map.addListener(listener);
                
                subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(new Action0() {
                    @Override
                    public void call() {
                        map.removeListener(listener);
                    }
                }));
            }
        });
    }
}
