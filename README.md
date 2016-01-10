# RxJavaFX: JavaFX bindings for RxJava

Learn more about RxJava on the <a href="https://github.com/ReactiveX/RxJava/wiki">Wiki Home</a> and the <a href="http://techblog.netflix.com/2013/02/rxjava-netflix-api.html">Netflix TechBlog post</a> where RxJava was introduced.

RxJavaFX is a simple API to convert JavaFX events into RxJava Observables. It also has a scheduler to safely move emissions to the JavaFX Event Dispatch Thread. 

## Master Build Status

<a href='https://travis-ci.org/ReactiveX/RxJavaFX/builds'><img src='https://travis-ci.org/ReactiveX/RxJavaFX.svg?branch=0.x'></a>

## Communication

- Google Group: [RxJava](http://groups.google.com/d/forum/rxjava)
- Twitter: [@RxJava](http://twitter.com/RxJava)
- [GitHub Issues](https://github.com/ReactiveX/RxJava/issues)


## Binaries

Binaries and dependency information for Maven, Ivy, Gradle and others can be found at [http://search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Cio.reactivex.rxjava-reactive-streams).

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

RxJavaFX has two sets of features: 
- Factories to turn `Node` and `ObservableValue` events into an RxJava `Observable`
- A scheduler for the JavaFX dispatch thread

###Node Events
You can get event emissions by calling `JavaFxObservable.fromNodeEvents()` and pass the JavaFX `Node` and the `EventType` you are interested in.  This will return an RxJava `Observable`. 
```java
Button incrementBttn = new Button("Increment");

Observable<ActionEvent> bttnEvents =
        JavaFxObservable.fromNodeEvents(incrementBttn, ActionEvent.ACTION);
```


### ObservableValue Changes
Not to be confused with the RxJava `Observable`, the JavaFX `ObservableValue` can be converted into an RxJava `Observable` that emits the initial value and all value changes. 

```java
TextField textInput = new TextField();

Observable<String> textInputs =
        JavaFxObservable.fromObservableValue(textInput.textProperty());
```

Note that many Nodes in JavaFX will have an initial value, which sometimes can be `null`, and you might consider using RxJava's `skip()` operator to ignore this initial value. 

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

##Differences from ReactFX
[ReactFX](https://github.com/TomasMikula/ReactFX) is a popular API to implement reactive patterns with JavaFX using the `EventStream`. However, RxJava uses an `Observable` and the two are not (directly) compatible with each other. 

Although ReactFX has some asynchronous operators like `threadBridge`, ReactFX emphasizes *synchronous* behavior. This means it encourages keeping events on the JavaFX thread. RxJavaFX, which fully embraces RxJava and *asynchronous* design, can switch between threads and schedulers with ease.  As long as subscriptions affecting the UI are observed on the JavaFX thread, you can leverage the powerful operators and libraries of RxJava safely.

If you are heavily dependent on RxJava, asynchronous processing, or do not want your entire reactive codebase to be UI-focused, you will probably want to use RxJavaFX. 

## Comprehensive Example
```java
package rx.schedulers;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import rx.Observable;
import rx.Subscription;
import rx.observables.JavaFxObservable;

public class RxJavaFXTest extends Application {

    private final Button incrementBttn;
    private final Label incrementLabel;
    private final Subscription sub1;

    private final TextField textInput;
    private final Label fippedTextLabel;
    private final Subscription sub2;

    public RxJavaFXTest() {

        //initialize increment demo
        incrementBttn = new Button("Increment");
        incrementLabel =  new Label("");

        Observable<ActionEvent> bttnEvents =
                JavaFxObservable.fromNodeEvents(incrementBttn, ActionEvent.ACTION);

        sub1 = bttnEvents.map(e -> 1).scan(0,(x,y) -> x + y)
                .map(Object::toString)
                .subscribe(incrementLabel::setText);

        //initialize text flipper
        textInput = new TextField();
        fippedTextLabel = new Label();

        Observable<String> textInputs =
                JavaFxObservable.fromObservableValue(textInput.textProperty());

        sub2 = textInputs.observeOn(Schedulers.computation())
                .map(s -> new StringBuilder(s).reverse().toString())
                .observeOn(JavaFxScheduler.getInstance())
                .subscribe(fippedTextLabel::setText);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        GridPane gridPane = new GridPane();

        gridPane.setHgap(10);
        gridPane.setVgap(10);

        gridPane.add(incrementBttn,0,0);
        gridPane.add(incrementLabel,1,0);

        gridPane.add(textInput,0,1);
        gridPane.add(fippedTextLabel, 1,1);

        Scene scene = new Scene(gridPane);
        primaryStage.setWidth(265);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        sub1.unsubscribe();
        sub2.unsubscribe();
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
