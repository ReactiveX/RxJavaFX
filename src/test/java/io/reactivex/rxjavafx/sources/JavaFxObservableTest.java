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
import io.reactivex.observers.TestObserver;
import io.reactivex.rxjavafx.observables.JavaFxObservable;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Duration;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

public final class JavaFxObservableTest {

	@BeforeClass
	public static void initJFX() {
		try {
			javafx.application.Platform.startup(() ->{});
		}catch(final IllegalStateException ignore) {
		}
	}

    @Test
    public void testIntervalSource() {

        final CountDownLatch latch = new CountDownLatch(5);

        JavaFxObservable.interval(Duration.millis(1000)).take(5)
                .subscribe(v -> latch.countDown());

        try {
            latch.await();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testListEmitOnChanged() {

        ObservableList<String> sourceList = FXCollections.observableArrayList("Alpha", "Beta", "Gamma");

        TestObserver<List<String>> testObserver = new TestObserver<>();

        JavaFxObservable.emitOnChanged(sourceList)
                .subscribe(testObserver);

        testObserver.assertValueCount(1);

        sourceList.add("Delta");

        testObserver.assertValueCount(2);
    }

    @Test
    public void testListPropertyEmitOnChanged() {

        ListProperty<String> sourceList = new SimpleListProperty<>(FXCollections.observableArrayList("Alpha", "Beta", "Gamma"));

        TestObserver<List<String>> testObserver = new TestObserver<>();

        JavaFxObservable.emitOnChanged(sourceList)
                .subscribe(testObserver);

        testObserver.assertValueCount(1);

        sourceList.add("Delta");

        testObserver.assertValueCount(2);

        sourceList.setValue(FXCollections.observableArrayList("Zeta", "Eta", "Iota"));

        testObserver.assertValueCount(3);
    }
    @Test
    public void testRxObservableChanges() {
        Property<String> sourceProperty = new SimpleStringProperty();
        Observable<Change<String>> emissions = JavaFxObservable.changesOf(sourceProperty).take(4);

        TestObserver<Change<String>> testObserver = new TestObserver<>();

        emissions.subscribe(testObserver);

        sourceProperty.setValue("Alpha");
        sourceProperty.setValue("Beta");
        sourceProperty.setValue(null);
        sourceProperty.setValue("Gamma");

        testObserver.assertValues(
                        new Change<>(null, "Alpha"),
                        new Change<>("Alpha", "Beta"),
                        new Change<>("Beta", null),
                        new Change<>(null, "Gamma")
        );
    }

    @Test
    public void testRxObservableListAdds() {

        ObservableList<String> sourceList = FXCollections.observableArrayList();
        Observable<String> emissions = JavaFxObservable.additionsOf(sourceList);

        CountDownLatch gate = new CountDownLatch(1);

        emissions.observeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .take(3)
                .toList()
                .toObservable()
                .subscribe(l -> assertTrue(l.equals(Arrays.asList("Alpha", "Beta", "Gamma"))), Throwable::printStackTrace, gate::countDown);;

        Platform.runLater(() -> {
            sourceList.add("Alpha");
            sourceList.add("Beta");
            sourceList.remove("Alpha");
            sourceList.add("Gamma");
        });

        try {
            gate.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testRxObservableListRemoves() {

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

        ObservableList<String> sourceList = FXCollections.observableArrayList();
        Observable<ListChange<String>> emissions = JavaFxObservable.changesOf(sourceList);

        CountDownLatch gate = new CountDownLatch(1);

        var expected = List.of(
                ListChange.of("Alpha", Flag.ADDED, 0),
                ListChange.of("Beta", Flag.ADDED, 1),
                ListChange.of("Alpha", Flag.REMOVED, 0),
                ListChange.of("Gamma", Flag.ADDED, 1),
                ListChange.of("Gamma", Flag.REMOVED, 1),
                ListChange.of("Epsilon", Flag.ADDED, 0)
        );
        emissions.observeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .take(6)
                .toList()
                .toObservable()
                .subscribe(l -> assertEquals(expected, l),
                                Throwable::printStackTrace,
                                gate::countDown);

        Platform.runLater(() -> {
            sourceList.add("Alpha");
            sourceList.add("Beta");
            sourceList.remove("Alpha");
            sourceList.add("Gamma");
            sourceList.remove("Gamma");
            sourceList.add(0, "Epsilon");
        });

        try {
            gate.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testRxObservableListDistinctChangeMappings() {

        ObservableList<String> sourceList = FXCollections.observableArrayList();
        Observable<ListChange<Integer>> emissions = JavaFxObservable.distinctMappingsOf(sourceList, String::length);

        CountDownLatch gate = new CountDownLatch(1);

        var expected = List.of(
                ListChange.of(5, Flag.ADDED, 0),
                ListChange.of(4, Flag.ADDED, 1),
                ListChange.of(5, Flag.REMOVED, 1),
                ListChange.of(7, Flag.ADDED, 1)
                );
        emissions.observeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .take(4)
                .toList()
                .toObservable()
                .subscribe(l -> assertEquals(expected, l),
                                Throwable::printStackTrace,
                                gate::countDown
                );

        Platform.runLater(() -> {
            sourceList.add("Alpha");
            sourceList.add("Beta");
            sourceList.add("Alpha");
            sourceList.remove("Alpha");
            sourceList.add("Gamma");
            sourceList.remove("Gamma");
            sourceList.remove("Alpha");
            sourceList.add(1, "Epsilon");
        });

        try {
            gate.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testRxObservableListDistinctChanges() {
        ObservableList<String> sourceList = FXCollections.observableArrayList();
        Observable<ListChange<String>> emissions = JavaFxObservable.distinctChangesOf(sourceList);

        CountDownLatch gate = new CountDownLatch(1);

        var expected = List.of(
                ListChange.of("Alpha", Flag.ADDED, 0),
                ListChange.of("Beta", Flag.ADDED, 1),
                ListChange.of("Gamma", Flag.ADDED, 2),
                ListChange.of("Gamma", Flag.REMOVED, 2),
                ListChange.of("Alpha", Flag.REMOVED, 1),
                ListChange.of("Epsilon", Flag.ADDED, 1)
        );

        emissions.observeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.platform())
                .take(6)
                .toList()
                .toObservable()
                .subscribe(l -> assertEquals(expected, l),
                                Throwable::printStackTrace,
                                gate::countDown
                );

        Platform.runLater(() -> {
            sourceList.add("Alpha");
            sourceList.add("Beta");
            sourceList.add("Alpha");
            sourceList.remove("Alpha");
            sourceList.add("Gamma");
            sourceList.remove("Gamma");
            sourceList.remove("Alpha");
            sourceList.add(1, "Epsilon");
        });

        try {
            gate.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testRxObservableListUpdates() {

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
                .observeOn(JavaFxScheduler.platform())
                .take(2)
                .count()
                .toObservable()
                .subscribe(ct -> assertTrue(ct == 2),Throwable::printStackTrace,gate::countDown);

        Platform.runLater(() -> {
            sourceList.addAll(person1,person2,person3);
            person1.age.setValue(24);
            person2.age.setValue(32);
        });

        try {
            gate.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
