package jetoze.tzudoku.hint;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import jetoze.tzudoku.model.Grid;

public class YWingTest {

    @Test
    public void threeYWingCandidatesInSameBoxShouldBeIgnored() {
        Grid grid = GridBuilder.builder().box(7, 
                "6[14][13]",
                "[34]25",
                "789").build();
        assertFalse(YWing.findNext(grid).isPresent());
    }
    
}
