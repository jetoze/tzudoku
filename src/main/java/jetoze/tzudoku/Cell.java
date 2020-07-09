package jetoze.tzudoku;

import java.util.Optional;

public interface Cell {

	Optional<Value> getValue();
	
	boolean hasValue();
		
	boolean isGiven();
	
}
