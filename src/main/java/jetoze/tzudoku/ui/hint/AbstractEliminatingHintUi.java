package jetoze.tzudoku.ui.hint;

import static java.util.Objects.*;
import static jetoze.tzudoku.ui.hint.HintUiUtils.valuesInOrder;

import jetoze.tzudoku.hint.EliminatingHint;
import jetoze.tzudoku.ui.GridUiModel;

abstract class AbstractEliminatingHintUi<T extends EliminatingHint> implements HintUi {

    private final T hint;
    
    protected AbstractEliminatingHintUi(T hint) {
        this.hint = requireNonNull(hint);
    }
    
    @Override
    public final void apply(GridUiModel model) {
        model.removeCandidatesFromCells(hint.getTargetPositions(), hint.getValues());
    }

    @Override
    public final String getShortDescription() {
        return createShortDescription(hint);
    }
    
    protected String createShortDescription(T hint) {
        String valueString = hint.getValues().size() == 1
                ? hint.getValues().iterator().next().toString()
                : valuesInOrder(hint.getValues());
        return hint.getTechnique().getName() + ": " + valueString;
    }

    @Override
    public final String getHtmlInfo() {
        return createHtml(hint);
    }
    
    protected abstract String createHtml(T hint);

    @Override
    public final HintCellDecorator getCellDecorator(GridUiModel model) {
        requireNonNull(model);
        return createCellDecorator(model, hint);
    }
    
    protected HintCellDecorator createCellDecorator(GridUiModel model, T hint) {
        return new EliminatingHintCellDecorator(model, hint);
    }

}
