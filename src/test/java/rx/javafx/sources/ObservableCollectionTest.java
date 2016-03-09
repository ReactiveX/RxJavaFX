package rx.javafx.sources;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import org.junit.Test;

public final class ObservableCollectionTest {


    @Test
    public void testObservableList() {
        JFXPanel panel = new JFXPanel();

        ObservableList<String> items = FXCollections.observableArrayList();

        ObservableListSource.fromObservableListAdds(items).subscribe(v -> System.out.println("ADDED: " + v));
        ObservableListSource.fromObservableListRemovals(items).subscribe(v -> System.out.println("REMOVED: " + v));

        items.add("ABQ");
        items.add("HOU");

        items.setAll("LAX","PHX","FLL");
    }
}
