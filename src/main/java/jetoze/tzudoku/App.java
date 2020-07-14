package jetoze.tzudoku;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import jetoze.gunga.KeyBindings;
import jetoze.gunga.UiThread;
import jetoze.gunga.layout.Layouts;
import jetoze.tzudoku.ui.ControlPanel;
import jetoze.tzudoku.ui.GameBoard;
import jetoze.tzudoku.ui.GridUi;
import jetoze.tzudoku.ui.PuzzleUiController;
import jetoze.tzudoku.ui.PuzzleUiModel;
import jetoze.tzudoku.ui.StatusPanel;

public class App {

    public static void main(String[] args) throws IOException {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        PuzzleInventory inventory = new PuzzleInventory(new File("/Users/torgil/coding/data/tzudoku"));
        App app = new App(inventory);
        UiThread.run(app::start);
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
        StatusPanel statusPanel = new StatusPanel();
        PuzzleUiController controller = new PuzzleUiController(frame, model, statusPanel);
        
        GridUi gridUi = new GridUi(model.getGridModel());
        ControlPanel controlPanel = new ControlPanel(model.getGridModel(), controller);
        GameBoard gameBoard = new GameBoard(gridUi, controlPanel);
        
        Layouts.border()
            .center(gameBoard.getUi())
            .south(statusPanel)
            .buildAsContent(frame);

        gridUi.registerActions(KeyBindings.whenInFocusedWindow(frame.getRootPane()));

        frame.pack();
        frame.setVisible(true);
        frame.requestFocusInWindow();
        
        controller.selectPuzzle();
    }

}
