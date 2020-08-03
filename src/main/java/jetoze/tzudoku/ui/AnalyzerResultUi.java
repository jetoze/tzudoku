package jetoze.tzudoku.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;

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
        JPanel stats = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 5, 5);
        
        c.gridx = 0;
        c.gridy = 0;
        stats.add(new JLabel("Solved:"), c);
        
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1.0;
        stats.add(new JLabel(result.isSolved() ? "Yes" : "No"), c);
        
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.0;
        stats.add(new JLabel("Time:"), c);
        
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1.0;
        stats.add(new JLabel(result.getDuration().toMillis() + " ms"), c);
        
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.0;
        stats.add(new JLabel("Number of Steps:"), c);
        
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1.0;
        stats.add(new JLabel(Integer.toString(result.getHintsApplied().size())), c);
        
        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0.0;
        stats.add(new JLabel("Number of Techniques:"), c);
        
        c.gridx = 1;
        c.gridy = 3;
        c.weightx = 1.0;
        long countTechniques = result.getHintsApplied().stream()
                .map(Hint::getTechnique)
                .distinct()
                .count();
        stats.add(new JLabel(Long.toString(countTechniques)), c);

        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        TableWidget techniqueTable = new TableWidget(createTechniquesTableModel(result));
        // TODO: Enable sorting. 
        stats.add(techniqueTable.getUi(), c);
        
        // TODO: List of individual steps.
        
        return stats;
    }
    
    private TableModel createTechniquesTableModel(GridSolver.Result result) {
        Multiset<SolvingTechnique> data = result.getHintsApplied().stream()
                .map(Hint::getTechnique)
                .collect(ImmutableMultiset.toImmutableMultiset());
        DefaultTableModel model = new DefaultTableModel(new String[] { "Technique", "Times Used" }, 0);
        data.entrySet().forEach(e -> model.addRow(new Object[] { e.getElement(), e.getCount() }));
        return model;
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
