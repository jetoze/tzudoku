package jetoze.tzudoku.model;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.Streams;

public class Position {
    private final int row;
    private final int column;

    public static Position of(int row, int column) {
        return new Position(row, column);
    }

    public Position(int row, int column) {
        checkRow(row);
        checkColumn(column);
        this.row = row;
        this.column = column;
    }

    private static void checkRow(int row) {
        checkArgument(row > 0 && row <= 9, "row must be in [1,9], was %s", row);
    }

    private static void checkColumn(int column) {
        checkArgument(column > 0 && column <= 9, "column must be in [1,9], was %s", column);
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }
    
    public int getBox() {
        // Rows 1-3 -> Boxes 1-3
        // Rows 4-6 -> Boxes 4-6
        // Rows 7-9 -> Boxes 7-9
        int minBox = 1 + 3 * ((row - 1) / 3);
        return minBox + (column - 1) / 3;
    }

    public Position left() {
        return new Position(row, column > 1 ? column - 1 : 9);
    }

    public Position right() {
        return new Position(row, (column < 9) ? column + 1 : 1);
    }

    public Position up() {
        return new Position(row > 1 ? row - 1 : 9, column);
    }

    public Position down() {
        return new Position((row < 9) ? row + 1 : 1, column);
    }
    
    public Stream<Position> seenBy() {
        Stream<Position> othersInRow = positionsInRow(row)
                .filter(p -> p != this);
        Stream<Position> othersInColumn = positionsInColumn(column)
                .filter(p -> p != this);
        Stream<Position> othersInBox = positionsInBox(getBox())
                .filter(p -> p.getRow() != row && p.getColumn() != column);
        return Streams.concat(othersInRow, othersInColumn, othersInBox);
    }

    @Override
    public String toString() {
        return String.format("[%d, %s]", row, column);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof Position) {
            Position that = (Position) o;
            return this.row == that.row && this.column == that.column;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }
    
    public static Stream<Position> all() {
        return IntStream.rangeClosed(1, 9)
                .mapToObj(Position::positionsInRow)
                .flatMap(Function.identity());
    }

    public static Stream<Position> positionsInRow(int row) {
        checkRow(row);
        return IntStream.rangeClosed(1, 9).mapToObj(col -> new Position(row, col));
    }

    public static Stream<Position> positionsInColumn(int column) {
        checkColumn(column);
        return IntStream.rangeClosed(1, 9).mapToObj(row -> new Position(row, column));
    }

    public static Stream<Position> positionsInBox(int box) {
        checkArgument(box > 0 && box <= 9, "box must be in [1,9], was %s", box);
        // 1, 2, 3 --> 1 3 * ((box - 1) / 3) + 1
        // 4, 5, 6 --> 4
        // 7, 8, 9 --> 7

        // 1, 4, 7 --> 1 1 + ((col % 3) - 1) * 3;
        // 2, 5, 8 --> 4
        // 3, 6, 9 --> 7
        int firstRow = 3 * ((box - 1) / 3) + 1;
        int firstCol = 1 + ((box - 1) % 3) * 3;
        return Stream.of(Position.of(firstRow, firstCol), Position.of(firstRow, firstCol + 1),
                Position.of(firstRow, firstCol + 2), Position.of(firstRow + 1, firstCol),
                Position.of(firstRow + 1, firstCol + 1), Position.of(firstRow + 1, firstCol + 2),
                Position.of(firstRow + 2, firstCol), Position.of(firstRow + 2, firstCol + 1),
                Position.of(firstRow + 2, firstCol + 2));
    }
    
}