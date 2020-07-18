package jetoze.tzudoku.ui;

import java.awt.Component;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;

import jetoze.gunga.binding.Binding;
import jetoze.gunga.binding.BooleanBinding;
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
    // TODO: Is there any change the model will outlive this UI? IOW, is it necessary to
    //       dispose the bindings we install? I don't think so, but I'm not sure.
    // TODO: Should we really be using a table here, with Status and Last Updated columns?
    //       That would allow for a bit more natural sorting.
    
    private final ListWidget<PuzzleInfo> list;
    private final CheckBoxWidget showCompletedPuzzlesCheckBox = new CheckBoxWidget("Show completed puzzles");
    
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
        BooleanBinding.bindAndSyncUi(model.getShowCompletedPuzzles(), showCompletedPuzzlesCheckBox);
        Binding.oneWayBinding(model.getListFilter(), list.getModel()::setFilter).syncUi();;
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
                .south(showCompletedPuzzlesCheckBox)
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
