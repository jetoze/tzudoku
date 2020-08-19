package jetoze.tzudoku.ui;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import jetoze.gunga.KeyBindings;
import jetoze.gunga.KeyStrokes;
import jetoze.gunga.UiThread;
import jetoze.gunga.widget.Focusable;
import jetoze.gunga.widget.Widget;

public class InputDialog {

    // TODO: Move this to gunga, and build out more here as the need arises.
    @Nullable
    private final JFrame owner;
    @Nullable
    private final String title;
    @Nullable
    private final Icon icon;
    private final int optionType;
    private final Supplier<JComponent> contentSupplier;
    private final Focusable focusReceiver;
    // TODO: Come up with a way to generalize the jobs here, for the case of other input
    // types than OK_CANCEL.
    private final Runnable okJob;
    private final Runnable cancelJob;
    private final boolean modal;
    private final ValidStateMonitor validStateMonitor;
    private Consumer<Boolean> validationListener;

    private InputDialog(JFrame owner, String title, @Nullable Icon icon, int optionType, Supplier<JComponent> contentSupplier, 
            Focusable focusReceiver, Runnable okJob, Runnable cancelJob, boolean modal, ValidStateMonitor validStateMonitor) {
        // All input validation is done by the Builder.
        this.owner = owner;
        this.title = title;
        this.icon = icon;
        this.optionType = optionType;
        this.contentSupplier = contentSupplier;
        this.focusReceiver = focusReceiver;
        this.okJob = okJob;
        this.cancelJob = cancelJob;
        this.modal = modal;
        this.validStateMonitor = validStateMonitor;
    }

    public void open() {
        JDialog dialog = new JDialog(owner, title, modal);
        JButton[] buttons = createButtons(dialog);
        validStateMonitor.addValidationListener(validationListener);
        
        JOptionPane optionPane = new JOptionPane(
                contentSupplier.get(),
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION,
                null,
                buttons,
                buttons[0] /* TODO: Make default option configurable*/);
        dialog.setContentPane(optionPane);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.getRootPane().setDefaultButton(buttons[0]); // TODO: Again, default option should be configurable.
        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(WindowEvent e) {
                UiThread.runLater(focusReceiver::requestFocus);
            }
        });
        KeyBindings.whenAncestorOfFocusedComponent(dialog.getRootPane())
            .add(KeyStrokes.ESCAPE, "escape", () -> dialog.setVisible(false));
        dialog.setVisible(true);
        // FIXME: This assumes the dialog is modal. We must implement the removal of the validation
        // listener that works for modeless dialogs as well. Using a WindowListener doesn't seem to 
        // work, at least not on OSX -- the windowClosing/Closed events are not received.
        validStateMonitor.removeValidationListener(validationListener);
    }
    
    private JButton[] createButtons(JDialog dialog) {
        // FIXME: This method does too much, since it also creates the 
        // validation listener as a side effect.
        switch (optionType) {
        case JOptionPane.OK_CANCEL_OPTION:
            JButton ok = createDialogButton(dialog, "OK", okJob);
            JButton cancel =  createDialogButton(dialog, "Cancel", cancelJob);
            validationListener = ok::setEnabled;
            return new JButton[] { ok, cancel };
        default:
            throw new RuntimeException("Unsupported option type: " + optionType);
        }
    }
    
    private static JButton createDialogButton(JDialog dialog, String text, Runnable job) {
        JButton button = new JButton(text) {

            @Override
            public String toString() {
                return getText();
            }
        };
        button.addActionListener(e -> {
            dialog.setVisible(false);
            job.run();
        });
        return button;
    }


    public static Builder okCancel() {
        return new Builder(JOptionPane.OK_CANCEL_OPTION);
    }
    
    public interface ValidStateMonitor {
        
        void addValidationListener(Consumer<Boolean> listener);
        
        void removeValidationListener(Consumer<Boolean> listener);
    }
    
    
    public static class Builder {
        // TODO: Support for customized button labels.
        private Supplier<JComponent> contentSupplier;
        private Focusable focusReceiver;
        private final int optionType;
        // TODO: Come up with a way to generalize the jobs here, for the case of other input
        // types than OK_CANCEL.
        private Runnable okJob = () -> {};
        private Runnable cancelJob = () -> {};
        @Nullable
        private JFrame owner;
        @Nullable
        private String title;
        @Nullable
        private Icon icon;
        private boolean modal = true;
        
        private ValidStateMonitor validStateMonitor = new ValidStateMonitor() {
            
            @Override
            public void addValidationListener(Consumer<Boolean> listener) {/**/}
            
            @Override
            public void removeValidationListener(Consumer<Boolean> listener) {/**/}
        };

        private Builder(int optionType) {
            this.optionType = optionType;
        }

        public Builder withContent(Widget widget) {
            requireNonNull(widget);
            this.contentSupplier = widget::getUi;
            this.focusReceiver = widget;
            return this;
        }
        
        public Builder withContent(JComponent component) {
            requireNonNull(component);
            this.contentSupplier = () -> component;
            this.focusReceiver = Focusable.of(component);
            return this;
        }
        
        public Builder withOwner(JFrame owner) {
            this.owner = requireNonNull(owner);
            return this;
        }
        
        public Builder withTitle(String title) {
            this.title = requireNonNull(title);
            return this;
        }
        
        public Builder withIcon(Icon icon) {
            this.icon = requireNonNull(icon);
            return this;
        }
        
        public Builder withOkJob(Runnable job) {
            this.okJob = requireNonNull(job);
            return this;
        }
        
        public Builder withCancelJob(Runnable job) {
            this.cancelJob = requireNonNull(job);
            return this;
        }
        
        public Builder modeless() {
            this.modal = false;
            return this;
        }
        
        public Builder withValidStateMonitor(ValidStateMonitor m) {
            this.validStateMonitor = requireNonNull(m);
            return this;
        }
        
        public InputDialog build() {
            checkState(contentSupplier != null, "Must provide the content before building the dialog");
            return new InputDialog(owner, title, icon, optionType, contentSupplier, focusReceiver, okJob, cancelJob, modal, validStateMonitor);
        }
    }
    
}
