package jetoze.tzudoku.model;

import static tzeth.preconds.MorePreconditions.*;
import static java.util.Objects.*;

public class Puzzle {
    // TODO: Sandwiches, thermos, killer cages, etc.
    private final String name;
    private final Grid grid;
    
    public Puzzle(String name, Grid grid) {
        this.name = checkNotBlank(name);
        this.grid = requireNonNull(grid);
    }

    public String getName() {
        return name;
    }

    public Grid getGrid() {
        return grid;
    }
}
