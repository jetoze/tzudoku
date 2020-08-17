package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import javax.annotation.Nullable;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import jetoze.gunga.UiThread;
import jetoze.gunga.layout.Layouts;
import jetoze.gunga.widget.ComboBoxWidget;
import jetoze.tzudoku.model.Cell;
import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.KillerCage;
import jetoze.tzudoku.model.KillerCageSums;
import jetoze.tzudoku.model.KillerCages;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Puzzle;
import jetoze.tzudoku.model.Sandwiches;

public class PuzzleBuilderController {
    // TODO: Edit Killer Cage action. To begin with at least allow changing the cage sum.
    //       Ultimately we obviously also want to allow changing the shape of the cell.
    private final JFrame appFrame;
    private final PuzzleBuilderModel model;
    private final AddKillerCageAction addKillerCageAction = new AddKillerCageAction();
    private final DeleteKillerCageAction deleteKillerCageAction = new DeleteKillerCageAction();
    
    public PuzzleBuilderController(JFrame appFrame, PuzzleBuilderModel model) {
        this.appFrame = requireNonNull(appFrame);
        this.model = requireNonNull(model);
        model.getGridModel().addListener(new GridUiModelListener() {

            @Override
            public void onSelectionChanged() {
                ImmutableSet<Position> selectedCells = model.getGridModel().getSelectedPositions();
                addKillerCageAction.setSelectedPositions(selectedCells);
                deleteKillerCageAction.setSelectedPositions(selectedCells);
            }
        });
    }
    
    public Action getAddKillerCageAction() {
        return addKillerCageAction;
    }
    
    public DeleteKillerCageAction getDeleteKillerCageAction() {
        return deleteKillerCageAction;
    }

    public void defineSandwiches() {
        SandwichDefinitionsUi sandwichDefinitionsUi = new SandwichDefinitionsUi(model.getSandwiches());
        int option = JOptionPane.showConfirmDialog(
                appFrame, 
                sandwichDefinitionsUi.getUi(), 
                "Define Sandwiches", 
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            UiThread.runLater(() -> {
                Sandwiches sandwiches = sandwichDefinitionsUi.getSandwiches();
                model.setSandwiches(sandwiches);
                // TODO: Must update the UI.
            });
        }
    }
    
    public boolean isExitAllowed() {
        // TODO: Implement me. If there are unsaved changes, prompt the user to save them.
        return true;
    }
    
    public void reset() {
        // TODO: Check for unsaved changes.
        model.reset();
    }
    
    public void createPuzzle(Consumer<? super Puzzle> consumer) {
        String name = model.getPuzzleName();
        if (name.isBlank()) {
            showErrorMessage("Please enter a puzzle name.");
            return;
        }
        // TODO: Wait indication.
        UiThread.offload(() -> createPuzzleFromTemplate(name), consumer);
    }
    
    @Nullable
    private Puzzle createPuzzleFromTemplate(String name) {
        Grid templateGrid = model.getGridModel().getGrid();
        if (model.isEmpty()) {
            UiThread.run(() -> showErrorMessage("The puzzle is empty."));
            return null;
        }
        ImmutableSet<Position> duplicates = templateGrid.getCellsWithDuplicateValues();
        if (!duplicates.isEmpty()) {
            UiThread.run(() -> showErrorMessage("There are duplicate values."));
            return null;
        }
        Map<Position, Cell> cells = new HashMap<>();
        ImmutableMap<Position, Cell> templateCells = templateGrid.getCells();
        templateCells.forEach((p, c) -> {
            Cell cellToUse = c.getValue()
                    .map(Cell::given)
                    .orElse(c);
            cells.put(p, cellToUse);
        });
        Grid grid = new Grid(cells);
        Puzzle puzzle = new Puzzle(name, grid, model.getSandwiches(), model.getKillerCages());
        try {
            model.getInventory().addNewPuzzle(puzzle);
            return puzzle;
        } catch (IOException e) {
            // TODO: Log the exception
            UiThread.run(() -> showErrorMessage("Could not save the puzzle: " + e.getMessage()));
            return null;
        }
    }
    
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(
                appFrame, 
                message, 
                "Invalid puzzle", 
                JOptionPane.ERROR_MESSAGE);
    }
    
    private void addOrRemoveKillerCage(KillerCage cage, BiFunction<KillerCages, KillerCage, KillerCages> operator) {
        KillerCages current = model.getKillerCages();
        KillerCages updated = operator.apply(current, cage);
        model.setKillerCages(updated);
        model.getGridModel().clearSelection();
    }
    
    
    private class AddKillerCageAction extends AbstractAction {
        
        private ImmutableSet<Position> selectedCells;
        
        public AddKillerCageAction() {
            super("Add Killer Cage...");
            setSelectedPositions(ImmutableSet.of());
        }
        
        public void setSelectedPositions(ImmutableSet<Position> positions) {
            boolean enabled = KillerCage.isValidShape(positions) && !model.getKillerCages().containsCageAt(positions) &&
                    !model.getKillerCages().intersects(positions);
            setEnabled(enabled);
            if (enabled) {
                this.selectedCells = positions;
            } else {
                this.selectedCells = ImmutableSet.of();
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO: Prompt the user for an optional sum.
            // Since we wrap this in an invokeLater we pass in a reference to this.selectedCells,
            // to handle the corner case of the selection changing in between.
            UiThread.acceptLater(this::addCage, this.selectedCells);
        }
        
        private void addCage(ImmutableSet<Position> cageShape) {
            int sum = selectSum(cageShape);
            if (sum < 0) {
                // The user canceled
                return;
            }
            KillerCage cage = (sum == 0)
                    ? new KillerCage(cageShape)
                    : new KillerCage(cageShape, sum);
            addOrRemoveKillerCage(cage, KillerCages::add);
        }
        
        /**
         * 
         * @return 0 if the cage should not have a sum, > 0 for the selected sum, or -1 if 
         * the user canceled.
         */
        private int selectSum(ImmutableSet<Position> cageShape) {
            int noSum = 0;
            List<Integer> possibleSums = KillerCageSums.getPossibleSums(cageShape.size());
            possibleSums.add(0, noSum);
            ComboBoxWidget<Integer> selector = new ComboBoxWidget<>(possibleSums);
            selector.setRenderer(new SumRenderer(noSum, "[No Sum Given]"));
            selector.selectFirst();
            JPanel ui = Layouts.border(0, 5)
                    .north("Select the sum that goes into the Killer Cage")
                    .south(selector)
                    .build();
            int ret = JOptionPane.showConfirmDialog(appFrame, ui, "Killer Cage Sum", JOptionPane.OK_CANCEL_OPTION);
            return (ret == JOptionPane.OK_OPTION)
                    ? selector.getSelectedItem().orElse(0)
                    : -1;
        }
    }
    
    
    
    private class DeleteKillerCageAction extends AbstractAction {
        
        @Nullable
        private KillerCage cageToDelete;
        
        public DeleteKillerCageAction() {
            super("Delete Killer Cage");
            setEnabled(false);
        }
        
        public void setSelectedPositions(ImmutableSet<Position> positions) {
            setCageToDelete(model.getKillerCages().getCage(positions).orElse(null));
        }

        private void setCageToDelete(@Nullable KillerCage cage) {
            setEnabled(cage != null);
            this.cageToDelete = cage;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO: Prompt the user for confirmation?
            UiThread.acceptLater(this::deleteCage, cageToDelete);
        }
        
        private void deleteCage(KillerCage cage) {
            addOrRemoveKillerCage(cage, KillerCages::remove);
        }
    }
}
