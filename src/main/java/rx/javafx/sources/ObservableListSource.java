package rx.javafx.sources;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import rx.Observable;
import rx.schedulers.JavaFxScheduler;

public final class ObservableListSource {

    public static <T> Observable<T> fromObservableListAdds(final ObservableList<T> source) {

        return Observable.create((Observable.OnSubscribe<T>) subscriber -> {

            source.addListener((ListChangeListener<T>) c -> {
                while (c.next()) {
                    if (c.wasAdded()) {
                        c.getAddedSubList().forEach(subscriber::onNext);
                    }
                }
            });
        }).subscribeOn(JavaFxScheduler.getInstance());
    }
    public static <T> Observable<T> fromObservableListRemovals(final ObservableList<T> source) {

        return Observable.create((Observable.OnSubscribe<T>) subscriber -> {

            source.addListener((ListChangeListener<T>) c -> {
                while (c.next()) {
                    if (c.wasRemoved()) {
                        c.getRemoved().forEach(subscriber::onNext);
                    }
                }
            });
        }).subscribeOn(JavaFxScheduler.getInstance());
    }
}
