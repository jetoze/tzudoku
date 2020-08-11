package jetoze.tzudoku.ui.hint;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;

import jetoze.tzudoku.hint.EliminatingHint;
import jetoze.tzudoku.hint.HiddenMultiple;
import jetoze.tzudoku.hint.Hint;
import jetoze.tzudoku.hint.SimpleColoring;
import jetoze.tzudoku.hint.Single;
import jetoze.tzudoku.hint.YWing;
import jetoze.tzudoku.hint.XyzWing;
import jetoze.tzudoku.ui.GridUiModel;
import jetoze.tzudoku.ui.GridUiModel.HighlightedCells;

public class HintCellDecorators {

    private final GridUiModel model;
    
    public HintCellDecorators(GridUiModel model) {
        this.model = requireNonNull(model);
    }

    public HintCellDecorator getDecorator(Hint hint) {
        requireNonNull(hint);
        if (hint instanceof Single) {
            return new SingleHintCellDecorator(model, (Single) hint);
        } else if (hint instanceof YWing) {
            return new PivotAndWingsCellDecorator<>(model, ((YWing) hint));
        } else if (hint instanceof XyzWing) {
            return new PivotAndWingsCellDecorator<>(model, ((XyzWing) hint));
        } else if (hint instanceof EliminatingHint) {
            return new EliminatingHintCellDecorator(model, (EliminatingHint) hint);
        } else if (hint instanceof HiddenMultiple) {
            // TODO: Implement proper decorator
            return new TargetCellsDecorator(model, ((HiddenMultiple) hint).getTargets());
        } else if (hint instanceof SimpleColoring) {
            // TODO: Implement proper decorator
            return new TargetCellsDecorator(model, ((SimpleColoring) hint).getTargets());
        }
        return HintCellDecorator.NO_DECORATION;
    }
    
    
    private static class SingleHintCellDecorator extends CellHighlightDecorator<Single> {
        
        public SingleHintCellDecorator(GridUiModel model, Single single) {
            super(model, single);
        }

        @Override
        protected Collection<HighlightedCells> getHighlights(Single hint) {
            return Collections.singleton(new HighlightedCells(hint.getPosition(), HintHighlightColors.SINGLE_CELL));
        }
    }
    
}
