package jetoze.tzudoku.hint;

import static jetoze.tzudoku.model.Value.FIVE;
import static jetoze.tzudoku.model.Value.FOUR;
import static jetoze.tzudoku.model.Value.SIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;

public class NakedMultipleTest {

    @Test
    public void tripleInRowShouldBeDetected() {
        // A row with a triple from positions 4, 5, and 8 (spread out over two boxes)
        Grid grid = GridBuilder.builder()
                .row(1, "123[46][45][57][789][56][789]")
                .build();
        Optional<NakedMultiple> optTriple = NakedMultiple.findNakedTriple(grid);
        assertTrue(optTriple.isPresent());
        NakedMultiple multiple = optTriple.get();
        assertEquals(EnumSet.of(FOUR, FIVE, SIX), multiple.getValues());
        assertEquals(ImmutableSet.of(new Position(1, 4), new Position(1, 5), new Position(1, 8)), multiple.getForcingPositions());
    }

    @Test
    public void tripleInColumnShouldBeDetected() {
        // Column 2 with a triple from rows 4, 5, and 8 (spread out over two boxes)
        Grid grid = GridBuilder.builder()
                .column(2, "123[46][45][57][789][56][789]")
                .build();
        Optional<NakedMultiple> optTriple = NakedMultiple.findNakedTriple(grid);
        assertTrue(optTriple.isPresent());
        NakedMultiple multiple = optTriple.get();
        assertEquals(EnumSet.of(FOUR, FIVE, SIX), multiple.getValues());
        assertEquals(ImmutableSet.of(new Position(4, 2), new Position(5, 2), new Position(8, 2)), multiple.getForcingPositions());
    }
    
    @Test
    public void tripleInBoxShouldBeDetected() {
        // Triple in Box 1
        Grid grid = GridBuilder.builder().box(1, 
                "1[46][789]", 
                "[45]2[57]", 
                "3[789][56]").build();
        Optional<NakedMultiple> optTriple = NakedMultiple.findNakedTriple(grid);
        assertTrue(optTriple.isPresent());
        NakedMultiple multiple = optTriple.get();
        assertEquals(EnumSet.of(FOUR, FIVE, SIX), multiple.getValues());
        assertEquals(ImmutableSet.of(new Position(1, 2), new Position(2, 1), new Position(3, 3)), multiple.getForcingPositions());
    }
    
}
