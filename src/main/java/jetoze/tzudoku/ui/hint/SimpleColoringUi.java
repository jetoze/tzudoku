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
        // TODO: In some cases (Too Crowded House) we can assign the Value in some cells.
        model.removeCandidatesFromCells(hint.getTargets(), ImmutableSet.of(hint.getValue()));
    }

    @Override
    public String getShortDescription() {
        return hint.getTechnique().getName() + ": " + hint.getValue();
    }

    @Override
    public String getHtmlInfo() {
        // TODO: I need much more information.
        return "<html>Simple Coloring eliminates the value " + hint.getValue() + 
                " from these cells:<br><br>" + positions(hint.getTargets()) +
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
            ImmutableSet<Position> targetCells = hint.getTargets();
            List<HighlightedCells> highlights = new ArrayList<>();
            highlights.add(new HighlightedCells(targetCells, HintHighlightColors.TARGET_CELL));
            if (!blueCells.equals(targetCells)) {
                highlights.add(new HighlightedCells(blueCells, HintHighlightColors.SIMPLE_COLORING_BLUE));
            }
            if (!orangeCells.equals(targetCells)) {
                highlights.add(new HighlightedCells(orangeCells, HintHighlightColors.SIMPLE_COLORING_ORANGE));
            }
            return highlights;
        }
    }
    
    
}
