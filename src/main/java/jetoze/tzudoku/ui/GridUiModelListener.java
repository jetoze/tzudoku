package jetoze.tzudoku.ui;

public interface GridUiModelListener {

    default void onCellStateChanged() {/**/}

    default void onNewEnterValueModeSelected(EnterValueMode newMode) {/**/}

}
