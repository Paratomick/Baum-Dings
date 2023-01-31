/**
 * Contains one or multiple changes to the world
 */
public class WorldChange {
    public SingleWorldChange[] changes;

    public WorldChange() {
        changes = new SingleWorldChange[0];
    }

    public void add(SingleWorldChange change) {
        SingleWorldChange[] temp = changes;
        changes = new SingleWorldChange[temp.length + 1];
        for (int i = 0; i < temp.length; i++) {
            changes[i] = temp[i];
        }
        changes[temp.length] = change;
    }
}

/**
 * Contains field coordinates with the previous value of the field.
 */
class SingleWorldChange {
    public int x, y, state;

    public SingleWorldChange(int x, int y, int state) {
        this.x = x;
        this.y = y;
        this.state = state;
    }

    public SingleWorldChange(Feld feld) {
        this.x = feld.getX();
        this.y = feld.getY();
        this.state = feld.getState();
    }
}

/**
 * This exists because I don't want more methods for changes in the Main.<br>
 * It creates WorldChanges without being created over and over again in Main.
 */
class WorldChangeGenerator {
    public WorldChange change;

    public WorldChangeGenerator() {
        change = new WorldChange();
    }

    public WorldChange get() {
        WorldChange temp = change;
        change = new WorldChange();
        return temp;
    }
}
