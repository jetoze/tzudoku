package jetoze.tzudoku.hint;

public interface Hint {

    /**
     * Returns the solving technique that produced this hint.
     */
    SolvingTechnique getTechnique();

    /**
     * Applies this hint to the Grid in which it was found.
     */
    void apply();
    
}
