package jetoze.tzudoku.ui.hint;

import java.util.Arrays;
import java.util.Collection;

import jetoze.tzudoku.hint.EliminatingHint;
import jetoze.tzudoku.ui.GridUiModel;
import jetoze.tzudoku.ui.GridUiModel.HighlightedCells;

class EliminatingHintCellDecorator extends CellHighlightDecorator<EliminatingHint> {
    
    public EliminatingHintCellDecorator(GridUiModel model, EliminatingHint hint) {
        super(model, hint);
    }

    @Override
    protected Collection<HighlightedCells> getHighlights(EliminatingHint hint) {
        return Arrays.asList(
                new HighlightedCells(hint.getForcingPositions(), HintHighlightColors.FORCING_CELL),
                new HighlightedCells(hint.getTargetPositions(), HintHighlightColors.TARGET_CELL));
    }
}