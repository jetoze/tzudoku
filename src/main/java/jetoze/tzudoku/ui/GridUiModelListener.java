package jetoze.tzudoku.ui;

public interface GridUiModelListener {

	default void repaintBoard() {/**/}
	
	default void onNewEnterValueModeSelected(EnterValueMode newMode) {/**/}
	
}
