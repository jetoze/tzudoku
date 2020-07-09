package jetoze.tzudoku;

import java.util.Optional;

import com.google.common.collect.ImmutableSet;

public interface Cell {

	Optional<Value> getValue();
	
	boolean hasValue();
	
	ImmutableSet<Value> getCenterPencilMarks();
	
	ImmutableSet<Value> getCornerPencilMarks();
	
	boolean isGiven();
	
	// TODO: Should we add properties for background color and selected = true/false?
	// If so, do we make this an abstract class instead of an interface?
	// A reason not to do it is that these are UI-specific properties. Perhaps better to
	// store this information elsewhere?
	// Question: Is the same true for the pencil marks then?
	
}
