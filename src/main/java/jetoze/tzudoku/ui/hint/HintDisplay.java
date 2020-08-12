package jetoze.tzudoku.ui.hint;

import static java.util.Objects.requireNonNull;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import jetoze.gunga.UiThread;
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
    
    public void display(Hint hint) {
        requireNonNull(hint);
        HintUi hintUi = hintUiFactory.getUi(hint);
        HintCellDecorator decorator = hintUi.getCellDecorator(model);
        decorator.decorate();
        try {
            boolean apply = showHintInfo(hint, hintUi);
            if (apply) {
                UiThread.runLater(() -> hintUi.apply(model));
            }
        } finally {
            decorator.clear();
        }
    }
    
    /**
     * Displays the HTML information provided by the HintUi in a popup dialog, 
     * with the choice of also applying the hint to the puzzle.
     * 
     * @return {@code true} if the user chose to apply the hint, {@code false} otherwise.
     */
    private boolean showHintInfo(Hint hint, HintUi hintUi) {
        String htmlInfo = hintUi.getHtmlInfo();
        Object[] choices = {"Apply", "Close"};
        Object defaultChoice = choices[1];
        int choice = JOptionPane.showOptionDialog(appFrame,
                     new JLabel(htmlInfo),
                     hint.getTechnique().getName(),
                     JOptionPane.YES_NO_OPTION,
                     JOptionPane.INFORMATION_MESSAGE,
                     null,
                     choices,
                     defaultChoice);
        return choice == JOptionPane.YES_OPTION;
    }
}
