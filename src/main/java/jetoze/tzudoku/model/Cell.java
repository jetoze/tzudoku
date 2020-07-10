package jetoze.tzudoku.model;

import java.util.Optional;

public interface Cell {

    Optional<Value> getValue();

    boolean hasValue();

    boolean isGiven();

}
