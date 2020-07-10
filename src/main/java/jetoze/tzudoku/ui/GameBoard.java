package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import jetoze.gunga.layout.Layouts;

public class GameBoard {

    private final GridUi gridUi;
    private final ControlPanel controlPanel;
    private final JPanel board;

    public GameBoard(GridUi gridUi, ControlPanel controlPanel) {
        this.gridUi = requireNonNull(gridUi);
        this.controlPanel = requireNonNull(controlPanel);
        this.board = layoutUi();
        installGlobalMouseListener();
    }

    public JComponent getUi() {
        return board;
    }

    private JPanel layoutUi() {
        JPanel gridWrapper = new JPanel(new FlowLayout());
        gridWrapper.add(gridUi.getUi());
        JPanel controlPanelWrapper = new JPanel(new FlowLayout());
        controlPanelWrapper.add(controlPanel.getUi());

        return Layouts.border()
                .west(gridWrapper)
                .east(controlPanelWrapper)
                .build();
    }

    private void installGlobalMouseListener() {
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {

            @Override
            public void eventDispatched(AWTEvent event) {
                if (event instanceof MouseEvent) {
                    MouseEvent evt = (MouseEvent) event;
                    if (evt.getID() == MouseEvent.MOUSE_CLICKED) {
                        Object source = evt.getSource();
                        if (!(source instanceof Component)) {
                            return;
                        }
                        Component sourceComponent = (Component) source;
                        boolean isInGrid = SwingUtilities.isDescendingFrom(sourceComponent, gridUi.getUi());
                        if (isInGrid) {
                            return;
                        }
                        boolean isInControlPanel = SwingUtilities.isDescendingFrom(sourceComponent, controlPanel.getUi());
                        if (isInControlPanel) {
                            return;
                        }
                        // Click outside of the grid and the control panel
                        // --> Clear the selection in the grid
                        gridUi.clearSelection();
                        
                        // TODO: Implement me.
                    }
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
    }

}
