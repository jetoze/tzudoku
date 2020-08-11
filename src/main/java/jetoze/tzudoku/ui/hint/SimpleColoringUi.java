package jetoze.tzudoku.ui.hint;

import static java.util.Objects.requireNonNull;
import static jetoze.tzudoku.ui.hint.HintUiUtils.positions;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.hint.SimpleColoring;
import jetoze.tzudoku.ui.GridUiModel;

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
    public String getHtmlInfo() {
        // TODO: I need much more information.
        return "<html>Simple Coloring eliminates the value " + hint.getValue() + 
                " from these cells:<br><br>" + positions(hint.getTargets()) +
                "</html>";
    }

    @Override
    public HintCellDecorator getCellDecorator(GridUiModel model) {
        // TODO: I need a dedicated cell decorator.
        return new TargetCellsDecorator(model, hint.getTargets());
    }
}
