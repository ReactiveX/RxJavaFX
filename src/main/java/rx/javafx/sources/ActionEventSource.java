package rx.javafx.sources;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import rx.Observable;
import rx.schedulers.JavaFxScheduler;
import rx.subscriptions.JavaFxSubscriptions;

public final class ActionEventSource {
    private ActionEventSource() {}

    public static Observable<ActionEvent> fromActionEvents(final Node node) {
        return NodeEventSource.fromNodeEvents(node, ActionEvent.ACTION);
    }
    public static Observable<ActionEvent> fromActionEvents(final ContextMenu source) {
        return Observable.create((Observable.OnSubscribe<ActionEvent>) subscriber -> {
            final EventHandler<ActionEvent> handler = subscriber::onNext;

            source.addEventHandler(ActionEvent.ANY, handler);

            subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeEventHandler(ActionEvent.ANY, handler)));
        }).subscribeOn(JavaFxScheduler.getInstance());
    }
    public static Observable<ActionEvent> fromActionEvents(final MenuItem source) {
        return Observable.create((Observable.OnSubscribe<ActionEvent>) subscriber -> {
            final EventHandler<ActionEvent> handler = subscriber::onNext;

            source.addEventHandler(ActionEvent.ANY, handler);

            subscriber.add(JavaFxSubscriptions.unsubscribeInEventDispatchThread(() -> source.removeEventHandler(ActionEvent.ANY, handler)));
        }).subscribeOn(JavaFxScheduler.getInstance());
    }

}
