package jetoze.tzudoku.ui;

/**
 * Listens to changes made to a GridUiModel.
 */
public interface GridUiModelListener {
    
    // TODO: Revisit me. My only purpose at the moment is to trigger repaints of
    // the grid. Is there a better way?

    /**
     * Notifies this listener that a new puzzle was loaded into the model.
     */
    default void onNewPuzzleLoaded() {/**/}
    
    /**
     * Notifies this listener that the state of one or more cells changed. This could mean
     * changes to the cell's value, pencil marks, or color.
     */
    default void onCellStateChanged() {/**/}

}
