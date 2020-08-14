package jetoze.tzudoku.model;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

public class KillerCageTest {

    public KillerCageTest() {
        // TODO Auto-generated constructor stub
    }

    @Test
    public void requiresAtLeastTwoCells() {
        assertThrows(IllegalArgumentException.class, () -> killerCage(new Position(1, 1)));
    }
    
    @Test
    public void allowsAtMostNineCells() {
        Set<Position> positions = Position.positionsInRow(1).collect(toSet());
        // Should be fine
        new KillerCage(positions);
        positions.add(new Position(2, 1));
        assertThrows(IllegalArgumentException.class, () -> new KillerCage(positions));
    }
    
    @Test
    public void testVariousShapes() {
        // Just verifying that these cages can be created without IllegalArgumentExceptions
        killerCage(new Position(1, 1), new Position(1, 2));
        killerCage(new Position(1, 1), new Position(2, 1));
        
        killerCage(new Position(1, 1), new Position(1, 2),
                   new Position(2, 1), new Position(2, 2),
                   new Position(3, 1),
                   new Position(4, 1));
        
        killerCage(                    new Position(2, 4), 
                   new Position(3, 3), new Position(3, 4), new Position(3, 5), new Position(3, 6),
                                                           new Position(4, 5), new Position(4, 6),
                                                           new Position(5, 5));
    }
    
    @Test
    public void cellsMustBeOrthogonallyConnected() {
        assertThrows(IllegalArgumentException.class, 
                () -> killerCage(new Position(1, 1), new Position(2, 2)));
        assertThrows(IllegalArgumentException.class, 
                () -> killerCage(new Position(1, 1), new Position(1, 2),
                                 new Position(2, 1), new Position(2, 2),
                                 new Position(3, 1), new Position(4, 2)));
    }
    
    @Test
    public void testLocationOfSum() {
        assertEquals(new Position(1, 1), killerCage(new Position(1, 1), new Position(1, 2)).getLocationOfSum());
        assertEquals(new Position(1, 1), killerCage(new Position(1, 1), new Position(2, 1)).getLocationOfSum());
        
        KillerCage cage = killerCage(            new Position(2, 4), 
                             new Position(3, 3), new Position(3, 4), new Position(3, 5), new Position(3, 6),
                                                                     new Position(4, 5), new Position(4, 6),
                                                                     new Position(5, 5));
        assertEquals(new Position(2, 4), cage.getLocationOfSum());
    }
    
    @Test
    public void testBoundaries() {
        KillerCage cage = killerCage(           new Position(2, 4), 
                            new Position(3, 3), new Position(3, 4), new Position(3, 5), new Position(3, 6),
                                                                    new Position(4, 5), new Position(4, 6),
                                                                    new Position(5, 5));
        assertEquals(ImmutableSet.of(new Position(2, 4), new Position(3, 3), new Position(4, 5), new Position(5, 5)),
                cage.getLeftBoundary());
        assertEquals(ImmutableSet.of(new Position(2, 4), new Position(3, 6), new Position(4, 6), new Position(5, 5)),
                cage.getRightBoundary());
        assertEquals(ImmutableSet.of(new Position(2, 4), new Position(3, 3), new Position(3, 5), new Position(3, 6)),
                cage.getUpperBoundary());
        assertEquals(ImmutableSet.of(new Position(3, 3), new Position(3, 4), new Position(5, 5), new Position(4, 6)),
                cage.getLowerBoundary());

        cage = killerCage(new Position(2, 2),
                          new Position(3, 2),
                          new Position(4, 2));
        assertEquals(cage.getPositions(), cage.getLeftBoundary());
        assertEquals(cage.getPositions(), cage.getRightBoundary());
        assertEquals(ImmutableSet.of(new Position(2, 2)), cage.getUpperBoundary());
        assertEquals(ImmutableSet.of(new Position(4, 2)), cage.getLowerBoundary());
    }
    
    @Test
    public void intersects() {
        KillerCage cage1 = killerCage(new Position(2, 2), new Position(2, 3), new Position(2, 4));
        KillerCage cage2 = killerCage(new Position(1, 1), new Position(1, 2),
                                      new Position(2, 1), new Position(2, 2),
                                      new Position(3, 1));
        assertTrue(cage1.intersects(cage2));
        assertTrue(cage2.intersects(cage1));
        
        KillerCage cage3 = killerCage(                                      new Position(2, 5),
                new Position(3, 2), new Position(3, 3), new Position(3, 4), new Position(3, 5));
        assertFalse(cage1.intersects(cage3));
        assertFalse(cage3.intersects(cage1));
        assertFalse(cage2.intersects(cage3));
        assertFalse(cage3.intersects(cage2));
    }
    
    
    private static KillerCage killerCage(Position... positions) {
        return new KillerCage(ImmutableSet.copyOf(positions));
    }
}
