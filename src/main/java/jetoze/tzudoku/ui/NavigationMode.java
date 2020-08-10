package jetoze.tzudoku.ui;

import jetoze.tzudoku.model.Position;

/**
 * Defines the behavior when navigating left, right, up, or down from a given cell, in particular
 * when the given cell is on the edge of the grid.
 * <p>
 * TODO: Fill me in.
 */
public enum NavigationMode {

    WRAP_AROUND {

        @Override
        protected Position leftOfFirstColumn(Position p) {
            return new Position(p.getRow(), 9);
        }

        @Override
        protected Position rightOfLastColumn(Position p) {
            return new Position(p.getRow(), 1);
        }

        @Override
        protected Position upFromFirstRow(Position p) {
            return new Position(9, p.getColumn());
        }

        @Override
        protected Position downFromLastRow(Position p) {
            return new Position(1, p.getColumn());
        }
    },
    
    TRAVERSE {

        @Override
        protected Position leftOfFirstColumn(Position p) {
            int row = p.getRow() > 1
                    ? p.getRow() - 1
                    : 9;
            int col = 9;
            return new Position(row, col);
        }

        @Override
        protected Position rightOfLastColumn(Position p) {
            int row = p.getRow() < 9
                    ? p.getRow() + 1
                    : 1;
            int col = 1;
            return new Position(row, col);
        }

        @Override
        protected Position upFromFirstRow(Position p) {
            int row = 9;
            int col = p.getColumn() > 1
                    ? p.getColumn() - 1
                    : 9;
            return new Position(row, col);
        }

        @Override
        protected Position downFromLastRow(Position p) {
            int row = 1;
            int col = p.getColumn() < 9
                    ? p.getColumn() + 1
                    : 1;
            return new Position(row, col);
        }
    };
    
    public final Position left(Position p) {
        int col = p.getColumn();
        return col > 1
                ? new Position(p.getRow(), col - 1)
                : leftOfFirstColumn(p);
    }
    
    protected abstract Position leftOfFirstColumn(Position p);
    
    public final Position right(Position p) {
        int col = p.getColumn();
        return col < 9
                ? new Position(p.getRow(), col + 1)
                : rightOfLastColumn(p);
    }
    
    protected abstract Position rightOfLastColumn(Position p);
    
    public final Position up(Position p) {
        int row = p.getRow();
        return row > 1
                ? new Position(row - 1, p.getColumn())
                : upFromFirstRow(p);
    }
   
    protected abstract Position upFromFirstRow(Position p);

    public final Position down(Position p) {
        int row = p.getRow();
        return row < 9
                ? new Position(row + 1, p.getColumn())
                : downFromLastRow(p);
    }
    
    protected abstract Position downFromLastRow(Position p);

    
}
