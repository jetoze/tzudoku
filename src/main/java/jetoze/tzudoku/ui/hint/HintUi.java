package jetoze.tzudoku.ui.hint;

import jetoze.tzudoku.ui.GridUiModel;

/**
 * Defines the behavior of a discovered hint in the UI.
 */
public interface HintUi { // TODO: What's a good name for this interface?

    /**
     * Applies the hint to the grid backed by the given model.
     */
    // TODO: Should the model be passed as input here, or to the factory method that creates
    // the HintUi? If here, is it really up to each individual HintUi implementation to verify
    // that the Grid in the model is the same Grid as the hint itself is operating on?
    void apply(GridUiModel model);
    
    String getShortDescription();
    
    /**
     * Returns an HTML snippet describing the hint in more detail than {@link #getShortDescription()}.
     */
    String getHtmlInfo();
    
    /**
     * Returns a cell decorator that can provide visual cues of the hint in the puzzle grid
     * backed by the given model.
     */
    HintCellDecorator getCellDecorator(GridUiModel model);
    
}
