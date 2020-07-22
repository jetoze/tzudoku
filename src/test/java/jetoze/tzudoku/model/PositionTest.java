package jetoze.tzudoku.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PositionTest {

    @Test
    public void testBoxForPosition() {
        Position p = new Position(1, 1);
        assertEquals(1, p.getBox());
        
        p = new Position(3, 3);
        assertEquals(1, p.getBox());
        
        p = new Position(1, 6);
        assertEquals(2, p.getBox());
        
        p = new Position(2, 8);
        assertEquals(3, p.getBox());
        
        p = new Position(4, 1);
        assertEquals(4, p.getBox());
        
        p = new Position(5, 6);
        assertEquals(5, p.getBox());
        
        p = new Position(6, 7);
        assertEquals(6, p.getBox());
        
        p = new Position(7, 1);
        assertEquals(7, p.getBox());
        
        p = new Position(7, 4);
        assertEquals(8, p.getBox());
        
        p = new Position(7, 7);
        assertEquals(9, p.getBox());
        
        p = new Position(9, 9);
        assertEquals(9, p.getBox());
    }
    
    
}
