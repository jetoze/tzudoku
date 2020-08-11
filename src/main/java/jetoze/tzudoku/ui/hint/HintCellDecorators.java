package jetoze.tzudoku.ui.hint;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.hint.EliminatingHint;
import jetoze.tzudoku.hint.HiddenMultiple;
import jetoze.tzudoku.hint.HingeAndWingsHint;
import jetoze.tzudoku.hint.Hint;
import jetoze.tzudoku.hint.SimpleColoring;
import jetoze.tzudoku.hint.Single;
import jetoze.tzudoku.hint.YWing;
import jetoze.tzudoku.hint.XyzWing;
import jetoze.tzudoku.model.Position;
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
            return new HingeAndWingsCellDecorator<>(model, ((YWing) hint));
        } else if (hint instanceof XyzWing) {
            return new HingeAndWingsCellDecorator<>(model, ((XyzWing) hint));
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
    
    
    private static abstract class CellHighlightDecorator<T extends Hint> implements HintCellDecorator {

        private final GridUiModel model;
        private final T hint;
        
        public CellHighlightDecorator(GridUiModel model, T hint) {
            this.model = model;
            this.hint = hint;
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
    
    
    private static class SingleHintCellDecorator extends CellHighlightDecorator<Single> {
        
        public SingleHintCellDecorator(GridUiModel model, Single single) {
            super(model, single);
        }

        @Override
        protected Collection<HighlightedCells> getHighlights(Single hint) {
            return Collections.singleton(new HighlightedCells(hint.getPosition(), HintHighlightColors.SINGLE_CELL));
        }
    }
    
    
    private static class EliminatingHintCellDecorator extends CellHighlightDecorator<EliminatingHint> {
        
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
    
    
    private static class HingeAndWingsCellDecorator<T extends EliminatingHint & HingeAndWingsHint> extends CellHighlightDecorator<T> {

        public HingeAndWingsCellDecorator(GridUiModel model, T hint) {
            super(model, hint);
        }

        @Override
        protected Collection<HighlightedCells> getHighlights(T hint) {
            return Arrays.asList(
                    new HighlightedCells(hint.getHinge(), HintHighlightColors.CENTER_CELL),
                    new HighlightedCells(hint.getWings(), HintHighlightColors.WING_CELL),
                    new HighlightedCells(hint.getTargetPositions(), HintHighlightColors.TARGET_CELL));
        }
    }


    // TODO: This class should hopefully only be temporary, until we've implemented proper
    // decorators for all hints.
    private static class TargetCellsDecorator implements HintCellDecorator {
        private final GridUiModel model;
        private final ImmutableSet<Position> targetPositions;
        
        public TargetCellsDecorator(GridUiModel model, ImmutableSet<Position> targetPositions) {
            this.model = model;
            this.targetPositions = targetPositions;
        }

        @Override
        public void decorate() {
            model.highlightCells(new HighlightedCells(targetPositions, HintHighlightColors.FORCING_CELL));
        }

        @Override
        public void clear() {
            model.clearHighlightColors();
        }
    }
    
}
