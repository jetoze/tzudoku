package jetoze.tzudoku.ui;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.awt.event.KeyEvent;

import jetoze.attribut.Properties;
import jetoze.attribut.Property;
import jetoze.gunga.KeyBindings;
import jetoze.gunga.KeyStrokes;
import jetoze.tzudoku.model.CellColor;
import jetoze.tzudoku.model.Position;
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
        return new ValueInputController(model, Purpose.SOLVING);
    }

    /**
     * Creates a ValueInputController for use when building a puzzle.
     */
    public static ValueInputController forBuilding(GridUiModel model) {
        return new ValueInputController(model, Purpose.BUILDING);
    }
    
    private final GridUiModel model;
    private final Purpose purpose;
    private final Property<EnterValueMode> enterValueMode = Properties.newProperty(
            "enterValueMode", EnterValueMode.NORMAL);

    private ValueInputController(GridUiModel model, Purpose purpose) {
        this.model = requireNonNull(model);
        this.purpose = requireNonNull(purpose);
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
            enterValue(value);
            break;
        case CORNER_PENCIL_MARK:
            checkState(this.purpose == Purpose.SOLVING, "Pencil marks are not supported when building puzzles");
            model.toggleCornerMark(value);
            break;
        case CENTER_PENCIL_MARK:
            checkState(this.purpose == Purpose.SOLVING, "Pencil marks are not supported when building puzzles");
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
    
    private void enterValue(Value value) {
        model.enterValue(value);
        if (purpose == Purpose.BUILDING) {
            model.getLastSelectedCell().ifPresent(c -> {
                Position currentPosition = c.getPosition();
                Position nextPosition = model.getNavigationMode().right(currentPosition);
                model.selectCellAt(nextPosition);
            });
        }
    }

    public void registerActions(KeyBindings keyBindings) {    
        for (Value v : Value.values()) {
            keyBindings.add(KeyStrokes.forKey(KeyEvent.VK_0 + v.toInt()), "enter-" + v, 
                    () -> updateModel(v));
        }
        if (this.purpose == Purpose.SOLVING) {
            keyBindings.add(KeyStrokes.forKey(KeyEvent.VK_N), "normal-value-mode",
                    () -> setEnterValueMode(EnterValueMode.NORMAL));
            keyBindings.add(KeyStrokes.forKey(KeyEvent.VK_R), "corner-value-mode",
                    () -> setEnterValueMode(EnterValueMode.CORNER_PENCIL_MARK));
            keyBindings.add(KeyStrokes.forKey(KeyEvent.VK_C), "center-value-mode",
                    () -> setEnterValueMode(EnterValueMode.CENTER_PENCIL_MARK));
        }
    }
    
    
    private static enum Purpose {
        SOLVING, BUILDING
    }

}
