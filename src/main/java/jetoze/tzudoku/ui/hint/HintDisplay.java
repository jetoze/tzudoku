package jetoze.tzudoku.ui.hint;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.apache.commons.text.StringSubstitutor;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import jetoze.tzudoku.hint.BoxLineReduction;
import jetoze.tzudoku.hint.HiddenMultiple;
import jetoze.tzudoku.hint.Hint;
import jetoze.tzudoku.hint.NakedMultiple;
import jetoze.tzudoku.hint.PointingPair;
import jetoze.tzudoku.hint.SimpleColoring;
import jetoze.tzudoku.hint.Single;
import jetoze.tzudoku.hint.Swordfish;
import jetoze.tzudoku.hint.XWing;
import jetoze.tzudoku.hint.XyWing;
import jetoze.tzudoku.model.House;
import jetoze.tzudoku.model.House.Type;
import jetoze.tzudoku.model.Position;
import jetoze.tzudoku.model.Value;
import jetoze.tzudoku.ui.GridUiModel;

public class HintDisplay { // TODO: This is a bad name, but this class may be temporary anyway.

    private final JFrame appFrame;
    private final HintCellDecorators cellDecorators;
    private final GridUiModel model;
    
    public HintDisplay(JFrame appFrame, GridUiModel model) {
        this.appFrame = requireNonNull(appFrame);
        this.model = requireNonNull(model);
        // TODO: Should I also be injected?
        this.cellDecorators = new HintCellDecorators(model);
    }
    
    public void showHintInfo(Hint hint) {
        requireNonNull(hint);
        if (hint instanceof Single) {
            showSingleInfo((Single) hint);
        } else if (hint instanceof PointingPair) {
            showPointingPairInfo((PointingPair) hint);
        } else if (hint instanceof BoxLineReduction) {
            showBoxLineReductionInfo((BoxLineReduction) hint);
        } else if (hint instanceof NakedMultiple) {
            showNakedMultipleInfo((NakedMultiple) hint);
        } else if (hint instanceof HiddenMultiple) {
            showHiddenMultipleInfo((HiddenMultiple) hint);
        } else if (hint instanceof XWing) {
            showXWingInfo((XWing) hint);
        } else if (hint instanceof XyWing) {
            showXyWingInfo((XyWing) hint);
        } else if (hint instanceof SimpleColoring) {
            showSimpleColoringInfo((SimpleColoring) hint);
        } else if (hint instanceof Swordfish) {
            showSwordfishInfo((Swordfish) hint);
        } else {
            throw new UnsupportedOperationException("I don't know how to display hints of type " + hint.getTechnique());
        }
    }
    
    public void showSingleInfo(Single single) {
        String s = "<html>Found a " + single.getTechnique().getName() + ":<br>" + single.getPosition() + 
                "<br>Value: " + single.getValue() + "</html>";
        showHintInfo(single, s);
    }

    public void showPointingPairInfo(PointingPair hint) {
        String template = "<html>Found a Pointing Pair:<br><br>" +
                "The digit ${value} in ${box} is confined to ${positions} in ${rowOrColumn}.<br>" +
                "${value} can therefore be eliminated from ${targets} in ${rowOrColumn}.</html>";
        String forcingPositions = toStringInOrder(hint.getForcingPositions(), hint.getRowOrColumn());
        String targetPositions = toStringInOrder(hint.getTargetPositions(), hint.getRowOrColumn());
        Map<String, Object> args = ImmutableMap.of(
                "value", hint.getValue(),
                "box", hint.getBox(),
                "positions", forcingPositions,
                "targets", targetPositions,
                "rowOrColumn", hint.getRowOrColumn());
        String html = new StringSubstitutor(args).replace(template);
        showHintInfo(hint, html);
    }
    
    private static String toStringInOrder(ImmutableSet<Position> positions, House house) {
        return positions.stream()
                .sorted(house.getType().positionOrder())
                .map(Object::toString)
                .collect(joining(" "));
    }

    public void showBoxLineReductionInfo(BoxLineReduction hint) {
        String template = "<html>Found a Box Line Reduction:<br><br>" +
                "The digit ${value} in ${rowOrColumn} is confined to positions ${positions} in ${box}.<br>" +
                "${value} can therefore be eliminated from ${targets} in ${box}.</html>";
        String forcingPositions = toStringInOrder(hint.getForcingPositions(), hint.getRowOrColumn());
        String targetPositions = toStringInOrder(hint.getTargetPositions(), hint.getBox());
        Map<String, Object> args = ImmutableMap.of(
                "value", hint.getValue(),
                "rowOrColumn", hint.getRowOrColumn(),
                "positions", forcingPositions,
                "targets", targetPositions,
                "box", hint.getBox());
        String html = new StringSubstitutor(args).replace(template);
        showHintInfo(hint, html);
    }
    
    public void showNakedMultipleInfo(NakedMultiple multiple) {
        StringBuilder s = new StringBuilder("<html>Found a ")
                .append(multiple.getTechnique().getName())
                .append(":<br>");
        multiple.getForcingPositions().forEach(p -> s.append(p).append("<br>"));
        s.append("Values: ").append(multiple.getValues()).append("</html>");
        showHintInfo(multiple, s.toString());
    }
    
    public void showHiddenMultipleInfo(HiddenMultiple multiple) {
        StringBuilder s = new StringBuilder("<html>Found a ")
                .append(multiple.getTechnique().getName())
                .append(" of ")
                .append(sortedStringOfValues(multiple.getHiddenValues()))
                .append("<br><br>Values that can be eliminated:<br>");
        for (Position t : multiple.getTargets()) {
            s.append(t)
                .append(": ")
                .append(sortedStringOfValues(multiple.getValuesToEliminate(t)))
                .append("<br>");
        }
        showHintInfo(multiple, s.toString());
    }
    
    public void showXWingInfo(XWing xwing) {
        StringBuilder s = new StringBuilder("<html>Found an X-Wing:<br><br>");
        s.append("Positions: ");
        s.append(xwing.getForcingPositions().stream().map(Object::toString).collect(Collectors.joining(" ")));
        s.append("<br><br>");
        s.append(xwing.getValue()).append(" can be eliminated from:<br>");
        s.append(xwing.getTargetPositions().stream().map(Object::toString).collect(Collectors.joining(" ")));
        s.append("</html>");
        showHintInfo(xwing, s.toString());
    }
    
    public void showXyWingInfo(XyWing xyWing) {
        StringBuilder s = new StringBuilder("<html>Found an XY-Wing:<br>");
        s.append(xyWing.getCenter());
        xyWing.getWings().forEach(w -> s.append("<br>").append(w));
        s.append("<br><br>").append(xyWing.getValue().toInt())
            .append(" can be eliminated from these cells:");
        xyWing.getTargetPositions().forEach(t -> s.append("<br>").append(t));
        s.append("</html>");
        showHintInfo(xyWing, s.toString());
    }
    
    public void showSimpleColoringInfo(SimpleColoring simpleColoring) {
        // TODO: I need to include more info
        String s = "<html>Simple Coloring eliminates the value " + simpleColoring.getValue() + 
                " from these cells:<br><br>" + simpleColoring.getTargets().stream().map(Object::toString).collect(joining(" ")) +
                "</html>";
        showHintInfo(simpleColoring, s);
    }
    
    public void showSwordfishInfo(Swordfish swordfish) {
        String s = "<html>A Swordfish in " +
                String.format("%s %d, %d, and %d ", (swordfish.getHouseType() == Type.ROW ? "rows" : "columns"), 
                        swordfish.getHouses().get(0).getNumber(),
                        swordfish.getHouses().get(1).getNumber(),
                        swordfish.getHouses().get(2).getNumber()) +
                "eliminates the value " + swordfish.getValue() + 
                " from these cells:<br><br>" + swordfish.getTargetPositions().stream().map(Object::toString).collect(joining(" ")) +
                "</html>";
        showHintInfo(swordfish, s);
    }

    private static String sortedStringOfValues(Collection<Value> values) {
        return values.stream().sorted().map(Object::toString).collect(joining(" "));
    }
    
    private void showHintInfo(Hint hint, String html) {
        HintCellDecorator decorator = cellDecorators.getDecorator(hint);
        decorator.decorate();
        try {
            JOptionPane.showMessageDialog(appFrame, new JLabel(html));
        } finally {
            decorator.clear();
        }
    }

}
