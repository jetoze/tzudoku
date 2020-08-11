package jetoze.tzudoku.ui.hint;

import static jetoze.tzudoku.ui.hint.HintUiUtils.*;

import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

import com.google.common.collect.ImmutableMap;

import jetoze.tzudoku.hint.YWing;

class YWingUi extends AbstractPivotAndWingsUi<YWing> {

    public YWingUi(YWing hint) {
        super(hint);
    }

    @Override
    protected String createHtml(YWing hint) {
        String template = "<html>A Y-Wing with its pivot cell at ${pivot} and wings at ${wings} eliminates<br>"
                + "the digit ${value} from ${targets}</html>";
        Map<String, Object> args = ImmutableMap.of(
                "pivot", hint.getPivot(),
                "wings", positions(hint.getWings()),
                "value", hint.getValue(),
                "targets", positions(hint.getTargetPositions()));
        return new StringSubstitutor(args).replace(template);
    }
}
