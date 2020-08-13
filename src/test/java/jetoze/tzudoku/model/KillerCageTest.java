package jetoze.tzudoku.model;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    
    
    private static KillerCage killerCage(Position... positions) {
        return new KillerCage(ImmutableSet.copyOf(positions));
    }
}
