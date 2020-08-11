package jetoze.tzudoku.ui.hint;

import java.util.Arrays;
import java.util.Collection;

import jetoze.tzudoku.hint.EliminatingHint;
import jetoze.tzudoku.hint.PivotAndWingsHint;
import jetoze.tzudoku.ui.GridUiModel;
import jetoze.tzudoku.ui.GridUiModel.HighlightedCells;

class PivotAndWingsCellDecorator<T extends EliminatingHint & PivotAndWingsHint> extends CellHighlightDecorator<T> {

    public PivotAndWingsCellDecorator(GridUiModel model, T hint) {
        super(model, hint);
    }

    @Override
    protected Collection<HighlightedCells> getHighlights(T hint) {
        return Arrays.asList(
                new HighlightedCells(hint.getPivot(), HintHighlightColors.CENTER_CELL),
                new HighlightedCells(hint.getWings(), HintHighlightColors.WING_CELL),
                new HighlightedCells(hint.getTargetPositions(), HintHighlightColors.TARGET_CELL));
    }
}