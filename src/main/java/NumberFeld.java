import javafx.scene.control.TextField;

/**
 * Created by Max on 13.10.2018.
 */
public class NumberFeld extends Feld {

    private int num = 0;
    private TextField textField;

    public NumberFeld(World world, int y, int x) {
        super(world, y, x);
        textField = new TextField("0");
        textField.getStyleClass().add("NumberFeld");

        getChildren().setAll(textField);

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            int number = num;
            try {
                num = Integer.parseInt(newValue);
            } catch (Exception e) {
                textField.textProperty().set("" + number);
            }
        });

        if(x == -1 && y == -1) {
            textField.visibleProperty().set(false);
            textField.disableProperty().set(true);
        }
    }

    public void setNumber(int num) {
        this.num = num;
        textField.setText("" + num);
        if(num == 0) {
            getStyleClass().setAll("numGrey");
        } else {
            getStyleClass().setAll("numWhite");
        }
    }

    public int getNumber() {
        return num;
    }
}
