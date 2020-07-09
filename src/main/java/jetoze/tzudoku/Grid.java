package jetoze.tzudoku;

import com.google.common.collect.ImmutableList;
import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.*;

public final class Grid {
	private final ImmutableList<Cell> cells;
	
	public static Grid exampleOfSolvedGrid() {
		return new Grid(IntStream.of(
			     8, 2, 7, 1, 5, 4, 3, 9, 6,
			     9, 6, 5, 3, 2, 7, 1, 4, 8,
			     3, 4, 1, 6, 8, 9, 7, 5, 2,
			     5, 9, 3, 4, 6, 8, 2, 7, 1,
			     4, 7, 2, 5, 1, 3, 6, 8, 9,
			     6, 1, 8, 9, 7, 2, 4, 3, 5,
			     7, 8, 6, 2, 3, 5, 9, 1, 4,
			     1, 5, 4, 7, 9, 6, 8, 2, 3,
			     2, 3, 9, 8, 4, 1, 5, 6, 7)
				.mapToObj(Grid::toCell));
	}
	
	private static Cell toCell(int value) {
		return (value == 0)
				? UnknownCell.empty()
				: new GivenCell(Value.of(value));
	}
	
	public static Grid exampleOfUnsolvedGrid() {
		return new Grid(IntStream.of(
			     0, 0, 0, 2, 6, 0, 7, 0, 1,
			     6, 8, 0, 0, 7, 0, 0, 9, 0,
			     1, 9, 0, 0, 0, 4, 5, 0, 0,
			     8, 2, 0, 1, 0, 0, 0, 4, 0,
			     0, 0, 4, 6, 0, 2, 9, 0, 0,
			     0, 5, 0, 0, 0, 3, 0, 2, 8,
			     0, 0, 9, 3, 0, 0, 0, 7, 4,
			     0, 4, 0, 0, 5, 0, 0, 3, 6,
			     7, 0, 3, 0, 1, 8, 0, 0, 0)
				.mapToObj(Grid::toCell));
	}
	
	public Grid(Stream<Cell> cells) {
		this(cells.collect(toList()));
	}
	
	public Grid(List<Cell> cells) {
		checkArgument(cells.size() == 81, "Must provide 81 cells");
		this.cells = ImmutableList.copyOf(cells);
	}
	
	public Cell cellAt(int row, int col) {
		checkArgument(row > 0 && row <= 9);
		checkArgument(col > 0 && col <= 9);
		int index = (row - 1) * 9 + (col - 1);
		return cells.get(index);
	}

	public boolean isSolved() {
		if (!allCellsHaveValues()) {
			return false;
		}
		for (int n = 1; n <= 9; ++n) {
			if (!hasAllValues(getRow(n))) {
				return false;
			}
			if (!hasAllValues(getColumn(n))) {
				return false;
			}
			if (!hasAllValues(getBox(n))) {
				return false;
			}
		}
		return true;
	}
	
	private boolean allCellsHaveValues() {
		return cells.stream().map(Cell::getValue).allMatch(Optional::isPresent);
	}
	
	private boolean hasAllValues(Stream<Cell> cells) {
		Set<Value> values = cells
				.map(Cell::getValue)
				.flatMap(Optional::stream)
				.collect(toSet());
		return values
				.equals(Value.ALL);
	}
	
	public Stream<Cell> getRow(int n) {
		checkArgument(n > 0 && n <= 9);
		int first = (n - 1) * 9;
		return IntStream.range(first, first + 9)
				.mapToObj(cells::get);
	}
	
	public Stream<Cell> getColumn(int n) {
		checkArgument(n > 0 && n <= 9);
		int first = n - 1;
		int last = first + 72;
		return IntStream.iterate(first, x -> x <= last, x -> x + 9)
			.mapToObj(cells::get);
	}
	
	private Stream<Cell> getBox(int n) {
		IntStream indeces = getBoxIndeces(n);
		return indeces
			.mapToObj(cells::get);
	}

	private static IntStream getBoxIndeces(int n) {
		checkArgument(n > 0 && n <= 9);
		// Box 1:
		// 0 1 2 
		// 9 10 11 
		// 18 19 20
		
		// Box 2:
		// 3 4 5
		// 12 13 14
		// 21 22 23
		
		// Box 3:
		// 6 7 8
		// 15 16 17
		// 24 25 26
		
		// Box 4:
		// 27 28 29
		// 36 37 38
		// 45 46 47
		
		// Box 5:
		// 30 31 32
		// 39 40 41
		// 48 49 50
		
		// Box 6:
		// 33 34 35
		// 42, 43, 44
		// 51, 52, 53
		
		// Box 7:
		// 54 55 56
		// 63 64 65
		// 72 73 74
		
		// Box 8:
		// 57 58 59
		// 66 67 68
		// 75 76 77
		
		// Box 9:
		// 60 61 62
		// 69 70 71
		// 78 79 80
		// TODO: Can this be simplified?
		int first = ((n - 1) / 3) * 27 + 3 * ((n - 1) % 3);
		return IntStream.of(first, first + 1, first + 2,
					 first + 9, first + 10, first + 11,
					 first + 18, first + 19, first + 20);
	}
	
	public static void main(String[] args) {
		Grid grid = exampleOfSolvedGrid();
		for (int n = 1; n <= 9; ++n) {
			grid.getRow(n).map(Cell::getValue)
				.flatMap(Optional::stream)
				.map(v -> v.toInt() + " ")
				.forEach(System.out::print);
			System.out.println();
		}
		System.out.println(grid.isSolved());
	}
	
}
