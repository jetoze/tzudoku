package jetoze.tzudoku.ui;

import java.awt.Component;
import java.util.List;
import java.util.Optional;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;

import jetoze.gunga.selection.Selection;
import jetoze.gunga.widget.ListWidget;
import jetoze.gunga.widget.ListWidget.SelectionMode;
import jetoze.gunga.widget.Widget;
import jetoze.tzudoku.model.PuzzleInfo;

public final class InventoryUi implements Widget {
    // TODO: Add filtering controls, such as hiding/displaying completed puzzles, name search field
    // TODO: Add status panel, that displays name and lastUpdated date of the selected puzzle.
    private final ListWidget<PuzzleInfo> list;
    
    public InventoryUi(List<PuzzleInfo> puzzles) {
        list = new ListWidget<>(puzzles);
        list.setVisibleRowCount(8);
        list.setSelectionMode(SelectionMode.SINGLE);
        list.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(((PuzzleInfo) value).getName());
                return this;
            }
        });
    }
    
    public Optional<PuzzleInfo> getSelectedPuzzle() {
        Selection<PuzzleInfo> selection = list.getSelection();
        return selection.isEmpty()
                ? Optional.empty()
                : Optional.of(selection.getItems().get(0));
    }
    
    @Override
    public JComponent getUi() {
        return list.getUi();
    }

    @Override
    public void requestFocus() {
        list.requestFocus();
    }
}
