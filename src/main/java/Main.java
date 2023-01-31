import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    private boolean grass = false;
    private int height = 10, width = 10;

    private Stage stage;
    private Scene scene;
    private StackPane root;
    private World world;
    private SideMenu sideMenu;
    private Menu menu;
    private VictoryScreen victoryScreen;

    private boolean freePlay = true;

    private boolean menuOpen = false;
    private boolean gameOver = false;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;
        root = new StackPane();
        scene = new Scene(root, (width + 4) * 40, (height + 2) * 40);
        scene.getStylesheets().add("style.css");
        primaryStage.setTitle("Baum Dings");
        primaryStage.setScene(scene);
        primaryStage.show();

        world = new World(height, width, (height * width / 4));
        sideMenu = new SideMenu();
        menu = new Menu();
        victoryScreen = new VictoryScreen();

        closeMenu();
        gameOver = false;

        WorldChangeGenerator change = new WorldChangeGenerator();

        scene.setOnMousePressed(k -> {
            if (!gameOver && !menuOpen) {
                if (k.getButton().equals(MouseButton.PRIMARY)) {
                    Feld f = world.findFeld(k.getSceneX(), k.getSceneY());
                    if (f != null) {
                        if (f.getState() < Feld.TREE) {
                            change.change.add(new SingleWorldChange(f));
                            f.setState((f.getState() + 1) % 3);
                            if (world.checkForWin()) showScore();
                            if (f.getState() == Feld.GRASS) {
                                grass = true;
                            }
                        }
                    }
                } else if (freePlay && k.getButton().equals(MouseButton.SECONDARY)) {
                    Feld f = world.findFeld(k.getSceneX(), k.getSceneY());
                    if (f != null) {
                        change.change.add(new SingleWorldChange(f));
                        f.setState(f.getState()!=Feld.TREE?Feld.TREE:Feld.EMPTY);
                    }
                }
            }
        });
        scene.setOnMouseReleased(k -> {
            if (k.getButton().equals(MouseButton.PRIMARY)) {
                grass = false;
                if (change.change.changes.length > 0) {
                    world.addChange(change.get());
                }
            }
        });
        scene.setOnMouseDragged(k -> {
            if (!gameOver && !menuOpen && k.getButton().equals(MouseButton.PRIMARY)) {
                Feld f = world.findFeld(k.getSceneX(), k.getSceneY());
                if (f != null && grass && f.getState() == Feld.EMPTY) {
                    change.change.add(new SingleWorldChange(f));
                    f.setState(Feld.GRASS);
                    if (world.checkForWin()) showScore();
                }
            }
        });

        sideMenu.getMenu().setOnAction(e -> openMenu());
        sideMenu.getBack().setOnMouseClicked(e -> {
            if (!gameOver && !menuOpen) world.revokeChange();
        });
        sideMenu.getSave().setOnMouseClicked(e -> {
            if (!gameOver && !menuOpen) world.save();
        });
        sideMenu.getLoad().setOnMouseClicked(e -> {
            if (!gameOver && !menuOpen) world.load();
        });
        sideMenu.getHelp().setOnMouseClicked(e -> {
            if (!gameOver && !menuOpen) {
                world.checkWithGenerated();
                new Finisher(world);
            }
        });

        menu.getResume().setOnAction(e -> closeMenu());
        menu.getRestart().setOnMouseClicked(e -> {
            world.init();
            gameOver = false;
            closeMenu();
        });
        menu.getStart().setOnMouseClicked(e -> {
            readSize();
            updateSize();
            closeMenu();
            gameOver = false;
        });
        menu.getStop().setOnMouseClicked(e -> primaryStage.close());

        menu.getNumber1().setOnAction(e -> {
            readSize();
            updateSize();
            closeMenu();
            gameOver = false;
        });
        menu.getNumber2().setOnAction(e -> {
            readSize();
            updateSize();
            closeMenu();
            gameOver = false;
        });
        menu.getNumber1().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                menu.getNumber1().textProperty().set(newValue.replaceAll("[^\\d]", ""));
            }
        });
        menu.getNumber2().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                menu.getNumber2().textProperty().set(newValue.replaceAll("[^\\d]", ""));
            }
        });

        victoryScreen.getClose().setOnAction(e -> openMenu());
    }

    public void readSize() {
        height = Integer.parseInt(menu.getNumber1().textProperty().get());
        width = Integer.parseInt(menu.getNumber2().textProperty().get());
        world.setSize(height, width, (height * width / 4));
    }

    public void updateSize() {
        int nh = (height + 2) * 40 + 30;
        int nw = (width + 4) * 40 + 16;
        if (nh > stage.getHeight()) {
            stage.setHeight(nh);
        }
        if (nw > stage.getHeight()) {
            stage.setWidth(nw);
        }
    }

    public void openMenu() {
        root.getChildren().setAll(world, menu);
        menuOpen = true;
        sideMenu.getMenu().setCancelButton(false);
        sideMenu.getMenu().setDefaultButton(false);
        menu.getResume().setCancelButton(true);
        menu.getResume().setDefaultButton(true);
    }

    public void closeMenu() {
        root.getChildren().setAll(world, sideMenu);
        menuOpen = false;
        menu.getResume().setCancelButton(false);
        menu.getResume().setDefaultButton(false);
        sideMenu.getMenu().setCancelButton(true);
        sideMenu.getMenu().setDefaultButton(true);
    }

    public void showScore() {
        System.out.println("WIN");
        victoryScreen.setTime(world.getTimePlayed());
        root.getChildren().setAll(world, victoryScreen);
        gameOver = true;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
