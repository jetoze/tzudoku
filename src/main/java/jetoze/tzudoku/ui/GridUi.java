package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.function.UnaryOperator;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import jetoze.gunga.KeyBindings;
import jetoze.gunga.KeyStrokes;
import jetoze.gunga.widget.Widget;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

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
        board.setBorder(UiConstants.getBoardBorder(true));
        model.addListener(new GridUiModelListener() {

            @Override
            public void onCellStateChanged() {
                board.repaint();
            }

            @Override
            public void onNewPuzzleLoaded() {
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
    
    public void setEnabled(boolean enabled) {
        board.setEnabled(enabled);
        board.setBorder(UiConstants.getBoardBorder(enabled));
        board.setFocusable(false);
        model.getCells().forEach(c -> {
            c.setEnabled(enabled);
            c.setFocusable(enabled);
        });
    }

    public void clearSelection() {
        model.clearSelection();
    }

    public void registerActions(KeyBindings keyBindings) {
        for (Value v : Value.values()) {
            keyBindings.add(KeyStrokes.forKey(KeyEvent.VK_0 + v.toInt()), "enter-" + v, 
                    () -> model.enterValue(v));
        }
        keyBindings.add(KeyStrokes.forKey(KeyEvent.VK_BACK_SPACE), "clear-cells-via-backspace", model::delete);
        registerSelectionActions(keyBindings, KeyEvent.VK_LEFT, Position::left);
        registerSelectionActions(keyBindings, KeyEvent.VK_RIGHT, Position::right);
        registerSelectionActions(keyBindings, KeyEvent.VK_UP, Position::up);
        registerSelectionActions(keyBindings, KeyEvent.VK_DOWN, Position::down);
        keyBindings.add(KeyStrokes.forKey(KeyEvent.VK_N), "normal-value-mode",
                () -> model.setEnterValueMode(EnterValueMode.NORMAL));
        keyBindings.add(KeyStrokes.forKey(KeyEvent.VK_R), "corner-value-mode",
                () -> model.setEnterValueMode(EnterValueMode.CORNER_PENCIL_MARK));
        keyBindings.add(KeyStrokes.forKey(KeyEvent.VK_C), "center-value-mode",
                () -> model.setEnterValueMode(EnterValueMode.CENTER_PENCIL_MARK));
        keyBindings.add(KeyStrokes.commandDown(KeyEvent.VK_Z), "undo", model::undo);
        keyBindings.add(KeyStrokes.commandShiftDown(KeyEvent.VK_Z), "redo", model::redo);
    }

    private void registerSelectionActions(KeyBindings keyBindings, int keyCode, UnaryOperator<Position> nextPosition) {
        keyBindings.add(KeyStrokes.forKey(keyCode), keyCode + "-single-select",
                () -> selectNext(nextPosition, false));
        keyBindings.add(KeyStrokes.commandDown(keyCode), keyCode + "-multi-select",
                () -> selectNext(nextPosition, true));
    }

    private void selectNext(UnaryOperator<Position> nextPosition, boolean isMultiSelect) {
        model.getLastSelectedCell().map(CellUi::getPosition).map(nextPosition).map(model::getCell)
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
            UiConstants.drawGrid((Graphics2D) g, model.getSize(), isEnabled());
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(model.getSize().getBoardSize(), model.getSize().getBoardSize());
        }
    }

}
