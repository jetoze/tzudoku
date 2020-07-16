package jetoze.tzudoku.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class PuzzleStorageRepresentationTest {

    @Test
    public void deserializePuzzleWithSandwiches() {
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
        Puzzle p1 = new Puzzle(puzzleName, grid, sandwiches);

        // Act
        String json = new PuzzleStorageRepresentation(p1).toJson();
        Puzzle p2 = PuzzleStorageRepresentation.fromJson(json).restorePuzzle(puzzleName);
        
        // Assert
        assertEquals(puzzleName, p2.getName(), "Wrong name");
        assertTrue(grid.isEquivalent(p2.getGrid()), "Wrong grid");
        assertEquals(sandwiches, p2.getSandwiches(), "Wrong sandwiches");
    }

    @Test
    public void deserializePuzzleWithoutSandwiches() {
        // Arrange
        String puzzleName = "Test Puzzle";
        Grid grid = Grid.exampleOfUnsolvedGrid();
        Puzzle p1 = new Puzzle(puzzleName, grid, Sandwiches.EMPTY);

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
        cells.get(0).getPencilMarks()
                .toggleCorner(Value.ONE)
                .toggleCorner(Value.TWO);
        cells.get(1).getPencilMarks()
            .toggleCenter(Value.THREE)
            .toggleCenter(Value.FOUR);
        cells.get(2).getPencilMarks()
            .toggleCorner(Value.FIVE)
            .toggleCenter(Value.FOUR)
            .toggleCenter(Value.FIVE)
            .toggleCenter(Value.SIX);
        cells.get(2).setColor(CellColor.BLUE);
        cells.get(3).setColor(CellColor.ORANGE);
        Puzzle p1 = new Puzzle(puzzleName, grid, Sandwiches.EMPTY);

        // Act
        String json = new PuzzleStorageRepresentation(p1).toJson();
        Puzzle p2 = PuzzleStorageRepresentation.fromJson(json).restorePuzzle(puzzleName);
        
        // Assert
        assertEquals(puzzleName, p2.getName(), "Wrong name");
        assertTrue(grid.isEquivalent(p2.getGrid()), "Wrong grid");
        assertEquals(Sandwiches.EMPTY, p2.getSandwiches(), "Wrong sandwiches");
    }
    
}
