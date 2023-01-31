import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;

/**
 * Created by Max on 12.10.2018.
 */
public class Feld extends StackPane {
    public static final int EMPTY = 0, GRASS = 1, TENT = 2, TREE = 3;

    private static final String[] style = {"empty", "grass", "tent", "tree"};

    private World world;
    private int x, y;
    private int state;

    public Feld(World world, int y, int x) {
        this.world = world;
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        getStyleClass().setAll(style[state]);
        world.updateNumbers(this);
    }

    public String toString() {
        return "(" + style[state] + ", (" + y + ", " + x + "))";
    }
}
