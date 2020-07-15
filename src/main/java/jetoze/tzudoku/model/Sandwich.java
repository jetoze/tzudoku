package jetoze.tzudoku.model;

import static tzeth.preconds.MorePreconditions.*;

import java.util.Objects;

import javax.annotation.Nullable;

public class Sandwich {
    // TODO: Is "position" a confusing name for this property, seeing how
    // it represents a row or a column, and we have a Position class representing
    // a row and a column.
    private final int position;
    private final int sum;
    
    public Sandwich(int position, int sum) {
        this.position = checkInRange(position, 1, 9);
        this.sum = checkInRange(sum, 0, 35);
    }

    public int getPosition() {
        return position;
    }

    public int getSum() {
        return sum;
    }
    
    @Override
    public String toString() {
        return String.format("Position: %d, Sum: %d", position, sum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, sum);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Sandwich) {
            Sandwich that = (Sandwich) obj;
            return (this.position == that.position) && (this.sum == that.sum);
        }
        return false;
    }
}
