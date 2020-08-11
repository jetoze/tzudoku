package jetoze.tzudoku.ui.hint;

import static java.util.Objects.requireNonNull;

import jetoze.tzudoku.hint.BoxLineReduction;
import jetoze.tzudoku.hint.HiddenMultiple;
import jetoze.tzudoku.hint.Hint;
import jetoze.tzudoku.hint.NakedMultiple;
import jetoze.tzudoku.hint.PointingPair;
import jetoze.tzudoku.hint.Single;
import jetoze.tzudoku.hint.XWing;
import jetoze.tzudoku.hint.XyzWing;
import jetoze.tzudoku.hint.YWing;

/**
 * Factory that creates a HintUi for a given Hint. 
 */
public class HintUiFactory {

    // TODO: One overload for each type of hint?
    
    public HintUi getUi(Hint hint) {
        requireNonNull(hint);
        if (hint instanceof Single) {
            return new SingleUi((Single) hint);
        } else if (hint instanceof PointingPair) {
            return new PointingPairUi((PointingPair) hint);
        } else if (hint instanceof BoxLineReduction) {
            return new BoxLineReductionUi((BoxLineReduction) hint);
        } else if (hint instanceof NakedMultiple) {
            return new NakedMultipleUi((NakedMultiple) hint);
        } else if (hint instanceof HiddenMultiple) {
            return new HiddenMultipleUi((HiddenMultiple) hint);
        } else if (hint instanceof XWing) {
            return new XWingUi((XWing) hint);
        } else if (hint instanceof YWing) {
            return new YWingUi((YWing) hint);
        } else if (hint instanceof XyzWing) {
            return new XyzWingUi((XyzWing) hint);
        }
        throw new UnsupportedOperationException("Not supported in the UI: " + hint.getTechnique());
    }
    
}
