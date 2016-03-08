package rx.javafx.sources;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import rx.Observable;
import rx.Subscriber;
import rx.schedulers.JavaFxScheduler;
import rx.subscriptions.JavaFxSubscriptions;

public final class WindowEventSource {

    private WindowEventSource() {}

    public static <T extends WindowEvent> Observable<T> fromWindowEvents(final Window source, final EventType<T> eventType) {

        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> subscriber) {
                final EventHandler<T> handler = subscriber::onNext;

                source.addEventHandler(eventType, handler);

                subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeEventHandler(eventType, handler)));
            }

        }).subscribeOn(JavaFxScheduler.getInstance());
    }
}
