package jetoze.tzudoku.ui.hint;

import static jetoze.tzudoku.ui.hint.HintUiUtils.*;

import jetoze.tzudoku.hint.Swordfish;
import jetoze.tzudoku.model.House.Type;

class SwordfishUi extends AbstractEliminatingHintUi<Swordfish> {

    public SwordfishUi(Swordfish hint) {
        super(hint);
    }

    @Override
    protected String createHtml(Swordfish hint) {
        return "<html>A Swordfish in " +
                String.format("%s %d, %d, and %d ", (hint.getHouseType() == Type.ROW ? "rows" : "columns"), 
                        hint.getHouses().get(0).getNumber(),
                        hint.getHouses().get(1).getNumber(),
                        hint.getHouses().get(2).getNumber()) +
                "eliminates the value " + hint.getValue() + 
                " from these cells:<br><br>" + positions(hint.getTargetPositions()) +
                "</html>";
    }
}
