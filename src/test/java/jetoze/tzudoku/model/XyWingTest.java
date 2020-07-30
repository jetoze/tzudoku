package jetoze.tzudoku.model;

import static jetoze.tzudoku.model.Value.EIGHT;
import static jetoze.tzudoku.model.Value.FIVE;
import static jetoze.tzudoku.model.Value.FOUR;
import static jetoze.tzudoku.model.Value.NINE;
import static jetoze.tzudoku.model.Value.ONE;
import static jetoze.tzudoku.model.Value.SEVEN;
import static jetoze.tzudoku.model.Value.SIX;
import static jetoze.tzudoku.model.Value.THREE;
import static jetoze.tzudoku.model.Value.TWO;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

public class XyWingTest {

    @Test
    public void threeXyWingCandidatesInSameBoxShouldBeIgnored() {
        Grid grid = Grid.builder().box(7, 
                Cell.given(SIX), cellWithCandidates(ONE, FOUR), cellWithCandidates(ONE, THREE),
                cellWithCandidates(THREE, FOUR), Cell.given(TWO), Cell.given(FIVE),
                Cell.given(SEVEN), Cell.given(EIGHT), Cell.given(NINE)).build();
        assertFalse(XyWing.findNext(grid).isPresent());
    }

    private static Cell cellWithCandidates(Value... values) {
        Cell cell = Cell.empty();
        cell.getCenterMarks().setValues(ImmutableSet.copyOf(values));
        return cell;
    }
    
}
