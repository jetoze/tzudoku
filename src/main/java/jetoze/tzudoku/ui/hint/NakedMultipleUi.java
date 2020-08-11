package jetoze.tzudoku.ui.hint;

import static jetoze.tzudoku.ui.hint.HintUiUtils.*;

import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

import com.google.common.collect.ImmutableMap;

import jetoze.tzudoku.hint.NakedMultiple;

class NakedMultipleUi extends AbstractEliminatingHintUi<NakedMultiple> {

    public NakedMultipleUi(NakedMultiple hint) {
        super(hint);
    }

    @Override
    protected String createHtml(NakedMultiple hint) {
        String template = "<html>Found a ${technique}:<br><br>" +
                "The digits ${values} are confined to cells ${positions} in ${house}.<br>" +
                "These values can therefore be eliminated from all other positions in the ${houseType}:<br>" +
                "${targets}</html>";
        Map<String, Object> args = ImmutableMap.<String, Object>builder()
                .put("technique", hint.getTechnique())
                .put("values", valuesInOrder(hint.getValues()))
                .put("positions", positionsInOrder(hint.getForcingPositions(), hint.getHouse()))
                .put("house", hint.getHouse())
                .put("houseType", hint.getHouse().getType())
                .put("targets", positionsInOrder(hint.getTargetPositions(), hint.getHouse()))
                .build();
        String html = new StringSubstitutor(args).replace(template);
        return html;
    }

}
