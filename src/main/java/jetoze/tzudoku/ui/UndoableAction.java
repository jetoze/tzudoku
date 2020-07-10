package jetoze.tzudoku.ui;

public interface UndoableAction {

    void undo();

    void perform();
    
    boolean isNoOp();

}
