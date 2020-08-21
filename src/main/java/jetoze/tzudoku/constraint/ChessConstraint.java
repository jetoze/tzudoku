package jetoze.tzudoku.constraint;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;

import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

public enum ChessConstraint implements Constraint {

    @SerializedName("ki")
    KINGS_MOVE {

        @Override
        protected ImmutableSet<Position> reachableFrom(Position p0) {
            // TODO: Should we consider positions in the same row and column?
            // Technically they are a knights move away, but they are also
            // already covered by the classic sudoku constraint. Hmmm.
            Set<Integer> rows = reachableRowOrColumnNumbers(p0.getRow());
            Set<Integer> columns = reachableRowOrColumnNumbers(p0.getColumn());
            return Sets.cartesianProduct(rows, columns).stream()
                    .map(list -> new Position(list.get(0), list.get(1)))
                    .filter(p -> !p.equals(p0))
                    .collect(toImmutableSet());
        }
        
        private Set<Integer> reachableRowOrColumnNumbers(int start) {
            Set<Integer> nums = new HashSet<>();
            nums.add(start);
            if (start > 1) {
                nums.add(start - 1);
            }
            if (start < 9) {
                nums.add(start + 1);
            }
            return nums;
        }
    },
    
    @SerializedName("kn")
    KNIGHTS_MOVE {

        @Override
        protected ImmutableSet<Position> reachableFrom(Position p) {
            // TODO: Can I be done in a more elegant way?
            ImmutableSet.Builder<Position> builder = ImmutableSet.builder();
            if (p.getRow() > 2) {
                if (p.getColumn() > 1) {
                    builder.add(new Position(p.getRow() - 2, p.getColumn() - 1));
                }
                if (p.getColumn() < 9) {
                    builder.add(new Position(p.getRow() - 2, p.getColumn() + 1));
                }
            }
            if (p.getRow() > 1) {
                if (p.getColumn() > 2) {
                    builder.add(new Position(p.getRow() - 1, p.getColumn() - 2));
                }
                if (p.getColumn() < 8) {
                    builder.add(new Position(p.getRow() - 1, p.getColumn() + 2));
                }
            }
            if (p.getRow() < 9) {
                if (p.getColumn() > 2) {
                    builder.add(new Position(p.getRow() + 1, p.getColumn() - 2));
                }
                if (p.getColumn() < 8) {
                    builder.add(new Position(p.getRow() + 1, p.getColumn() + 2));
                }
            }
            if (p.getRow() < 8) {
                if (p.getColumn() > 1) {
                    builder.add(new Position(p.getRow() + 2, p.getColumn() - 1));
                }
                if (p.getColumn() < 9) {
                    builder.add(new Position(p.getRow() + 2, p.getColumn() + 1));
                }
            }
            return builder.build();
        }
    };
    
    @Override
    public final ImmutableSet<Position> validate(Grid grid) {
        return Position.all()
                .flatMap(p -> check(grid, p))
                .collect(toImmutableSet());
    }
    
    private Stream<Position> check(Grid grid, Position p) {
        Cell cell = grid.cellAt(p);
        return cell.getValue()
                .map(digit -> check(grid, p, digit))
                .orElse(Stream.empty());
    }
    
    private Stream<Position> check(Grid grid, Position p0, Value digit) {
        return reachableFrom(p0).stream()
                .filter(p -> grid.cellAt(p).hasValue(digit));
    }

    // TODO: I'd like this to return a Stream. Is that going to be convenient for the Knights move constraint?
    protected abstract ImmutableSet<Position> reachableFrom(Position p);
    
}
