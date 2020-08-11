package jetoze.tzudoku.ui.hint;

import jetoze.tzudoku.hint.EliminatingHint;
import jetoze.tzudoku.hint.PivotAndWingsHint;
import jetoze.tzudoku.ui.GridUiModel;

abstract class AbstractPivotAndWingsUi<T extends EliminatingHint & PivotAndWingsHint> extends AbstractEliminatingHintUi<T> {
    
    protected AbstractPivotAndWingsUi(T hint) {
        super(hint);
    }

    @Override
    protected HintCellDecorator createCellDecorator(GridUiModel model, T hint) {
        return new PivotAndWingsCellDecorator<>(model, hint);
    }

}
