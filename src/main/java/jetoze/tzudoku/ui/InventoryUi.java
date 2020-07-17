package jetoze.tzudoku.ui;

import java.awt.Component;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;

import jetoze.gunga.BooleanBinding;
import jetoze.gunga.binding.ListBinding;
import jetoze.gunga.layout.Layouts;
import jetoze.gunga.selection.Selection;
import jetoze.gunga.widget.CheckBoxWidget;
import jetoze.gunga.widget.ListWidget;
import jetoze.gunga.widget.ListWidget.SelectionMode;
import jetoze.gunga.widget.Widget;
import jetoze.tzudoku.model.PuzzleInfo;

public final class InventoryUi implements Widget {
    // TODO: Add filtering controls, such as hiding/displaying completed puzzles, name search field
    // TODO: Add status panel, that displays name and lastUpdated date of the selected puzzle.
    private final ListWidget<PuzzleInfo> list;
    // FIXME: I think it makes more sense to have this be "Show completed puzzles"
    private final CheckBoxWidget hideCompletedPuzzlesCheckBox = new CheckBoxWidget("Hide completed puzzles");
    
    public InventoryUi(InventoryUiModel model) {
        list = new ListWidget<>();
        list.setVisibleRowCount(20);
        list.setSelectionMode(SelectionMode.SINGLE);
        list.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                PuzzleInfo puzzleInfo = (PuzzleInfo) value;
                setText(puzzleInfo.getName());
                setIcon(UiLook.getPuzzleStateIcon(puzzleInfo.getState()));
                setToolTipText(buildTooltip(puzzleInfo));
                return this;
            }
            
            private String buildTooltip(PuzzleInfo puzzleInfo) {
                DateTimeFormatter fmt = DateTimeFormatter.RFC_1123_DATE_TIME
                        .withZone(ZoneId.systemDefault());
                String lastUpdated = puzzleInfo.lastUpdated()
                        .map(fmt::format)
                        .orElse("");
                return String.format("<html><b>Name:</b> %s<br><b>State:</b> %s<br><b>Last Updated: </b>%s</html>", 
                        puzzleInfo.getName(), puzzleInfo.getState(), lastUpdated);
            }
        });
        ListBinding.bindAndSyncUi(model.getListItems(), list);
        BooleanBinding.bindAndSyncUi(model.getHideCompletedPuzzles(), hideCompletedPuzzlesCheckBox);
        // TODO: Bind other properties.
    }
    
    public Optional<PuzzleInfo> getSelectedPuzzle() {
        Selection<PuzzleInfo> selection = list.getSelection();
        return selection.isEmpty()
                ? Optional.empty()
                : Optional.of(selection.getItems().get(0));
    }
    
    @Override
    public JComponent getUi() {
        return Layouts.border(0, 8)
                .center(list)
                .south(hideCompletedPuzzlesCheckBox)
                .build();
    }

    @Override
    public void requestFocus() {
        list.requestFocus();
    }
    
    public void setEnabled(boolean enabled) {
        list.setEnabled(enabled);
    }
}
