package rx.javafx.sources;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import rx.Observable;
import rx.schedulers.JavaFxScheduler;

import java.util.HashSet;

public final class ObservableListSource {

    public static <T> Observable<ObservableList<T>> fromObservableListCurrent(final ObservableList<T> source) {

        return Observable.create((Observable.OnSubscribe<ObservableList<T>>) subscriber -> {

            source.addListener((ListChangeListener<T>) c -> {
                subscriber.onNext(source);
            });
        }).subscribeOn(JavaFxScheduler.getInstance());
    }

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
    public static <T> Observable<FXCollectionChange<T>> fromObservableListChanges(final ObservableList<T> source) {
        return Observable.create((Observable.OnSubscribe<FXCollectionChange<T>>) subscriber -> {

            source.addListener((ListChangeListener<T>) c -> {
                while (c.next()) {
                    if (c.wasAdded()) {
                        c.getAddedSubList().forEach(v -> subscriber.onNext(FXCollectionChange.of(v,Flag.ADDED)));
                    }
                    if (c.wasRemoved()) {
                        c.getRemoved().forEach(v -> subscriber.onNext(FXCollectionChange.of(v,Flag.REMOVED)));
                    }
                }
            });
        }).subscribeOn(JavaFxScheduler.getInstance());
    }

    public static <T> Observable<FXCollectionChange<T>> fromObservableListDistinctChanges(final ObservableList<T> source) {

        return Observable.create((Observable.OnSubscribe<FXCollectionChange<T>>) subscriber -> {

            final HashSet<T> distinctActives = new HashSet<>();
            distinctActives.addAll(source);

            source.addListener((ListChangeListener<T>) c -> {

                while (c.next()) {
                    if (c.wasAdded()) {
                        c.getAddedSubList().stream().filter(distinctActives::add)
                                .forEach(v -> subscriber.onNext(FXCollectionChange.of(v,Flag.ADDED)));
                    }
                    if (c.wasRemoved()) {
                        c.getRemoved().stream().filter(v -> !source.contains(v))
                                .filter(distinctActives::remove)
                                .forEach(v -> subscriber.onNext(FXCollectionChange.of(v,Flag.REMOVED)));
                    }
                }
            });
        }).subscribeOn(JavaFxScheduler.getInstance());
    }
    public static final class FXCollectionChange<T> {
        private final T value;
        private final Flag flag;

        private FXCollectionChange(T value, Flag flag) {
            this.value = value;
            this.flag = flag;
        }
        public static <T> FXCollectionChange<T> of(T value, Flag flag) {
            return new FXCollectionChange<>(value, flag);
        }
        public T getValue() {
            return value;
        }
        public Flag getFlag() {
            return flag;
        }
        @Override
        public String toString() {
            return flag + " " + value;
        }
    }
    public enum Flag {
        ADDED,
        REMOVED;
    }
}
