package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.util.Stack;
import java.util.function.Consumer;

public class UndoRedoState {

    private final Stack<UndoableAction> undoStack = new Stack<>();
    private final Stack<UndoableAction> redoStack = new Stack<>();

    public void add(UndoableAction action) {
        requireNonNull(action);
        undoStack.add(action);
        redoStack.clear();
    }

    public void undo() {
        transition(undoStack, redoStack, UndoableAction::undo);
    }

    public void redo() {
        transition(redoStack, undoStack, UndoableAction::perform);
    }

    private void transition(Stack<UndoableAction> from, Stack<UndoableAction> to, Consumer<UndoableAction> work) {
        if (from.isEmpty()) {
            return;
        }
        UndoableAction action = from.pop();
        work.accept(action);
        to.push(action);
    }

}
