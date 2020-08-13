package jetoze.tzudoku.hint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.hint.SimpleColoring.Color;
import jetoze.tzudoku.hint.SimpleColoring.TooCrowdedHouse;
import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.House;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public class SimpleColoringTest {

    @Test
    public void seesBothColors_1() {
        Grid grid = GridBuilder.builder()
                .row(1, "[38]6[27] 945 [78][1378][178]")
                .row(2, "154 378 692")
                .row(3, "7[38]9 162 4[358][58]")
                .row(4, "62[57] 831 [57]49")
                .row(5, "[89][789]3 456 2[178][178]")
                .row(6, "41[58] 297 [58]63")
                .row(7, "5[78]1 623 9[78][47]")
                .row(8, "2[47][68] 719 3[58][568]")
                .row(9, "[39][39][67] 584 1[27][67]")
                .build();
        Optional<SimpleColoring> opt = SimpleColoring.analyze(grid);
        
        assertTrue(opt.isPresent());
        SimpleColoring hint = opt.get();
        
        assertSame(Value.EIGHT, hint.getValue());
        
        assertEquals(ImmutableSet.of(new Position(1, 8), new Position(3, 8)), hint.getCellsToEliminate());

        assertFalse(hint.getHouseTooCrowded().isPresent());
        assertTrue(hint.getCellsThatCanBePenciledIn().isEmpty());
        
        hint.apply();
        // Verify that the value was eliminated from the target cells
        assertEquals(ImmutableSet.of(Value.ONE, Value.THREE, Value.SEVEN), 
                grid.cellAt(new Position(1, 8)).getCenterMarks().getValues());
        assertEquals(ImmutableSet.of(Value.THREE, Value.FIVE), 
                grid.cellAt(new Position(3, 8)).getCenterMarks().getValues());
        // Verify that the value is still a candidate in all the blue and orange cells.
        assertTrue(Stream.of(Color.values()).flatMap(c -> hint.getCellsOfColor(c).stream())
                .map(grid::cellAt)
                .map(Cell::getCenterMarks)
                .allMatch(pm -> pm.contains(Value.EIGHT)));
    }


    @Test
    public void seesBothColors_2() {
        Grid grid = GridBuilder.builder()
                .row(1, "[38]6[27] 945 [78][137][178]")
                .row(2, "154 378 692")
                .row(3, "7[38]9 162 4[35][58]")
                .row(4, "62[57] 831 [57]49")
                .row(5, "[89][789]3 456 2[178][178]")
                .row(6, "41[58] 297 [58]63")
                .row(7, "5[78]1 623 9[78][47]")
                .row(8, "2[47][68] 719 3[58][568]")
                .row(9, "[39][39][67] 584 1[27][67]")
                .build();
        Optional<SimpleColoring> opt = SimpleColoring.analyze(grid);
        
        assertTrue(opt.isPresent());
        SimpleColoring hint = opt.get();
        
        assertSame(Value.EIGHT, hint.getValue());
        
        assertEquals(ImmutableSet.of(new Position(5, 9)), hint.getCellsToEliminate());

        assertFalse(hint.getHouseTooCrowded().isPresent());
        assertTrue(hint.getCellsThatCanBePenciledIn().isEmpty());
        
        hint.apply();
        // Verify that the value was eliminated from the target cells
        assertEquals(ImmutableSet.of(Value.ONE, Value.SEVEN), 
                grid.cellAt(new Position(5, 9)).getCenterMarks().getValues());
        // Verify that the value is still a candidate in all the blue and orange cells.
        assertTrue(Stream.of(Color.values()).flatMap(c -> hint.getCellsOfColor(c).stream())
                .map(grid::cellAt)
                .map(Cell::getCenterMarks)
                .allMatch(pm -> pm.contains(Value.EIGHT)));
    }
    
    @Test
    public void tooCrowdedHouse_1() {
        Grid grid = GridBuilder.builder()
                .row(1, "289 [146][46][14] 3[47]5")
                .row(2, "364 [57]9[57] 812")
                .row(3, "517 283 964")
                .row(4, "893 [457]2[457] 6[45]1")
                .row(5, "145 836 729")
                .row(6, "726 [19][45][19] [45]8[34]")
                .row(7, "451 378 296")
                .row(8, "[69]72 [4569]1[459] [45]38")
                .row(9, "[69]38 [4569][456][2] 1[45]7")
                .build();
        Optional<SimpleColoring> opt = SimpleColoring.analyze(grid);
        assertTrue(opt.isPresent());
        SimpleColoring hint = opt.get();

        assertSame(Value.FIVE, hint.getValue());
        
        assertEquals(ImmutableSet.of(new Position(6, 7), new Position(9, 5), new Position(9, 8)), hint.getCellsToEliminate());
        
        assertTrue(hint.getHouseTooCrowded().isPresent());
        TooCrowdedHouse tooCrowdedHouse = hint.getHouseTooCrowded().get();
        assertEquals(House.row(9), tooCrowdedHouse.getHouse());
        
        assertEquals(ImmutableSet.of(new Position(4, 8), new Position(6, 5), new Position(8, 7)), hint.getCellsThatCanBePenciledIn());
        
        hint.apply();
        // Verify that the value was entered into the cells of the opposite color
        assertTrue(hint.getCellsThatCanBePenciledIn().stream().map(grid::cellAt)
                .allMatch(cell -> cell.hasValue() && cell.getValue().get() == Value.FIVE));
        // Verify that the value was eliminated from the other cells
        assertTrue(hint.getCellsToEliminate().stream().map(grid::cellAt)
                .map(Cell::getCenterMarks)
                .noneMatch(pm -> pm.contains(Value.FIVE)));
    }

}
