package jetoze.tzudoku.ui;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

public class UndoRedoState {

    private final Stack<UndoableAction> undoStack = new Stack<>();
    private final Stack<UndoableAction> redoStack = new Stack<>();
    @Nullable
    private List<UndoableAction> compoundChangeList;

    public void add(UndoableAction action) {
        requireNonNull(action);
        if (compoundChangeList == null) {
            undoStack.add(action);
            redoStack.clear();
        } else {
            compoundChangeList.add(action);
        }
    }

    public void undo() {
        checkState(compoundChangeList == null, "Cannot undo while in a compound change");
        transition(undoStack, redoStack, UndoableAction::undo);
    }

    public void redo() {
        checkState(compoundChangeList == null, "Cannot redo while in a compound change");
        transition(redoStack, undoStack, UndoableAction::perform);
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }
    
    public void startCompoundChange() {
        // TODO: Any point in supported nested compound changes?
        if (compoundChangeList == null) {
            compoundChangeList = new ArrayList<>();
        }
    }
    
    public void stopCompoundChange() {
        checkState(compoundChangeList != null, "Not in a compound change");
        if (compoundChangeList.isEmpty()) {
            return;
        }
        CompoundChange change = new CompoundChange(compoundChangeList);
        compoundChangeList = null;
        add(change);
    }
    
    private void transition(Stack<UndoableAction> from, Stack<UndoableAction> to, Consumer<UndoableAction> work) {
        if (from.isEmpty()) {
            return;
        }
        UndoableAction action = from.pop();
        work.accept(action);
        to.push(action);
    }
    
    private static class CompoundChange implements UndoableAction {
        private final ImmutableList<UndoableAction> steps;

        public CompoundChange(List<UndoableAction> steps) {
            this.steps = ImmutableList.copyOf(steps);
        }

        @Override
        public void undo() {
            steps.reverse().stream().forEach(UndoableAction::undo);
        }

        @Override
        public void perform() {
            steps.forEach(UndoableAction::perform);
        }

        @Override
        public boolean isNoOp() {
            return steps.stream().allMatch(UndoableAction::isNoOp);
        }
    }

}
