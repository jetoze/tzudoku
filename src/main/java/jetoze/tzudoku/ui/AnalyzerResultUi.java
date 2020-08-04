package jetoze.tzudoku.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

import jetoze.gunga.layout.Layouts;
import jetoze.gunga.widget.TableWidget;
import jetoze.gunga.widget.Widget;
import jetoze.tzudoku.hint.Hint;
import jetoze.tzudoku.hint.SolvingTechnique;
import jetoze.tzudoku.model.GridSolver;

public class AnalyzerResultUi implements Widget {
    
    private final JComponent ui;
    
    public AnalyzerResultUi(GridSolver.Result result) {
        this.ui = layoutUi(result);
    }

    private JComponent layoutUi(GridSolver.Result result) {
        // TODO: List of individual steps.
        JPanel stats = Layouts.form()
                // TODO: Use an icon to distinguish between solved and unsolved.
                .addRow("Solved:", result.isSolved() ? "Yes" : "No")
                .addRow("Time:", result.getDuration().toMillis() + " ms")
                .addRow("Number of Steps:", Integer.toString(result.getHintsApplied().size()))
                .addRow("Number of Techniques:", Long.toString(result.getNumberOfTechniquesUsed()))
                .build();
        TableWidget techniqueTable = buildTechniqueTable(result);
        return Layouts.border(0, 5)
                .north(stats)
                .center(techniqueTable)
                .build();
    }

    private TableWidget buildTechniqueTable(GridSolver.Result result) {
        TableWidget.Builder techniqueTableBuilder = TableWidget.builder("Technique", "Times Used");
        Multiset<SolvingTechnique> data = result.getHintsApplied().stream()
                .map(Hint::getTechnique)
                .collect(ImmutableMultiset.toImmutableMultiset());
        data.entrySet().forEach(e -> techniqueTableBuilder.addRow(e.getElement(), e.getCount()));
        // TODO: Enable sorting. 
        return techniqueTableBuilder.build();
    }

    @Override
    public JComponent getUi() {
        return ui;
    }

    @Override
    public void requestFocus() {
        // Nothing to give focus to.
    }

}
