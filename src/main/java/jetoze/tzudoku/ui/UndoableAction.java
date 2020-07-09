package jetoze.tzudoku.ui;

public interface UndoableAction {

	public void doAction();
	
	public void undoAction();
	
}
