import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class Finisher {

    private World world;

    public Finisher(World world) {
        this.world = world;

        AtomicBoolean hasPlantedGrass = new AtomicBoolean(false);
        // Grass every Feld not near a Tree, or next to a tent
        getByType(Feld.EMPTY).forEach(empty -> {
            boolean grass = true;
            if (getNeighbours(empty, Feld.TENT).size() == 0) {
                for (Feld neighbour : getDirectNeighbours(empty, Feld.TREE)) {
                    if (!isBound(neighbour) && !doesTentBlockATree(empty, neighbour)) {
                        grass = false;
                    }
                }
            }
            if (grass) {
                hasPlantedGrass.set(true);
                plantGrass(empty);
            }
        });

        if (!hasPlantedGrass.get()) {
            // Fill all empty direct neighbours for trees with only one neighbour that still need a tent with a tent.
            getByType(Feld.TREE).forEach(tree -> {
                if (isBound(tree)) return;
                Object[] neighbourEmpties = getDirectNeighbours(tree, Feld.EMPTY)
                        .stream().filter(feld -> !doesTentBlockATree(feld, tree)).toArray();
                ArrayList<Feld> neighbourTents = getDirectNeighbours(tree, Feld.TENT);
                ArrayList<Feld> unusedTents = new ArrayList<>();
                for (Feld neighbour : neighbourTents) {
                    if (!isBound(neighbour)) {
                        unusedTents.add(neighbour);
                    }
                }
                if (neighbourEmpties.length == 1 && unusedTents.size() == 0) {
                    setupTent((Feld) neighbourEmpties[0], tree);
                }
                if (neighbourEmpties.length == 0 && unusedTents.size() == 1) {
                    linkTreeToTent(unusedTents.get(0), tree);
                }
            });

            // Link Tents that only have one unused neighbouring tree to that tree.
            getByType(Feld.TENT).forEach(tent -> {
                if (isBound(tent)) return;
                ArrayList<Feld> trees = getDirectNeighbours(tent, Feld.TREE);
                ArrayList<Feld> unusedTrees = new ArrayList<>();
                for (Feld neighbour : trees) {
                    if (!isBound(neighbour)) {
                        unusedTrees.add(neighbour);
                    }
                }
                if (unusedTrees.size() == 1) {
                    linkTreeToTent(tent, unusedTrees.get(0));
                }
            });

            for (int y = 0; y < world.getWorldHeight(); y++) {
                checkLine(y, true);
            }
            for (int x = 0; x < world.getWorldWidth(); x++) {
                checkLine(x, false);
            }
        }
    }


    private ArrayList<Feld> getLine(int n, boolean xAxis) {
        return getLine(n, xAxis, -1);
    }

    private ArrayList<Feld> getLine(int n, boolean xAxis, int type) {
        ArrayList<Feld> list = new ArrayList<>();
        for (int n2 = 0; n2 < world.getWorldWidth(); n2++) {
            Feld f = world.getFeld(xAxis ? n : n2, xAxis ? n2 : n);
            if (type == -1 || f.getState() == type) {
                list.add(f);
            }
        }
        return list;
    }

    private ArrayList<Feld> getDirectNeighbours(Feld f) {
        return getDirectNeighbours(f, -1);
    }

    private ArrayList<Feld> getDirectNeighbours(Feld f, int type) {
        ArrayList<Feld> list = new ArrayList<>();
        for (int[] coordinates : getDirectNeighboursCoordinates(new int[]{f.getY(), f.getX()})) {
            Feld neighbour = world.getFeld(coordinates[0], coordinates[1]);
            if (type == -1 || neighbour.getState() == type) {
                list.add(neighbour);
            }
        }
        return list;
    }

    private ArrayList<int[]> getDirectNeighboursCoordinates(int[] coordinates) {
        ArrayList<int[]> list = new ArrayList<>();
        if (coordinates[1] - 1 >= 0) {
            list.add(new int[]{coordinates[0], coordinates[1] - 1});
        }
        if (coordinates[1] + 1 < world.getWorldWidth()) {
            list.add(new int[]{coordinates[0], coordinates[1] + 1});
        }
        if (coordinates[0] - 1 >= 0) {
            list.add(new int[]{coordinates[0] - 1, coordinates[1]});
        }
        if (coordinates[0] + 1 < world.getWorldHeight()) {
            list.add(new int[]{coordinates[0] + 1, coordinates[1]});
        }
        return list;
    }

    private ArrayList<Feld> getNeighbours(Feld f) {
        return getNeighbours(f, -1);
    }

    private ArrayList<Feld> getNeighbours(Feld f, int type) {
        ArrayList<Feld> list = new ArrayList<>();
        for (int[] coordinates : getNeighboursCoordinates(new int[]{f.getY(), f.getX()})) {
            Feld neighbour = world.getFeld(coordinates[0], coordinates[1]);
            if (type == -1 || neighbour.getState() == type) {
                list.add(neighbour);
            }
        }
        return list;
    }

    private ArrayList<int[]> getNeighboursCoordinates(int[] coordinates) {
        ArrayList<int[]> list = new ArrayList<>();
        for (int x = Math.max(0, coordinates[1] - 1); x <= Math.min(world.getWorldWidth() - 1, coordinates[1] + 1); x++) {
            for (int y = Math.max(0, coordinates[0] - 1); y <= Math.min(world.getWorldHeight() - 1, coordinates[0] + 1); y++) {
                if (x != coordinates[1] || y != coordinates[0]) {
                    list.add(new int[]{y, x});
                }
            }
        }
        return list;
    }

    private ArrayList<Feld> getByType(int type) {
        ArrayList<Feld> list = new ArrayList<>();
        for (int x = 0; x < world.getWorldWidth(); x++) {
            for (int y = 0; y < world.getWorldHeight(); y++) {
                Feld f = world.getFeld(y, x);
                if (f.getState() == type) {
                    list.add(f);
                }
            }
        }
        return list;
    }

    private void checkLine(int n, boolean xAxis) {
        int amount = ((NumberFeld) world.getFeld(xAxis ? n : -1, xAxis ? -1 : n)).getNumber();
        int currentTents = getLine(n, xAxis, Feld.TENT).size();
        int possibleNewTents = 0;

        if (amount == currentTents) {
            getLine(n, xAxis, Feld.EMPTY).forEach(this::plantGrass);
            return;
        }

        ArrayList<ArrayList<Feld>> segments = new ArrayList<>();
        ArrayList<Feld> currentSegment = new ArrayList<>();

        int definitiveTents = 0;
        ArrayList<Feld> line = getLine(n, xAxis);
        ArrayList<Feld> emptiesToRemove = new ArrayList<>();
        for(Feld f : line) {
            if(f.getState() == Feld.EMPTY && !emptiesToRemove.contains(f)) {
                ArrayList<Feld> prev = new ArrayList<>();
                prev.add(f);

                ArrayList<Feld> trees = getDirectNeighbours(f, Feld.TREE);
                trees.removeIf(this::isBound);
                ArrayList<Feld> chain = new ArrayList<>(trees);

                for(int i = 0; i < chain.size(); i++) {
                    Feld chainFeld = chain.get(i);
                    if((xAxis?chainFeld.getY():chainFeld.getX()) != n) break;
                    prev.add(chainFeld);

                    ArrayList<Feld> neighbours = getDirectNeighbours(chainFeld, chainFeld.getState()==Feld.TREE?Feld.EMPTY:Feld.TREE);
                    neighbours.removeAll(prev);
                    neighbours.removeIf(this::isBound);

                    if(neighbours.size() == 0 && chainFeld.getState() == Feld.EMPTY){
                        ArrayList<Feld> emptiesInChain = new ArrayList<>(prev);
                        emptiesInChain.removeIf(feld -> feld.getState() != Feld.EMPTY);
                        ArrayList<Feld> treesInChain = new ArrayList<>(prev);
                        treesInChain.removeIf(feld -> feld.getState() != Feld.TREE);

                        definitiveTents += treesInChain.size();
                        emptiesToRemove.addAll(emptiesInChain);
                    } else {
                        chain.addAll(neighbours);
                    }
                }
            }
        }
        line.removeAll(emptiesToRemove);

        for (Feld f : line) {
            if (f.getState() == Feld.EMPTY) {
                currentSegment.add(f);
            } else {
                if (!currentSegment.isEmpty()) {
                    possibleNewTents += (currentSegment.size() + 1) / 2;
                    segments.add(currentSegment);
                    currentSegment = new ArrayList<>();
                }
            }
        }
        if (!currentSegment.isEmpty()) {
            possibleNewTents += (currentSegment.size() + 1) / 2;
            segments.add(currentSegment);
            currentSegment = new ArrayList<>();
        }
        System.out.printf("%s%d: Need %d, have and place for %d, so %d, definitive %d\n", xAxis?"Row":"Column", n, amount, possibleNewTents, (possibleNewTents + currentTents) - amount, definitiveTents);

        if (possibleNewTents == amount - currentTents - definitiveTents) {
            segments.forEach(segment -> {
                if (segment.size() % 2 == 1) {
                    for (int i = 0; i < segment.size(); i += 2) {
                        setupTent(segment.get(i), null);
                    }
                } else {
                    for (Feld feld : segment) {
                        plantGrassOnNeighbour(feld, !xAxis);
                    }
                }
            });
        } else if (possibleNewTents == amount - currentTents - definitiveTents + 1) {
            boolean lastOneWasUneven = false;
            int lastPos = 0;
            for (ArrayList<Feld> segment : segments) {
                if (segment.size() % 2 == 1) {
                    Feld firstFeldInSegement = segment.get(segment.size() - 1);
                    int currentPos = xAxis ? firstFeldInSegement.getX() : firstFeldInSegement.getY();

                    if (lastOneWasUneven && currentPos - lastPos <= 2) {
                        plantGrassOnNeighbour(world.getFeld(
                                firstFeldInSegement.getY() - (xAxis ? 0 : 1),
                                firstFeldInSegement.getX() - (xAxis ? 1 : 0)
                        ), !xAxis);
                    }

                    lastOneWasUneven = true;
                    Feld lastFeldInSegement = segment.get(segment.size() - 1);
                    lastPos = xAxis ? lastFeldInSegement.getX() : lastFeldInSegement.getY();

                    for (int i = 1; i < segment.size(); i += 2) {
                        plantGrassOnNeighbour(segment.get(i), !xAxis);
                    }
                } else {
                    lastOneWasUneven = false;
                }
            }
        }
    }

    private void plantGrassOnNeighbour(Feld feld, boolean xAxis) {
        ArrayList<Feld> neighbours = new ArrayList<>();
        if (xAxis && feld.getX() > 0)
            neighbours.add(world.getFeld(feld.getY(), feld.getX() - 1));
        if (xAxis && feld.getX() < world.getWorldWidth() - 1)
            neighbours.add(world.getFeld(feld.getY(), feld.getX() + 1));
        if (!xAxis && feld.getY() > 0)
            neighbours.add(world.getFeld(feld.getY() - 1, feld.getX()));
        if (!xAxis && feld.getY() < world.getWorldHeight() - 1)
            neighbours.add(world.getFeld(feld.getY() + 1, feld.getX()));

        for (Feld neighbour : neighbours) {
            if (neighbour.getState() == Feld.EMPTY) {
                plantGrass(neighbour);
            }
        }
    }

    private void plantGrass(Feld grass) {
        if (grass.getState() != Feld.EMPTY) {
            System.err.println("Feld is not empty!");
            return;
        }
        grass.setState(Feld.GRASS);
    }

    private void setupTent(Feld tent, Feld tree) {
        if (tent == null) {
            System.err.println("At least one parameter is null!");
            return;
        }
        if (tent.getState() != Feld.EMPTY) {
            System.err.println("Feld is not empty!");
            return;
        }
        if (getNeighbours(tent, Feld.TREE).size() < 1) {
            System.err.println("No tree next to this Feld!");
            return;
        }
        if (getNeighbours(tent, Feld.TENT).size() > 0) {
            System.err.println("One neighbour is already a tent!");
            return;
        }
        // Set up the tent.
        tent.setState(Feld.TENT);
        // Fill empty neighbours with grass.
        getNeighbours(tent, Feld.EMPTY).forEach(this::plantGrass);
        // Link tree and tent
        if (tree == null) {
            ArrayList<Feld> neighbours = getDirectNeighbours(tent, Feld.TREE);
            if (neighbours.size() == 1) {
                tree = neighbours.get(0);
            }
        }
        linkTreeToTent(tent, tree);
    }

    private void linkTreeToTent(Feld tent, Feld tree) {
        if (tent != null && tree != null) {
            if (!tent.getStyleClass().contains("connectedTent")) {
                tent.getStyleClass().add("connectedTent");
            }
            if (!tree.getStyleClass().contains("connectedTree")) {
                tree.getStyleClass().add("connectedTree");
            }
        }
    }

    private boolean isBound(Feld feld) {
        return feld.getStyleClass().contains("connectedTent") || feld.getStyleClass().contains("connectedTree");
    }

    private boolean doesTentBlockATree(Feld tent, Feld tree) {
        boolean[][] linked = world.getLinkedAsArray();
        int[][] states = world.getStatesAsArray();
        if (doesTentBlockATree(new int[]{tent.getY(), tent.getX()}, new int[]{tree.getY(), tree.getX()}, states, linked)) {
            return true;
        } else {
            int[] yTentCounts = new int[world.getWorldHeight()];
            int[] xTentCounts = new int[world.getWorldWidth()];
            for (int y = 0; y < world.getWorldHeight(); y++) {
                for (int x = 0; x < world.getWorldWidth(); x++) {
                    if (states[y][x] == Feld.TENT) {
                        yTentCounts[y]++;
                        xTentCounts[x]++;
                    }
                }
            }
            for (int x = 0; x < world.getWorldWidth(); x++) {
                if (((NumberFeld) world.getFeld(-1, x)).getNumber() < xTentCounts[x]) {
                    return true;
                }
            }
            for (int y = 0; y < world.getWorldHeight(); y++) {
                if (((NumberFeld) world.getFeld(y, -1)).getNumber() < yTentCounts[y]) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean doesTentBlockATree(int[] tent, int[] tree, int[][] states, boolean[][] linked) {
        if (linked[tree[0]][tree[1]]) return false;

        linked[tent[0]][tent[1]] = true;
        linked[tree[0]][tree[1]] = true;
        states[tent[0]][tent[1]] = Feld.TENT;
        states[tree[0]][tree[1]] = Feld.TREE;

        ArrayList<int[]> updateQueue = new ArrayList<>();

        // Grow Grass around new tent
        for (int[] coordinates : filterListByTypeInState(getNeighboursCoordinates(tent), states, linked, Feld.EMPTY)) {
            states[coordinates[0]][coordinates[1]] = Feld.GRASS;
            updateQueue.addAll(filterListByTypeInState(getDirectNeighboursCoordinates(coordinates), states, linked, Feld.TREE));
        }

        // Look at all near trees
        updateQueue.addAll(filterListByTypeInState(getNeighboursCoordinates(tent), states, linked, Feld.TREE));
        for (int[] neighbouringTree : updateQueue) {
            if (linked[neighbouringTree[0]][neighbouringTree[1]]) continue;
            // Do they have a unbound tent next to them?
            if (filterListByTypeInState(getDirectNeighboursCoordinates(neighbouringTree), states, linked, Feld.TENT).size() > 0) {
                return false;
            }
            // Check if every near tree still has places to place tents, and continue recursion with trees that can only place one tent.
            ArrayList<int[]> neighbouringEmpties = filterListByTypeInState(getDirectNeighboursCoordinates(neighbouringTree), states, linked, Feld.EMPTY);

            if (neighbouringEmpties.isEmpty()) return true;
            if (neighbouringEmpties.size() == 1 && doesTentBlockATree(neighbouringEmpties.get(0), neighbouringTree, states, linked)) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<int[]> filterListByTypeInState(ArrayList<int[]> list, int[][] states, boolean[][] linked, int type) {
        return filterListByTypeInState(list, states, linked, type, null);
    }

    private ArrayList<int[]> filterListByTypeInState(ArrayList<int[]> list, int[][] states, boolean[][] linked, int type, int[] filteredCoordinate) {
        ArrayList<int[]> newList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            int[] c = list.get(i);
            if (filteredCoordinate != null && c[0] == filteredCoordinate[0] && c[1] == filteredCoordinate[1]) continue;
            if (!linked[c[0]][c[1]] && (type == -1 || states[c[0]][c[1]] == type)) {
                newList.add(c);
            }
        }
        return newList;
    }
}
