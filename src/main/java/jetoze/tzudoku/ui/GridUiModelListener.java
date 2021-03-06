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
     * Notifies this listener that the value of one or more cells changed.
     * <p>
     * This notification is always accompanied by a {@link #onCellStateChanged()} notification. 
     */
    default void onCellValueChanged() {/**/}
    
    /**
     * Notifies this listener that the state of one or more cells changed. This could mean
     * changes to the cell's value, pencil marks, or color.
     */
    default void onCellStateChanged() {/**/}
    
    // TODO: Lift out selection to a separate model w/ associated listener?
    
    /**
     * Notifies this listener that the cell selection changed (Cells were selected or unselected).
     */
    default void onSelectionChanged() {/**/}
    

}
