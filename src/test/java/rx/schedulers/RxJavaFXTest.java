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
