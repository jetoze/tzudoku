package jetoze.tzudoku.ui;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.CellColor;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.PencilMarks;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.UnknownCell;
import jetoze.tzudoku.model.Value;

public class GridUiModel {
    private final Grid grid;
    private final ImmutableMap<Position, CellUi> cellUis;
    @Nullable
    private CellUi lastSelectedCell;
    private EnterValueMode enterValueMode = EnterValueMode.NORMAL;
    private final UndoRedoState undoRedoState = new UndoRedoState();
    private final List<GridUiModelListener> listeners = new ArrayList<>();

    public GridUiModel(Grid grid) {
        this.grid = grid;
        this.cellUis = grid.getCells().entrySet().stream()
                .collect(ImmutableMap.toImmutableMap(Entry::getKey, e -> new CellUi(e.getKey(), e.getValue())));
    }

    public Grid getGrid() {
        return grid;
    }

    public ImmutableCollection<CellUi> getCells() {
        return cellUis.values();
    }

    public CellUi getCell(Position pos) {
        return cellUis.get(requireNonNull(pos));
    }

    public Optional<CellUi> getLastSelectedCell() {
        return Optional.ofNullable(lastSelectedCell).filter(CellUi::isSelected);
    }

    public void selectCell(CellUi cell, boolean isMultiSelect) {
        lastSelectedCell = cell;
        boolean changed = !cell.isSelected()
                || (!isMultiSelect && cellUis.values().stream().anyMatch(CellUi::isSelected));
        cell.setSelected(true);
        if (!isMultiSelect) {
            cellUis.values().stream().filter(c -> c != cell).forEach(c -> c.setSelected(false));
        }
        if (changed) {
            notifyListeners(GridUiModelListener::onCellStateChanged);
        }
    }

    public EnterValueMode getEnterValueMode() {
        return enterValueMode;
    }

    public void setEnterValueMode(EnterValueMode enterValueMode) {
        requireNonNull(enterValueMode);
        if (enterValueMode != this.enterValueMode) {
            this.enterValueMode = enterValueMode;
            notifyListeners(lst -> lst.onNewEnterValueModeSelected(enterValueMode));
        }
    }

    public void enterValue(Value value) {
        ImmutableList<UnknownCell> cells = getSelectedUnknownCells().collect(toImmutableList());
        applyValueToCells(cells, value);
        notifyListeners(GridUiModelListener::onCellStateChanged);
    }

    private void applyValueToCells(ImmutableList<UnknownCell> cells, Value value) {
        UndoableAction action = getApplyValueAction(cells, value);
        if (action.isNoOp()) {
            return;
        }
        undoRedoState.add(action);
        action.perform();
    }

    private UndoableAction getApplyValueAction(ImmutableList<UnknownCell> cells, Value value) {
        switch (enterValueMode) {
        case NORMAL:
            return new SetValueAction(value, cells);
        case CORNER_PENCIL_MARK:
            return new TogglePencilMarkAction(value, PencilMarks::toggleCorner, cells);
        case CENTER_PENCIL_MARK:
            return new TogglePencilMarkAction(value, PencilMarks::toggleCenter, cells);
        case COLOR:
            CellColor color = CellColor.fromValue(value);
            return new SetColorAction(color, cells);
        default:
            throw new RuntimeException("Unexpected mode: " + enterValueMode);
        }
    }

    private Stream<UnknownCell> getSelectedUnknownCells() {
        return cellUis.values()
                .stream()
                .filter(CellUi::isSelected)
                .map(CellUi::getCell)
                .filter(Predicate.not(Cell::isGiven))
                .map(UnknownCell.class::cast);
    }

    // TODO: This is not a very good name.
    public void delete() {
        List<UnknownCell> cells = getSelectedUnknownCells()
                .filter(Predicate.not(UnknownCell::isEmpty))
                .collect(toList());
        clearCellsImpl(cells, false);
    }

    public void reset() {
        List<UnknownCell> cells = cellUis.values()
                .stream()
                .map(CellUi::getCell)
                .filter(Predicate.not(Cell::isGiven))
                .map(UnknownCell.class::cast)
                .collect(toList());
        clearCellsImpl(cells, true);
    }

    private void clearCellsImpl(List<UnknownCell> cells, boolean reset) {
        if (!cells.isEmpty()) {
            ClearCellsAction action = new ClearCellsAction(cells, reset);
            undoRedoState.add(action);
            action.perform();
        }
    }
    
    public void clearSelection() {
        boolean hasSelectedCells = cellUis.values().stream()
                .anyMatch(CellUi::isSelected);
        if (!hasSelectedCells) {
            return;
        }
        cellUis.values().forEach(c -> c.setSelected(false));
        notifyListeners(GridUiModelListener::onCellStateChanged);
    }

    public void undo() {
        undoRedoState.undo();
    }

    public void redo() {
        undoRedoState.redo();
    }

    public void addListener(GridUiModelListener listener) {
        listeners.add(requireNonNull(listener));
    }

    public void removeListener(GridUiModelListener listener) {
        listeners.remove(requireNonNull(listener));
    }

    private void notifyListeners(Consumer<GridUiModelListener> notification) {
        listeners.forEach(notification);
    }

    private class SetValueAction implements UndoableAction {
        private final Value value;
        private final ImmutableMap<UnknownCell, Optional<Value>> cellsAndTheirOldValues;

        public SetValueAction(Value value, List<UnknownCell> cells) {
            this.value = requireNonNull(value);
            this.cellsAndTheirOldValues = cells.stream()
                    .collect(toImmutableMap(Function.identity(), UnknownCell::getValue));
        }

        @Override
        public boolean isNoOp() {
            return cellsAndTheirOldValues.values().stream()
                    .allMatch(o -> o.isPresent() && o.get() == value);
        }

        @Override
        public void perform() {
            cellsAndTheirOldValues.keySet().forEach(c -> c.setValue(value));
            notifyListeners(GridUiModelListener::onCellStateChanged);
        }

        @Override
        public void undo() {
            for (Map.Entry<UnknownCell, Optional<Value>> e : cellsAndTheirOldValues.entrySet()) {
                UnknownCell cell = e.getKey();
                Optional<Value> value = e.getValue();
                value.ifPresentOrElse(cell::setValue, cell::clearContent);
            }
            notifyListeners(GridUiModelListener::onCellStateChanged);
        }
    }

    private class TogglePencilMarkAction implements UndoableAction {
        private final Value value;
        private final BiConsumer<PencilMarks, Value> pencil;
        private final ImmutableList<UnknownCell> cells;

        public TogglePencilMarkAction(Value value, BiConsumer<PencilMarks, Value> pencil,
                ImmutableList<UnknownCell> cells) {
            this.value = requireNonNull(value);
            this.pencil = requireNonNull(pencil);
            this.cells = requireNonNull(cells);
        }

        @Override
        public boolean isNoOp() {
            // Since we are toggling the values, this action will never be a no-op.
            return false;
        }

        @Override
        public void perform() {
            toggle();
        }

        private void toggle() {
            cells.forEach(c -> pencil.accept(c.getPencilMarks(), value));
            notifyListeners(GridUiModelListener::onCellStateChanged);
        }

        @Override
        public void undo() {
            toggle();
        }
    }
    
    
    private class SetColorAction implements UndoableAction {
        private final CellColor color;
        private final ImmutableMap<UnknownCell, CellColor> cellsAndTheirOldColors;

        public SetColorAction(CellColor color, List<UnknownCell> cells) {
            this.color = requireNonNull(color);
            this.cellsAndTheirOldColors = cells.stream()
                    .collect(toImmutableMap(Function.identity(), UnknownCell::getColor));
        }

        @Override
        public boolean isNoOp() {
            return cellsAndTheirOldColors.values().stream()
                    .allMatch(c -> c == color);
        }

        @Override
        public void perform() {
            cellsAndTheirOldColors.keySet().forEach(c -> c.setColor(color));
            notifyListeners(GridUiModelListener::onCellStateChanged);
        }

        @Override
        public void undo() {
            cellsAndTheirOldColors.forEach((cell, oldColor) -> cell.setColor(oldColor));
            notifyListeners(GridUiModelListener::onCellStateChanged);
        }
    }
    

    private class ClearCellsAction implements UndoableAction {
        private final ImmutableMap<UnknownCell, PreviousCellState> cellsAndTheirPreviousState;
        private final boolean reset;

        public ClearCellsAction(List<UnknownCell> cells, boolean reset) {
            this.cellsAndTheirPreviousState = cells.stream()
                    .collect(toImmutableMap(Function.identity(), PreviousCellState::new));
            this.reset = reset;
        }
        
        @Override
        public boolean isNoOp() {
            return cellsAndTheirPreviousState.keySet().stream()
                    .allMatch(UnknownCell::isEmpty);
        }

        @Override
        public void perform() {
            cellsAndTheirPreviousState.keySet().forEach(c -> {
                if (reset) {
                    c.reset();
                } else {
                    c.clearContent();
                }
            });
            notifyListeners(GridUiModelListener::onCellStateChanged);
        }

        @Override
        public void undo() {
            cellsAndTheirPreviousState.forEach((c, s) -> s.restore(c));
            notifyListeners(GridUiModelListener::onCellStateChanged);
        }
    }

    private static class PreviousCellState {
        private final Optional<Value> value;
        private final ImmutableSet<Value> cornerMarks;
        private final ImmutableSet<Value> centerMarks;
        private final CellColor color;
        
        public PreviousCellState(UnknownCell cell) {
            this.value = cell.getValue();
            this.cornerMarks = ImmutableSet.copyOf(cell.getPencilMarks().iterateOverCornerMarks());
            this.centerMarks = ImmutableSet.copyOf(cell.getPencilMarks().iterateOverCenterMarks());
            this.color = cell.getColor();
        }
        
        public void restore(UnknownCell cell) {
            value.ifPresent(cell::setValue);
            PencilMarks pencilMarks = cell.getPencilMarks();
            pencilMarks.clear();
            cornerMarks.forEach(pencilMarks::toggleCorner);
            centerMarks.forEach(pencilMarks::toggleCenter);
            cell.setColor(color);
        }
    }

}
