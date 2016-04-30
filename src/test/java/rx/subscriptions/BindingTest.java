package rx.subscriptions;

import javafx.beans.binding.Binding;
import javafx.embed.swing.JFXPanel;
import org.junit.Test;
import rx.Observable;
import rx.schedulers.JavaFxScheduler;
import rx.subscribers.JavaFxSubscriber;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.*;

import static org.junit.Assert.assertTrue;

public final class BindingTest {

    public BindingTest() {
        new JFXPanel();
    }
    @Test
    public void testCompositeBinding() {
        CompositeBinding bindings = new CompositeBinding();

        Observable<Long> source = Observable.interval(1,TimeUnit.SECONDS);
        CountDownLatch unsbuscribeWait = new CountDownLatch(2);

        Binding<Long> binding1 = JavaFxSubscriber.toBinding(source.doOnUnsubscribe(unsbuscribeWait::countDown).observeOn(JavaFxScheduler.getInstance()));
        bindings.add(binding1);

        Binding<Long> binding2 = JavaFxSubscriber.toBinding(source.doOnUnsubscribe(unsbuscribeWait::countDown).reduce(0L,(x,y) -> x + y).observeOn(JavaFxScheduler.getInstance()));
        bindings.add(binding2);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(bindings.hasSubscriptions());

        bindings.dispose();

        try {
            unsbuscribeWait.await(10,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
