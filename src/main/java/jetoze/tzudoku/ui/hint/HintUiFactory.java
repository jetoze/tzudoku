package jetoze.tzudoku.ui.hint;

import static java.util.Objects.requireNonNull;

import jetoze.tzudoku.hint.BoxLineReduction;
import jetoze.tzudoku.hint.HiddenMultiple;
import jetoze.tzudoku.hint.Hint;
import jetoze.tzudoku.hint.NakedMultiple;
import jetoze.tzudoku.hint.PointingPair;
import jetoze.tzudoku.hint.SimpleColoring;
import jetoze.tzudoku.hint.Single;
import jetoze.tzudoku.hint.Swordfish;
import jetoze.tzudoku.hint.XWing;
import jetoze.tzudoku.hint.XyzWing;
import jetoze.tzudoku.hint.YWing;

/**
 * Factory that creates a HintUi for a given Hint. 
 */
public class HintUiFactory {

    // One vague idea I have at the moment is to have at least two different HintUiFactory
    // implementations, to cover these two situations:
    //
    // 1. Every cell in the grid has either a value or candidates penciled in.
    // 2. Some cells exist in the grid that have neither a value nor candidates penciled in.
    //
    // The SolvingTechniques all assume the first scenario, but there is nothing stopping
    // the user from running a hint check in the second scenario. Any hint returned from 
    // such a hint check must be treated as very suspicious, since it could be flat out wrong.
    // What I'm thinking is that in the second case we will create a copy of the Grid, 
    // fill in all remaining candidates in the copy, and send the copy to the hint check.
    // The hint that comes back cannot be applied to the grid the user is working on, so 
    // the apply() method of the corresponding HintUi should do nothing. (In fact, the UI layer
    // should detect this situation and not offer the option to Apply the hint in the first place.)
    // We could also craft a more vague HTML info for the hint, that we display to the user, that
    // doesn't include all the details.
    
    // TODO: One overload for each type of hint?
    
    public HintUi getUi(Hint hint) {
        requireNonNull(hint);
        // TODO: Use a switch on the SolvingTechnique enum instead? We still need 
        // casts, but at least we eliminate all the instanceofs.
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
        } else if (hint instanceof SimpleColoring) {
            return new SimpleColoringUi((SimpleColoring) hint);
        } else if (hint instanceof Swordfish) {
            return new SwordfishUi((Swordfish) hint);
        }
        throw new UnsupportedOperationException("Not supported in the UI: " + hint.getTechnique());
    }
    
}
