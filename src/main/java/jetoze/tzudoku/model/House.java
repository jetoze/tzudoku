package jetoze.tzudoku.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Represents a house in a sudoku grid. 
 * <p>
 * A House is not tied to a specific grid, i.e. it does not contain grid-specific 
 * information such as what values are currently entered. A House merely represents
 * the set of positions that make up the house in any grid.
 * 
 */
public class House {
    
    /**
     * Represents the types of houses.
     */
    public static enum Type {
        ROW(Position::getRow, Position::positionsInRow),
        COLUMN(Position::getColumn, Position::positionsInColumn),
        BOX(Position::getBox, Position::positionsInBox);
        
        private final ToIntFunction<Position> houseNumberFunction;
        private final IntFunction<Stream<Position>> positionsSupplier;

        private Type(ToIntFunction<Position> houseNumberFunction, IntFunction<Stream<Position>> positionsSupplier) {
            this.houseNumberFunction = houseNumberFunction;
            this.positionsSupplier = positionsSupplier;
        }
        
        /**
         * Returns a Stream of the Positions in this house.
         * 
         * @param num the number (1-9) of the house
         */
        public Stream<Position> positions(int num) {
            return positionsSupplier.apply(num);
        }
        
        /**
         * Creates a House of this type with the given number.
         */
        public final House createHouse(int num) {
            return new House(this, num);
        }

        /**
         * Returns a Comparator that sorts Positions belonging to a House of this type
         * in the order in which they appear in the House. The positions in a Row are sorted
         * by their Column number, and vice versa. The positions in a Box are sorted by
         * Row and Column.
         */
        public final Comparator<Position> positionOrder() {
            switch (this) {
            case ROW:
                return Comparator.comparing(Position::getColumn);
            case COLUMN:
                return Comparator.comparing(Position::getRow);
            case BOX:
                return Comparator.comparing(Position::getRow).thenComparing(Position::getColumn);
            default:
                throw new RuntimeException("Unknown house type: " + this);
            }
        }
        
        @Override
        public final String toString() {
            switch (this) {
            case ROW:
                return "Row";
            case COLUMN:
                return "Column";
            case BOX:
                return "Box";
            default:
                throw new RuntimeException("Unknown house type: " + this);
            }
        }
    };
    
    /**
     * Immutable set containing all the houses in a sudoku grid.
     */
    public static final ImmutableSet<House> ALL = loadAllHouses();
    
    private static ImmutableSet<House> loadAllHouses() {
        ImmutableSet.Builder<House> builder = ImmutableSet.builder();
        for (House.Type type : Type.values()) {
            IntStream.rangeClosed(1, 9)
                .mapToObj(num -> new House(type, num))
                .forEach(builder::add);
        }
        return builder.build();
    }
    
    private final Type type;
    private final int number;
        
    public House(Type type, int number) {
        this.type = requireNonNull(type);
        checkArgument(number >= 1 && number <= 9, "number must be >= 1 and <=9, but was %s", number);
        this.number = number;
    }
    
    /**
     * Returns a House representation of the given row.
     */
    public static House row(int rowNum) {
        return new House(Type.ROW, rowNum);
    }
    
    /**
     * Returns a House representation of the given column.
     */
    public static House column(int colNum) {
        return new House(Type.COLUMN, colNum);
    }
    
    /**
     * Returns a House representation of the given box.
     */
    public static House box(int boxNum) {
        return new House(Type.BOX, boxNum);
    }
    
    /**
     * If the given set contains two or more positions that all are in the
     * same line or column, returns the corresponding row- or column-based House.
     * 
     * @return an Optional containing the House representing the row or column the
     *         positions belong to. An empty Optional is returned if the positions
     *         do not line up, or if the set contains less than two positions.
     */
    public static Optional<House> ifInRowOrColumn(Set<Position> positions) {
        if (positions.size() < 2) {
            return Optional.empty();
        }
        int row = 0;
        int column = 0;
        for (Position p : positions) {
            if (row == 0 && column == 0) {
                row = p.getRow();
                column = p.getColumn();
            } else {
                if (row != p.getRow()) {
                    row = -1;
                }
                if (column != p.getColumn()) {
                    column = -1;
                }
            }
            if (row == -1 && column == -1) {
                break;
            }
        }
        if (row > 0) {
            return Optional.of(row(row));
        } else if (column > 0) {
            return Optional.of(column(column));
        } else {
            return Optional.empty();
        }
    }
        
    /**
     * If the given set contains two or more positions that all are in the
     * same Box, returns that Box.
     * 
     * @return an Optional containing the House representing the Box the
     *         positions belong to. An empty Optional is returned if the positions
     *         are not confined to a box, or if the set contains less than two positions.
     */
    public static Optional<House> ifInBox(Set<Position> positions) {
        if (positions.size() < 2) {
            return Optional.empty();
        }
        return allInSameBox(positions)
                ? Optional.of(box(positions.iterator().next().getBox()))
                : Optional.empty();
    }
    
    /**
     * Checks if the given positions all belong to the same box.
     * 
     * @return {@code true} for any non-empty set containing positions that all
     *         belong to the same box.
     */
    public static boolean allInSameBox(Set<Position> positions) {
        return positions.stream()
                .mapToInt(Type.BOX.houseNumberFunction)
                .distinct()
                .count() == 1L;
    }
    
    /**
     * If the given set contains two or more positions that all are in the same
     * House, returns the corresponding House.
     * 
     * @return an Optional containing the House to which the positions belong to. An
     *         empty Optional is returned if the positions all not all in the same
     *         House, or if the set contains less than two positions.
     */
    public static Optional<House> ifInSameHouse(Set<Position> positions) {
        if (positions.size() < 2) {
            return Optional.empty();
        }
        int row = 0;
        int column = 0;
        int box = 0;
        for (Position p : positions) {
            if (row == 0) {
                // The first position we're looking at
                row = p.getRow();
                column = p.getColumn();
                box = p.getBox();
            } else {
                if (row != p.getRow()) {
                    row = -1;
                }
                if (column != p.getColumn()) {
                    column = -1;
                }
                if (box != p.getBox()) {
                    box = -1;
                }
            }
            if (row == -1 && column == -1 && box == -1) {
                break;
            }
        }
        if (row > 0) {
            return Optional.of(row(row));
        } else if (column > 0) {
            return Optional.of(column(column));
        } else if (box > 0) {
            return Optional.of(box(box));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Returns the type of this House.
     */
    public Type getType() {
        return type;
    }
    
    /**
     * Returns the number of this house (e.g. the 7th row, the 4th column).
     */
    public int getNumber() {
        return number;
    }
    
    public Position getPosition(int n) {
        switch (type) {
        case ROW:
            // this.number represents the row, n represents the column
            return new Position(this.number, n);
        case COLUMN:
            // this.number represents the column, n represents the row
            return new Position(n, this.number);
        case BOX:
            throw new RuntimeException("Not implemented yet");
        default:
            throw new RuntimeException("Unknown House type: " + type);
        }
    }
    
    /**
     * Returns a Stream of the positions in this house.
     */
    public Stream<Position> getPositions() {
        return type.positions(number);
    }
    
    public ImmutableList<Position> toList() {
        return getPositions().collect(toImmutableList());
    }
    
    /**
     * Checks if the given position is contained in this House.
     */
    public boolean contains(Position p) {
        return type.houseNumberFunction.applyAsInt(p) == this.number;
    }

    /**
     * Returns a set of the values not yet entered into this house.
     * 
     * @param grid the grid
     * @return an EnumSet of the Values not yet entered
     */
    public EnumSet<Value> getRemainingValues(Grid grid) {
        requireNonNull(grid);
        EnumSet<Value> values = EnumSet.allOf(Value.class);
        getPositions()
            .map(grid::cellAt)
            .map(Cell::getValue)
            .flatMap(Optional::stream)
            .forEach(values::remove);
        return values;
    }
    
    /**
     * Returns a set of the positions of the cells in this house that match 
     * the given condition.
     */
    public ImmutableSet<Position> getMatchingPositions(Grid grid, Predicate<? super Cell> condition) {
        requireNonNull(grid);
        requireNonNull(condition);
        return getPositions().filter(p -> {
            Cell cell = grid.cellAt(p);
            return condition.test(cell);
        }).collect(toImmutableSet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, number);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof House) {
            House that = (House) obj;
            return this.type == that.type && this.number == that.number;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s %d", type, number);
    }
}
