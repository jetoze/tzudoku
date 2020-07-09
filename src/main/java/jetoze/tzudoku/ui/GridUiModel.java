package jetoze.tzudoku.ui;

import static com.google.common.collect.ImmutableList.*;
import static com.google.common.collect.ImmutableMap.*;
import static java.util.Objects.*;

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

import jetoze.tzudoku.Cell;
import jetoze.tzudoku.Grid;
import jetoze.tzudoku.PencilMarks;
import jetoze.tzudoku.Position;
import jetoze.tzudoku.UnknownCell;
import jetoze.tzudoku.Value;

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
		return Optional.ofNullable(lastSelectedCell)
				.filter(CellUi::isSelected);
	}

	public void selectCell(CellUi cell, boolean isMultiSelect) {
		lastSelectedCell = cell;
		cell.setSelected(true);
		if (!isMultiSelect) {
			cellUis.values().stream()
				.filter(c -> c != cell)
				.forEach(c -> c.setSelected(false));
		}
		// TODO: Only do this if something changed.
		notifyListeners(GridUiModelListener::onCellStateChanged);
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
		ImmutableList<UnknownCell> cells = getSelectedUnknownCells()
				.collect(toImmutableList());
		applyValueToCells(cells, value);
		notifyListeners(GridUiModelListener::onCellStateChanged);
	}
	
	private void applyValueToCells(ImmutableList<UnknownCell> cells, Value value) {
		UndoableAction action = getApplyValueAction(cells, value);
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
		default:
			throw new RuntimeException("Unexpected mode: " + enterValueMode);
		}
	}

	private Stream<UnknownCell> getSelectedUnknownCells() {
		return cellUis.values().stream()
			.filter(CellUi::isSelected)
			.map(CellUi::getCell)
			.filter(Predicate.not(Cell::isGiven))
			.map(UnknownCell.class::cast);
	}
	
	public void clearSelectedCells() {
		ImmutableList<UnknownCell> cells = getSelectedUnknownCells()
			.filter(Predicate.not(UnknownCell::isEmpty))
			.collect(toImmutableList());
		if (!cells.isEmpty()) {
			ClearCellsAction action = new ClearCellsAction(cells);
			undoRedoState.add(action);
			action.perform();
		}
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
		public void perform() {
			cellsAndTheirOldValues.keySet().forEach(c -> c.setValue(value));
			notifyListeners(GridUiModelListener::onCellStateChanged);
		}

		@Override
		public void undo() {
			for (Map.Entry<UnknownCell, Optional<Value>> e : cellsAndTheirOldValues.entrySet()) {
				UnknownCell cell = e.getKey();
				Optional<Value> value = e.getValue();
				value.ifPresentOrElse(cell::setValue, cell::clearValue);
			}
			notifyListeners(GridUiModelListener::onCellStateChanged);
		}
	}
	
	private class TogglePencilMarkAction implements UndoableAction {
		private final Value value;
		private final BiConsumer<PencilMarks, Value> pencil;
		private final ImmutableList<UnknownCell> cells;
		
		public TogglePencilMarkAction(Value value, 
									  BiConsumer<PencilMarks, Value> pencil, 
									  ImmutableList<UnknownCell> cells) {
			this.value = requireNonNull(value);
			this.pencil = requireNonNull(pencil);
			this.cells = requireNonNull(cells);
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
	
	private class ClearCellsAction implements UndoableAction {
		private final ImmutableMap<UnknownCell, PreviousCellState> cellsAndTheirPreviousState;
		
		public ClearCellsAction(List<UnknownCell> cells) {
			this.cellsAndTheirPreviousState = cells.stream().collect(toImmutableMap(
					Function.identity(), PreviousCellState::of));
		}
		
		
		@Override
		public void perform() {
			cellsAndTheirPreviousState.keySet().forEach(UnknownCell::clearValue);
			notifyListeners(GridUiModelListener::onCellStateChanged);
		}

		@Override
		public void undo() {
			cellsAndTheirPreviousState.forEach((c, s) -> s.restore(c));
			notifyListeners(GridUiModelListener::onCellStateChanged);
		}
	}
	
	private static interface PreviousCellState {
		void restore(UnknownCell cell);
		
		static PreviousCellState of(UnknownCell cell) {
			return cell.getValue()
					.<PreviousCellState>map(CellHadValue::new)
					.orElseGet(() -> new CellHadPencilMarks(cell));
		}
	}
	
	private static class CellHadValue implements PreviousCellState {
		private final Value value;

		public CellHadValue(Value value) {
			this.value = requireNonNull(value);
		}

		@Override
		public void restore(UnknownCell cell) {
			cell.setValue(value);
		}
	}
	
	private static class CellHadPencilMarks implements PreviousCellState {
		private final ImmutableSet<Value> cornerMarks;
		private final ImmutableSet<Value> centerMarks;
		
		public CellHadPencilMarks(UnknownCell cell) {
			this.cornerMarks = ImmutableSet.copyOf(cell.getPencilMarks().iterateOverCornerMarks());
			this.centerMarks = ImmutableSet.copyOf(cell.getPencilMarks().iterateOverCenterMarks());
		}

		@Override
		public void restore(UnknownCell cell) {
			PencilMarks pencilMarks = cell.getPencilMarks();
			cornerMarks.forEach(pencilMarks::toggleCorner);
			centerMarks.forEach(pencilMarks::toggleCenter);
		}
	}

}
