package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import jetoze.tzudoku.model.Value;

public class ControlPanel {
	private final GridUiModel model;
	
	private final JToggleButton normalModeButton = new JToggleButton(
			new SetEnterValueModeAction(EnterValueMode.NORMAL, "Normal"));
	
	private final JToggleButton cornerPencilMarkModeButton = new JToggleButton(
			new SetEnterValueModeAction(EnterValueMode.CORNER_PENCIL_MARK, "Corner"));
	
	private final JToggleButton centerPencilMarkModeButton = new JToggleButton(
			new SetEnterValueModeAction(EnterValueMode.CENTER_PENCIL_MARK, "Center"));
	
	public ControlPanel(GridUiModel model) {
		this.model = requireNonNull(model);
		selectModeButton(model.getEnterValueMode());
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(normalModeButton);
		buttonGroup.add(cornerPencilMarkModeButton);
		buttonGroup.add(centerPencilMarkModeButton);
		UiConstants.makeOverLarge(normalModeButton);
		UiConstants.makeOverLarge(cornerPencilMarkModeButton);
		UiConstants.makeOverLarge(centerPencilMarkModeButton);
		model.addListener(new GridUiModelListener() {

			@Override
			public void onNewEnterValueModeSelected(EnterValueMode newMode) {
				selectModeButton(newMode);
			}
		});
	}
	
	private void selectModeButton(EnterValueMode mode) {
		switch (mode) {
			case NORMAL:
				normalModeButton.setSelected(true);
				break;
			case CORNER_PENCIL_MARK:
				cornerPencilMarkModeButton.setSelected(true);
				break;
			case CENTER_PENCIL_MARK:
				centerPencilMarkModeButton.setSelected(true);
				break;
			default:
				throw new RuntimeException();
		}
	}
	
	public JPanel getUi() {
		JPanel top = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		top.add(normalModeButton, c);
		c.gridy = 1;
		top.add(cornerPencilMarkModeButton, c);
		c.gridy = 2;
		top.add(centerPencilMarkModeButton, c);
		
		c.gridy = 3;
		top.add(new JLabel(" "/*empty space for now*/), c);
		
		c.gridx = 2;
		c.gridy = 3;
		c.gridwidth = 3;
		top.add(largeButton("Delete", model::clearSelectedCells), c);
		
		List<JButton> valueButtons = Value.ALL.stream()
				.map(EnterValueAction::new)
				.map(ControlPanel::smallButton)
				.collect(Collectors.toList());
		c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 1;
		for (JButton b : valueButtons) {
			top.add(b, c);
			++c.gridx;
			if (c.gridx == 5) {
				c.gridx = 2;
				c.gridy++;
			}
		}
		
		JPanel bottom = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.5;
		
		c.gridx = 0;
		c.gridy = 0;
		bottom.add(largeButton("Undo", model::undo), c);
		c.gridx = 1;
		c.gridy = 0;
		bottom.add(largeButton("Redo", model::redo), c);
		c.gridx = 0;
		c.gridy = 1;
		bottom.add(largeButton("Restart", model::reset), c);
		c.gridx = 1;
		c.gridy = 1;
		bottom.add(largeButton("Check", this::checkSolution), c);

		JPanel ui = new JPanel(new BorderLayout(0, 10));
		ui.add(top, BorderLayout.NORTH);
		ui.add(bottom, BorderLayout.SOUTH);
		return ui;
	}
	
	private void checkSolution() {
		boolean solved = model.getGrid().isSolved();
		// TODO: add a UI for this
		System.out.println(solved ? "Solved! :)" : "Not solved :(");
	}
	
	private static JButton largeButton(String text, Runnable work) {
		JButton b = new JButton(createAction(text, work));
		UiConstants.makeOverLarge(b);
		return b;
	}
	
	private static Action createAction(String name, Runnable action) {
		return new AbstractAction(name) {

			@Override
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(action);
			}
		};
	}
	
	private static JButton smallButton(Action action) {
		JButton b = new JButton(action);
		UiConstants.makeOverSmall(b);
		return b;
	}
	
	
	private class SetEnterValueModeAction extends AbstractAction {
		private final EnterValueMode mode;
		
		public SetEnterValueModeAction(EnterValueMode mode, String name) {
			super(name);
			this.mode = mode;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			EventQueue.invokeLater(() -> model.setEnterValueMode(mode));
		}
	}
	
	private class EnterValueAction extends AbstractAction {
		private final Value value;
		
		public EnterValueAction(Value value) {
			super(value.toString());
			this.value = value;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			EventQueue.invokeLater(() -> model.enterValue(value));
		}
	}

}
