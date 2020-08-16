package jetoze.tzudoku;

import static java.util.Objects.requireNonNull;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import jetoze.gunga.Actions;
import jetoze.gunga.KeyBindings;
import jetoze.gunga.KeyStrokes;
import jetoze.gunga.UiThread;
import jetoze.gunga.layout.Layouts;
import jetoze.tzudoku.model.Puzzle;
import jetoze.tzudoku.ui.CellInputController;
import jetoze.tzudoku.ui.ControlPanel;
import jetoze.tzudoku.ui.GameBoard;
import jetoze.tzudoku.ui.GridUi;
import jetoze.tzudoku.ui.HintController;
import jetoze.tzudoku.ui.PuzzleUiController;
import jetoze.tzudoku.ui.PuzzleUiModel;
import jetoze.tzudoku.ui.StatusPanel;
import jetoze.tzudoku.ui.UiLook;
import jetoze.tzudoku.ui.hint.HintUiFactory;

public class TzudokuApp {

    public static void main(String[] args) throws IOException {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WikiTeX");
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
        UiLook.installSystemLookAndFeel();
        
        PuzzleUiModel model = new PuzzleUiModel(inventory);
        JFrame appFrame = new JFrame("tzudoku");
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        StatusPanel statusPanel = new StatusPanel();
        PuzzleUiController controller = new PuzzleUiController(appFrame, model, statusPanel);
        
        GridUi gridUi = new GridUi(model.getGridModel());
        CellInputController cellInputController = CellInputController.forSolving(model.getGridModel());
        HintController hintController = new HintController(appFrame, model.getGridModel(), new HintUiFactory());
        ControlPanel controlPanel = new ControlPanel(model.getGridModel(), controller, cellInputController, hintController);
        GameBoard gameBoard = new GameBoard(gridUi, controlPanel);
        
        Layouts.border()
            .center(gameBoard.getUi())
            .south(statusPanel)
            .buildAsContent(appFrame);
        
        // TODO: Mnemonics. Create MenuWidget and MenuItemWidget that understands things like "&Puzzle".
        //       Use the same strategy for buttons.
        // TODO: Add a "menu builder"?
        JMenuBar menuBar = new JMenuBar();
        JMenu puzzleMenu = new JMenu("Puzzle");
        // TODO: Use an "action builder" for this?
        Action openAction = Actions.toAction("Open...", controller::selectPuzzle);
        openAction.putValue(Action.ACCELERATOR_KEY, KeyStrokes.commandDown(KeyEvent.VK_O));
        Action newAction = Actions.toAction("New...", () -> System.out.println("TODO: Open the Puzzle Builder"));
        newAction.putValue(Action.ACCELERATOR_KEY, KeyStrokes.commandDown(KeyEvent.VK_N));
        Action saveAction = Actions.toAction("Save", controller::saveProgress);
        saveAction.putValue(Action.ACCELERATOR_KEY,  KeyStrokes.commandDown(KeyEvent.VK_S));
        puzzleMenu.add(new JMenuItem(openAction));
        puzzleMenu.add(new JMenuItem(newAction));
        puzzleMenu.add(new JSeparator());
        puzzleMenu.add(new JMenuItem(saveAction));
        menuBar.add(puzzleMenu);
        appFrame.setJMenuBar(menuBar);
        
        
        KeyBindings keyBindings = KeyBindings.whenInFocusedWindow(appFrame.getRootPane());
        gridUi.registerDefaultActions(keyBindings);
        cellInputController.registerKeyBindings(keyBindings);
        hintController.registerKeyBindings(keyBindings);

        appFrame.pack();
        appFrame.setLocationRelativeTo(null);
        appFrame.setVisible(true);
        appFrame.requestFocusInWindow();
        
        if (puzzle != null) {
            controller.loadPuzzle(puzzle);
        } else {
            controller.selectPuzzle();
        }
    }

}
