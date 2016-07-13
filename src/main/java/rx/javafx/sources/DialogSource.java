package rx.javafx.sources;

import javafx.scene.control.Dialog;
import java.util.Optional;
import rx.Observable;
import rx.schedulers.JavaFxScheduler;

public final class DialogSource {
    private DialogSource() {}

    public static <T> Observable<T> fromDialogSource(final Dialog<T> dialog) {
        return Observable.fromCallable(dialog::showAndWait)
                .subscribeOn(JavaFxScheduler.getInstance())
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
