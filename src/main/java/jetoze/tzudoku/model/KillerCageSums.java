package jetoze.tzudoku.model;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableMap;

/**
 * The possible sums for killer cages of various sizes.
 */
public class KillerCageSums {

    private static final ImmutableMap<Integer, Integer> minSums = ImmutableMap.<Integer, Integer>builder()
            .put(2, 3)
            .put(3, 6)
            .put(4, 10)
            .put(5, 15)
            .put(6, 21)
            .put(7, 28)
            .put(8, 36)
            .build();
    
    private static final ImmutableMap<Integer, Integer> maxSums = ImmutableMap.<Integer, Integer>builder()
            .put(2, 17)
            .put(3, 24)
            .put(4, 30)
            .put(5, 35)
            .put(6, 39)
            .put(7, 42)
            .put(8, 44)
            .build();
    
    /**
     * Returns a modifiable list of the possible sums for a killer cage of the given size, in ascending order.
     */
    public static final List<Integer> getPossibleSums(int numberOfCells) {
        checkArgument(numberOfCells >= 2 && numberOfCells <= 9, "A killer cage must have 2-9 cells (input was %s)", numberOfCells);
        if (numberOfCells == 9) {
            List<Integer> list = new ArrayList<>();
            list.add(45);
            return list;
        }
        return IntStream.rangeClosed(minSums.get(numberOfCells), maxSums.get(numberOfCells))
                .mapToObj(Integer::valueOf)
                .collect(toList());
    }
    
    private KillerCageSums() {/**/}

}
