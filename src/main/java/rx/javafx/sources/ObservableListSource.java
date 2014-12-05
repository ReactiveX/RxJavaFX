package rx.javafx.sources;

import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.JavaFxSubscriptions;

public class ObservableListSource {

    /**
     * @see rx.observables.JavaFxObservable#fromObservableList
     */
    public static <T> Observable<List<? extends T>> fromObservableList(ObservableList<T> list) {
        return Observable.create(new Observable.OnSubscribe<List<? extends T>>() {
            @Override
            public void call(Subscriber<? super List<? extends T>> subscriber) {
                subscriber.onNext(list);
                
                final ListChangeListener<T> listener = new ListChangeListener<T>() {
                    @Override
                    public void onChanged(Change<? extends T> change) {
                        subscriber.onNext(change.getList());
                    }
                };
                
                list.addListener(listener);
                
                subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(new Action0() {
                    @Override
                    public void call() {
                        list.removeListener(listener);
                    }
                }));
            }
        });
    }
}
