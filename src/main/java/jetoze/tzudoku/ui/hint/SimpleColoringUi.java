package jetoze.tzudoku.ui.hint;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.hint.SimpleColoring;
import jetoze.tzudoku.hint.SimpleColoring.Color;
import jetoze.tzudoku.hint.SimpleColoring.TooCrowdedHouse;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.ui.GridUiModel;
import jetoze.tzudoku.ui.GridUiModel.HighlightedCells;

class SimpleColoringUi implements HintUi {

    private final SimpleColoring hint;
    
    public SimpleColoringUi(SimpleColoring hint) {
        this.hint = requireNonNull(hint);
    }

    @Override
    public void apply(GridUiModel model) {
        // TODO: This messes up undo/redo since we are not applying these changes as an atomic operation.
        hint.getCellsThatCanBePenciledIn().forEach(p -> model.enterValue(p, hint.getValue()));
        model.removeCandidatesFromCells(hint.getCellsToEliminate(), ImmutableSet.of(hint.getValue()));
    }

    @Override
    public String getShortDescription() {
        return hint.getTechnique().getName() + ": " + hint.getValue();
    }

    @Override
    public String getHtmlInfo() {
        // TODO: I'm a bit clumsy and can be refined some.
        String html = "<html>Simple Coloring on digit " + hint.getValue() + 
                hint.getHouseTooCrowded().map(this::getTooCrowdedHousePreamble)
                .orElseGet(this::getSeesOppositeColorPreamble) + "<br><br>";
        ImmutableSet<Position> canBePenciledIn = hint.getCellsThatCanBePenciledIn();
        if (!canBePenciledIn.isEmpty()) {
            html += "The digit " + hint.getValue() + " can be entered into the following cells:<br>" +
                    HintUiUtils.positions(canBePenciledIn) + "<br><br>";
        }
        html += "The digit " + hint.getValue() + " can be eliminated from the following cells:<br>" +
                HintUiUtils.positions(hint.getCellsToEliminate()) + "</html>";
        return html;
    }
    
    private String getTooCrowdedHousePreamble(TooCrowdedHouse houseTooCrowded) {
        return " produced a Too Crowded House, with two " +
                houseTooCrowded.getColor().name().toLowerCase() + " cells in " + houseTooCrowded.getHouse() + ".";
    }
    
    private String getSeesOppositeColorPreamble() {
        return " resulted with one or more cells seeing cells of opposite colors.";
    }

    @Override
    public HintCellDecorator getCellDecorator(GridUiModel model) {
        return new SimpleColoringCellDecorator(model, hint);
    }
    
    
    private static class SimpleColoringCellDecorator extends CellHighlightDecorator<SimpleColoring> {

        public SimpleColoringCellDecorator(GridUiModel model, SimpleColoring hint) {
            super(model, hint);
        }

        @Override
        protected Collection<HighlightedCells> getHighlights(SimpleColoring hint) {
            ImmutableSet<Position> blueCells = hint.getCellsOfColor(Color.BLUE);
            ImmutableSet<Position> orangeCells = hint.getCellsOfColor(Color.ORANGE);
            List<HighlightedCells> highlights = new ArrayList<>();
            highlights.add(new HighlightedCells(blueCells, HintHighlightColors.SIMPLE_COLORING_BLUE));
            highlights.add(new HighlightedCells(orangeCells, HintHighlightColors.SIMPLE_COLORING_ORANGE));
            if (hint.getCellsThatCanBePenciledIn().isEmpty()) {
                ImmutableSet<Position> targetCells = hint.getCellsToEliminate();
                highlights.add(new HighlightedCells(targetCells, HintHighlightColors.TARGET_CELL));
            }
            return highlights;
        }
    }
}
