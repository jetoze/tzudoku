package jetoze.tzudoku;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import jetoze.gunga.KeyBindings;
import jetoze.gunga.UiThread;
import jetoze.gunga.layout.Layouts;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.GridState;
import jetoze.tzudoku.ui.ControlPanel;
import jetoze.tzudoku.ui.GameBoard;
import jetoze.tzudoku.ui.GridSize;
import jetoze.tzudoku.ui.GridUi;
import jetoze.tzudoku.ui.GridUiModel;
import jetoze.tzudoku.ui.PuzzleUiController;
import jetoze.tzudoku.ui.PuzzleUiModel;

public class App {

    public static void main(String[] args) throws IOException {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        if ("".isEmpty()) {
            PuzzleInventory inventory = new PuzzleInventory(new File("/Users/torgil/coding/data/tzudoku"));
            App app = new App(inventory);
            UiThread.run(app::start);
        } else {
            Grid grid = loadGrid();
            UiThread.run(() -> {
                installNimbus();

                JFrame frame = new JFrame("tzudoku");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                GridUiModel model = new GridUiModel(grid, GridSize.REGULAR);
                GridUi gridUi = new GridUi(model);
                ControlPanel controlPanel = new ControlPanel(model, null);
                GameBoard gameBoard = new GameBoard(gridUi, controlPanel);
                
                Layouts.border()
                    .center(gameBoard.getUi())
                    .buildAsContent(frame);

                gridUi.registerActions(KeyBindings.whenInFocusedWindow(frame.getRootPane()));

                frame.pack();
                frame.setVisible(true);
                frame.requestFocusInWindow();
            });
        }
    }

    private static void installNimbus() {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, you can set the GUI to another look and feel.
        }
    }

    private static Grid loadGrid() throws IOException {
        File file = new File("/Users/torgil/coding/data/tzudoku/the_daily/the_daily_sudoku_2020-07-11.json");
        String json = Files.readString(file.toPath());
        GridState state = GridState.fromJson(json);
        return state.restoreGrid();
    }
    
    private final PuzzleInventory inventory;
    
    public App(PuzzleInventory inventory) {
        this.inventory = requireNonNull(inventory);
    }
    
    public void start() {
        UiThread.throwIfNotUiThread();
        installNimbus();
        
        PuzzleUiModel model = new PuzzleUiModel(inventory);
        JFrame frame = new JFrame("tzudoku");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        PuzzleUiController controller = new PuzzleUiController(frame, model);
        
        GridUi gridUi = new GridUi(model.getGridModel());
        ControlPanel controlPanel = new ControlPanel(model.getGridModel(), controller);
        GameBoard gameBoard = new GameBoard(gridUi, controlPanel);
        
        Layouts.border()
            .center(gameBoard.getUi())
            .buildAsContent(frame);

        gridUi.registerActions(KeyBindings.whenInFocusedWindow(frame.getRootPane()));

        frame.pack();
        frame.setVisible(true);
        frame.requestFocusInWindow();
        
        controller.selectPuzzle();
    }

}
