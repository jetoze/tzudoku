package jetoze.tzudoku.model;

import static java.util.Objects.requireNonNull;
import static tzeth.preconds.MorePreconditions.checkNotBlank;

public class Puzzle {
    
    public static final Puzzle EMPTY = new Puzzle("[Empty]", Grid.emptyGrid());
    
    // TODO: Thermos, killer cages, etc.
    private final String name;
    private final Grid grid;
    private final Sandwiches sandwiches;
    
    public Puzzle(String name, Grid grid) {
        this(name, grid, Sandwiches.EMPTY);
    }
    
    public Puzzle(String name, Grid grid, Sandwiches sandwiches) {
        this.name = checkNotBlank(name);
        this.grid = requireNonNull(grid);
        this.sandwiches = requireNonNull(sandwiches);
    }

    public String getName() {
        return name;
    }

    public Grid getGrid() {
        return grid;
    }
    
    public Sandwiches getSandwiches() {
        return sandwiches;
    }
    
    public boolean isSolved() {
        return grid.isSolved();
    }
    
    public boolean isEmpty() {
        // TODO: Check things like killer cages, thermos, etc, hee.
        return grid.isEmpty() && sandwiches.isEmpty();
    }
}
