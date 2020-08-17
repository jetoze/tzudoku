package jetoze.tzudoku.ui;

import static java.util.Objects.*;

public class PuzzleBuilderException extends Exception {

    public PuzzleBuilderException(String message) {
        super(requireNonNull(message));
    }

    public PuzzleBuilderException(String message, Throwable cause) {
        super(requireNonNull(message), cause);
    }

}
