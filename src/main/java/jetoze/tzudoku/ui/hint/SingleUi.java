package jetoze.tzudoku.ui.hint;

import static java.util.Objects.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

import com.google.common.collect.ImmutableMap;

import jetoze.tzudoku.hint.Single;
import jetoze.tzudoku.ui.GridUiModel;
import jetoze.tzudoku.ui.GridUiModel.HighlightedCells;

class SingleUi implements HintUi {

    private final Single single;
    
    public SingleUi(Single single) {
        this.single = requireNonNull(single);
    }

    @Override
    public void apply(GridUiModel model) {
        model.enterValue(single.getPosition(), single.getValue());
    }

    @Override
    public String getHtmlInfo() {
        if (single.isNaked()) {
            return "<html>" + single.getPosition() + " is a Naked Single: " + single.getValue() + "</html>";
        } else {
            String template = "<html>${cell} is a Hidden Single. It is the only cell in ${house} where "
                    + "the digit ${value} can be placed.</html>";
            Map<String, Object> args = ImmutableMap.of(
                    "cell", single.getPosition(),
                    "house", single.getHouse(),
                    "value", single.getValue());
            return new StringSubstitutor(args).replace(template);
        }
    }

    @Override
    public HintCellDecorator getCellDecorator(GridUiModel model) {
        return new SingleHintCellDecorator(model, single);
    }

    
    private static class SingleHintCellDecorator extends CellHighlightDecorator<Single> {
        
        public SingleHintCellDecorator(GridUiModel model, Single single) {
            super(model, single);
        }

        @Override
        protected Collection<HighlightedCells> getHighlights(Single hint) {
            return Collections.singleton(new HighlightedCells(hint.getPosition(), HintHighlightColors.SINGLE_CELL));
        }
    }
}
