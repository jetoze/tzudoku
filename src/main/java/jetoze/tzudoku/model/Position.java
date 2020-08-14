package jetoze.tzudoku.model;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.Streams;

public class Position {
    /**
     * Comparator that compares two Positions by row, using the column as tiebreaker.
     */
    public static final Comparator<Position> BY_ROW_AND_COLUMN = 
            Comparator.comparing(Position::getRow).thenComparing(Position::getColumn);
    
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
    
    /**
     * Checks if this position sees the other position, i.e. if the two positions
     * share at least one house. In this context, a position is not considered to see
     * itself, so this method returns false if {@code other} is the same position
     * as {@code this} position.
     */
    public boolean sees(Position other) {
        return ((this.row == other.row) ||
                (this.column == other.column) ||
                (this.getBox() == other.getBox())) && !this.equals(other);
    }
    
    /**
     * Returns a stream of all the other positions in the grid that is seen by this position.
     * This position itself is not included in the Stream.
     */
    public Stream<Position> seenBy() {
        Stream<Position> othersInRow = positionsInRow(row)
                .filter(Predicate.not(this::equals));
        Stream<Position> othersInColumn = positionsInColumn(column)
                .filter(Predicate.not(this::equals));
        Stream<Position> othersInBox = positionsInBox(getBox())
                .filter(p -> p.getRow() != row && p.getColumn() != column);
        return Streams.concat(othersInRow, othersInColumn, othersInBox);
    }
    
    /**
     * Returns an Stream containing the three houses this position is a member of.
     */
    public Stream<House> memberOf() {
        return Stream.of(House.row(row), House.column(column), House.box(getBox()));
    }
    
    /**
     * Returns the Position above this one, unless this Position is in the first row.
     */
    public Optional<Position> up() {
        return row > 1
                ? Optional.of(new Position(row - 1, column))
                : Optional.empty();
    }
    
    /**
     * Returns the Position below this one, unless this Position is in the last row.
     */
    public Optional<Position> down() {
        return row < 9
                ? Optional.of(new Position(row + 1, column))
                : Optional.empty();
    }
    
    /**
     * Returns the Position to the left of this one, unless this Position is in the first column.
     */
    public Optional<Position> left() {
        return column > 1
                ? Optional.of(new Position(row, column - 1))
                : Optional.empty();
    }
    
    /**
     * Returns the Position to the right of this one, unless this Position is in the last column.
     */
    public Optional<Position> right() {
        return column < 9
                ? Optional.of(new Position(row, column + 1))
                : Optional.empty();
    }
    
    /**
     * Returns a Stream of the positions that are orthogonally connected to this one.
     */
    public Stream<Position> orthogonallyConnected() {
        return Stream.of(up(), down(), left(), right())
                .flatMap(Optional::stream);
    }

    /**
     * Returns the string "r[row]c[column]", i.e. "r3c7" for the position
     * at row 3, column 7.
     * <p>
     * The returned string can be parsed back into an equivalent Position by calling {@link #fromString(String)}.
     */
    @Override
    public String toString() {
        return String.format("r%dc%d", row, column);
    }
    
    /**
     * Creates a Position instance from its {@link #toString() string representation}. 
     */
    public static Position fromString(String s) {
        checkArgument(s.length() == 4, "Invalid string: %s", s);
        checkArgument(s.charAt(0) == 'r', "Invalid string: %s", s);
        checkArgument(s.charAt(2) == 'c', "Invalid string: %s", s);
        int row = s.charAt(1) - 48;
        checkArgument(row >= 1 && row <= 9, "Invalid string: %s", s);
        int col = s.charAt(3) - 48;
        checkArgument(col >= 1 && col <= 9, "Invalid string: %s", s);
        return new Position(row, col);
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
    
    /**
     * Returns a Stream of all Positions that are seen by the given positions, not including
     * the positions themselves.
     * 
     * @throws IllegalArgumentException if {@code positions} contains less than two elements.
     */
    public static Stream<Position> seenByAll(Position... positions) {
        Position p0 = positions[0];
        Predicate<Position> filter = p -> true;
        for (int n = 1; n < positions.length; ++n) {
            Position pn = positions[n];
            filter = filter.and(pn::sees);
        }
        return p0.seenBy().filter(filter);
    }
    
}