/**
 * Copyright 2017 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.reactivex.rxjavafx.sources;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.util.Duration;
import org.junit.Test;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public final class JavaFxObservableTest {

    @Test
    public void testIntervalSource() {
        new JFXPanel();

        final CountDownLatch latch = new CountDownLatch(5);

        JavaFxObservable.interval(Duration.millis(100)).take(5)
                .subscribe(v -> latch.countDown());

        try {
            boolean finished = latch.await(1000, TimeUnit.MILLISECONDS);
            assertTrue(finished);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testRxObservableListAdds() {
//        new JFXPanel();

        ObservableList<String> sourceList = FXCollections.observableArrayList();
        Observable<String> emissions = JavaFxObservable.additionsOf(sourceList);

        CountDownLatch gate = new CountDownLatch(1);

        emissions.observeOn(Schedulers.io())
//                .observeOn(JavaFxScheduler.platform())
                .take(3)
                .toList()
                .toObservable()
                .subscribe(l -> assertTrue(l.containsAll(Arrays.asList("Alpha","Beta","Gamma"))),Throwable::printStackTrace,gate::countDown);

        CompletableFuture.runAsync(() -> {
            sourceList.add("Alpha");
            sourceList.add("Beta");
            sourceList.remove("Alpha");
            sourceList.add("Gamma");
        });

        try {
            boolean finished = gate.await(1000, TimeUnit.MILLISECONDS);
            assertTrue(finished);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testRxObservableListRemoves() {
//        new JFXPanel();

        ObservableList<String> sourceList = FXCollections.observableArrayList();
        Observable<String> emissions = JavaFxObservable.removalsOf(sourceList);

        CountDownLatch gate = new CountDownLatch(1);

        emissions.observeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .take(2)
                .toSortedList()
                .toObservable()
                .subscribe(l -> assertTrue(l.equals(Arrays.asList("Alpha","Gamma"))),Throwable::printStackTrace,gate::countDown);

        Platform.runLater(() -> {
            sourceList.add("Alpha");
            sourceList.add("Beta");
            sourceList.remove("Alpha");
            sourceList.add("Gamma");
            sourceList.remove("Gamma");
        });

        try {
            gate.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRxObservableListChanges() {
//        new JFXPanel();

        ObservableList<String> sourceList = FXCollections.observableArrayList();
        Observable<ListChange<String>> emissions = JavaFxObservable.changesOf(sourceList);

        CountDownLatch gate = new CountDownLatch(1);

        class FlagAndCount {
            final Flag flag;
            final long count;
            FlagAndCount(Flag flag, long count) {
                this.flag = flag;
                this.count = count;
            }

        }
        emissions.observeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .take(5)
                .groupBy(ListChange::getFlag)
                .flatMapSingle(grp -> grp.count().map(ct -> new FlagAndCount(grp.getKey(),ct)))
                .subscribe(l -> {
                    if (l.flag.equals(Flag.ADDED)) { assertTrue(l.count == 3); }
                    if (l.flag.equals(Flag.REMOVED)) { assertTrue(l.count == 2); }
                },Throwable::printStackTrace,gate::countDown);

        Platform.runLater(() -> {
            sourceList.add("Alpha");
            sourceList.add("Beta");
            sourceList.remove("Alpha");
            sourceList.add("Gamma");
            sourceList.remove("Gamma");
        });

        try {
            gate.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testRxObservableListDistinctChangeMappings() {
//        new JFXPanel();

        ObservableList<String> sourceList = FXCollections.observableArrayList();
        Observable<ListChange<Integer>> emissions = JavaFxObservable.distinctMappingsOf(sourceList, String::length);

        CountDownLatch gate = new CountDownLatch(1);

        class FlagAndCount {
            final Flag flag;
            final long count;
            FlagAndCount(Flag flag, long count) {
                this.flag = flag;
                this.count = count;
            }

        }
        emissions.observeOn(Schedulers.io())
//                .observeOn(JavaFxScheduler.platform())
                .take(3)
                .groupBy(ListChange::getFlag)
                .flatMapSingle(grp -> grp.count().map(ct -> new FlagAndCount(grp.getKey(),ct)))
                .subscribe(l -> {
                    if (l.flag.equals(Flag.ADDED)) { assertTrue(l.count == 2); }
                    if (l.flag.equals(Flag.REMOVED)) { assertTrue(l.count == 1); }
                },Throwable::printStackTrace,gate::countDown);

        CompletableFuture.runAsync(() -> {
            sourceList.add("Alpha");
            sourceList.add("Beta");
            sourceList.add("Alpha");
            sourceList.remove("Alpha");
            sourceList.add("Gamma");
            sourceList.remove("Gamma");
            sourceList.remove("Alpha");
        });

        try {
            boolean finished = gate.await(1000, TimeUnit.MILLISECONDS);
            assertTrue(finished);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testRxObservableListDistinctChanges() {
//        new JFXPanel();

        ObservableList<String> sourceList = FXCollections.observableArrayList();
        Observable<ListChange<String>> emissions = JavaFxObservable.distinctChangesOf(sourceList);

        CountDownLatch gate = new CountDownLatch(1);

        class FlagAndCount {
            final Flag flag;
            final long count;
            FlagAndCount(Flag flag, long count) {
                this.flag = flag;
                this.count = count;
            }

        }
        emissions.observeOn(Schedulers.io())
//                .observeOn(JavaFxScheduler.platform())
                .take(5)
                .groupBy(ListChange::getFlag)
                .flatMapSingle(grp -> grp.count().map(ct -> new FlagAndCount(grp.getKey(),ct)))
                .subscribe(l -> {
                    if (l.flag.equals(Flag.ADDED)) { assertTrue(l.count == 3); }
                    if (l.flag.equals(Flag.REMOVED)) { assertTrue(l.count == 2); }
                },Throwable::printStackTrace,gate::countDown);

        CompletableFuture.runAsync(() -> {
            sourceList.add("Alpha");
            sourceList.add("Beta");
            sourceList.add("Alpha");
            sourceList.remove("Alpha");
            sourceList.add("Gamma");
            sourceList.remove("Gamma");
            sourceList.remove("Alpha");
        });

        try {
            boolean finished = gate.await(1000, TimeUnit.MILLISECONDS);
            assertTrue(finished);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testRxObservableListUpdates() {
//        new JFXPanel();

        class Person {
            Property<String> name;
            Property<Integer> age = new SimpleObjectProperty<>();

            Person(String name, Integer age) {
                this.name = new ReadOnlyStringWrapper(name);
                this.age.setValue(age);
            }
            @Override
            public String toString() {
                return name.getValue();
            }
        }

        Person person1 = new Person("Tom Salma",23);
        Person person2 = new Person("Jacob Mores", 31);
        Person person3 = new Person("Sally Reyes", 32);

        ObservableList<Person> sourceList = FXCollections.observableArrayList(user -> new javafx.beans.Observable[]{user.age} );
        Observable<Person> emissions = JavaFxObservable.updatesOf(sourceList);

        CountDownLatch gate = new CountDownLatch(1);

        emissions.observeOn(Schedulers.io())
//                .observeOn(JavaFxScheduler.platform())
                .take(2)
                .count()
                .toObservable()
                .subscribe(ct -> assertTrue(ct == 2),Throwable::printStackTrace,gate::countDown);

        CompletableFuture.runAsync(() -> {
            sourceList.addAll(person1,person2,person3);
            person1.age.setValue(24);
            person2.age.setValue(32);
        });

        try {
            boolean finished = gate.await(1000, TimeUnit.MILLISECONDS);
            assertTrue(finished);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testcompositeObservableInfinite() {

        new JFXPanel();

        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            final List<String> emissions = new ArrayList<>();
            CompositeObservable<String> compositeObservable = new CompositeObservable<>();

            PublishSubject<String> source1 = PublishSubject.create();
            PublishSubject<String> source2 = PublishSubject.create();
            PublishSubject<String> source3 = PublishSubject.create();

            Disposable sub1 = compositeObservable.add(source1);
            Disposable sub2 = compositeObservable.add(source2);
            Disposable sub3 = compositeObservable.add(source3);

            compositeObservable.toObservable().subscribe(emissions::add);

            source1.onNext("Alpha");
            assertTrue(emissions.get(0).equals("Alpha"));

            source2.onNext("Beta");
            assertTrue(emissions.get(1).equals("Beta"));

            source3.onNext("Gamma");
            assertTrue(emissions.get(2).equals("Gamma"));

            source1.onNext("Delta");
            assertTrue(emissions.get(3).equals("Delta"));

            sub2.dispose();

            source2.onNext("Epsilon");
            assertTrue(emissions.size() == 4);

            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
