package jetoze.tzudoku.model;

import static com.google.common.base.Preconditions.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

public class Sandwiches {
    public static final Sandwiches EMPTY = new Sandwiches();
    
    private final ImmutableSet<Sandwich> rows;
    private final ImmutableSet<Sandwich> columns;
    
    private Sandwiches() {
        rows = ImmutableSet.of();
        columns = ImmutableSet.of();
    }
    
    public Sandwiches(Set<Sandwich> rows, Set<Sandwich> columns) {
        this.rows = ImmutableSet.copyOf(rows);
        this.columns = ImmutableSet.copyOf(columns);
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
        private final Set<Sandwich> rows = new HashSet<>();
        private final Set<Sandwich> columns = new HashSet<>();
        
        public Builder row(int row, int sum) {
            Sandwich s = new Sandwich(row, sum);
            checkArgument(!rows.contains(s), "A sandwich already exists for row %s", row);
            rows.add(s);
            return this;
        }
        
        public Builder column(int col, int sum) {
            Sandwich s = new Sandwich(col, sum);
            checkArgument(!columns.contains(s), "A sandwich already exists for column %s", col);
            columns.add(s);
            return this;
        }
        
        public Sandwiches build() {
            return (rows.isEmpty() && columns.isEmpty())
                    ? EMPTY
                    : new Sandwiches(rows, columns);
        }
    }
}
