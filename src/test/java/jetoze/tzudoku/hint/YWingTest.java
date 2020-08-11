package jetoze.tzudoku.hint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class YWingTest {

    @Test
    public void exampleFromDailySudoku_20200809() {
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
        Optional<YWing> opt = YWing.findNext(grid);
        
        assertTrue(opt.isPresent());
        YWing hint = opt.get();
        
        assertEquals(Value.SEVEN, hint.getValue());
        assertEquals(new Position(2, 4), hint.getPivot());
        assertEquals(ImmutableSet.of(new Position(3, 5), new Position(6, 4)), hint.getWings());
        assertEquals(ImmutableSet.of(new Position(4, 5), new Position(6, 5)), hint.getTargetPositions());

        hint.apply();
        assertEquals(ImmutableSet.of(Value.TWO, Value.EIGHT), grid.cellAt(new Position(4, 5)).getCenterMarks().getValues());
        assertEquals(ImmutableSet.of(Value.TWO, Value.FIVE, Value.NINE), grid.cellAt(new Position(6, 5)).getCenterMarks().getValues());
    }
    
    @Test
    public void threeYWingCandidatesInSameBoxShouldBeIgnored() {
        Grid grid = GridBuilder.builder().box(7, 
                "6[14][13]",
                "[34]25",
                "789").build();
        assertFalse(YWing.findNext(grid).isPresent());
    }
    
}
