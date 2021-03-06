package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.Component;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;

import jetoze.gunga.binding.Binding;
import jetoze.gunga.binding.BooleanBinding;
import jetoze.gunga.binding.ListBinding;
import jetoze.gunga.layout.Layouts;
import jetoze.gunga.selection.SelectionAction;
import jetoze.gunga.widget.CheckBoxWidget;
import jetoze.gunga.widget.ListWidget;
import jetoze.gunga.widget.ListWidget.SelectionMode;
import jetoze.gunga.widget.Widget;
import jetoze.tzudoku.model.PuzzleInfo;

public final class InventoryUi implements Widget {
    // TODO: Add name search field
    // TODO: Add status panel, that displays name and lastUpdated date of the selected puzzle.
    // TODO: Is there any change the model will outlive this UI? IOW, is it necessary to
    //       dispose the bindings we install? I don't think so, but I'm not sure.
    // TODO: Should we really be using a table here, with Status and Last Updated columns?
    //       That would allow for a bit more natural sorting.
    
    private final InventoryUiModel model;
    private final ListWidget<PuzzleInfo> list;
    private final CheckBoxWidget showCompletedPuzzlesCheckBox = new CheckBoxWidget("Show completed puzzles");
    
    public InventoryUi(InventoryUiModel model) {
        this.model = requireNonNull(model);
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
        ListBinding.bind(model.getListItems(), list);
        BooleanBinding.bind(model.getShowCompletedPuzzles(), showCompletedPuzzlesCheckBox);
        Binding.oneWayBinding(model.getListFilter(), list::setFilter).syncUi();
        list.addSelectionListener(selection -> {
            if (selection.isEmpty()) {
                model.setSelectedPuzzle(null);
            } else {
                model.setSelectedPuzzle(selection.getItems().get(0));
            }
        });
    }
    
    public Optional<PuzzleInfo> getSelectedPuzzle() {
        // TODO: Remove this method? Clients should be asking the model directly.
        return model.getSelectedPuzzle();
    }

    public void setPuzzleLoader(Consumer<PuzzleInfo> loader) {
        SelectionAction<PuzzleInfo> action = SelectionAction.forSingleItem("Load", loader);
        action.setSelectionSource(list);
        list.setDefaultAction(action);
    }
    
    public boolean isEmpty() {
        return model.isEmpty();
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
        showCompletedPuzzlesCheckBox.setEnabled(enabled);
    }
}
