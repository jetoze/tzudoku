package jetoze.tzudoku.model;

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
}
