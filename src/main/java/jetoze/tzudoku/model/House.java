package jetoze.tzudoku.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Objects.requireNonNull;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
        ROW(Position::positionsInRow),
        COLUMN(Position::positionsInColumn),
        BOX(Position::positionsInBox);
        
        private final IntFunction<Stream<Position>> positionsSupplier;

        private Type(IntFunction<Stream<Position>> positionsSupplier) {
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
     * Returns the type of this House.
     */
    public Type getType() {
        return type;
    }
    
    /**
     * Returns a Stream of the positions in this house.
     */
    public Stream<Position> getPositions() {
        return type.positions(number);
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
     * Returns a set of the positions in this house that do not have a
     * value in the given grid.
     */
    public ImmutableSet<Position> getPositionsWithoutValues(Grid grid) {
        requireNonNull(grid);
        return getPositions().filter(p -> {
            Cell cell = grid.cellAt(p);
            return !cell.hasValue();
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
