package jetoze.tzudoku.constraint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.House;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class SandwichTest {

    @Test
    public void testSandwichValidation() {
        Sandwich sandwich = new Sandwich(House.row(1), 10);
        
        Grid grid = Grid.emptyGrid();
        assertTrue(sandwich.validate(grid).isEmpty());
        
        Position p1 = new Position(1, 1);
        Position p2 = new Position(1, 2);
        Position p3 = new Position(1, 3);
        Position p4 = new Position(1, 4);

        grid.cellAt(p2).setValue(Value.TWO);
        grid.cellAt(p3).setValue(Value.EIGHT);
        assertTrue(sandwich.validate(grid).isEmpty());
        
        grid.cellAt(p1).setValue(Value.NINE);
        assertTrue(sandwich.validate(grid).isEmpty());

        grid.cellAt(p4).setValue(Value.ONE);
        assertTrue(sandwich.validate(grid).isEmpty());
        
        grid.cellAt(p2).clearContent();
        assertTrue(sandwich.validate(grid).isEmpty());
        
        grid.cellAt(p2).setValue(Value.FIVE);
        assertEquals(ImmutableSet.of(p2, p3), sandwich.validate(grid), 
                "All the cells in a completed sandwich should be marked invalid if their sum is > the sandwich sum");
        
        sandwich = new Sandwich(House.row(1), 7);
        grid.cellAt(p1).setValue(Value.ONE);
        grid.cellAt(p2).clearContent();
        grid.cellAt(p3).setValue(Value.SEVEN);
        grid.cellAt(p4).setValue(Value.NINE);
        assertEquals(ImmutableSet.of(p3), sandwich.validate(grid), 
                "A cell in an incomplete sandwich should be marked invalid if its digit is >= the sandwich sum");
    }
    
    @Test
    public void testValidationOfZeroSumSandwich() {
        Sandwich sandwich = new Sandwich(House.row(1), 0);
        
        Grid grid = Grid.emptyGrid();
        assertTrue(sandwich.validate(grid).isEmpty());
        
        Position p1 = new Position(1, 1);
        Position p2 = new Position(1, 2);
        Position p3 = new Position(1, 3);
        grid.cellAt(p1).setValue(Value.ONE);
        grid.cellAt(p2).setValue(Value.FIVE);
        grid.cellAt(p3).setValue(Value.NINE);
        assertFalse(sandwich.validate(grid).isEmpty());
    }

}
