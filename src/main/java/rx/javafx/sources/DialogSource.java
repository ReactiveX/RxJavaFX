package rx.javafx.sources;

import io.reactivex.Observable;
import javafx.scene.control.Dialog;
import java.util.Optional;
import rx.schedulers.JavaFxScheduler;

public final class DialogSource {
    private DialogSource() {}

    public static <T> Observable<T> fromDialogSource(final Dialog<T> dialog) {
        return Observable.fromCallable(dialog::showAndWait)
                .subscribeOn(JavaFxScheduler.platform())
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

}
