package jetoze.tzudoku.hint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class XyzWingTest {

    @Test
    public void exampleFromDailySudoku_20200809_1() {
        Grid grid = GridBuilder.builder()
                .row(1, "578 [19][169]3 [469][149]2")
                .row(2, "123 [789]4[678] [689]5[67]")
                .row(3, "964 5[178]2 3[18][17]")
                .row(4, "41[27] 3[278][78] 569")
                .row(5, "395 4[16][16] 278")
                .row(6, "68[27] [79][2579][57] 134")
                .row(7, "75[16] 2[158]9 [468][148]3")
                .row(8, "24[16] [178]3[1578] [689][189][156]")
                .row(9, "839 6[15]4 72[15]")
                .build();
        Optional<XyzWing> opt = XyzWing.analyze(grid);
        
        assertTrue(opt.isPresent());
        
        XyzWing hint = opt.get();
        assertSame(Value.ONE, hint.getValue());
        assertEquals(new Position(1, 5), hint.getPivot());
        assertEquals(ImmutableSet.of(new Position(1, 4), new Position(5, 5)), hint.getWings());
        assertEquals(ImmutableSet.of(new Position(3, 5)), hint.getTargetPositions());
        
        hint.apply();
        assertEquals(ImmutableSet.of(Value.SEVEN, Value.EIGHT), grid.cellAt(new Position(3, 5)).getCenterMarks().getValues());
    }

    @Test
    public void exampleFromDailySudoku_20200809_2() {
        // Appling the XYZ-Wing detected in the first example gives us this grid, which has 
        // another XYZ-Wing waiting to be detected.
        Grid grid = GridBuilder.builder()
                .row(1, "578 [19][169]3 [469][149]2")
                .row(2, "123 [789]4[678] [689]5[67]")
                .row(3, "964 5[78]2 3[18][17]")
                .row(4, "41[27] 3[278][78] 569")
                .row(5, "395 4[16][16] 278")
                .row(6, "68[27] [79][2579][57] 134")
                .row(7, "75[16] 2[158]9 [468][148]3")
                .row(8, "24[16] [178]3[1578] [689][189][156]")
                .row(9, "839 6[15]4 72[15]")
                .build();
        Optional<XyzWing> opt = XyzWing.analyze(grid);
        
        assertTrue(opt.isPresent());
        
        XyzWing hint = opt.get();
        assertSame(Value.SEVEN, hint.getValue());
        assertEquals(new Position(2, 6), hint.getPivot());
        assertEquals(ImmutableSet.of(new Position(2, 9), new Position(3, 5)), hint.getWings());
        assertEquals(ImmutableSet.of(new Position(2, 4)), hint.getTargetPositions());
        
        hint.apply();
        assertEquals(ImmutableSet.of(Value.EIGHT, Value.NINE), grid.cellAt(new Position(2, 4)).getCenterMarks().getValues());
    }

    @Test
    public void exampleFromDailySudoku_20200809_3() {
        // The grid that keeps on giving. Appling the XYZ-Wing detected in the second example 
        // gives us this grid, which has a third XYZ-Wing waiting to be detected.
        Grid grid = GridBuilder.builder()
                .row(1, "578 [19][169]3 [469][149]2")
                .row(2, "123 [89]4[678] [689]5[67]")
                .row(3, "964 5[78]2 3[18][17]")
                .row(4, "41[27] 3[278][78] 569")
                .row(5, "395 4[16][16] 278")
                .row(6, "68[27] [79][2579][57] 134")
                .row(7, "75[16] 2[158]9 [468][148]3")
                .row(8, "24[16] [178]3[1578] [689][189][156]")
                .row(9, "839 6[15]4 72[15]")
                .build();
        Optional<XyzWing> opt = XyzWing.analyze(grid);
        
        assertTrue(opt.isPresent());
        
        XyzWing hint = opt.get();
        assertSame(Value.ONE, hint.getValue());
        assertEquals(new Position(8, 9), hint.getPivot());
        assertEquals(ImmutableSet.of(new Position(8, 3), new Position(9, 9)), hint.getWings());
        assertEquals(ImmutableSet.of(new Position(8, 8)), hint.getTargetPositions());
        
        hint.apply();
        assertEquals(ImmutableSet.of(Value.EIGHT, Value.NINE), grid.cellAt(new Position(8, 8)).getCenterMarks().getValues());
    }
}
