package rx.javafx.sources;

import java.util.Set;

import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.JavaFxSubscriptions;

public class ObservableSetSource {

    /**
     * @see rx.observables.JavaFxObservable#fromObservableSet
     */
    public static <T> Observable<Set<? extends T>> fromObservableSet(ObservableSet<T> set) {
        return Observable.create(new Observable.OnSubscribe<Set<? extends T>>() {
            @Override
            public void call(Subscriber<? super Set<? extends T>> subscriber) {
                subscriber.onNext(set);
                
                final SetChangeListener<T> listener = new SetChangeListener<T>() {
                    @Override
                    public void onChanged(Change<? extends T> change) {
                        subscriber.onNext(change.getSet());
                    }
                };
                
                set.addListener(listener);
                
                subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(new Action0() {
                    @Override
                    public void call() {
                        set.removeListener(listener);
                    }
                }));
            }
        });
    }
}
