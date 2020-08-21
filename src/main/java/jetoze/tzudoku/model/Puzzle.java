package jetoze.tzudoku.model;

import static java.util.Objects.requireNonNull;

import com.google.common.base.CharMatcher;

import jetoze.tzudoku.constraint.KillerCages;
import jetoze.tzudoku.constraint.Sandwiches;

import static com.google.common.base.Preconditions.*;

public class Puzzle {
    
    private static final CharMatcher NAME_CHAR_MATCHER = CharMatcher.inRange('a', 'z')
        .or(CharMatcher.inRange('A', 'Z'))
        .or(CharMatcher.inRange('0', '9'))
        .or(CharMatcher.anyOf(" _-()."));

    public static final Puzzle EMPTY = new Puzzle("(Empty)", Grid.emptyGrid());
    
    private final String name;
    private final Grid grid;
    private final Sandwiches sandwiches;
    private final KillerCages killerCages;
    // TODO: Thermos, when we have them.
    
    public Puzzle(String name, Grid grid) {
        this(name, grid, Sandwiches.EMPTY, KillerCages.EMPTY);
    }
    
    public Puzzle(String name, Grid grid, Sandwiches sandwiches, KillerCages killerCages) {
        this.name = validateName(name);
        this.grid = requireNonNull(grid);
        this.sandwiches = requireNonNull(sandwiches);
        this.killerCages = requireNonNull(killerCages);
    }
    
    private static String validateName(String name) {
        checkArgument(!name.isBlank(), "Name cannot be blank");
        // TODO: Error message should say what characters are valid
        checkArgument(NAME_CHAR_MATCHER.matchesAllOf(name), "Invalid puzzle name character: %s", name);
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
    
    public KillerCages getKillerCages() {
        return killerCages;
    }

    public boolean isSolved() {
        return grid.isSolved();
    }
    
    public boolean isEmpty() {
        // TODO: Thermos, when we have them.
        return grid.isEmpty() && sandwiches.isEmpty() && killerCages.isEmpty();
    }
}
