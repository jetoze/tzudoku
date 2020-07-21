package jetoze.tzudoku.model;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

public class Thermometer {

    private final ImmutableList<Position> positions;
    
    public Thermometer(List<Position> positions) {
        validateInput(positions);
        this.positions = ImmutableList.copyOf(positions);
    }

    private static void validateInput(List<Position> positions) {
        checkArgument(positions.stream().allMatch(Objects::nonNull), 
                "The thermometer must not contain null positions (%s)", positions);
        checkArgument(positions.size() > 1 && positions.size() <= 9,
                "The thermometer must contain 2-9 positions (had %s)", positions.size());
        // Each position must be a King's move away from the previous one.
        for (int n = 1; n < positions.size(); ++n) {
            Position p1 = positions.get(n - 1);
            Position p2 = positions.get(n);
            int dx = Math.abs(p1.getRow() - p2.getRow());
            int dy = Math.abs(p1.getColumn() - p2.getColumn());
            checkArgument(dx <= 1 && dy <= 1, "Invalid gap between positions %s (%s) and %s (%s)",
                    n - 1, p1, n, p2);
        }
        // Cannot loop or cross itself
        HashSet<Position> unique = new HashSet<>();
        for (Position p : positions) {
            checkArgument(unique.add(p), "The thermometer must not contain positions (%s)", p);
        }
    }
    
    public ImmutableList<Position> getPositions() {
        return positions;
    }
    
}
