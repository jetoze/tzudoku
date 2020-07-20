package jetoze.tzudoku;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;
import javax.swing.JFrame;

import jetoze.gunga.KeyBindings;
import jetoze.gunga.UiThread;
import jetoze.gunga.layout.Layouts;
import jetoze.tzudoku.model.Puzzle;
import jetoze.tzudoku.ui.ControlPanel;
import jetoze.tzudoku.ui.GameBoard;
import jetoze.tzudoku.ui.GridUi;
import jetoze.tzudoku.ui.PuzzleUiController;
import jetoze.tzudoku.ui.PuzzleUiModel;
import jetoze.tzudoku.ui.StatusPanel;
import jetoze.tzudoku.ui.UiLook;

public class TzudokuApp {

    public static void main(String[] args) throws IOException {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        PuzzleInventory inventory = new PuzzleInventory(new File("/Users/torgil/coding/data/tzudoku"));
        TzudokuApp app = new TzudokuApp(inventory);
        UiThread.run(app::start);
    }
    
    private final PuzzleInventory inventory;
    @Nullable
    private final Puzzle puzzle;
    
    public TzudokuApp(PuzzleInventory inventory) {
        this(inventory, null);
    }
    
    public TzudokuApp(PuzzleInventory inventory, @Nullable Puzzle puzzle) {
        this.inventory = requireNonNull(inventory);
        this.puzzle = puzzle;
    }
    
    public void start() {
        UiThread.throwIfNotUiThread();
        UiLook.installNimbus();
        
        PuzzleUiModel model = new PuzzleUiModel(inventory);
        JFrame frame = new JFrame("tzudoku");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        StatusPanel statusPanel = new StatusPanel();
        PuzzleUiController controller = new PuzzleUiController(frame, model, statusPanel);
        
        GridUi gridUi = new GridUi(model.getGridModel());
        ControlPanel controlPanel = new ControlPanel(model.getGridModel(), controller);
        GameBoard gameBoard = new GameBoard(gridUi, controlPanel);
        
        Layouts.border()
            .center(gameBoard.getUi())
            .south(statusPanel)
            .buildAsContent(frame);

        KeyBindings keyBindings = KeyBindings.whenInFocusedWindow(frame.getRootPane());
        gridUi.registerDefaultActions(keyBindings);
        gridUi.registerValueModeActions(keyBindings);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.requestFocusInWindow();
        
        if (puzzle != null) {
            controller.loadPuzzle(puzzle);
        } else {
            controller.selectPuzzle();
        }
    }

}
