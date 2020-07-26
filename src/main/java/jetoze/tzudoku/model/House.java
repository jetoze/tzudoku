package jetoze.tzudoku.model;

import java.util.EnumSet;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.Stream;

/**
 * Represents the types of houses in a sudoku grid.
 */
public enum House {

    ROW(Position::positionsInRow),
    COLUMN(Position::positionsInColumn),
    BOX(Position::positionsInBox);
    
    private final IntFunction<Stream<Position>> positionsSupplier;

    private House(IntFunction<Stream<Position>> positionsSupplier) {
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
     * Returns a set of the values not yet entered into the house of the given
     * number.
     * 
     * @param num  the number (1-9) of the house
     * @param grid the grid
     * @return an EnumSet of the Values not yet entered
     */
    public EnumSet<Value> getRemainingValues(int num, Grid grid) {
        EnumSet<Value> values = EnumSet.allOf(Value.class);
        positions(num)
            .map(grid::cellAt)
            .map(Cell::getValue)
            .flatMap(Optional::stream)
            .forEach(values::remove);
        return values;
    }
    
}
