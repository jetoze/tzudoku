package jetoze.tzudoku.ui;

import static java.util.Objects.*;

import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

class ControlPanel {
	private final GridUiModel model;
	
	private final JToggleButton normalModeButton = new JToggleButton(
			new SetEnterValueModeAction(EnterValueMode.VALUE, "Normal"));
	
	private final JToggleButton centerPencilMarkModeButton = new JToggleButton(
			new SetEnterValueModeAction(EnterValueMode.CENTER_PENCIL_MARK, "Center"));
	
	private final JToggleButton cornerPencilMarkModeButton = new JToggleButton(
			new SetEnterValueModeAction(EnterValueMode.CORNER_PENCIL_MARK, "Corner"));
	
	public ControlPanel(GridUiModel model) {
		this.model = requireNonNull(model);
		selectModeButton(model.getEnterValueMode());
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(normalModeButton);
		buttonGroup.add(centerPencilMarkModeButton);
		buttonGroup.add(cornerPencilMarkModeButton);
		model.addListener(new GridUiModelListener() {

			@Override
			public void onNewEnterValueModeSelected(EnterValueMode newMode) {
				selectModeButton(newMode);
			}
		});
	}
	
	private void selectModeButton(EnterValueMode mode) {
		switch (mode) {
			case VALUE:
				normalModeButton.setSelected(true);
				break;
			case CENTER_PENCIL_MARK:
				centerPencilMarkModeButton.setSelected(true);
				break;
			case CORNER_PENCIL_MARK:
				cornerPencilMarkModeButton.setSelected(true);
				break;
			default:
				throw new RuntimeException();
		}
	}
	
	public JPanel getUi() {
		GridLayout layout = new GridLayout(3, 1, 0, 10);
		JPanel p = new JPanel(layout);
		p.add(normalModeButton);
		p.add(centerPencilMarkModeButton);
		p.add(cornerPencilMarkModeButton);
		return p;
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
}
