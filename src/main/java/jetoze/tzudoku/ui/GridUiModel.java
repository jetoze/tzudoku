package jetoze.tzudoku.ui;

import static java.util.Objects.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;

import jetoze.tzudoku.Cell;
import jetoze.tzudoku.Grid;
import jetoze.tzudoku.Position;
import jetoze.tzudoku.UnknownCell;
import jetoze.tzudoku.Value;

public class GridUiModel {
	private final ImmutableMap<Position, CellUi> cellUis;
	@Nullable
	private CellUi lastSelectedCell;
	private EnterValueMode enterValueMode = EnterValueMode.NORMAL;
	private final List<GridUiModelListener> listeners = new ArrayList<>();
	
	
	public GridUiModel(Grid grid) {
		this.cellUis = grid.getCells().entrySet().stream()
				.collect(ImmutableMap.toImmutableMap(Entry::getKey, e -> new CellUi(e.getKey(), e.getValue())));
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
		notifyListeners(GridUiModelListener::repaintBoard);
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
		getSelectedUnknownCells()
			.forEach(c -> applyValueToCell(c, value));
		notifyListeners(GridUiModelListener::repaintBoard);
	}
	
	private void applyValueToCell(UnknownCell cell, Value value) {
		switch (enterValueMode) {
		case NORMAL:
			cell.setValue(value);
			break;
		case CENTER_PENCIL_MARK:
			cell.toggleCenterPencilMark(value);
			break;
		case CORNER_PENCIL_MARK:
			cell.toggleCornerPencilMark(value);
			break;
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
		getSelectedUnknownCells()
			.forEach(UnknownCell::clearValue);
		notifyListeners(GridUiModelListener::repaintBoard);
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

}
