package jetoze.tzudoku.ui.hint;

import static java.util.Objects.*;

import java.util.Collection;

import jetoze.tzudoku.hint.Hint;
import jetoze.tzudoku.ui.GridUiModel;
import jetoze.tzudoku.ui.GridUiModel.HighlightedCells;

abstract class CellHighlightDecorator<T extends Hint> implements HintCellDecorator {

    private final GridUiModel model;
    private final T hint;
    
    public CellHighlightDecorator(GridUiModel model, T hint) {
        this.model = requireNonNull(model);
        this.hint = requireNonNull(hint);
    }

    @Override
    public final void decorate() {
        model.highlightCells(getHighlights(hint));
    }

    @Override
    public final void clear() {
        model.clearHighlightColors();
    }
    
    protected abstract Collection<HighlightedCells> getHighlights(T hint);
}