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
                .row(1, "1[27][167] [58]39 [247][578][68]")
                .row(2, "[345][345][345] 12[58] [247][578][68]")
                .row(3, "[289][289][289] [467][467][467] 13[568]")
                .build();
        
        Optional<PointingPair> opt = PointingPair.findNext(grid);
        
        assertTrue(opt.isPresent());
        PointingPair pp = opt.get();
        assertSame(Value.SEVEN, pp.getValue());
        assertEquals(ImmutableSet.of(new Position(1, 7), new Position(1, 8)), pp.getTargets());
    }
    
    /**
     * Verifies that we detect the case where a pointing pair in a box
     * identifies targets in a different row (or column) in the same box.
     */
    @Test
    public void testTargetsInSameBox() {
        // 7s are confined to Row 1 in Box 1, eliminating 7s from Rows 2 and 3 in Box 1.
        Grid grid = GridBuilder.builder()
                .row(1, "1[27][167] [589][458]3 [2468][2469][4569]")
                .row(2, "3[478][489] [247][578][68] 1[678][2468]")
                .row(3, "[2478][459][457] [269][2689][467] 13[568]")
                .build();
        
        Optional<PointingPair> opt = PointingPair.findNext(grid);
        
        assertTrue(opt.isPresent());
        PointingPair pp = opt.get();
        assertSame(Value.SEVEN, pp.getValue());
        assertEquals(ImmutableSet.of(new Position(2, 2), new Position(3, 1), new Position(3, 3)), pp.getTargets());
    }
    
    @Test
    public void pointingPairMustBeConfinedToBox() {
        Grid grid = GridBuilder.builder()
                .row(1, "1[2347][2347] 8[36][569] [678][459][2369]")
                .row(2, "5[2347][23478] [5689][345]1 [2467][589][345]")
                .row(3, "[3456][23457][1345] [23456][23457][456789] [456789][1345][23457]")
                // Add a fourth row to make sure we don't find PointingPair along the columns of
                // the first three rows.
                .fullyUnknownRow(4)
                .build();
        
        Optional<PointingPair> opt = PointingPair.findNext(grid);
        
        assertFalse(opt.isPresent());
    }

}
