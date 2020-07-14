package jetoze.tzudoku.ui;

import static tzeth.preconds.MorePreconditions.checkNotBlank;
import static tzeth.preconds.MorePreconditions.checkPositive;

import javax.annotation.Nullable;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import jetoze.gunga.UiThread;
import jetoze.gunga.layout.Layouts;
import jetoze.gunga.widget.Widget;

public class StatusPanel implements Widget {

    @Nullable
    private Object currentStatusIdentifier;
    private final JLabel label = new JLabel(" ");
    private final JPanel panel;
    
    public StatusPanel() {
        label.setHorizontalAlignment(SwingConstants.LEADING);
        label.setBorder(new EmptyBorder(2, 2, 2, 0));
        panel = Layouts.border()
                .west(label)
                .build();
        panel.setBorder(new BevelBorder(BevelBorder.LOWERED));
    }
    
    public void setStatus(String text) {
        setStatus(text, 5);
    }
    
    public void setStatus(String text, int timeToLiveInSeconds) {
        UiThread.throwIfNotUiThread();
        checkNotBlank(text);
        checkPositive(timeToLiveInSeconds);
        Object identifier = new Object();
        currentStatusIdentifier = identifier;
        label.setText(text);
        Timer timer = new Timer(timeToLiveInSeconds * 1000, e -> clearIfSameStatus(identifier));
        timer.setRepeats(false);
        timer.start();
    }
    
    
    private void clearIfSameStatus(Object identifier) {
        if (identifier == this.currentStatusIdentifier) {
            currentStatusIdentifier = null;
            label.setText(" ");
        }
    }
    
    @Override
    public JComponent getUi() {
        return panel;
    }

    @Override
    public void requestFocus() {
        // Nothing to focus
    }
}
