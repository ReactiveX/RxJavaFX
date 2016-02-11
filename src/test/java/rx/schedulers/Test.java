package rx.schedulers;

import javafx.beans.binding.Binding;
import rx.Observable;
import rx.subscribers.JavaFxSubscriber;

public class Test {
    private void test() {
        Observable<String> source = Observable.just("Alpha","Beta","Gamma");

        Binding binding = JavaFxSubscriber.toBinding(source.map(String::length));

    }
}
