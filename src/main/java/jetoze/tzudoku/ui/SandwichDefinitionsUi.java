package jetoze.tzudoku.ui;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.Collectors.toList;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.TitledBorder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import jetoze.gunga.layout.Layouts;
import jetoze.gunga.widget.ComboBoxWidget;
import jetoze.gunga.widget.Widget;
import jetoze.tzudoku.model.House;
import jetoze.tzudoku.model.Sandwich;
import jetoze.tzudoku.model.Sandwiches;

/**
 * UI for defining Sandwiches when building a puzzle.
 */
public class SandwichDefinitionsUi implements Widget {
    // TODO: Rewrite me with a model(?)

    private static final Integer NO_SANDWICH = -1;
    
    private final ImmutableMap<Integer, ComboBoxWidget<Integer>> rowSandwiches;
    private final ImmutableMap<Integer, ComboBoxWidget<Integer>> columnSandwiches;
    private final JComponent ui;
    
    public SandwichDefinitionsUi() {
        this(Sandwiches.EMPTY);
    }
    
    public SandwichDefinitionsUi(Sandwiches sandwiches) {
        rowSandwiches = createSumSelectors();
        columnSandwiches = createSumSelectors();
        populateSelectors(sandwiches.getRows(), rowSandwiches);
        populateSelectors(sandwiches.getColumns(), columnSandwiches);
        ui = layoutUi();
    }

    private static ImmutableMap<Integer, ComboBoxWidget<Integer>> createSumSelectors() {
        return IntStream.rangeClosed(1, 9)
                .mapToObj(Integer::valueOf)
                .collect(toImmutableMap(i -> i, i -> createSumSelector()));
    }
    
    private static void populateSelectors(ImmutableSet<Sandwich> sums, ImmutableMap<Integer, ComboBoxWidget<Integer>> selectors) {
        sums.forEach(s -> {
            ComboBoxWidget<Integer> selector = selectors.get(s.getPosition());
            selector.setSelectedItem(s.getSum());
        });
    }
    
    private JComponent layoutUi() {
        JPanel top = new JPanel(new GridLayout(0, 2, 10, 0));
        top.add(layoutSumSelectors(House.Type.ROW, rowSandwiches));
        top.add(layoutSumSelectors(House.Type.COLUMN, columnSandwiches));
        
        JPanel bottom = Layouts.border().west(UiLook.makeSmallButton("Clear", this::clear)).build();
        
        return Layouts.border(0, 4).center(top).south(bottom).build();
    }
    
    private static JComponent layoutSumSelectors(House.Type houseType, ImmutableMap<Integer, ComboBoxWidget<Integer>> sumSelectors) {
        assert houseType != House.Type.BOX;
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder(houseType == House.Type.ROW
                ? "Row Sandwiches"
                : "Column Sandwiches"));
        GridBagConstraints c = new GridBagConstraints();
        c.ipadx = 5;
        c.ipady = 5;
        c.gridy = 0;
        String labelText = (houseType == House.Type.ROW)
                ? "Row "
                : "Column ";
        for (int n = 1; n <= 9; ++n) {
            JLabel label = new JLabel(labelText + n + ":");
            c.weightx = 0.0;
            c.gridx = 0;
            p.add(label, c);
            c.gridx = 1;
            c.weightx = 1.0;
            p.add(sumSelectors.get(n).getUi(), c);
            ++c.gridy;
        }
        return p;
    }
    
    private void clear() {
        Stream.concat(rowSandwiches.values().stream(), columnSandwiches.values().stream())
            .forEach(w -> w.setSelectedItem(NO_SANDWICH));
    }
    
    /**
     * Returns the Sandwiches defined by this UI.
     */
    public Sandwiches getSandwiches() {
        Sandwiches.Builder builder = Sandwiches.builder();
        rowSandwiches.forEach((row, sumSelector) -> {
            sumSelector.getSelectedItem().filter(i -> i != NO_SANDWICH).ifPresent(sum -> builder.row(row, sum));
        });
        columnSandwiches.forEach((col, sumSelector) -> {
            sumSelector.getSelectedItem().filter(i -> i != NO_SANDWICH).ifPresent(sum -> builder.column(col, sum));
        });
        return builder.build();
    }
    
    @Override
    public JComponent getUi() {
        return ui;
    }

    @Override
    public void requestFocus() {
        rowSandwiches.get(1).requestFocus();
    }
    
    // XXX: I don't particularly like the use of a combobox here, but it
    // gets us off the ground quicker with regards to validation and such.
    private static ComboBoxWidget<Integer> createSumSelector() {
        ComboBoxWidget<Integer> comboBox = new ComboBoxWidget<>(IntStream.rangeClosed(NO_SANDWICH, 35)
                .filter(i -> i != 1) // 1 is not a valid Sandwich sum
                .mapToObj(Integer::valueOf)
                .collect(toList()));
        comboBox.setRenderer(new SumRenderer());
        return comboBox;
    }
    
    
    private static class SumRenderer implements ListCellRenderer<Integer> {
        private final DefaultListCellRenderer delegate = new DefaultListCellRenderer();

        @Override
        public Component getListCellRendererComponent(JList<? extends Integer> list, Integer value, int index,
                boolean isSelected, boolean cellHasFocus) {
            String text = (value == -1)
                    ? "[No Sandwich]"
                    : Integer.toString(value);
            return delegate.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
        }
    }
}
