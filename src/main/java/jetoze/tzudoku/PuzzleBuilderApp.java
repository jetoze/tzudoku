package jetoze.tzudoku;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;

import jetoze.gunga.UiThread;
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
        // TODO: Implement me.
    }

}
