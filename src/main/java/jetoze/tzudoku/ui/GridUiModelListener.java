package jetoze.tzudoku.ui;

public interface GridUiModelListener {

    default void onNewPuzzleLoaded() {/**/}
    
    default void onCellStateChanged() {/**/}

}
