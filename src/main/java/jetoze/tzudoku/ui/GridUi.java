package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.function.BiFunction;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import jetoze.gunga.KeyBindings;
import jetoze.gunga.KeyStrokes;
import jetoze.gunga.widget.Widget;
import jetoze.tzudoku.model.Position;

public class GridUi implements Widget {
    private final GridUiModel model;
    private final MouseHandler mouseHandler = new MouseHandler();
    private final Board board = new Board();

    public GridUi(GridUiModel model) {
        this.model = requireNonNull(model);
        model.getCells().forEach(c -> {
            c.addMouseListener(mouseHandler);
            c.addMouseMotionListener(mouseHandler);
            c.setBounds(model.getSize().getCellBounds(c.getPosition()));
            board.add(c);
        });
        board.setBounds(0, 0, model.getSize().getBoardSize(), model.getSize().getBoardSize());
        board.setBorder(UiLook.BOARD_BORDER);
        model.addListener(new GridUiModelListener() {

            @Override
            public void onCellStateChanged() {
                board.repaint();
            }

            @Override
            public void onNewPuzzleLoaded() {
                board.repaint();
            }

            @Override
            public void onSelectionChanged() {
                board.repaint();
            }
        });
    }
    
    @Override
    public JComponent getUi() {
        return board;
    }
    
    @Override
    public void requestFocus() {
        model.getCell(new Position(1, 1)).requestFocusInWindow();
    }

    public void clearSelection() {
        model.clearSelection();
    }

    public void registerDefaultActions(KeyBindings keyBindings) {
        keyBindings.add(KeyStrokes.forKey(KeyEvent.VK_BACK_SPACE), "clear-cells-via-backspace", model::delete);
        registerSelectionActions(keyBindings, KeyEvent.VK_LEFT, NavigationMode::left);
        registerSelectionActions(keyBindings, KeyEvent.VK_RIGHT, NavigationMode::right);
        registerSelectionActions(keyBindings, KeyEvent.VK_UP, NavigationMode::up);
        registerSelectionActions(keyBindings, KeyEvent.VK_DOWN, NavigationMode::down);
        keyBindings.add(KeyStrokes.commandDown(KeyEvent.VK_Z), "undo", model::undo);
        keyBindings.add(KeyStrokes.commandShiftDown(KeyEvent.VK_Z), "redo", model::redo);
    }

    private void registerSelectionActions(KeyBindings keyBindings, int keyCode, BiFunction<NavigationMode, Position, Position> nextPosition) {
        keyBindings.add(KeyStrokes.forKey(keyCode), keyCode + "-single-select",
                () -> selectNext(nextPosition, false));
        keyBindings.add(KeyStrokes.commandDown(keyCode), keyCode + "-multi-select",
                () -> selectNext(nextPosition, true));
    }

    private void selectNext(BiFunction<NavigationMode, Position, Position> nextPosition, boolean isMultiSelect) {
        model.getLastSelectedCell()
            .map(CellUi::getPosition)
            .map(p -> nextPosition.apply(model.getNavigationMode(), p))
            .map(model::getCell)
            .ifPresentOrElse(c -> model.selectCell(c, isMultiSelect), this::selectTopLeftCell);
    }

    private void selectTopLeftCell() {
        CellUi cellUi = model.getCell(new Position(1, 1));
        model.selectCell(cellUi, false);
    }

    private class MouseHandler extends MouseInputAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            Object source = e.getSource();
            if (!(source instanceof CellUi)) {
                return;
            }
            boolean isMultiSelect = (e.getModifiersEx() & Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()) != 0;
            CellUi cell = (CellUi) source;
            model.selectCell(cell, isMultiSelect);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            boolean buttonDown = (e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0;
            if (!buttonDown) {
                return;
            }
            Object source = e.getSource();
            if (source instanceof CellUi) {
                CellUi cellUi = (CellUi) source;
                model.selectCell(cellUi, true);
            }
        }
    }

    private class Board extends JPanel {

        public Board() {
            super(null);
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            g.fillRect(0, 0, getWidth(), getWidth());
            super.paintComponent(g);
            UiLook.drawGrid((Graphics2D) g, model.getSize());
            UiLook.drawSandwiches((Graphics2D) g, model.getSandwiches(), model.getSize());
        }

        @Override
        protected void paintChildren(Graphics g) {
            // When we paint the individual Cells we fill their backgrounds -->
            // we must paint the Cells before we draw the killer cage boundaries.
            // TODO: The same thing applies once we have Thermos - they must be 
            // painted here as well.
            super.paintChildren(g);
            UiLook.drawKillerCages((Graphics2D) g, model.getKillerCages(), model.getSize());
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(model.getSize().getBoardSize(), model.getSize().getBoardSize());
        }
    }

}
