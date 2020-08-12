package jetoze.tzudoku.ui.hint;

import static java.util.Objects.requireNonNull;
import static jetoze.tzudoku.ui.hint.HintUiUtils.valuesInOrder;

import java.util.Collections;

import jetoze.tzudoku.hint.HiddenMultiple;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.ui.GridUiModel;

class HiddenMultipleUi implements HintUi {

    private final HiddenMultiple hint;
    
    public HiddenMultipleUi(HiddenMultiple hint) {
        this.hint = requireNonNull(hint);
    }

    @Override
    public void apply(GridUiModel model) {
        // XXX: This messes up Undo-Redo, since we can't apply this change
        // as an atomic operation at the moment.
        for (Position target : hint.getTargets()) {
            model.removeCandidatesFromCells(Collections.singleton(target), hint.getValuesToEliminate(target));
        }
    }

    @Override
    public String getShortDescription() {
        return hint.getTechnique().getName() + ": " + valuesInOrder(hint.getHiddenValues());
    }

    @Override
    public String getHtmlInfo() {
        StringBuilder s = new StringBuilder("<html>Found a ")
                .append(hint.getTechnique().getName())
                .append(" of ")
                .append(valuesInOrder(hint.getHiddenValues()))
                .append("<br><br>Values that can be eliminated:<br>");
        for (Position t : hint.getTargets()) {
            s.append(t)
                .append(": ")
                .append(valuesInOrder(hint.getValuesToEliminate(t)))
                .append("<br>");
        }
        return s.toString();
    }

    @Override
    public HintCellDecorator getCellDecorator(GridUiModel model) {
        return new TargetCellsDecorator(model, hint.getTargets());
    }

}
