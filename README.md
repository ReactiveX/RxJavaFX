# RxJavaFX: JavaFX bindings for RxJava

Learn more about RxJava on the <a href="https://github.com/ReactiveX/RxJava/wiki">Wiki Home</a> and the <a href="http://techblog.netflix.com/2013/02/rxjava-netflix-api.html">Netflix TechBlog post</a> where RxJava was introduced.

RxJavaFX is a simple API to convert JavaFX events into RxJava Observables and vice versa. It also has a scheduler to safely move emissions to the JavaFX Event Dispatch Thread. 

## Master Build Status

<a href='https://travis-ci.org/ReactiveX/RxJavaFX/builds'><img src='https://travis-ci.org/ReactiveX/RxJavaFX.svg?branch=0.x'></a>

## Communication

- Google Group: [RxJava](http://groups.google.com/d/forum/rxjava)
- Twitter: [@RxJava](http://twitter.com/RxJava)
- [GitHub Issues](https://github.com/ReactiveX/RxJava/issues)


## Binaries

Binaries and dependency information for Maven, Ivy, Gradle and others can be found at [http://search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Cio.reactivex.rxjavafx).

Example for Maven:

```xml
<dependency>
    <groupId>io.reactivex</groupId>
    <artifactId>rxjavafx</artifactId>
    <version>x.y.z</version>
</dependency>
```

Gradle: 

```groovy 
dependencies {
	compile 'io.reactivex:rxjavafx:x.y.z'
}
```
Ivy:

```xml
<dependency org="io.reactivex" name="rxjavafx" rev="x.y.z" />
```

## Build

To build:

```
$ git clone git@github.com:ReactiveX/RxJavaFX.git
$ cd RxJavaFX/
$ ./gradlew build
```

## Features

RxJavaFX has a lightweight set of features:
- Factories to turn `Node`, `ObservableValue`, `ObservableList`, and other component events into an RxJava `Observable`
- Factories to turn an RxJava `Observable` into a JavaFX `Binding`. 
- A scheduler for the JavaFX dispatch thread

###Node Events
You can get event emissions by calling `JavaFxObservable.fromNodeEvents()` and pass the JavaFX `Node` and the `EventType` you are interested in.  This will return an RxJava `Observable`. 

```java
Button incrementBttn = new Button("Increment");

Observable<ActionEvent> bttnEvents =
        JavaFxObservable.fromNodeEvents(incrementBttn, ActionEvent.ACTION);
```

###Action Events
Action events are common and do not only apply to `Node` types. They also emit from `MenuItem` and `ContextMenu` instances, as well as a few other types. 

Therefore, a few overloaded factories are provided to emit `ActionEvent` items from these controls

#####Button ActionEvents
```java
Button incrementBttn = new Button("Increment");

Observable<ActionEvent> bttnEvents =
        JavaFxObservable.fromActionEvents(incrementBttn);
```
#####MenuItem ActionEvents
```java
MenuItem menuItem = new MenuItem("Select me");

Observable<ActionEvent> menuItemEvents = 
        JavaFxObservable.fromActionEvents(menuItem);
```

###Other Event Factories

There are also factories provided to convert events from a `Window` as well as a `Scene` into an `Observable`. If you would like to see factories for other components and event types, please let us know or put in a PR. 

#####Emitting Scene Events

```java
Observable<MouseEvent> sceneMouseMovements =
     JavaFxObservable.fromSceneEvents(scene, MouseEvent.MOUSE_MOVED);

sceneMouseMovements.subscribe(v -> System.out.println(v.getSceneX() + "," + v.getSceneY()));
```

#####Emitting Window Hiding Events
```java
 Observable<WindowEvent> windowHidingEvents =
    JavaFxObservable.fromWindowEvents(primaryStage,WindowEvent.WINDOW_HIDING);

windowHidingEvents.subscribe(v -> System.out.println("Hiding!"));
```

###ObservableValue 
Not to be confused with the RxJava `Observable`, the JavaFX `ObservableValue` can be converted into an RxJava `Observable` that emits the initial value and all value changes. 

```java
TextField textInput = new TextField();

Observable<String> textInputs =
        JavaFxObservable.fromObservableValue(textInput.textProperty());
```
Note that many Nodes in JavaFX will have an initial value, which sometimes can be `null`, and you might consider using RxJava's `skip()` operator to ignore this initial value. 

#####ObservableValue Changes

For every change to an `ObservableValue`, you can emit the old value and new value as a pair. The two values will be wrapped up in a `Change` class and you can access them via `getOldVal()` and `getNewVal()`. Just call the `JavaFxObservable.fromObservableValueChanges()` factory. 

```java
SpinnerValueFactory<Integer> svf = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100);
Spinner spinner = new Spinner<>();
spinner.setValueFactory(svf);
spinner.setEditable(true);

Label spinnerChangesLabel = new Label();
Subscription subscription = JavaFxObservable.fromObservableValueChanges(spinner.valueProperty())
        .map(change -> "OLD: " + change.getOldVal() + " NEW: " + change.getNewVal())
        .subscribe(spinnerChangesLabel::setText);
```

###ObservableList, ObservableMap, and ObservableSet

There are several factories to emit many useful `ObservableList`, `ObservableMap`, and `ObservableSet` events as Observables. These all can be found as static factory methods in the `JavaFxObservable` static class. 


|Factory Method|Parameter Type|Return Type|Description|
|---|---|---|---
|fromObservableList()|ObservableList&lt;T>|Observable&lt;ObservableList&lt;T>>|Emits the entire `ObservableList` every time it changes|
|fromObservableListAdds()|ObservableList&lt;T>|Observable&lt;T>|Emits additions to an `ObservableList`|
|fromfromObservableListRemovals()|ObservableList&lt;T>|Observable&lt;T>|Emits removals from an `ObservableList`|
|fromObservableListUpdates|ObservableList&lt;T>|Observable&lt;ListChange&lt;T>>|Emits every item that was the result of a change to an `ObservableList`, with an `ADDED`, `REMOVED`, or `UPDATED` flag|
|fromObservableListDistinctChanges()|ObservableList&lt;T>| Observable&lt;ListChange&lt;R>>|Emits only *distinct* addtions and removals to an `ObservableList`|
|fromObservableListDistinctChanges()|ObservableList&lt;T>, Func1&lt;T,R>| Observable&lt;ListChange&lt;R>>|Emits only *distinct* additions and removals to an `ObservableList` and emits the mapping|
|fromObservableListDistinctChanges()|ObservableList&lt;T>, Func1&lt;T,R>| Observable&lt;ListChange&lt;R>>|Emits only *distinct* additions and removals to an `ObservableList` based on a mapping|
|fromObservableMap()|ObservableMap&lt;K,T>|Observable&lt;ObservableMap&lt;K,T>>|Emits the entire `ObservableMap` every time it changes|
|fromObservableMapAdds()|ObservableMap&lt;K,T>|Observable&lt;Map.Entry&lt;K,T>>|Emits every `Map.Entry<K,T>` added to an `ObservableMap`|
|fromObservableMapRemovals()|ObservableMap&lt;K,T>|Observable&lt;Map.Entry&lt;K,T>>|Emits every `Map.Entry<K,T>` removed from an `ObservableMap`|
|fromObservableMapChanges()|ObservableMap&lt;K,T>|Observable&lt;MapChange&lt;K,T>>|Emits every key/value pair with an `ADDED` or `REMOVED` flag.|
|fromObservableSet()|ObservableSet&lt;T>|Observable&lt;ObservableSet&lt;T>>|Emits the entire `ObservableSet` every time it changes|
|fromObservableSetAdds()|ObservableSet&lt;T>|Observable&lt;T>|Emits every addition to an `ObservableSet`|
|fromObservableSetRemovals()|ObservableSet&lt;T>|Observable&lt;T>|Emits every removal to an `ObservableSet`|
|fromObservableSetChanges()|ObservableSet&lt;T>|Observable&lt;SetChange&lt;T>|Emits every item `ADDED` or `REMOVED` item from an `ObservableSet` with the corresponding flag|


###Binding
You can convert an RxJava `Observable` into a JavaFX `Binding` by calling the `JavaFxSubscriber.toBinding()` factory. Calling the `dispose()` method on the `Binding` will handle the unsubscription from the `Observable`.  You can then take this `Binding` to bind other control properties to it. 

```java
Button incrementBttn = new Button("Increment");
Label incrementLabel =  new Label("");

Observable<ActionEvent> bttnEvents =
        JavaFxObservable.fromNodeEvents(incrementBttn, ActionEvent.ACTION);
        
Observable<String> accumulations = bttnEvents.map(e -> 1)
        .scan(0,(x, y) -> x + y)
        .map(Object::toString);
        
Binding<String> binding = JavaFxSubscriber.toBinding(accumulations);

incrementLabel.textProperty().bind(binding);

//do stuff, then dispose Binding
binding.dispose();

```

It is usually good practice to specify an `onError` to the `Binding`, just like a normal `Subscriber` so you can handle any errors that are communicated up the chain. 

```
incrementLabel.textProperty().bind(binding, e -> e.printStackTrace());
```


###Lazy Binding

The `toBinding()` factory above will eagerly subscribe the `Observable` to the `Binding` implementation. But if you want to delay the subscription to the `Observable` until the `Binding` is actually used (specifically when its `getValue()` is called), use `toLazyBinding()` instead. 

```java
Binding<String> lazyBinding = JavaFxSubscriber.toBinding(myObservable);
```
This can be handy for data controls like `TableView`, which will only request values for records that are visible. Using the `toLazyBinding()` to feed column values will cause subscriptions to only happen with visible records. 

###CompositeBinding

You also have the option to use a `CompositeBinding` to group multiple `Binding`s together, and `dispose()` them all at once. It is the JavaFX equivalent to `CompositeSubscription`. 

```java
Binding<Long> binding1 = ...
bindings.add(binding1);

Binding<Long> binding2 = ... 
bindings.add(binding2);

//do stuff on UI, and dispose() both bindings
bindings.dispose();
```        


### JavaFX Scheduler

When you update any JavaFX control, it must be done on the JavaFX Event Dispatch Thread. Fortunately, the `JavaFxScheduler` makes it trivial to take work off the JavaFX thread and put it back when the results are ready.  Below we can use the `observeOn()` to pass text value emissions to a computation thread where the text will be flipped. Then we can pass `JavaFxScheduler.getInstance()` to another `observeOn()` afterwards to put it back on the JavaFX thread. From there it will update the `flippedTextLabel`.

```java
TextField textInput = new TextField();
Label fippedTextLabel = new Label();

Observable<String> textInputs =
        JavaFxObservable.fromObservableValue(textInput.textProperty());

sub2 = textInputs.observeOn(Schedulers.computation())
        .map(s -> new StringBuilder(s).reverse().toString())
        .observeOn(JavaFxScheduler.getInstance())
        .subscribe(fippedTextLabel::setText);
```

###JavaFX Interval

There is a JavaFX equivalent to `Observable.interval()` that will emit on the JavaFX thread instead. Calling `JavaFxObservable.interval()` will push consecutive `Long` values at the specified `Duration`. 

```java
Observable<Long> everySecond = JavaFxObservable.interval(Duration.millis(1000));
```

##Differences from ReactFX
[ReactFX](https://github.com/TomasMikula/ReactFX) is a popular API to implement reactive patterns with JavaFX using the `EventStream`. However, RxJava uses an `Observable` and the two are not (directly) compatible with each other. 

Although ReactFX has some asynchronous operators like `threadBridge`, ReactFX emphasizes *synchronous* behavior. This means it encourages keeping events on the JavaFX thread. RxJavaFX, which fully embraces RxJava and *asynchronous* design, can switch between threads and schedulers with ease.  As long as subscriptions affecting the UI are observed on the JavaFX thread, you can leverage the powerful operators and libraries of RxJava safely.

If you are heavily dependent on RxJava, asynchronous processing, or do not want your entire reactive codebase to be UI-focused, you will probably want to use RxJavaFX. 

##Notes for Kotlin 
If you are building your JavaFX application with [Kotlin](https://kotlinlang.org/), check out [RxKotlinFX](https://github.com/thomasnield/RxKotlinFX) to leverage this library through Kotlin extension functions.

## Comprehensive Example
```java

import javafx.application.Application;
import javafx.beans.binding.Binding;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import rx.Observable;
import rx.Subscription;
import rx.observables.JavaFxObservable;
import rx.subscribers.JavaFxSubscriber;

public class RxJavaFXTest extends Application {

    private final Button incrementBttn;
    private final Label incrementLabel;
    private final Binding<String> binding1;

    private final TextField textInput;
    private final Label flippedTextLabel;
    private final Binding<String> binding2;

    private final Spinner<Integer> spinner;
    private final Label spinnerChangesLabel;
    private final Subscription subscription;

    public RxJavaFXTest() {

        //initialize increment
        //demoTurns button events into Binding
        incrementBttn = new Button("Increment");
        incrementLabel =  new Label("");

        Observable<ActionEvent> bttnEvents =
                JavaFxObservable.fromActionEvents(incrementBttn);

        binding1 = JavaFxSubscriber.toBinding(bttnEvents.map(e -> 1).scan(0,(x, y) -> x + y)
                .map(Object::toString));

        incrementLabel.textProperty().bind(binding1);

        //initialize text flipper
        //Schedules on computation Scheduler for text flip calculation
        //Then resumes on JavaFxScheduler thread to update Binding
        textInput = new TextField();
        flippedTextLabel = new Label();

        Observable<String> textInputs =
                JavaFxObservable.fromObservableValue(textInput.textProperty());

        binding2 = JavaFxSubscriber.toBinding(textInputs.observeOn(Schedulers.computation())
                .map(s -> new StringBuilder(s).reverse().toString())
                .observeOn(JavaFxScheduler.getInstance()));

        flippedTextLabel.textProperty().bind(binding2);

        //initialize Spinner value changes
        //Emits Change items containing old and new value
        //Uses RxJava Subscription instead of Binding just to show that option
        SpinnerValueFactory<Integer> svf = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100);
        spinner = new Spinner<>();
        spinner.setValueFactory(svf);
        spinner.setEditable(true);

        spinnerChangesLabel = new Label();
        subscription = JavaFxObservable.fromObservableValueChanges(spinner.valueProperty())
                .map(change -> "OLD: " + change.getOldVal() + " NEW: " + change.getNewVal())
                .subscribe(spinnerChangesLabel::setText);

    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        GridPane gridPane = new GridPane();

        gridPane.setHgap(10);
        gridPane.setVgap(10);

        gridPane.add(incrementBttn,0,0);
        gridPane.add(incrementLabel,1,0);

        gridPane.add(textInput,0,1);
        gridPane.add(flippedTextLabel, 1,1);

        gridPane.add(spinner,0,2);
        gridPane.add(spinnerChangesLabel,1,2);

        Scene scene = new Scene(gridPane);


        primaryStage.setWidth(275);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        binding1.dispose();
        binding2.dispose();
        subscription.unsubscribe();
    }
}
```

## Bugs and Feedback

For bugs, questions and discussions please use the [Github Issues](https://github.com/ReactiveX/RxJavaFX/issues).

 
## LICENSE

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

<http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
