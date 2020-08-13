package jetoze.tzudoku.ui.hint;

import static java.util.Objects.requireNonNull;
import static jetoze.tzudoku.ui.hint.HintUiUtils.positions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.hint.SimpleColoring;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.ui.GridUiModel;
import jetoze.tzudoku.ui.GridUiModel.HighlightedCells;

class SimpleColoringUi implements HintUi {

    private final SimpleColoring hint;
    
    public SimpleColoringUi(SimpleColoring hint) {
        this.hint = requireNonNull(hint);
    }

    @Override
    public void apply(GridUiModel model) {
        // TODO: This messes up undo/redo since we are not applying these changes as an atomic operation.
        hint.getCellsThatCanBePenciledIn().forEach(p -> model.enterValue(p, hint.getValue()));
        model.removeCandidatesFromCells(hint.getCellsToEliminate(), ImmutableSet.of(hint.getValue()));
    }

    @Override
    public String getShortDescription() {
        return hint.getTechnique().getName() + ": " + hint.getValue();
    }

    @Override
    public String getHtmlInfo() {
        // TODO: I need much more information.
        return "<html>Simple Coloring eliminates the value " + hint.getValue() + 
                " from these cells:<br><br>" + positions(hint.getCellsToEliminate()) +
                "</html>";
    }

    @Override
    public HintCellDecorator getCellDecorator(GridUiModel model) {
        return new SimpleColoringCellDecorator(model, hint);
    }
    
    
    private static class SimpleColoringCellDecorator extends CellHighlightDecorator<SimpleColoring> {

        public SimpleColoringCellDecorator(GridUiModel model, SimpleColoring hint) {
            super(model, hint);
        }

        @Override
        protected Collection<HighlightedCells> getHighlights(SimpleColoring hint) {
            ImmutableSet<Position> blueCells = hint.getBlueCells();
            ImmutableSet<Position> orangeCells = hint.getOrangeCells();
            List<HighlightedCells> highlights = new ArrayList<>();
            highlights.add(new HighlightedCells(blueCells, HintHighlightColors.SIMPLE_COLORING_BLUE));
            highlights.add(new HighlightedCells(orangeCells, HintHighlightColors.SIMPLE_COLORING_ORANGE));
            if (hint.getCellsThatCanBePenciledIn().isEmpty()) {
                ImmutableSet<Position> targetCells = hint.getCellsToEliminate();
                highlights.add(new HighlightedCells(targetCells, HintHighlightColors.TARGET_CELL));
            }
            return highlights;
        }
    }
}
