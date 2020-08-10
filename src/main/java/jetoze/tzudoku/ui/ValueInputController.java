package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import jetoze.attribut.Properties;
import jetoze.attribut.Property;
import jetoze.tzudoku.model.Value;

/**
 * Acts as a conduit between the control panel UI and the GridUiModel, for entering
 * values, pencil marks, and cell colors into the model.
 */
public class ValueInputController { // TODO: Come up with a better name. "Value" is overloaded.

    private final GridUiModel model;
    private final Property<EnterValueMode> enterValueMode = Properties.newProperty(
            "enterValueMode", EnterValueMode.NORMAL);
    
    // TODO: Support listener that is notified when a new value is entered into the model?
    //       That would allow us to implement the puzzle builder mode where the next cell is
    //       automatically selected when a new value is entered.

    public ValueInputController(GridUiModel model) {
        this.model = requireNonNull(model);
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
            model.toggleCornerMark(value);
            break;
        case CENTER_PENCIL_MARK:
            model.toggleCenterMark(value);
            break;
        case COLOR:
            model.setCellColor(value);
            break;
        default:
            throw new RuntimeException("Unknown mode: " + enterValueMode.get());
        }
    }

}
