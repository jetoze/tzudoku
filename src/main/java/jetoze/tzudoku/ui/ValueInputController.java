package jetoze.tzudoku.ui;

import static com.google.common.base.Preconditions.*;
import static java.util.Objects.requireNonNull;

import java.awt.event.KeyEvent;

import jetoze.attribut.Properties;
import jetoze.attribut.Property;
import jetoze.gunga.KeyBindings;
import jetoze.gunga.KeyStrokes;
import jetoze.tzudoku.model.CellColor;
import jetoze.tzudoku.model.Value;

/**
 * Acts as a conduit between the control panel UI and the GridUiModel, for entering
 * values, pencil marks, and cell colors into the model.
 */
public class ValueInputController { // TODO: Come up with a better name. "Value" is overloaded.

    /**
     * Creates a ValueInputController for use when solving a puzzle.
     */
    public static ValueInputController forSolving(GridUiModel model) {
        return new ValueInputController(model, true);
    }

    /**
     * Creates a ValueInputController for use when building a puzzle.
     */
    public static ValueInputController forBuilding(GridUiModel model) {
        return new ValueInputController(model, false);
    }
    
    private final GridUiModel model;
    private final boolean supportsPencilMarks;
    private final Property<EnterValueMode> enterValueMode = Properties.newProperty(
            "enterValueMode", EnterValueMode.NORMAL);
    
    // TODO: Support listener that is notified when a new value is entered into the model?
    //       That would allow us to implement the puzzle builder mode where the next cell is
    //       automatically selected when a new value is entered.

    private ValueInputController(GridUiModel model, boolean supportsPencilMarks) {
        this.model = requireNonNull(model);
        this.supportsPencilMarks = supportsPencilMarks;
    }

    public Property<EnterValueMode> getEnterValueModeProperty() {
        return enterValueMode;
    }

    public void setEnterValueMode(EnterValueMode enterValueMode) {
        this.enterValueMode.set(enterValueMode);
    }
    
    public void updateModel(Value value) {
        requireNonNull(value);
        switch (enterValueMode.get()) {
        case NORMAL:
            model.enterValue(value);
            break;
        case CORNER_PENCIL_MARK:
            checkState(supportsPencilMarks, "Pencil marks are not supported");
            model.toggleCornerMark(value);
            break;
        case CENTER_PENCIL_MARK:
            checkState(supportsPencilMarks, "Pencil marks are not supported");
            model.toggleCenterMark(value);
            break;
        case COLOR:
            CellColor color = CellColor.fromValue(value);
            model.setCellColor(color);
            break;
        default:
            throw new RuntimeException("Unknown mode: " + enterValueMode.get());
        }
    }

    public void registerActions(KeyBindings keyBindings) {    
        for (Value v : Value.values()) {
            keyBindings.add(KeyStrokes.forKey(KeyEvent.VK_0 + v.toInt()), "enter-" + v, 
                    () -> updateModel(v));
        }
        if (supportsPencilMarks) {
            keyBindings.add(KeyStrokes.forKey(KeyEvent.VK_N), "normal-value-mode",
                    () -> setEnterValueMode(EnterValueMode.NORMAL));
            keyBindings.add(KeyStrokes.forKey(KeyEvent.VK_R), "corner-value-mode",
                    () -> setEnterValueMode(EnterValueMode.CORNER_PENCIL_MARK));
            keyBindings.add(KeyStrokes.forKey(KeyEvent.VK_C), "center-value-mode",
                    () -> setEnterValueMode(EnterValueMode.CENTER_PENCIL_MARK));
        }
    }

}
