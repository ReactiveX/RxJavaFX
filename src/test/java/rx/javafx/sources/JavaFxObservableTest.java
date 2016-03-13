package rx.javafx.sources;

import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import org.junit.Test;
import rx.Observable;
import rx.observables.JavaFxObservable;
import rx.schedulers.JavaFxScheduler;
import rx.schedulers.Schedulers;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertTrue;

public final class JavaFxObservableTest {

    @Test
    public void testRxObservableListAdds() {
        new JFXPanel();

        ObservableList<String> sourceList = FXCollections.observableArrayList();
        Observable<String> emissions = JavaFxObservable.fromObservableListAdds(sourceList);

        CountDownLatch gate = new CountDownLatch(1);

        emissions.observeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.getInstance())
                .take(3)
                .toList()
                .subscribe(l -> assertTrue(l.containsAll(Arrays.asList("Alpha","Beta","Gamma"))),Throwable::printStackTrace,gate::countDown);

        Platform.runLater(() -> {
            sourceList.add("Alpha");
            sourceList.add("Beta");
            sourceList.remove("Alpha");
            sourceList.add("Gamma");
        });

        try {
            gate.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testRxObservableListRemoves() {
        new JFXPanel();

        ObservableList<String> sourceList = FXCollections.observableArrayList();
        Observable<String> emissions = JavaFxObservable.fromObservableListRemovals(sourceList);

        CountDownLatch gate = new CountDownLatch(1);

        emissions.observeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.getInstance())
                .take(2)
                .toSortedList()
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
        new JFXPanel();

        ObservableList<String> sourceList = FXCollections.observableArrayList();
        Observable<ListChange<String>> emissions = JavaFxObservable.fromObservableListChanges(sourceList);

        CountDownLatch gate = new CountDownLatch(1);

        class FlagAndCount {
            final Flag flag;
            final int count;
            FlagAndCount(Flag flag, int count) {
                this.flag = flag;
                this.count = count;
            }

        }
        emissions.observeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.getInstance())
                .take(5)
                .groupBy(ListChange::getFlag)
                .flatMap(grp -> grp.count().map(ct -> new FlagAndCount(grp.getKey(),ct)))
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
        new JFXPanel();

        ObservableList<String> sourceList = FXCollections.observableArrayList();
        Observable<ListChange<Integer>> emissions = JavaFxObservable.fromObservableListDistinctMappings(sourceList, String::length);

        CountDownLatch gate = new CountDownLatch(1);

        class FlagAndCount {
            final Flag flag;
            final int count;
            FlagAndCount(Flag flag, int count) {
                this.flag = flag;
                this.count = count;
            }

        }
        emissions.observeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.getInstance())
                .take(3)
                .groupBy(ListChange::getFlag)
                .flatMap(grp -> grp.count().map(ct -> new FlagAndCount(grp.getKey(),ct)))
                .subscribe(l -> {
                    if (l.flag.equals(Flag.ADDED)) { assertTrue(l.count == 2); }
                    if (l.flag.equals(Flag.REMOVED)) { assertTrue(l.count == 1); }
                },Throwable::printStackTrace,gate::countDown);

        Platform.runLater(() -> {
            sourceList.add("Alpha");
            sourceList.add("Beta");
            sourceList.add("Alpha");
            sourceList.remove("Alpha");
            sourceList.add("Gamma");
            sourceList.remove("Gamma");
            sourceList.remove("Alpha");
        });

        try {
            gate.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testRxObservableListDistinctChanges() {
        new JFXPanel();

        ObservableList<String> sourceList = FXCollections.observableArrayList();
        Observable<ListChange<String>> emissions = JavaFxObservable.fromObservableListDistinctChanges(sourceList);

        CountDownLatch gate = new CountDownLatch(1);

        class FlagAndCount {
            final Flag flag;
            final int count;
            FlagAndCount(Flag flag, int count) {
                this.flag = flag;
                this.count = count;
            }

        }
        emissions.observeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.getInstance())
                .take(5)
                .groupBy(ListChange::getFlag)
                .flatMap(grp -> grp.count().map(ct -> new FlagAndCount(grp.getKey(),ct)))
                .subscribe(l -> {
                    if (l.flag.equals(Flag.ADDED)) { assertTrue(l.count == 3); }
                    if (l.flag.equals(Flag.REMOVED)) { assertTrue(l.count == 2); }
                },Throwable::printStackTrace,gate::countDown);

        Platform.runLater(() -> {
            sourceList.add("Alpha");
            sourceList.add("Beta");
            sourceList.add("Alpha");
            sourceList.remove("Alpha");
            sourceList.add("Gamma");
            sourceList.remove("Gamma");
            sourceList.remove("Alpha");
        });

        try {
            gate.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testRxObservableListUpdates() {
        new JFXPanel();

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
        Observable<Person> emissions = JavaFxObservable.fromObservableListUpdates(sourceList);

        CountDownLatch gate = new CountDownLatch(1);

        emissions.observeOn(Schedulers.io())
                .observeOn(JavaFxScheduler.getInstance())
                .take(2)
                .count()
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