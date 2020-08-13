package jetoze.tzudoku.hint;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class PointingPairTest {

    /**
     * Verifies that we detect the case where a pointing pair in a box 
     * identifies targets in the same row or columns in another box.
     */
    @Test
    public void testTargetsInOtherBox() {
        // A pointing pair of 7s in Row 1/Box 1, pointing at
        // two targets in Row 1/Box 3.
        Grid grid = GridBuilder.builder()
                .row(1, "1[2357][167] [58]39 [247][578][68]")
                .row(2, "[3456][2345][345] 1[27][578] [247][578][68]")
                .row(3, "[289][2489][2689] [467][4678][467] 13[25678]")
                .fullyUnknownBox(4)
                .build();
        
        Optional<PointingPair> opt = PointingPair.analyze(grid);
        
        assertTrue(opt.isPresent());
        PointingPair pp = opt.get();
        assertSame(Value.SEVEN, pp.getValue());
        assertEquals(ImmutableSet.of(new Position(1, 7), new Position(1, 8)), pp.getTargetPositions());
    }
    
    @Test
    public void anotherTestOfTargetsInOtherBox() {
        // I came across this situation in The Daily Sudoku 2020-08-05, where a Pointing Pair
        // should have been detected but wasn't. 8 in Box 6 is confined to r5c7 and r5c8, which
        // eliminates 8 from r5c1 and r5c3.
        Grid grid = GridBuilder.builder()
                .row(4, "[269][59][256] 387 [25]41")
                .row(5, "[78]1[3578] 642 [578][358]9")
                .row(6, "48[237] 591 [27]6[37]")
                .build();

        Optional<PointingPair> opt = PointingPair.analyze(grid);
        
        assertTrue(opt.isPresent());
        PointingPair pp = opt.get();
        assertSame(Value.EIGHT, pp.getValue());
        assertEquals(ImmutableSet.of(new Position(5, 1), new Position(5, 3)), pp.getTargetPositions());
    }

}
