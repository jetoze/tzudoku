package jetoze.tzudoku.ui.hint;

import static jetoze.tzudoku.ui.hint.HintUiUtils.positionsInOrder;

import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

import com.google.common.collect.ImmutableMap;

import jetoze.tzudoku.hint.PointingPair;

class PointingPairUi extends AbstractEliminatingHintUi<PointingPair> {
    
    public PointingPairUi(PointingPair hint) {
        super(hint);
    }

    @Override
    protected String createHtml(PointingPair hint) {
        String template = "<html>Found a Pointing Pair:<br><br>" +
                "The digit ${value} in ${box} is confined to ${positions} in ${rowOrColumn}.<br>" +
                "${value} can therefore be eliminated from ${targets} in ${rowOrColumn}.</html>";
        String forcingPositions = positionsInOrder(hint.getForcingPositions(), hint.getRowOrColumn());
        String targetPositions = positionsInOrder(hint.getTargetPositions(), hint.getRowOrColumn());
        Map<String, Object> args = ImmutableMap.of(
                "value", hint.getValue(),
                "box", hint.getBox(),
                "positions", forcingPositions,
                "targets", targetPositions,
                "rowOrColumn", hint.getRowOrColumn());
        String html = new StringSubstitutor(args).replace(template);
        return html;
    }

}
