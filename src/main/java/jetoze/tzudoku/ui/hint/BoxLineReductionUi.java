package jetoze.tzudoku.ui.hint;

import static jetoze.tzudoku.ui.hint.HintUiUtils.positionsInOrder;

import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

import com.google.common.collect.ImmutableMap;

import jetoze.tzudoku.hint.BoxLineReduction;

class BoxLineReductionUi extends AbstractEliminatingHintUi<BoxLineReduction> {
    
    public BoxLineReductionUi(BoxLineReduction hint) {
        super(hint);
    }

    @Override
    protected String createHtml(BoxLineReduction hint) {
        String template = "<html>Found a Box Line Reduction:<br><br>" +
                "The digit ${value} in ${rowOrColumn} is confined to positions ${positions} in ${box}.<br>" +
                "${value} can therefore be eliminated from ${targets} in ${box}.</html>";
        String forcingPositions = positionsInOrder(hint.getForcingPositions(), hint.getRowOrColumn());
        String targetPositions = positionsInOrder(hint.getTargetPositions(), hint.getBox());
        Map<String, Object> args = ImmutableMap.of(
                "value", hint.getValue(),
                "rowOrColumn", hint.getRowOrColumn(),
                "positions", forcingPositions,
                "targets", targetPositions,
                "box", hint.getBox());
        String html = new StringSubstitutor(args).replace(template);
        return html;
    }
}
