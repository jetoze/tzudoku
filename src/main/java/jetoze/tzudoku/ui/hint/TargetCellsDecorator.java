package jetoze.tzudoku.ui.hint;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.ui.GridUiModel;
import jetoze.tzudoku.ui.GridUiModel.HighlightedCells;

// TODO: This class should hopefully only be temporary, until we've implemented proper
// decorators for all hints.
class TargetCellsDecorator implements HintCellDecorator {
    private final GridUiModel model;
    private final ImmutableSet<Position> targetPositions;
    
    public TargetCellsDecorator(GridUiModel model, Set<Position> targetPositions) {
        this.model = requireNonNull(model);
        this.targetPositions = ImmutableSet.copyOf(targetPositions);
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