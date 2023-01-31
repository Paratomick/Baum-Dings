import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * A screen, shown to the user after he successfully won a game.
 */
public class VictoryScreen extends VBox {
    private Label label;
    private Button close;

    public VictoryScreen() {
        setId("victoryScreen");

        label = new Label("Gespielte Zeit: 00:00:00");
        close = new Button("Menu");

        getChildren().addAll(label, close);
    }

    public void setTime(long timePlayed) {
        int h = (int)(timePlayed / 3600000);
        int min = (int)(timePlayed / 60000) - h * 60;
        int s = (int)(timePlayed / 1000) - (h * 60 + min) * 60;
        label.setText("Gespielte Zeit: " + (h<10?"0"+h:h) + ":" + (min<10?"0"+min:min) + ":" + (s<10?"0"+s:s));
    }

    public Button getClose() {
        return close;
    }
}
