import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class SideMenu extends VBox {
    private Button menu, back, save, load, help;

    public SideMenu() {
        setId("sideMenu");

        menu = new Button();
        menu.getStyleClass().addAll("sideBtnMenu");
        back = new Button("<");
        back.getStyleClass().addAll("sideBtnBack");
        save = new Button("S");
        save.getStyleClass().addAll("sideBtnSave");
        load = new Button("L");
        load.getStyleClass().addAll("sideBtnLoad");
        help = new Button("?");
        help.getStyleClass().addAll("sideBtnBack");

        getChildren().addAll(menu, back, save, load, help);
    }

    public Button getMenu() {
        return menu;
    }

    public Button getBack() {
        return back;
    }

    public Button getSave() {
        return save;
    }

    public Button getLoad() {
        return load;
    }

    public Button getHelp() {
        return help;
    }
}
