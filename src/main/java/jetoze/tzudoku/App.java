package jetoze.tzudoku;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.GridState;
import jetoze.tzudoku.ui.GridUi;
import jetoze.tzudoku.ui.GridUiModel;

public class App {

    public static void main(String[] args) throws IOException {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        Grid grid = loadGrid();
        EventQueue.invokeLater(() -> {
            installNimbus();

            JFrame frame = new JFrame("tzudoku");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            GridUiModel model = new GridUiModel(grid);
            GridUi gridUi = new GridUi(model);
            JPanel gridWrapper = new JPanel(new FlowLayout());
            gridWrapper.add(gridUi.board);
            JPanel controlPanelWrapper = new JPanel(new FlowLayout());
            controlPanelWrapper.add(gridUi.controlPanel.getUi());
            frame.getContentPane().add(gridWrapper, BorderLayout.WEST);
            frame.getContentPane().add(controlPanelWrapper, BorderLayout.EAST);

            gridUi.registerActions(frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW),
                    frame.getRootPane().getActionMap());

            frame.pack();
            frame.setVisible(true);
            frame.requestFocusInWindow();
        });
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
        File file = new File("/Users/torgil/tmp/tzudoku_input.json");
        String json = Files.readString(file.toPath());
        GridState state = GridState.fromJson(json);
        return state.restoreGrid();
    }

}
