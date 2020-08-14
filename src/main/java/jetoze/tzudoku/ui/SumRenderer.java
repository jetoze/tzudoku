package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

class SumRenderer implements ListCellRenderer<Integer> {
    private final int noValue;
    private final String noValueString;
    private final DefaultListCellRenderer delegate = new DefaultListCellRenderer();

    public SumRenderer(int noValue, String noValueString) {
        this.noValue = noValue;
        this.noValueString = requireNonNull(noValueString);
    }
    
    @Override
    public Component getListCellRendererComponent(JList<? extends Integer> list, Integer value, int index,
            boolean isSelected, boolean cellHasFocus) {
        String text = (value == noValue)
                ? noValueString
                : Integer.toString(value);
        return delegate.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
    }
}