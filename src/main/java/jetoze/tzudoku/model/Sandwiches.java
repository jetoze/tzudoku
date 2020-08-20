package jetoze.tzudoku.model;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

public class Sandwiches implements Constraint {
    public static final Sandwiches EMPTY = new Sandwiches();
    
    private final ImmutableSet<Sandwich> rows;
    private final ImmutableSet<Sandwich> columns;
    
    private Sandwiches() {
        rows = ImmutableSet.of();
        columns = ImmutableSet.of();
    }
    
    public Sandwiches(Collection<Sandwich> rows, Collection<Sandwich> columns) {
        checkNoDuplicatePositions(rows, "row sandwiches");
        checkNoDuplicatePositions(columns, "column sandwiches");
        this.rows = ImmutableSet.copyOf(rows);
        this.columns = ImmutableSet.copyOf(columns);
    }

    private static void checkNoDuplicatePositions(Collection<Sandwich> c, String rowOrColumn) {
        Set<Integer> positions = new HashSet<>();
        for (Sandwich s : c) {
            checkArgument(positions.add(s.getHouse().getNumber()), "Duplicate position in %s", rowOrColumn, 
                    s.getHouse().getNumber());
        }
    }
    
    public ImmutableSet<Sandwich> getRows() {
        return rows;
    }

    public ImmutableSet<Sandwich> getColumns() {
        return columns;
    }
    
    public boolean isEmpty() {
        return rows.isEmpty() && columns.isEmpty();
    }
    
    @Override
    public ImmutableSet<Position> validate(Grid grid) {
        return Constraint.validateAll(grid, Stream.concat(rows.stream(), columns.stream()));
    }

    public String toString() {
        return String.format("Rows: %s. Columns: %s", rows, columns);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(rows, columns);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Sandwiches) {
            Sandwiches that = (Sandwiches) obj;
            return this.rows.equals(that.rows) && this.columns.equals(that.columns);
        }
        return false;
    }

    public static Builder builder() {
        return new Builder();
    }
    
    
    public static class Builder {
        private final Map<Integer, Sandwich> rows = new TreeMap<>();
        private final Map<Integer, Sandwich> columns = new TreeMap<>();
        
        public Builder row(int row, int sum) {
            checkArgument(!rows.containsKey(row), "A sandwich already exists for row %s", row);
            Sandwich s = new Sandwich(House.row(row), sum);
            rows.put(row, s);
            return this;
        }
        
        public Builder column(int col, int sum) {
            checkArgument(!columns.containsKey(col), "A sandwich already exists for column %s", col);
            Sandwich s = new Sandwich(House.column(col), sum);
            columns.put(col, s);
            return this;
        }
        
        public Sandwiches build() {
            return (rows.isEmpty() && columns.isEmpty())
                    ? EMPTY
                    : new Sandwiches(rows.values(), columns.values());
        }
    }
}
