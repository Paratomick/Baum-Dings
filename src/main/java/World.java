import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.*;

/**
 * Created by Max on 12.10.2018.
 */
public class World extends VBox {

    private int height, width, num;

    private int[][] generated;
    private int[][] worldSave;

    private LinkedList<WorldChange> changes;

    private long startTime = 0;
    private long timePlayed = 0;

    public World(int height, int width, int num) {
        this.changes = new LinkedList<>();

        setSize(height, width, num);
    }

    public void generate() {
        generated = new int[height][width];
        worldSave = new int[height][width];

        Random rand = new Random();

        for (int i = 0, maxI = 0; i < num && maxI < 20 * num; i++, maxI++) {
            int y = rand.nextInt(height), x = rand.nextInt(width);
            if (generated[y][x] == Feld.EMPTY) {
                List<int[]> list = new ArrayList<>();
                int[][] directNeighbors = getDirectNeighbors(y, x);
                for (int i1 = 0; i1 < directNeighbors.length; i1++) {
                    int gy = directNeighbors[i1][0], gx = directNeighbors[i1][1];
                    if (generated[gy][gx] == Feld.EMPTY
                            && !generateIsNextToTent(gy, gx)) {
                        list.add(directNeighbors[i1]);
                    }
                }
                if (!list.isEmpty()) {
                    generated[y][x] = Feld.TREE;
                    int[] f = list.get(rand.nextInt(list.size()));
                    generated[f[0]][f[1]] = Feld.TENT;
                } else {
                    i--;
                }
            }
        }
        for (int i = 0; i < generated.length; i++) {
            for (int i1 = 0; i1 < generated[i].length; i1++) {
                if (generated[i][i1] == Feld.EMPTY) generated[i][i1] = Feld.GRASS;
            }
        }
    }

    public void generateExpensiveButGood() {
        generated = new int[height][width];
        worldSave = new int[height][width];
        long seed = new Random().nextLong();
        Random rand = new Random(seed);

        System.out.println("Generate new map with: h:" + height + ", w:" + width + ", seed: " + seed);

        //Populate a map with every tile.
        //key = y * width + x;
        Map<Integer, int[]> remainingTiles = new HashMap<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                remainingTiles.put(y * width + x, new int[]{y, x});
            }
        }

        while (!remainingTiles.isEmpty()) {
            //Get a random remaining tile
            int id = rand.nextInt(remainingTiles.size());
            Iterator<int[]> iter = remainingTiles.values().iterator();
            for (int i = 0; i < id; i++) {
                iter.next();
            }
            int[] tent = iter.next();
            //Check if it has free neighbor tiles for a tree
            int[][] directNeighbors = getDirectNeighbors(tent[0], tent[1]);
            boolean uselessTile = true;
            for (int[] neighbor : directNeighbors) {
                if (generated[neighbor[0]][neighbor[1]] == Feld.EMPTY) {
                    uselessTile = false;
                }
            }
            //Generate tent with tree, or remove the tile from the list
            if (uselessTile) {
                //Remove useless tile.
                remainingTiles.remove(tent[0] * width + tent[1]);
            } else {
                //Generate Tent
                generated[tent[0]][tent[1]] = Feld.TENT;
                //Generate Tree on one of the four sides.
                boolean treeGenerated = false;
                int randomOffset = rand.nextInt(directNeighbors.length);
                for (int i = 0; i < directNeighbors.length && !treeGenerated; i++) {
                    int[] neighbor = directNeighbors[(i + randomOffset) % directNeighbors.length];
                    if (generated[neighbor[0]][neighbor[1]] == Feld.EMPTY) {
                        generated[neighbor[0]][neighbor[1]] = Feld.TREE;
                        treeGenerated = true;
                    }
                }
                //Remove tent tile and all eight neighbor tiles from the list (excluding tiles outside the world).
                for (int iy = tent[0] > 0 ? -1 : 0; iy <= (tent[0] < height - 1 ? 1 : 0); iy++) {
                    for (int ix = tent[1] > 0 ? -1 : 0; ix <= (tent[1] < width - 1 ? 1 : 0); ix++) {
                        remainingTiles.remove((tent[0] + iy) * width + (tent[1] + ix));
                    }
                }
            }
        }
        for (int i = 0; i < generated.length; i++) {
            for (int i1 = 0; i1 < generated[i].length; i1++) {
                if (generated[i][i1] == Feld.EMPTY) generated[i][i1] = Feld.GRASS;
            }
        }
    }

    public int[][] getDirectNeighbors(int y, int x) {
        int[][] directNeighbors = new int[4][2];
        int i = 0;
        if (y > 0) {
            directNeighbors[i++] = new int[]{y - 1, x};
        }
        if (y < height - 1) {
            directNeighbors[i++] = new int[]{y + 1, x};
        }
        if (x > 0) {
            directNeighbors[i++] = new int[]{y, x - 1};
        }
        if (x < width - 1) {
            directNeighbors[i++] = new int[]{y, x + 1};
        }
        if (i < 4) {
            int[][] temp = directNeighbors;
            directNeighbors = new int[i][2];
            for (int i1 = 0; i1 < i; i1++) {
                directNeighbors[i1] = temp[i1];
            }
        }
        return directNeighbors;
    }

    public Feld[] getDirectNeighbors(int y, int x, int[][] states) {
        Feld[] directNeighbors = new Feld[4];
        int i = 0;
        if (y > 0) {
            directNeighbors[i] = new Feld(this, y - 1, x);
            directNeighbors[i++].setState(states[y - 1][x]);
        }
        if (y < height - 1) {
            directNeighbors[i] = new Feld(this, y + 1, x);
            directNeighbors[i++].setState(states[y + 1][x]);
        }
        if (x > 0) {
            directNeighbors[i] = new Feld(this, y, x - 1);
            directNeighbors[i++].setState(states[y][x - 1]);
        }
        if (x < width - 1) {
            directNeighbors[i] = new Feld(this, y, x + 1);
            directNeighbors[i++].setState(states[y][x + 1]);
        }
        if (i < 4) {
            Feld[] temp = directNeighbors;
            directNeighbors = new Feld[i];
            for (int i1 = 0; i1 < i; i1++) {
                directNeighbors[i1] = temp[i1];
            }
        }
        return directNeighbors;
    }

    public Feld[] getNeighbors(int y, int x) {
        Feld[] neighbors = new Feld[8];
        int i = 0;
        for (int iy = y > 0 ? -1 : 0; iy < (y <= height - 1 ? 1 : 0); iy++) {
            for (int ix = x > 0 ? -1 : 0; ix < (x <= width - 1 ? 1 : 0); ix++) {
                if (iy != 0 || ix != 0) neighbors[i++] = getFeld(y + iy, x + ix);
            }
        }
        if (i < 8) {
            Feld[] temp = neighbors;
            neighbors = new Feld[i];
            for (int i1 = 0; i1 < temp.length; i1++) {
                neighbors[i1] = temp[i1];
            }
        }
        return neighbors;
    }

    public boolean generateIsNextToTent(int y, int x) {
        for (int iy = y > 0 ? -1 : 0; iy <= (y < height - 1 ? 1 : 0); iy++) {
            for (int ix = x > 0 ? -1 : 0; ix <= (x < width - 1 ? 1 : 0); ix++) {
                if (generated[y + iy][x + ix] == Feld.TENT) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isNextToTent(int y, int x) {
        for (int iy = y > 0 ? -1 : 0; iy <= (y < height - 1 ? 1 : 0); iy++) {
            for (int ix = x > 0 ? -1 : 0; ix <= (x < width - 1 ? 1 : 0); ix++) {
                if ((iy != 0 || ix != 0) && getFeld(y + iy, x + ix).getState() == Feld.TENT) {
                    return true;
                }
            }
        }
        return false;
    }

    public Feld getFeld(int y, int x) {
        return (Feld) ((HBox) getChildren().get(y + 1)).getChildren().get(x + 1);
    }

    public void init() {
        for (int i = 0; i < height; i++) {
            for (int i2 = 0; i2 < width; i2++) {
                getFeld(i, i2).setState(generated[i][i2] == Feld.TREE ? Feld.TREE : Feld.EMPTY);
            }
        }
        //Spalten
        for (int i = 0; i < height; i++) {
            int num = 0;
            for (int i2 = 0; i2 < width; i2++) {
                if (generated[i][i2] == Feld.TENT) {
                    num++;
                }
            }
            ((NumberFeld) getFeld(i, -1)).setNumber(num);
        }
        //Zeilen
        for (int i = 0; i < width; i++) {
            int num = 0;
            for (int i2 = 0; i2 < height; i2++) {
                if (generated[i2][i] == Feld.TENT) {
                    num++;
                }
            }
            ((NumberFeld) getFeld(-1, i)).setNumber(num);
        }
        save();
        changes.clear();
        startTime = System.currentTimeMillis();
        timePlayed = 0;
    }

    public Feld findFeld(double sceneX, double sceneY) {
        for (int i = height - 1; i >= 0; i--) {
            for (int i2 = 0; i2 < width; i2++) {
                Feld n = getFeld(i, i2);
                Bounds boundsInScene = n.localToScene(n.getBoundsInLocal());
                if (sceneX >= boundsInScene.getMinX()
                        && sceneX < boundsInScene.getMaxX()
                        && sceneY >= boundsInScene.getMinY()
                        && sceneY < boundsInScene.getMaxY()) {
                    return n;
                }
            }
        }
        return null;
    }

    public void save() {
        for (int i = 0; i < height; i++) {
            for (int i2 = 0; i2 < width; i2++) {
                worldSave[i][i2] = getFeld(i, i2).getState();
            }
        }
    }

    public void load() {
        for (int i = 0; i < height; i++) {
            for (int i2 = 0; i2 < width; i2++) {
                getFeld(i, i2).setState(worldSave[i][i2]);
            }
        }
    }

    public void loadFinished() {
        for (int i = 0; i < height; i++) {
            for (int i2 = 0; i2 < width; i2++) {
                getFeld(i, i2).setState(generated[i][i2]);
            }
        }
    }

    public void checkWithGenerated() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Feld tile = getFeld(y, x);
                if (tile.getState() == Feld.TENT && generated[y][x] != Feld.TENT) {
                    if(!tile.getStyleClass().contains("wrongTent")) tile.getStyleClass().add("wrongTent");
                }
            }
        }
    }

    public boolean checkForWin() {
        long nanos = System.nanoTime();
        //Reset wrong tile style.
        for (int i = 0; i < height; i++) {
            for (int i2 = 0; i2 < width; i2++) {
                if (getFeld(i, i2).getStyleClass().contains("wrongTent")) {
                    getFeld(i, i2).getStyleClass().remove("wrongTent");
                } else if (getFeld(i, i2).getStyleClass().contains("wrongTree")) {
                    getFeld(i, i2).getStyleClass().remove("wrongTree");
                } else if (getFeld(i, i2).getStyleClass().contains("wrongConnectionTent")) {
                    getFeld(i, i2).getStyleClass().remove("wrongConnectionTent");
                } else if (getFeld(i, i2).getStyleClass().contains("wrongConnectionTree")) {
                    getFeld(i, i2).getStyleClass().remove("wrongConnectionTree");
                }
            }
        }

        //Win if nothing is wrong.
        boolean win = true;

        //Don't check the rest, if not every sidenumber is correct.
        for (int y = 0; y < height; y++) {
            if (!getFeld(y, -1).getStyleClass().contains("numGrey")) return false;
        }
        for (int x = 0; x < width; x++) {
            if (!getFeld(-1, x).getStyleClass().contains("numGrey")) return false;
        }

        //Fill the states into an array for faster access.
        int[][] check = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int i2 = 0; i2 < width; i2++) {
                Feld f = getFeld(i, i2);
                check[i][i2] = f.getState();
            }
        }

        //Check for tents next to tents.
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (check[y][x] == Feld.TENT && isNextToTent(y, x)) {
                    getFeld(y, x).getStyleClass().add("wrongTent");
                    win = false;
                }
            }
        }

        //Build a connection tree for every Tree, than look if amounts are matching and if tents are not used.
        //Trees already used in the connections of another tree, don't build their own connections.

        //Populate the tree and the tent map.
        TreeSet<Integer> remainingTrees = new TreeSet<>();
        TreeSet<Integer> remainingTents = new TreeSet<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (check[y][x] == Feld.TENT) remainingTents.add(y * width + x);
                else if (check[y][x] == Feld.TREE) remainingTrees.add(y * width + x);
            }
        }

        //Build the connections
        LinkedList<Integer> finishedConnection = new LinkedList<>();
        Stack<Integer> tempTiles = new Stack<>();
        while (!remainingTrees.isEmpty()) {
            int initialTile = remainingTrees.first();
            finishedConnection.clear();
            tempTiles.clear();
            int balance = 0; //+1 for trees, -1 for tents
            tempTiles.add(initialTile);
            remainingTrees.remove(initialTile);
            while (!tempTiles.isEmpty()) {
                int tile = tempTiles.pop();
                Set<Integer> checkList;

                if (check[tile / width][tile % width] == Feld.TREE) {
                    finishedConnection.add(tile);
                    balance++;
                    //Check for neighboring tents.
                    checkList = remainingTents;
                } else {
                    finishedConnection.add(tile);
                    balance--;
                    //Check for neighboring trees.
                    checkList = remainingTrees;
                }

                if (checkList.contains(tile - width)) { //Top
                    tempTiles.add(tile - width);
                    checkList.remove(tile - width);
                }
                if (checkList.contains(tile + width)) { //Bottom
                    tempTiles.add(tile + width);
                    checkList.remove(tile + width);
                }
                if (checkList.contains(tile - 1)) { //Left
                    tempTiles.add(tile - 1);
                    checkList.remove(tile - 1);
                }
                if (checkList.contains(tile + 1)) { //Right
                    tempTiles.add(tile + 1);
                    checkList.remove(tile + 1);
                }
            }
            //Mark unbalanced connections.
            System.out.println("bal: " + balance);
            if (balance != 0) {
                win = false;
                for (int tile : finishedConnection) {
                    if (check[tile / width][tile % width] == Feld.TREE) {
                        getFeld(tile / width, tile % width).getStyleClass().add("wrongConnectionTree");
                    } else {
                        getFeld(tile / width, tile % width).getStyleClass().add("wrongConnectionTent");
                    }
                }
            }
        }
        System.out.println("unusedTrees: " + remainingTrees.size());
        System.out.println("unusedTents: " + remainingTents.size());
        for(int tile : remainingTents) {
            win = false;
            getFeld(tile / width, tile % width).getStyleClass().add("wrongTent");
        }

        long time = System.nanoTime() - nanos;
        System.out.println("Time: " + time + " -> " + (time / 1e6) + "ms");
        if (win) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (getFeld(y, x).getState() == Feld.EMPTY) getFeld(y, x).setState(Feld.GRASS);
                }
            }
            timePlayed = System.currentTimeMillis() - startTime;
        }
        return win;
    }

    public void updateNumbers(Feld feld) {

        //Spalten
        int numTent = 0, numEmpty = 0;
        for (int y = 0; y < height; y++) {
            int state = getFeld(y, feld.getX()).getState();
            if (state == Feld.TENT) numTent++;
            else if (state == Feld.EMPTY) numEmpty++;
        }
        NumberFeld nf = ((NumberFeld) getFeld(-1, feld.getX()));
        if (numTent == nf.getNumber()) {
            nf.getStyleClass().setAll("numGrey");
        } else if (numTent > nf.getNumber()) {
            nf.getStyleClass().setAll("numRed");
        } else if (numEmpty > 0) {
            nf.getStyleClass().setAll("numWhite");
        } else {
            nf.getStyleClass().setAll("numRed");
        }

        //Zeilen
        numTent = 0;
        numEmpty = 0;
        for (int x = 0; x < width; x++) {
            int state = getFeld(feld.getY(), x).getState();
            if (state == Feld.TENT) numTent++;
            else if (state == Feld.EMPTY) numEmpty++;
        }
        nf = ((NumberFeld) getFeld(feld.getY(), -1));
        if (numTent == nf.getNumber()) {
            nf.getStyleClass().setAll("numGrey");
        } else if (numTent > nf.getNumber()) {
            nf.getStyleClass().setAll("numRed");
        } else if (numEmpty > 0) {
            nf.getStyleClass().setAll("numWhite");
        } else {
            nf.getStyleClass().setAll("numRed");
        }
    }

    public void setSize(int height, int width, int num) {
        this.num = num;
        setSize(height, width);
    }

    public void setSize(int height, int width) {
        this.height = height;
        this.width = width;

        getChildren().clear();
        HBox box = new HBox();
        box.getChildren().add(new NumberFeld(this, -1, -1));
        for (int x = 0; x < width; x++) {
            box.getChildren().add(new NumberFeld(this, -1, x));
        }
        getChildren().add(box);
        for (int y = 0; y < height; y++) {
            HBox hbox = new HBox();
            hbox.getChildren().add(new NumberFeld(this, y, -1));
            for (int x = 0; x < width; x++) {
                hbox.getChildren().add(new Feld(this, y, x));
            }
            getChildren().add(hbox);
        }

        generateExpensiveButGood();
        init();
    }

    public void addChange(WorldChange worldChange) {
        changes.add(worldChange);
    }

    public void revokeChange() {
        if (!changes.isEmpty()) {
            WorldChange change = changes.getLast();
            changes.removeLast();

            if (change.changes.length > 0) {
                for (int i = change.changes.length - 1; i >= 0; i--) {
                    SingleWorldChange swc = change.changes[i];
                    getFeld(swc.y, swc.x).setState(swc.state);
                }
            }
        }
    }

    public long getTimePlayed() {
        return timePlayed;
    }

    public int getWorldHeight() {
        return height;
    }

    public int getWorldWidth() {
        return width;
    }

    public boolean[][] getLinkedAsArray() {
        boolean[][] linked = new boolean[height][width];
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                Feld feld = getFeld(y, x);
                linked[y][x] = feld.getStyleClass().contains("connectedTent") || feld.getStyleClass().contains("connectedTree");
            }
        }
        return linked;
    }

    public int[][] getStatesAsArray() {
        int[][] states = new int[height][width];
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                states[y][x] = getFeld(y, x).getState();
            }
        }
        return states;
    }
}
