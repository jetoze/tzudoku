package jetoze.tzudoku.ui.hint;

import static jetoze.tzudoku.ui.hint.HintUiUtils.positions;

import jetoze.tzudoku.hint.XWing;

class XWingUi extends AbstractEliminatingHintUi<XWing> {

    public XWingUi(XWing hint) {
        super(hint);
    }

    @Override
    protected String createHtml(XWing hint) {
        StringBuilder s = new StringBuilder("<html>Found an X-Wing:<br><br>");
        s.append("Positions: ");
        s.append(positions(hint.getForcingPositions()));
        s.append("<br><br>");
        s.append(hint.getValue()).append(" can be eliminated from:<br>");
        s.append(positions(hint.getTargetPositions()));
        s.append("</html>");
        return s.toString();
    }
}
