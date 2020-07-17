package jetoze.tzudoku.ui;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Comparators;

import jetoze.attribut.ListProperty;
import jetoze.attribut.Properties;
import jetoze.attribut.Property;
import jetoze.tzudoku.PuzzleInventory;
import jetoze.tzudoku.model.PuzzleInfo;
import jetoze.tzudoku.model.PuzzleState;

public class InventoryUiModel {
    
    public static enum SortOrder {
        ALPHABETICAL(Comparator.comparing(PuzzleInfo::getName)),
        LAST_UPDATED(Comparator.comparing(PuzzleInfo::lastUpdated,
                Comparators.emptiesLast(Comparator.reverseOrder())));
        
        private final Comparator<PuzzleInfo> comparator;
        
        private SortOrder(Comparator<PuzzleInfo> comparator) {
            this.comparator = comparator;
        }
        
        private void sort(List<PuzzleInfo> puzzles) {
            puzzles.sort(comparator);
        }
        
        private void sort(ListProperty<PuzzleInfo> puzzles) {
            puzzles.sort(comparator);
        }
    }
    
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    
    private final ListProperty<PuzzleInfo> puzzleInfos;
    
    private final Property<Boolean> hideCompletedPuzzles = Properties.newProperty(
            "hideCompletedPuzzles", Boolean.TRUE, changeSupport);
    
    private final Property<SortOrder> sortOrder = Properties.newProperty(
            "sortOrder", SortOrder.LAST_UPDATED, changeSupport);
    
    private final Property<Predicate<PuzzleInfo>> listFilter =
            Properties.newProperty("listFilter", p -> true, changeSupport);
    
    public InventoryUiModel(PuzzleInventory inventory) {
        List<PuzzleInfo> sortedList = new ArrayList<>(inventory.listPuzzles());
        SortOrder.LAST_UPDATED.sort(sortedList);
        this.puzzleInfos = Properties.newListProperty("puzzles", sortedList, changeSupport);
        this.puzzleInfos.setDoDefensiveCopy(false);
        installInternalListeners();
    }

    private void installInternalListeners() {
        this.sortOrder.addListener(e -> {
            SortOrder sortOrder = (SortOrder) e.getNewValue();
            sortOrder.sort(puzzleInfos);
        });
        this.hideCompletedPuzzles.addListener(e -> {
            boolean hide = (Boolean) e.getNewValue();
            Predicate<PuzzleInfo> predicate = hide
                    ? p -> p.getState() != PuzzleState.SOLVED
                    : p -> true;
            listFilter.set(predicate);
        });
    }
    
    public ListProperty<PuzzleInfo> getListItems() {
        return puzzleInfos;
    }
    
    public Property<Predicate<PuzzleInfo>> getListFilter() {
        return listFilter;
    }
    
    public Property<Boolean> getHideCompletedPuzzles() {
        return hideCompletedPuzzles;
    }
    
    public Property<SortOrder> getSortOrder() {
        return sortOrder;
    }
    
}
