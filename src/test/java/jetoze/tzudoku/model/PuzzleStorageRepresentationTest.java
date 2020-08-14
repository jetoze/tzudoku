package jetoze.tzudoku.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

public class PuzzleStorageRepresentationTest {

    @Test
    public void deserializePuzzleWithSandwichesAndKillerCages() {
        // Arrange
        String puzzleName = "Test Puzzle";
        Grid grid = Grid.exampleOfUnsolvedGrid();
        Sandwiches sandwiches = Sandwiches.builder()
                .row(1, 12)
                .row(6, 0)
                .row(7, 35)
                .column(2, 9)
                .column(5, 19)
                .build();
        KillerCages killerCages = KillerCages.builder()
                .add(new KillerCage(ImmutableSet.of(new Position(2, 2), new Position(2, 3), new Position(2, 4))))
                .add(new KillerCage(ImmutableSet.of(new Position(4, 5), new Position(4, 6), new Position(5, 5)), 17))
                .build();
        Puzzle p1 = new Puzzle(puzzleName, grid, sandwiches, killerCages);

        // Act
        String json = new PuzzleStorageRepresentation(p1).toJson();
        Puzzle p2 = PuzzleStorageRepresentation.fromJson(json).restorePuzzle(puzzleName);
        
        // Assert
        assertEquals(puzzleName, p2.getName(), "Wrong name");
        assertTrue(grid.isEquivalent(p2.getGrid()), "Wrong grid");
        assertEquals(sandwiches, p2.getSandwiches(), "Wrong sandwiches");
        assertEquals(killerCages, p2.getKillerCages(), "Wrong killer cages");
    }

    @Test
    public void deserializePuzzleWithoutSandwichesOrKillerCages() {
        // Arrange
        String puzzleName = "Test Puzzle";
        Grid grid = Grid.exampleOfUnsolvedGrid();
        Puzzle p1 = new Puzzle(puzzleName, grid, Sandwiches.EMPTY, KillerCages.EMPTY);

        // Act
        String json = new PuzzleStorageRepresentation(p1).toJson();
        Puzzle p2 = PuzzleStorageRepresentation.fromJson(json).restorePuzzle(puzzleName);
        
        // Assert
        assertEquals(puzzleName, p2.getName(), "Wrong name");
        assertTrue(grid.isEquivalent(p2.getGrid()), "Wrong grid");
        assertEquals(Sandwiches.EMPTY, p2.getSandwiches(), "Wrong sandwiches");
    }
    
    @Test
    public void deserializePuzzleWithPencilMarksAndColors() {
        // Arrange
        String puzzleName = "Test Puzzle";
        Grid grid = Grid.exampleOfUnsolvedGrid();
        List<Cell> cells = grid.getCells().values().stream()
                .filter(Predicate.not(Cell::isGiven))
                .limit(4)
                .collect(Collectors.toList());
        cells.get(0).getCornerMarks()
                .toggle(Value.ONE)
                .toggle(Value.TWO);
        cells.get(1).getCenterMarks()
            .toggle(Value.THREE)
            .toggle(Value.FOUR);
        cells.get(2).getCornerMarks()
            .toggle(Value.FIVE);
        cells.get(2).getCenterMarks()
            .toggle(Value.FOUR)
            .toggle(Value.FIVE)
            .toggle(Value.SIX);
        cells.get(2).setColor(CellColor.BLUE);
        cells.get(3).setColor(CellColor.ORANGE);
        Puzzle p1 = new Puzzle(puzzleName, grid, Sandwiches.EMPTY, KillerCages.EMPTY);

        // Act
        String json = new PuzzleStorageRepresentation(p1).toJson();
        Puzzle p2 = PuzzleStorageRepresentation.fromJson(json).restorePuzzle(puzzleName);
        
        // Assert
        assertEquals(puzzleName, p2.getName(), "Wrong name");
        assertTrue(grid.isEquivalent(p2.getGrid()), "Wrong grid");
        assertEquals(Sandwiches.EMPTY, p2.getSandwiches(), "Wrong sandwiches");
    }
    
}
