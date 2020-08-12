package jetoze.tzudoku.ui.hint;

import static jetoze.tzudoku.ui.hint.HintUiUtils.*;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

import com.google.common.collect.ImmutableMap;

import jetoze.tzudoku.hint.XyzWing;
import jetoze.tzudoku.model.Position;

class XyzWingUi extends AbstractPivotAndWingsUi<XyzWing> {

    public XyzWingUi(XyzWing hint) {
        super(hint);
    }

    @Override
    protected String createHtml(XyzWing hint) {
        String template = "<html>An XYZ-Wing centered at ${center} and with wings at ${wing1} and ${wing2}<br>" +
                "eliminates the value ${value} from ${targets}.</html>";
        Iterator<Position> itWings = hint.getWings().iterator();
        Map<String, Object> args = ImmutableMap.of(
                "center", hint.getPivot(),
                "wing1", itWings.next(),
                "wing2", itWings.next(),
                "value", hint.getValue(),
                "targets", positions(hint.getTargetPositions()));
        return new StringSubstitutor(args).replace(template);
    }
}
