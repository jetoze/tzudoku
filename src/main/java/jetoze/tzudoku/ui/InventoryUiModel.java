package jetoze.tzudoku.ui;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

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
    
    private static final Predicate<PuzzleInfo> UNSOLVED_PUZZLES = p -> p.getState() != PuzzleState.SOLVED;
    
    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    
    private final ListProperty<PuzzleInfo> puzzleInfos;
    private final Property<PuzzleInfo> selectedPuzzle = Properties.newNullableProperty("selectedPuzzleInfo");
    
    private final Property<Boolean> showCompletedPuzzles = Properties.newProperty(
            "showCompletedPuzzles", Boolean.FALSE, changeSupport);
    
    private final Property<SortOrder> sortOrder = Properties.newProperty(
            "sortOrder", SortOrder.LAST_UPDATED, changeSupport);
    
    private final Property<Predicate<PuzzleInfo>> listFilter =
            Properties.newProperty("listFilter", UNSOLVED_PUZZLES, changeSupport);
    
    public InventoryUiModel(PuzzleInventory inventory) {
        List<PuzzleInfo> sortedList = new ArrayList<>(inventory.listPuzzles());
        SortOrder.LAST_UPDATED.sort(sortedList);
        this.puzzleInfos = Properties.newListProperty("puzzles", sortedList, changeSupport);
        installInternalListeners();
    }

    private void installInternalListeners() {
        this.sortOrder.addListener(e -> {
            SortOrder sortOrder = (SortOrder) e.getNewValue();
            sortOrder.sort(puzzleInfos);
        });
        this.showCompletedPuzzles.addListener(e -> {
            boolean show = (Boolean) e.getNewValue();
            Predicate<PuzzleInfo> predicate = show
                    ? p -> true
                    : UNSOLVED_PUZZLES;
            listFilter.set(predicate);
        });
    }
    
    public boolean isEmpty() {
        return puzzleInfos.isEmpty();
    }
    
    public ListProperty<PuzzleInfo> getListItems() {
        return puzzleInfos;
    }
    
    public Property<Predicate<PuzzleInfo>> getListFilter() {
        return listFilter;
    }
    
    public Property<Boolean> getShowCompletedPuzzles() {
        return showCompletedPuzzles;
    }
    
    public Property<SortOrder> getSortOrder() {
        return sortOrder;
    }

    public Optional<PuzzleInfo> getSelectedPuzzle() {
        return Optional.ofNullable(selectedPuzzle.get());
    }
    
    public void setSelectedPuzzle(@Nullable PuzzleInfo puzzleInfo) {
        this.selectedPuzzle.set(puzzleInfo);
    }
    
    public Property<PuzzleInfo> getSelectedPuzzleProperty() {
        return selectedPuzzle;
    }
    
    public void addValidationListener(Consumer<Boolean> listener) {
        // TODO: Add a corresponding removeListener method
        listener.accept(selectedPuzzle.get() != null);
        selectedPuzzle.addListener(e -> {
            Object newValue = e.getNewValue();
            listener.accept(newValue != null);
        });
    }
}
