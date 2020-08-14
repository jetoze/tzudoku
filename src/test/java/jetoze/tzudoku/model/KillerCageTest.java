package jetoze.tzudoku.model;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.function.Predicate;

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
        
        ImmutableSet<Position> leftBoundary = cage.getPositions().stream()
                .filter(Predicate.not(cage::hasCellToTheLeft))
                .collect(toImmutableSet());
        assertEquals(ImmutableSet.of(new Position(2, 4), new Position(3, 3), new Position(4, 5), new Position(5, 5)),
                leftBoundary);
        ImmutableSet<Position> rightBoundary = cage.getPositions().stream()
                .filter(Predicate.not(cage::hasCellToTheRight))
                .collect(toImmutableSet());
        assertEquals(ImmutableSet.of(new Position(2, 4), new Position(3, 6), new Position(4, 6), new Position(5, 5)),
                rightBoundary);
        ImmutableSet<Position> topBoundary = cage.getPositions().stream()
                .filter(Predicate.not(cage::hasCellAbove))
                .collect(toImmutableSet());
        assertEquals(ImmutableSet.of(new Position(2, 4), new Position(3, 3), new Position(3, 5), new Position(3, 6)),
                topBoundary);
        ImmutableSet<Position> lowerBoundary = cage.getPositions().stream()
                .filter(Predicate.not(cage::hasCellBelow))
                .collect(toImmutableSet());
        assertEquals(ImmutableSet.of(new Position(3, 3), new Position(3, 4), new Position(5, 5), new Position(4, 6)),
                lowerBoundary);
        
        cage = killerCage(new Position(1, 1), new Position(1, 2),
                          new Position(2, 1),
                          new Position(3, 1), new Position(3, 2),
                          new Position(4, 1),
                          new Position(5, 1), new Position(5, 2));
        leftBoundary = cage.getPositions().stream()
                .filter(Predicate.not(cage::hasCellToTheLeft))
                .collect(toImmutableSet());
        assertEquals(ImmutableSet.of(new Position(1, 1), new Position(2, 1), new Position(3, 1), new Position(4, 1), new Position(5, 1)),
                leftBoundary);
        rightBoundary = cage.getPositions().stream()
                .filter(Predicate.not(cage::hasCellToTheRight))
                .collect(toImmutableSet());
        assertEquals(ImmutableSet.of(new Position(1, 2), new Position(2, 1), new Position(3, 2), new Position(4, 1), new Position(5, 2)),
                rightBoundary);
        topBoundary = cage.getPositions().stream()
                .filter(Predicate.not(cage::hasCellAbove))
                .collect(toImmutableSet());
        assertEquals(ImmutableSet.of(new Position(1, 1), new Position(1, 2), new Position(3, 2), new Position(5, 2)),
                topBoundary);
        lowerBoundary = cage.getPositions().stream()
                .filter(Predicate.not(cage::hasCellBelow))
                .collect(toImmutableSet());
        assertEquals(ImmutableSet.of(new Position(1, 2), new Position(3, 2), new Position(5, 1), new Position(5, 2)),
                lowerBoundary);
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
