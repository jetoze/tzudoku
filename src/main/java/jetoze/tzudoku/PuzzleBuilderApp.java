package jetoze.tzudoku;

import static java.util.Objects.requireNonNull;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import jetoze.gunga.UiThread;
import jetoze.tzudoku.ui.PuzzleBuilderController;
import jetoze.tzudoku.ui.PuzzleBuilderModel;
import jetoze.tzudoku.ui.PuzzleBuilderUi;
import jetoze.tzudoku.ui.UiLook;

public class PuzzleBuilderApp {
    
    public static void main(String[] args) throws IOException {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        PuzzleInventory inventory = new PuzzleInventory(new File("/Users/torgil/coding/data/tzudoku"));
        PuzzleBuilderApp app = new PuzzleBuilderApp(inventory);
        UiThread.run(app::start);
    }
    
    private final PuzzleInventory inventory;
    
    public PuzzleBuilderApp(PuzzleInventory inventory) {
        this.inventory = requireNonNull(inventory);
    }
    
    public void start() {
        UiLook.installNimbus();
        JFrame appFrame = new JFrame("tzudoku puzzle builder");
        appFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        PuzzleBuilderModel model = new PuzzleBuilderModel(inventory);
        PuzzleBuilderUi ui = new PuzzleBuilderUi(model);
        PuzzleBuilderController controller = new PuzzleBuilderController(appFrame, model, ui);
        
        appFrame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(WindowEvent e) {
                UiThread.runLater(ui::requestFocus);
            }

            @Override
            public void windowClosing(WindowEvent e) {
                if (controller.isExitAllowed()) {
                    appFrame.dispose();
                }
            }
        });
        appFrame.getContentPane().add(ui.getUi(), BorderLayout.CENTER);
        appFrame.pack();
        appFrame.setLocationRelativeTo(null);

        appFrame.setVisible(true);
    }

}
