package jetoze.tzudoku.ui.hint;

import static java.util.stream.Collectors.joining;

import java.util.Collection;

import jetoze.tzudoku.model.House;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;

class HintUiUtils {

    static String valuesInOrder(Collection<Value> values) {
        return values.stream().sorted().map(Object::toString).collect(joining(" "));
    }
    
    static String positions(Collection<Position> positions) {
        return positions.stream()
                .map(Object::toString)
                .collect(joining(" "));
    }
    
    static String positionsInOrder(Collection<Position> positions, House house) {
        return positions.stream()
                .sorted(house.getType().positionOrder())
                .map(Object::toString)
                .collect(joining(" "));
    }
    
    private HintUiUtils() {/**/}

}
