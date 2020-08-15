package jetoze.tzudoku.ui;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

import jetoze.gunga.layout.Layouts;
import jetoze.gunga.widget.CheckBoxWidget;
import jetoze.gunga.widget.TableWidget;
import jetoze.gunga.widget.Widget;
import jetoze.tzudoku.hint.Hint;
import jetoze.tzudoku.hint.SolvingTechnique;
import jetoze.tzudoku.model.GridSolver;

public class AnalyzerResultUi implements Widget {
    
    private static final RowFilter<TableModel, Object> SHOW_TECHNIQUES = new RowFilter<TableModel, Object>() {

        @Override
        public boolean include(Entry<? extends TableModel, ? extends Object> entry) {
            return true;
        }
    };
    
    private static final RowFilter<TableModel, Object> HIDE_TECHNIQUES = new RowFilter<TableModel, Object>() {

        @Override
        public boolean include(Entry<? extends TableModel, ? extends Object> entry) {
            return false;
        }
    };
    
    private final TableWidget techniqueTable;
    private final TableRowSorter<TableModel> rowSorter;
    private final JComponent ui;
    
    public AnalyzerResultUi(GridSolver.Result result) {
        this.techniqueTable = buildTechniqueTable(result);
        this.rowSorter = new TableRowSorter<>(techniqueTable.getModel());
        this.rowSorter.setRowFilter(HIDE_TECHNIQUES);
        this.techniqueTable.setRowSorter(rowSorter);
        this.ui = layoutUi(result);
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

    private JComponent layoutUi(GridSolver.Result result) {
        // TODO: List of individual steps.
        JLabel solvedLabel = new JLabel(result.isSolved() ? "Yes" : "No", UiLook.getAnalyzerResultIcon(result), SwingConstants.LEADING);
        CheckBoxWidget showTechniquesCheckBox = new CheckBoxWidget();
        showTechniquesCheckBox.addChangeListener(this::setShowTechniques);
        JPanel stats = Layouts.form()
                .addRow("Solved:", solvedLabel)
                .addRow("Time:", result.getDuration().toMillis() + " ms")
                .addRow("Number of Steps:", Integer.toString(result.getHintsApplied().size()))
                .addRow("Number of Techniques:", Long.toString(result.getNumberOfTechniquesUsed()))
                .addRow("Show Techniques:", showTechniquesCheckBox)
                .build();
        return Layouts.border(0, 5)
                .north(stats)
                .center(techniqueTable)
                .build();
    }
    
    private void setShowTechniques(boolean show) {
        RowFilter<TableModel, Object> filter = show
                ? SHOW_TECHNIQUES
                : HIDE_TECHNIQUES;
        rowSorter.setRowFilter(filter);
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
