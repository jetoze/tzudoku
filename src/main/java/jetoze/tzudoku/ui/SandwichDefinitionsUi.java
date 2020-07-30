package jetoze.tzudoku.ui;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.Collectors.toList;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.stream.IntStream;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.TitledBorder;

import com.google.common.collect.ImmutableMap;

import jetoze.gunga.UiThread;
import jetoze.gunga.widget.ComboBoxWidget;
import jetoze.gunga.widget.Widget;
import jetoze.tzudoku.model.House;

/**
 * UI for defining Sandwiches when building a puzzle.
 */
public class SandwichDefinitionsUi implements Widget {

    private final ImmutableMap<Integer, ComboBoxWidget<Integer>> rowSandwiches;
    private final ImmutableMap<Integer, ComboBoxWidget<Integer>> columnSandwiches;
    private final JComponent ui;
    
    public SandwichDefinitionsUi() {
        rowSandwiches = createSumSelectors();
        columnSandwiches = createSumSelectors();
        ui = layoutUi();
    }

    private static ImmutableMap<Integer, ComboBoxWidget<Integer>> createSumSelectors() {
        return IntStream.rangeClosed(1, 9)
                .mapToObj(Integer::valueOf)
                .collect(toImmutableMap(i -> i, i -> createSumSelector()));
    }
    
    private JComponent layoutUi() {
        JPanel p = new JPanel(new GridLayout(0, 2, 10, 0));
        p.add(layoutSumSelectors(House.Type.ROW, rowSandwiches));
        p.add(layoutSumSelectors(House.Type.COLUMN, columnSandwiches));
        return p;
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
        ComboBoxWidget<Integer> comboBox = new ComboBoxWidget<>(IntStream.rangeClosed(-1, 35)
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
    
    
    public static void main(String[] args) {
        UiThread.run(() -> {
            UiLook.installNimbus();
            SandwichDefinitionsUi ui = new SandwichDefinitionsUi();
            JFrame frame = new JFrame("Sandwiches");
            frame.getContentPane().add(ui.getUi(), BorderLayout.CENTER);
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

}
