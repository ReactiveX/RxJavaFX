package rx.subscriptions;

import io.reactivex.Observable;
import javafx.beans.binding.Binding;
import javafx.embed.swing.JFXPanel;
import org.junit.Test;
import rx.observables.JavaFxObservable;
import rx.observers.JavaFxObserver;
import rx.schedulers.JavaFxScheduler;
import rx.observers.JavaFxSubscriber;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public final class BindingTest {

    public BindingTest() {
        new JFXPanel();
    }
    @Test
    public void testCompositeBinding() {
        CompositeBinding bindings = new CompositeBinding();

        Observable<Long> source = Observable.interval(1,TimeUnit.SECONDS);
        CountDownLatch unsubscribeWait = new CountDownLatch(2);

        Binding<Long> binding1 = JavaFxObserver.toBinding(source.doOnDispose(unsubscribeWait::countDown).observeOn(JavaFxScheduler.platform()));
        bindings.add(binding1);

        Binding<Long> binding2 = JavaFxObserver.toBinding(source.doOnDispose(unsubscribeWait::countDown).reduce(0L,(x,y) -> x + y).observeOn(JavaFxScheduler.platform()).toObservable());
        bindings.add(binding2);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(bindings.hasSubscriptions());

        bindings.dispose();

        try {
            unsubscribeWait.await(10,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
