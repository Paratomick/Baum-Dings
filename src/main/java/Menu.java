import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class Menu extends VBox {

    private Button resume, restart, start, stop;
    private TextField number1, number2;

    public Menu() {
        setId("menu");

        resume = new Button("Weiter spielen");
        restart = new Button("Level neu starten");
        start = new Button("Neues Spiel");
        stop = new Button("Schließen");
        number1 = new TextField("5");
        number2 = new TextField("5");
        Label x = new Label("x");

        getChildren().addAll(resume, restart, start, new HBox(number1, x, number2), stop);
    }

    public Button getResume() {
        return resume;
    }

    public Button getRestart() {
        return restart;
    }

    public Button getStart() {
        return start;
    }

    public Button getStop() {
        return stop;
    }

    public TextField getNumber1() {
        return number1;
    }

    public TextField getNumber2() {
        return number2;
    }
}

