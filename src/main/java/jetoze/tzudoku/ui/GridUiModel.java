package jetoze.tzudoku.ui;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import jetoze.attribut.Properties;
import jetoze.attribut.Property;
import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.CellColor;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.PencilMarks;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Puzzle;
import jetoze.tzudoku.model.Sandwiches;
import jetoze.tzudoku.model.ValidationResult;
import jetoze.tzudoku.model.Value;

public class GridUiModel {
    private Grid grid;
    private final BoardSize size;
    private ImmutableMap<Position, CellUi> cellUis;
    @Nullable
    private CellUi lastSelectedCell;
    private final Property<Boolean> highlightDuplicateCells = Properties.newProperty(
            "highlightDuplicateCells", Boolean.FALSE);
    private final Property<Boolean> eliminateCandidates = Properties.newProperty(
            "eliminateDuplicates", Boolean.FALSE);
    private EnterValueMode enterValueMode = EnterValueMode.NORMAL;
    // XXX: Does the Sandwiches really belong here?
    private final Property<Sandwiches> sandwiches;
    private final UndoRedoState undoRedoState = new UndoRedoState();
    private final List<GridUiModelListener> listeners = new ArrayList<>();

    public GridUiModel(Puzzle puzzle, BoardSize size) {
        this(puzzle.getGrid(), puzzle.getSandwiches(), size);
    }
    
    public GridUiModel(Grid grid, Sandwiches sandwiches, BoardSize size) {
        this.grid = requireNonNull(grid);
        this.sandwiches = Properties.newProperty("sandwiches", requireNonNull(sandwiches));
        this.size = requireNonNull(size);
        this.cellUis = grid.getCells().entrySet().stream()
                .collect(ImmutableMap.toImmutableMap(Entry::getKey, 
                        e -> new CellUi(e.getKey(), e.getValue(), size)));
        this.highlightDuplicateCells.addListener(e -> onHighlightDuplicateCellsSelectionChanged());
    }
    
    public void setPuzzle(Puzzle puzzle) {
        this.grid = puzzle.getGrid();
        this.sandwiches.set(puzzle.getSandwiches());
        cellUis.keySet().forEach(p -> {
            CellUi cellUi = cellUis.get(p);
            Cell cell = grid.cellAt(p);
            cellUi.setCell(cell);
        });
        notifyListeners(GridUiModelListener::onNewPuzzleLoaded);
    }

    public Grid getGrid() {
        return grid;
    }
    
    public Sandwiches getSandwiches() {
        return sandwiches.get();
    }
    
    public void setSandwiches(Sandwiches sandwiches) {
        this.sandwiches.set(requireNonNull(sandwiches));
        // HACK: This is just to get up and running.
        notifyListeners(GridUiModelListener::onNewPuzzleLoaded);
    }

    public BoardSize getSize() {
        return size;
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
    
    public void selectCellsAt(Collection<Position> positions) {
        List<CellUi> cellsToSelect = positions.stream()
                .map(cellUis::get)
                .filter(Predicate.not(CellUi::isSelected))
                .collect(toList());
        if (cellsToSelect.isEmpty()) {
            return;
        }
        cellUis.values().forEach(c -> c.setSelected(false));
        lastSelectedCell = cellsToSelect.get(cellsToSelect.size() - 1);
        cellsToSelect.forEach(c -> c.setSelected(true));
        notifyListeners(GridUiModelListener::onCellStateChanged);
    }
    
    public void selectCellAt(Position position) {
        selectCell(cellUis.get(position), false);
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
    
    public void setHighlightDuplicateCells(boolean b) {
        if (b == this.highlightDuplicateCells.get()) {
            return;
        }
        this.highlightDuplicateCells.set(b);
        onHighlightDuplicateCellsSelectionChanged();
    }
    
    private void onHighlightDuplicateCellsSelectionChanged() {
        // TODO: What if we are also currently displaying a ValidationResult?
        // See decorateInvalidCells(). --> Perhaps let that mode take precedence, 
        // and do the decoration here only if we're not currently displaying a 
        // validation result?
        if (highlightDuplicateCells.get()) {
            highlightDuplicateCells();
            notifyListeners(GridUiModelListener::onCellStateChanged);
        } else {
            removeInvalidCellsDecoration();
        }
    }
    
    private void highlightDuplicateCells() {
        ImmutableSet<Position> duplicates = grid.getCellsWithDuplicateValues();
        cellUis.forEach((p, c) -> c.setInvalid(duplicates.contains(p)));
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
        Stream<Position> positions = getSelectedPositionsForValueInput();
        applyValueToCells(positions, value);
        notifyListeners(GridUiModelListener::onCellStateChanged);
    }

    private void applyValueToCells(Stream<Position> positions, Value value) {
        UndoableAction action = getApplyValueAction(positions, value);
        if (action.isNoOp()) {
            return;
        }
        undoRedoState.add(action);
        action.perform();
    }

    private UndoableAction getApplyValueAction(Stream<Position> positions, Value value) {
        switch (enterValueMode) {
        case NORMAL:
            return new SetValueAction(value, positions);
        case CORNER_PENCIL_MARK:
            return new TogglePencilMarkAction(value, Cell::getCornerMarks, positions);
        case CENTER_PENCIL_MARK:
            return new TogglePencilMarkAction(value, Cell::getCenterMarks, positions);
        case COLOR:
            CellColor color = CellColor.fromValue(value);
            return new SetColorAction(color, positions);
        default:
            throw new RuntimeException("Unexpected mode: " + enterValueMode);
        }
    }
    
    private Stream<Position> getSelectedPositionsForValueInput() {
        Predicate<? super CellUi> condition = (enterValueMode == EnterValueMode.COLOR)
                ? c -> true // all cells can have a color
                : Predicate.not(CellUi::isGiven);
        return getSelectedPositions(condition);
    }

    private Stream<Cell> getSelectedCells() {
        return cellUis.values()
                .stream()
                .filter(CellUi::isSelected)
                .map(CellUi::getCell);
    }
    
    private Stream<Position> getSelectedPositions(Predicate<? super CellUi> condition) {
        return cellUis.values().stream()
                .filter(CellUi::isSelected)
                .filter(condition)
                .map(CellUi::getPosition);
    }

    // TODO: This is not a very good name.
    public void delete() {
        List<Cell> cells = getSelectedCells()
                .filter(Cell::hasNewInformation)
                .collect(toList());
        clearCellsImpl(cells, false);
    }

    public void reset() {
        List<Cell> cells = cellUis.values()
                .stream()
                .map(CellUi::getCell)
                .filter(Cell::hasNewInformation)
                .collect(toList());
        clearCellsImpl(cells, true);
        removeInvalidCellsDecoration();
    }

    private void clearCellsImpl(List<Cell> cells, boolean reset) {
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
    
    public void decorateInvalidCells(ValidationResult validationResult) {
        cellUis.forEach((p, c) -> c.setInvalid(validationResult.isInvalid(p)));
        notifyListeners(GridUiModelListener::onCellStateChanged);
    }
    
    public void removeInvalidCellsDecoration() {
        cellUis.values().forEach(c -> c.setInvalid(false));
        notifyListeners(GridUiModelListener::onCellStateChanged);
    }
    
    public void showRemainingCandidates() {
        // TODO: Ideally, the work to collect the candidates should be performed in
        // a background thread. That shouldn't be the responsibility of the model,
        // however, and it's also not immediately clear how to get undo/redo work.
        List<Cell> emptyCells = cellUis.values().stream()
                .map(CellUi::getCell)
                .filter(c -> !c.hasValue() && c.getCenterMarks().isEmpty())
                .collect(toImmutableList());
        if (emptyCells.isEmpty()) {
            return;
        }
        UndoableAction action = new ShowRemainingCandidatesAction(emptyCells);
        undoRedoState.add(action);
        action.perform();
    }
    
    /**
     * Removes the given values as candidates (i.e. center pencil marks) from the cells
     * at the given positions.
     */
    public void removeCandidatesFromCells(Collection<Position> positions, Collection<Value> valuesToRemove) {
        requireNonNull(positions);
        checkArgument(!valuesToRemove.isEmpty());
        RemoveCandidatesAction action = new RemoveCandidatesAction(positions, valuesToRemove);
        if (action.isNoOp()) {
            return;
        }
        undoRedoState.add(action);
        action.perform();
    }
    
    public Property<Boolean> getHighlightDuplicateCellsProperty() {
        return highlightDuplicateCells;
    }
    
    public Property<Boolean> getEliminateCandidatesProperty() {
        return eliminateCandidates;
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

    private void onCellValuesChanged() {
        if (highlightDuplicateCells.get()) {
            highlightDuplicateCells();
        }
        notifyListeners(GridUiModelListener::onCellStateChanged);
    }
    
    
    private class SetValueAction implements UndoableAction {
        private final Value value;
        private final ImmutableSet<Position> selectedPositions;
        private final ImmutableSet<Cell> selectedCells;
        private ImmutableMap<Cell, PreviousCellState> previousStates;

        public SetValueAction(Value value, Stream<Position> positions) {
            this.value = requireNonNull(value);
            this.selectedPositions = positions.collect(toImmutableSet());
            this.selectedCells = selectedPositions.stream()
                    .map(grid::cellAt)
                    .collect(toImmutableSet());
        }

        @Override
        public boolean isNoOp() {
            return selectedCells.stream()
                .map(c -> c.getValue().orElse(null))
                .allMatch(v -> v == value);
        }

        @Override
        public void perform() {
            // We must check the eliminateCandidates flag and rebuild the previous state here 
            // rather than in the constructor, since the user may change the eliminateCandidates 
            // flag between undos and redos of this action.
            ImmutableMap.Builder<Cell, PreviousCellState> previousStatesBuilder = ImmutableMap.builder();
            selectedCells.forEach(cell -> {
                previousStatesBuilder.put(cell, new PreviousCellState(cell));
                cell.setValue(value);
            });
            if (eliminateCandidates.get()) {
                selectedPositions.stream()
                    .flatMap(Position::seenBy)
                    .map(grid::cellAt)
                    .filter(Predicate.not(Cell::isGiven))
                    .filter(Predicate.not(this.selectedCells::contains))
                    .distinct()
                    .forEach(cell -> {
                        previousStatesBuilder.put(cell, new PreviousCellState(cell));
                        cell.getCornerMarks().remove(value);
                        cell.getCenterMarks().remove(value);
                    });
            }
            previousStates = previousStatesBuilder.build();
            onCellValuesChanged();
        }

        @Override
        public void undo() {
            previousStates.forEach((c, s) -> s.restore(c));
            onCellValuesChanged();
        }
    }

    
    private class TogglePencilMarkAction implements UndoableAction {
        private final Value value;
        private final Function<Cell, PencilMarks> pencilMarksSupplier;
        private final ImmutableList<Cell> cells;

        public TogglePencilMarkAction(Value value, Function<Cell, PencilMarks> pencil,
                Stream<Position> positions) {
            this.value = requireNonNull(value);
            this.pencilMarksSupplier = requireNonNull(pencil);
            this.cells = positions.map(grid::cellAt).collect(toImmutableList());
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
            cells.stream().map(pencilMarksSupplier).forEach(pm -> pm.toggle(value));
            notifyListeners(GridUiModelListener::onCellStateChanged);
        }

        @Override
        public void undo() {
            toggle();
        }
    }
    
    
    private class RemoveCandidatesAction implements UndoableAction {

        private final ImmutableMap<Cell, ImmutableSet<Value>> cellsAndTheirOldCandidates;
        private final ImmutableSet<Value> valuesToRemove;
        
        public RemoveCandidatesAction(Collection<Position> positions, Collection<Value> valuesToRemove) {
            this.cellsAndTheirOldCandidates = positions.stream()
                    .map(grid::cellAt)
                    .collect(toImmutableMap(c -> c, c -> c.getCenterMarks().getValues()));
            this.valuesToRemove = ImmutableSet.copyOf(valuesToRemove);
        }

        @Override
        public boolean isNoOp() {
            return cellsAndTheirOldCandidates.isEmpty() || cellsAndTheirOldCandidates.values().stream()
                    .allMatch(values -> Sets.intersection(values, valuesToRemove).isEmpty());
        }

        @Override
        public void perform() {
            cellsAndTheirOldCandidates.keySet().stream()
                .map(Cell::getCenterMarks)
                .forEach(m -> valuesToRemove.forEach(m::remove));
            notifyListeners(GridUiModelListener::onCellStateChanged);
        }
        
        @Override
        public void undo() {
            cellsAndTheirOldCandidates.forEach((cell, oldValues) -> cell.getCenterMarks().setValues(oldValues));
            notifyListeners(GridUiModelListener::onCellStateChanged);
        }
    }
    
    
    private class SetColorAction implements UndoableAction {
        private final CellColor color;
        private final ImmutableMap<Cell, CellColor> cellsAndTheirOldColors;

        public SetColorAction(CellColor color, Stream<Position> positions) {
            this.color = requireNonNull(color);
            this.cellsAndTheirOldColors = positions.map(grid::cellAt)
                    .collect(toImmutableMap(Function.identity(), Cell::getColor));
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
        private final ImmutableMap<Cell, PreviousCellState> cellsAndTheirPreviousState;
        private final boolean reset;

        public ClearCellsAction(List<Cell> cells, boolean reset) {
            this.cellsAndTheirPreviousState = cells.stream()
                    .collect(toImmutableMap(Function.identity(), PreviousCellState::new));
            this.reset = reset;
        }
        
        @Override
        public boolean isNoOp() {
            return cellsAndTheirPreviousState.keySet().stream()
                    .allMatch(Cell::isEmpty);
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
            onCellValuesChanged();
        }

        @Override
        public void undo() {
            cellsAndTheirPreviousState.forEach((c, s) -> s.restore(c));
            onCellValuesChanged();
        }
    }
    
    
    private class ShowRemainingCandidatesAction implements UndoableAction {
        private final ImmutableMap<Cell, ? extends Set<Value>> emptyCellsAndTheirPreviousState;

        public ShowRemainingCandidatesAction(List<Cell> emptyCells) {
            assert !emptyCells.isEmpty();
            emptyCellsAndTheirPreviousState = emptyCells.stream().collect(toImmutableMap(
                    c -> c, c -> c.getCenterMarks().getValues()));
        }

        @Override
        public void perform() {
            grid.showRemainingCandidates();
            notifyListeners(GridUiModelListener::onCellStateChanged);
        }
        
        @Override
        public void undo() {
            emptyCellsAndTheirPreviousState.forEach((c, s) -> c.getCenterMarks().setValues(s));
            notifyListeners(GridUiModelListener::onCellStateChanged);
        }

        @Override
        public boolean isNoOp() {
            return false;
        }
    }

    
    private static class PreviousCellState {
        private final Optional<Value> value;
        private final ImmutableSet<Value> cornerMarks;
        private final ImmutableSet<Value> centerMarks;
        private final CellColor color;
        
        public PreviousCellState(Cell cell) {
            this.value = cell.getValue();
            this.cornerMarks = cell.getCornerMarks().getValues();
            this.centerMarks = cell.getCenterMarks().getValues();
            this.color = cell.getColor();
        }
        
        public void restore(Cell cell) {
            if (!cell.isGiven()) {
                value.ifPresentOrElse(cell::setValue, cell::clearContent);
                cell.getCornerMarks().setValues(cornerMarks);
                cell.getCenterMarks().setValues(centerMarks);
            }
            cell.setColor(color);
        }
    }

}
