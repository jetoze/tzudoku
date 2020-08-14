package jetoze.tzudoku.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

public final class KillerCagesTest {

    @Test
    public void intersectionsNotAllowed() {
        KillerCage cage1 = killerCage(new Position(4, 5), new Position(4, 6), new Position(4, 7));
        KillerCage cage2 = killerCage(new Position(3, 6),
                                      new Position(4, 6),
                                      new Position(5, 6));
        assertThrows(IllegalArgumentException.class, () -> new KillerCages(Arrays.asList(cage1, cage2)));
    }
    
    @Test
    public void intersects() {
        KillerCage cage1 = killerCage(new Position(4, 5), new Position(4, 6), new Position(4, 7));
        KillerCage cage2 = killerCage(new Position(3, 2),
                                      new Position(4, 2),
                                      new Position(5, 2));
        KillerCages cages = new KillerCages(Arrays.asList(cage1, cage2));
        
        KillerCage cage3 = killerCage(new Position(5, 2), new Position(5, 3));
        assertTrue(cages.intersects(cage3));
        
        KillerCage cage4 = killerCage(new Position(9, 1), new Position(9, 2));
        assertFalse(cages.intersects(cage4));
    }
    
    @Test
    public void testAddOn() {
        KillerCage cage1 = killerCage(new Position(4, 5), new Position(4, 6), new Position(4, 7));
        KillerCage cage2 = killerCage(new Position(3, 2),
                                      new Position(4, 2),
                                      new Position(5, 2));
        KillerCages cages1 = new KillerCages(Arrays.asList(cage1, cage2));
        
        KillerCage cage3 = killerCage(new Position(7, 8), new Position(7, 9), new Position(8, 9));
        KillerCages cages2 = cages1.add(cage3);
        
        assertTrue(cages2.containsCage(cage1.getPositions()));
        assertTrue(cages2.containsCage(cage2.getPositions()));
        assertTrue(cages2.containsCage(cage3.getPositions()));
    }

    
    private static KillerCage killerCage(Position... positions) {
        return new KillerCage(ImmutableSet.copyOf(positions));
    }

}
