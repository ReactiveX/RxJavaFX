package rx.subscriptions;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import javafx.application.Platform;
import javafx.beans.binding.Binding;
import javafx.embed.swing.JFXPanel;
import org.junit.Test;
import rx.observables.JavaFxObservable;
import rx.observers.JavaFxObserver;
import rx.schedulers.JavaFxScheduler;
import rx.observers.JavaFxSubscriber;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

        sleep(3000);
        assertTrue(bindings.hasSubscriptions());

        bindings.dispose();

        try {
            unsubscribeWait.await(10,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testObserverBinding() {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {

            final AtomicInteger emissionCount = new AtomicInteger(0);

            Observable<String> items =
                    Observable.just("Alpha", "Beta", "Gamma", "Delta")
                            .doOnNext(s -> emissionCount.incrementAndGet());

            Binding<String> binding = JavaFxObserver.toBinding(items);

            assertTrue(emissionCount.get() == 4);
            assertTrue(binding.getValue().equals("Delta"));
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testObserverLazyBinding() {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {

            final AtomicInteger emissionCount = new AtomicInteger(0);

            Observable<String> items =
                    Observable.just("Alpha", "Beta", "Gamma", "Delta")
                        .doOnNext(s -> emissionCount.incrementAndGet());

            Binding<String> binding = JavaFxObserver.toLazyBinding(items);

            sleep(1000);

            assertTrue(emissionCount.get() == 0);

            binding.getValue();

            sleep(1000);

            assertTrue(emissionCount.get() == 4);
            assertTrue(binding.getValue().equals("Delta"));
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSubscriberBinding() {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {

            final AtomicInteger emissionCount = new AtomicInteger(0);

            Flowable<String> items =
                    Flowable.just("Alpha", "Beta", "Gamma", "Delta")
                            .doOnNext(s -> emissionCount.incrementAndGet());

            Binding<String> binding = JavaFxSubscriber.toBinding(items);

            assertTrue(emissionCount.get() == 4);
            assertTrue(binding.getValue().equals("Delta"));
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSubscriberLazyBinding() {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {

            final AtomicInteger emissionCount = new AtomicInteger(0);

            Flowable<String> items =
                    Flowable.just("Alpha", "Beta", "Gamma", "Delta")
                            .doOnNext(s -> emissionCount.incrementAndGet());

            Binding<String> binding = JavaFxSubscriber.toLazyBinding(items);

            sleep(1000);

            assertTrue(emissionCount.get() == 0);

            binding.getValue();

            sleep(1000);

            assertTrue(emissionCount.get() == 4);
            assertTrue(binding.getValue().equals("Delta"));
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
