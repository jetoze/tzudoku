package jetoze.tzudoku.hint;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import jetoze.tzudoku.model.Grid;

public class XyWingTest {

    @Test
    public void threeXyWingCandidatesInSameBoxShouldBeIgnored() {
        Grid grid = GridBuilder.builder().box(7, 
                "6[14][13]",
                "[34]25",
                "789").build();
        assertFalse(XyWing.findNext(grid).isPresent());
    }
    
}
