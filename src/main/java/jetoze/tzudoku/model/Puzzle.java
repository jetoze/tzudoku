package jetoze.tzudoku.model;

import static java.util.Objects.requireNonNull;

import com.google.common.base.CharMatcher;

import static com.google.common.base.Preconditions.*;

public class Puzzle {
    
    private static final CharMatcher NAME_CHAR_MATCHER = CharMatcher.inRange('a', 'z')
        .or(CharMatcher.inRange('A', 'Z'))
        .or(CharMatcher.inRange('0', '9'))
        .or(CharMatcher.is('_'))
        .or(CharMatcher.is('-'));

    public static final Puzzle EMPTY = new Puzzle("[Empty]", Grid.emptyGrid());
    
    // TODO: Thermos, killer cages, etc.
    private final String name;
    private final Grid grid;
    private final Sandwiches sandwiches;
    
    public Puzzle(String name, Grid grid) {
        this(name, grid, Sandwiches.EMPTY);
    }
    
    public Puzzle(String name, Grid grid, Sandwiches sandwiches) {
        this.name = validateName(name);
        this.grid = requireNonNull(grid);
        this.sandwiches = requireNonNull(sandwiches);
    }
    
    private static String validateName(String name) {
        checkArgument(!name.isBlank(), "Name cannot be blank");
        // TODO: Error message should say what characters are valid
        checkArgument(NAME_CHAR_MATCHER.matchesAllOf(name), "Invalid puzzle name character");
        return name;
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
