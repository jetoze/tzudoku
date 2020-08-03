package jetoze.tzudoku.hint;

import static jetoze.tzudoku.model.Value.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.EnumSet;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class HiddenMultipleTest {

    @Test
    public void hiddenTripleInRow() {
        Grid grid = GridBuilder.builder()
                .row(1, "[137]9[18] [347]2[347][158][56][16]")
                .build();
        
        Optional<HiddenMultiple> opt = HiddenMultiple.findHiddenTriple(grid);
        
        assertTrue(opt.isPresent());
        assertFalse(NakedMultiple.findNakedTriple(grid).isPresent());

        HiddenMultiple triple = opt.get();
        assertEquals(EnumSet.of(THREE, FOUR, SEVEN), triple.getHiddenValues());
        assertEquals(ImmutableSet.of(new Position(1, 1)), triple.getTargets());
        assertEquals(ImmutableMultimap.<Position, Value>builder().put(new Position(1, 1), ONE).build(),
                triple.getValuesToEliminate());
        
        triple.apply();
        for (Position p : triple.getTargets()) {
            assertTrue(Sets.difference(grid.cellAt(p).getCenterMarks().getValues(), triple.getHiddenValues()).isEmpty());
        }
    }
    
    @Test
    public void hiddenTripleInColumn() {
        Grid grid = GridBuilder.builder()
                .column(1, "[126][1256][4589] 7[1468][2389] [2356][236][235]")
                .build();
        
        Optional<HiddenMultiple> opt = HiddenMultiple.findHiddenTriple(grid);
        
        assertTrue(opt.isPresent());
        assertFalse(NakedMultiple.findNakedTriple(grid).isPresent());

        HiddenMultiple triple = opt.get();
        assertEquals(EnumSet.of(FOUR, EIGHT, NINE), triple.getHiddenValues());
        assertEquals(ImmutableSet.of(new Position(3, 1), new Position(5, 1), new Position(6, 1)), triple.getTargets());
        assertEquals(
                ImmutableMultimap.<Position, Value>builder()
                    .put(new Position(3, 1), FIVE)
                    .putAll(new Position(5, 1), ONE, SIX)
                    .putAll(new Position(6, 1), TWO, THREE)
                    .build(),
                triple.getValuesToEliminate());
        
        triple.apply();
        for (Position p : triple.getTargets()) {
            assertTrue(Sets.difference(grid.cellAt(p).getCenterMarks().getValues(), triple.getHiddenValues()).isEmpty());
        }
    }

}
