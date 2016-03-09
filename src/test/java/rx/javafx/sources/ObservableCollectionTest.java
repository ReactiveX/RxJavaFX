package rx.javafx.sources;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import org.junit.Test;

public final class ObservableCollectionTest {


    @Test
    public void testObservableList() {
        JFXPanel panel = new JFXPanel();

        Platform.runLater(() -> {
            ObservableList<String> items = FXCollections.observableArrayList();

            ObservableListSource.fromObservableListDistinctChanges(items)
                    .subscribe(System.out::println, Throwable::printStackTrace);

            items.add("ABQ");
            items.add("HOU");
            items.add("CHI");
            items.add("ABQ");
            items.add("HOU");

            items.remove("ABQ");
            items.remove("CHI");
            items.remove("ABQ");
        });

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
