package jetoze.tzudoku.ui.hint;

import static java.util.Objects.requireNonNull;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import jetoze.tzudoku.hint.Hint;
import jetoze.tzudoku.ui.GridUiModel;

public class HintDisplay { // TODO: This is a bad name, but this class may be temporary anyway.

    private final JFrame appFrame;
    private final GridUiModel model;
    private final HintUiFactory hintUiFactory;
    
    public HintDisplay(JFrame appFrame, GridUiModel model, HintUiFactory hintUiFactory) {
        this.appFrame = requireNonNull(appFrame);
        this.model = requireNonNull(model);
        this.hintUiFactory = requireNonNull(hintUiFactory);
    }
    
    public void showHintInfo(Hint hint) {
        requireNonNull(hint);
        HintUi hintUi = hintUiFactory.getUi(hint);
        String htmlInfo = hintUi.getHtmlInfo();
        HintCellDecorator decorator = hintUi.getCellDecorator(model);
        decorator.decorate();
        try {
            JOptionPane.showMessageDialog(appFrame, new JLabel(htmlInfo));
        } finally {
            decorator.clear();
        }
    }
}
