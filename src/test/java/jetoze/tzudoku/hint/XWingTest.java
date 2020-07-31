package jetoze.tzudoku.hint;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class XWingTest {

    @Test
    public void xWingIsDetected() {
        // Grid is from The Daily Sudoku, 2020-02-29. There is an X-wing defined
        // by positions (1,2), (1,7), (6,2), and (6,7), ruling out 3 as a candidate
        // from positions (1,1), (1,8), (6,5), and (6,6).
        Grid grid = GridBuilder.builder()
                .row(1, "[13478][3478]5 [14689][4689][48] [13][236][127]")
                .row(2, "96[17] 325 84[17]")
                .row(3, "[1348]2[18] [1468][468]7 9[36]5")
                .row(4, "[2378]1[278] [48][3478]9 5[238]6")
                .row(5, "[238]94 5[138]6 7[238][12]")
                .row(6, "5[378]6 2[1378][38] [13]94")
                .row(7, "653 7[489][248] [24]1[89]")
                .row(8, "[248][48]9 [468]51 [246]73")
                .row(9, "[12478][478][1278] [4689][34689][2348] [246]5[89]")
                .build();
        
        Optional<XWing> opt = XWing.findNext(grid);
        
        assertTrue(opt.isPresent());
        XWing xwing = opt.get();
        
        assertSame(Value.THREE, xwing.getValue());
        assertEquals(
                ImmutableSet.of(new Position(1, 2), new Position(1,  7), new Position(6, 2), new Position(6, 7)),
                xwing.getPositions());
        assertEquals(
                ImmutableSet.of(new Position(1, 1), new Position(1, 8), new Position(6, 5), new Position(6, 6)),
                xwing.getTargets());
    }
    
    public XWingTest() {
        // TODO Auto-generated constructor stub
    }

}
