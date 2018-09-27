/**
 * Copyright 2017 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.reactivex.rxjavafx.subscriptions;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.rxjavafx.observers.JavaFxObserver;
import io.reactivex.rxjavafx.observers.JavaFxSubscriber;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.subjects.PublishSubject;
import javafx.application.Platform;
import javafx.beans.binding.Binding;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public final class BindingTest {


    @Test
    public void testCompositeBinding() {
        CompositeBinding bindings = new CompositeBinding();

        Observable<Long> source = Observable.interval(1, TimeUnit.SECONDS);
        CountDownLatch unsubscribeWait = new CountDownLatch(2);

        Binding<Long> binding1 = JavaFxObserver.toBinding(source.doOnDispose(unsubscribeWait::countDown).observeOn(JavaFxScheduler.platform()));
        bindings.add(binding1);

        Binding<Long> binding2 = JavaFxObserver.toBinding(source.doOnDispose(unsubscribeWait::countDown).reduce(0L, (x, y) -> x + y).observeOn(JavaFxScheduler.platform()).toObservable());
        bindings.add(binding2);

        sleep(3000);
        assertTrue(bindings.hasSubscriptions());

        bindings.dispose();

        try {
            unsubscribeWait.await(10, TimeUnit.SECONDS);
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

            assertEquals(4, emissionCount.get());
            assertEquals("Delta", binding.getValue());
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testObserverNullBinding() {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {

            String nil = "null";
            PublishSubject<String> items = PublishSubject.create();
            Binding<String> binding = JavaFxObserver.toNullBinding(items, nil);
            items.onNext(nil);

            assertNull(binding.getValue());
            items.onNext("Alpha");
            assertEquals("Alpha", binding.getValue());
            items.onNext(nil);
            assertNull(binding.getValue());
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testObserverNullableBinding() {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {

            PublishSubject<Optional<String>> items = PublishSubject.create();
            Binding<String> binding = JavaFxObserver.toNullableBinding(items);
            items.onNext(Optional.empty());

            assertNull(binding.getValue());
            items.onNext(Optional.of("Alpha"));
            assertEquals("Alpha", binding.getValue());
            items.onNext(Optional.empty());
            assertNull(binding.getValue());
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

            assertEquals(0, emissionCount.get());

            binding.getValue();

            sleep(1000);

            assertEquals(4, emissionCount.get());
            assertEquals("Delta", binding.getValue());
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSubscriberNullBinding() {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {

            String nil = "null";
            PublishProcessor<String> items = PublishProcessor.create();
            Binding<String> binding = JavaFxSubscriber.toNullBinding(items, nil);
            items.onNext(nil);

            assertNull(binding.getValue());
            items.onNext("Alpha");
            assertEquals("Alpha", binding.getValue());
            items.onNext(nil);
            assertNull(binding.getValue());
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSubscriberNullableBinding() {
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {

            PublishProcessor<Optional<String>> items = PublishProcessor.create();
            Binding<String> binding = JavaFxSubscriber.toNullableBinding(items);
            items.onNext(Optional.empty());

            assertNull(binding.getValue());
            items.onNext(Optional.of("Alpha"));
            assertEquals("Alpha", binding.getValue());
            items.onNext(Optional.empty());
            assertNull(binding.getValue());
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

            assertEquals(4, emissionCount.get());
            assertEquals("Delta", binding.getValue());
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

            assertEquals(0, emissionCount.get());

            binding.getValue();

            sleep(1000);

            assertEquals(4, emissionCount.get());
            assertEquals("Delta", binding.getValue());
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
