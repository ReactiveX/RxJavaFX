package rx.javafx.sources;

import io.reactivex.Observable;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import rx.subscriptions.JavaFxSubscriptions;
import java.util.concurrent.atomic.AtomicLong;

public final class TimerSource {
    private TimerSource() {
    }


    public static Observable<Long> interval(final Duration duration) {
        return Observable.create(sub -> {
            final AtomicLong value = new AtomicLong(0);
            Timeline timeline = new Timeline(new KeyFrame(duration, ae -> sub.onNext(value.getAndIncrement())));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.play();

            sub.setDisposable(JavaFxSubscriptions.unsubscribeInEventDispatchThread(timeline::stop));
        });
    }
}
