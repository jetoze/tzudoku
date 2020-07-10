package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.function.UnaryOperator;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.event.MouseInputAdapter;

import jetoze.tzudoku.model.GridState;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class GridUi {
	private final GridUiModel model;
	private final MouseHandler mouseHandler = new MouseHandler();
	public final Board board = new Board();
	public final ControlPanel controlPanel;
	
	public GridUi(GridUiModel model) {
		this.model = requireNonNull(model);
		model.getCells().forEach(c -> {
			c.addMouseListener(mouseHandler);
			c.addMouseMotionListener(mouseHandler);
			c.setBounds(UiConstants.getCellBounds(c.getPosition()));
			board.add(c);
		});
		board.setBounds(0, 0, UiConstants.BOARD_SIZE, UiConstants.BOARD_SIZE);
		board.setBorder(UiConstants.getBoardBorder());
		controlPanel = new ControlPanel(model);
		model.addListener(new GridUiModelListener() {
			
			@Override
			public void onCellStateChanged() {
				board.repaint();
			}
		});
	}
	
	public void registerActions(InputMap inputMap, ActionMap actionMap) {
		for (Value v : Value.values()) {
			registerAction(
					inputMap, 
					actionMap, 
					KeyStroke.getKeyStroke(KeyEvent.VK_0 + v.toInt(), 0), 
					"enter-" + v,
					() -> model.enterValue(v));
		}
		registerAction(
				inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0, false),
				"clear-cells-via-backspace",
				() -> model.clearSelectedCells());
		registerSelectionActions(inputMap, actionMap, KeyEvent.VK_LEFT, Position::left);
		registerSelectionActions(inputMap, actionMap, KeyEvent.VK_RIGHT, Position::right);
		registerSelectionActions(inputMap, actionMap, KeyEvent.VK_UP, Position::up);
		registerSelectionActions(inputMap, actionMap, KeyEvent.VK_DOWN, Position::down);
		registerAction(
				inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_N, 0),
				"normal-value-mode",
				() -> model.setEnterValueMode(EnterValueMode.NORMAL));
		registerAction(
				inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_R, 0),
				"corner-value-mode",
				() -> model.setEnterValueMode(EnterValueMode.CORNER_PENCIL_MARK));
		registerAction(
				inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_C, 0),
				"center-value-mode",
				() -> model.setEnterValueMode(EnterValueMode.CENTER_PENCIL_MARK));
		registerAction(
				inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()),
				"undo",
				model::undo);
		registerAction(
				inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK),
				"redo",
				model::redo);
		registerAction(
				inputMap,
				actionMap,
				KeyStroke.getKeyStroke(KeyEvent.VK_J, 0),
				"dump-json",
				this::dumpJson);
	}
	
	private void registerAction(InputMap inputMap, 
							    ActionMap actionMap,
							    KeyStroke keyStroke,
							    String actionMapKey,
							    Runnable action) {
		inputMap.put(keyStroke, actionMapKey);
		actionMap.put(actionMapKey, new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				action.run();
			}
		});
	}
	
	private void registerSelectionActions(InputMap inputMap,
										  ActionMap actionMap,
										  int keyEvent,
										  UnaryOperator<Position> nextPosition) {
		registerAction(
				inputMap, 
				actionMap, 
				KeyStroke.getKeyStroke(keyEvent, 0, false), 
				keyEvent + "-single-select", 
				() -> selectNext(nextPosition, false));
		registerAction(
				inputMap, 
				actionMap, 
				KeyStroke.getKeyStroke(keyEvent, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(), false), 
				keyEvent + "-multi-select", 
				() -> selectNext(nextPosition, true));
	}
	
	private void dumpJson() {
		GridState state = new GridState(model.getGrid());
		String json = state.toJson();
		System.out.println(json);
	}
	
	private void selectNext(UnaryOperator<Position> nextPosition, boolean isMultiSelect) {
		model.getLastSelectedCell()
			.map(CellUi::getPosition)
			.map(nextPosition)
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
	
	
	private static class Board extends JPanel {
		
		public Board() {
			super(null);
			setBackground(Color.WHITE);
		}

		@Override
		protected void paintComponent(Graphics g) {
			g.fillRect(0, 0, getWidth(), getWidth());
			super.paintComponent(g);
			UiConstants.drawGrid((Graphics2D) g);
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(UiConstants.BOARD_SIZE, UiConstants.BOARD_SIZE);
		}
	}

}
