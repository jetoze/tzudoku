package jetoze.tzudoku.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

public class HouseTest {

    @Test
    public void testIfInRowOrColumn() {
        // Happy paths
        assertEquals(House.row(7), House.ifInRowOrColumn(
                ImmutableSet.of(new Position(7, 7), new Position(7, 3))).get());
        assertEquals(House.row(5), House.ifInRowOrColumn(
                ImmutableSet.of(new Position(5, 1), new Position(5, 2), new Position(5, 4), new Position(5, 8))).get());
        assertEquals(House.column(1), House.ifInRowOrColumn(
                ImmutableSet.of(new Position(7, 1), new Position(3, 1))).get());
        assertEquals(House.column(4), House.ifInRowOrColumn(
                ImmutableSet.of(new Position(5, 4), new Position(6, 4), new Position(7, 4), new Position(2, 4))).get());
        
        // Unhappy paths
        assertFalse(House.ifInRowOrColumn(ImmutableSet.of()).isPresent());
        assertFalse(House.ifInRowOrColumn(ImmutableSet.of(new Position(2, 2))).isPresent());
        assertFalse(House.ifInRowOrColumn(ImmutableSet.of(
                new Position(1, 1), new Position(1, 2), new Position(1, 3), new Position(1, 4), new Position(2, 2))).isPresent());

    }
    
    @Test
    public void testIfInBox() {
        // Happy paths
        assertEquals(House.box(3), House.ifInBox(
                ImmutableSet.of(new Position(1, 7), new Position(2, 8), new Position(3, 9))).get());
        assertEquals(House.box(1), House.ifInBox(
                ImmutableSet.of(new Position(1, 1), new Position(1, 2), new Position(1, 3), new Position(3, 2))).get());
        
        
        // Unhappy paths
        assertFalse(House.ifInBox(ImmutableSet.of()).isPresent());
        assertFalse(House.ifInBox(ImmutableSet.of(new Position(2, 2))).isPresent());
        assertFalse(House.ifInBox(ImmutableSet.of(
                new Position(1, 1), new Position(1, 2), new Position(1, 3), new Position(1, 4))).isPresent());
    }

}
