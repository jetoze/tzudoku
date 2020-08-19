package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

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
        
        // TODO: This centers the control panel vertically. Is there a better way?
        // At the very least write a utility for this.
        JPanel controlPanelWrapper = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        controlPanelWrapper.add(new JLabel(" "), c);
        
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.0;
        controlPanelWrapper.add(controlPanel.getUi(), c);
        
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.5;
        controlPanelWrapper.add(new JLabel(" "), c);
        
        controlPanelWrapper.setBorder(new EmptyBorder(0, 0, 0, 10));

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
                    }
                }
            }
        }, AWTEvent.MOUSE_EVENT_MASK);
    }

}
