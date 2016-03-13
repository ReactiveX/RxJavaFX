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
import rx.schedulers.JavaFxScheduler;
import rx.schedulers.Schedulers;
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
                JavaFxObservable.fromActionEvents(incrementBttn).observeOn(Schedulers.computation());

        binding1 = JavaFxSubscriber.toBinding(bttnEvents.map(e -> 1).scan(0,(x, y) -> x + y).observeOn(JavaFxScheduler.getInstance())
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