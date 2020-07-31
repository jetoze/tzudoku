package jetoze.tzudoku.hint;

import static jetoze.tzudoku.model.Value.EIGHT;
import static jetoze.tzudoku.model.Value.FIVE;
import static jetoze.tzudoku.model.Value.FOUR;
import static jetoze.tzudoku.model.Value.NINE;
import static jetoze.tzudoku.model.Value.ONE;
import static jetoze.tzudoku.model.Value.SEVEN;
import static jetoze.tzudoku.model.Value.SIX;
import static jetoze.tzudoku.model.Value.THREE;
import static jetoze.tzudoku.model.Value.TWO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class MultipleTest {

    @Test
    public void tripleInRowShouldBeDetected() {
        // A row with a triple from positions 4, 5, and 8 (spread out over two boxes)
        Grid grid = Grid.builder().row(1, 
                Cell.given(ONE),
                Cell.given(TWO),
                Cell.given(THREE),
                cellWithCandidates(FOUR, SIX),
                cellWithCandidates(FOUR, FIVE),
                cellWithCandidates(FIVE, SEVEN),
                cellWithCandidates(SEVEN, EIGHT, NINE),
                cellWithCandidates(FIVE, SIX),
                cellWithCandidates(SEVEN, EIGHT, NINE)).build();
        Optional<Multiple> optTriple = Multiple.findNextTriple(grid);
        assertTrue(optTriple.isPresent());
        Multiple multiple = optTriple.get();
        assertEquals(EnumSet.of(FOUR, FIVE, SIX), multiple.getValues());
        assertEquals(ImmutableSet.of(new Position(1, 4), new Position(1, 5), new Position(1, 8)), multiple.getPositions());
    }

    @Test
    public void tripleInColumnShouldBeDetected() {
        // Column 2 with a triple from rows 4, 5, and 8 (spread out over two boxes)
        Grid grid = Grid.builder().column(2, 
                Cell.given(ONE),
                Cell.given(TWO),
                Cell.given(THREE),
                cellWithCandidates(FOUR, SIX),
                cellWithCandidates(FOUR, FIVE),
                cellWithCandidates(FIVE, SEVEN),
                cellWithCandidates(SEVEN, EIGHT, NINE),
                cellWithCandidates(FIVE, SIX),
                cellWithCandidates(SEVEN, EIGHT, NINE)).build();
        Optional<Multiple> optTriple = Multiple.findNextTriple(grid);
        assertTrue(optTriple.isPresent());
        Multiple multiple = optTriple.get();
        assertEquals(EnumSet.of(FOUR, FIVE, SIX), multiple.getValues());
        assertEquals(ImmutableSet.of(new Position(4, 2), new Position(5, 2), new Position(8, 2)), multiple.getPositions());
    }
    
    @Test
    public void tripleInBoxShouldBeDetected() {
        // Triple in Box 1
        Grid grid = Grid.builder().box(1, 
                Cell.given(ONE),
                cellWithCandidates(FOUR, SIX),
                cellWithCandidates(SEVEN, EIGHT, NINE),
                cellWithCandidates(FOUR, FIVE),
                Cell.given(TWO),
                cellWithCandidates(FIVE, SEVEN),
                Cell.given(THREE),
                cellWithCandidates(SEVEN, EIGHT, NINE),
                cellWithCandidates(FIVE, SIX)).build();
        Optional<Multiple> optTriple = Multiple.findNextTriple(grid);
        assertTrue(optTriple.isPresent());
        Multiple multiple = optTriple.get();
        assertEquals(EnumSet.of(FOUR, FIVE, SIX), multiple.getValues());
        assertEquals(ImmutableSet.of(new Position(1, 2), new Position(2, 1), new Position(3, 3)), multiple.getPositions());
    }
    
    private static Cell cellWithCandidates(Value... candidates) {
        Cell cell = Cell.empty();
        for (Value v : candidates) {
            cell.getCenterMarks().toggle(v);
        }
        return cell;
    }
    
}
