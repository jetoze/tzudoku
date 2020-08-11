package jetoze.tzudoku.ui.hint;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.apache.commons.text.StringSubstitutor;

import com.google.common.collect.ImmutableMap;

import jetoze.tzudoku.hint.BoxLineReduction;
import jetoze.tzudoku.hint.HiddenMultiple;
import jetoze.tzudoku.hint.Hint;
import jetoze.tzudoku.hint.NakedMultiple;
import jetoze.tzudoku.hint.PointingPair;
import jetoze.tzudoku.hint.SimpleColoring;
import jetoze.tzudoku.hint.Single;
import jetoze.tzudoku.hint.Swordfish;
import jetoze.tzudoku.hint.XWing;
import jetoze.tzudoku.hint.YWing;
import jetoze.tzudoku.hint.XyzWing;
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
        } else if (hint instanceof YWing) {
            showYWingInfo((YWing) hint);
        } else if (hint instanceof SimpleColoring) {
            showSimpleColoringInfo((SimpleColoring) hint);
        } else if (hint instanceof Swordfish) {
            showSwordfishInfo((Swordfish) hint);
        } else {
            throw new UnsupportedOperationException("I don't know how to display hints of type " + hint.getTechnique());
        }
    }
    
    public void showSingleInfo(Single single) {
        String s = getSingleInfoText(single);
        showHintInfo(single, s);
    }
    
    private String getSingleInfoText(Single single) {
        if (single.isNaked()) {
            return "<html>" + single.getPosition() + " is a Naked Single: " + single.getValue() + "</html>";
        } else {
            String template = "<html>${cell} is a Hidden Single. It is the only cell in ${house} where "
                    + "the digit ${value} can be placed.</html>";
            Map<String, Object> args = ImmutableMap.of(
                    "cell", single.getPosition(),
                    "house", single.getHouse(),
                    "value", single.getValue());
            return new StringSubstitutor(args).replace(template);
        }
    }

    public void showPointingPairInfo(PointingPair hint) {
        String template = "<html>Found a Pointing Pair:<br><br>" +
                "The digit ${value} in ${box} is confined to ${positions} in ${rowOrColumn}.<br>" +
                "${value} can therefore be eliminated from ${targets} in ${rowOrColumn}.</html>";
        String forcingPositions = positionsInOrder(hint.getForcingPositions(), hint.getRowOrColumn());
        String targetPositions = positionsInOrder(hint.getTargetPositions(), hint.getRowOrColumn());
        Map<String, Object> args = ImmutableMap.of(
                "value", hint.getValue(),
                "box", hint.getBox(),
                "positions", forcingPositions,
                "targets", targetPositions,
                "rowOrColumn", hint.getRowOrColumn());
        String html = new StringSubstitutor(args).replace(template);
        showHintInfo(hint, html);
    }

    public void showBoxLineReductionInfo(BoxLineReduction hint) {
        String template = "<html>Found a Box Line Reduction:<br><br>" +
                "The digit ${value} in ${rowOrColumn} is confined to positions ${positions} in ${box}.<br>" +
                "${value} can therefore be eliminated from ${targets} in ${box}.</html>";
        String forcingPositions = positionsInOrder(hint.getForcingPositions(), hint.getRowOrColumn());
        String targetPositions = positionsInOrder(hint.getTargetPositions(), hint.getBox());
        Map<String, Object> args = ImmutableMap.of(
                "value", hint.getValue(),
                "rowOrColumn", hint.getRowOrColumn(),
                "positions", forcingPositions,
                "targets", targetPositions,
                "box", hint.getBox());
        String html = new StringSubstitutor(args).replace(template);
        showHintInfo(hint, html);
    }
    
    public void showNakedMultipleInfo(NakedMultiple hint) {
        String template = "<html>Found a ${technique}:<br><br>" +
                "The digits ${values} are confined to cells ${positions} in ${house}.<br>" +
                "These values can therefore be eliminated from all other positions in the ${houseType}:<br>" +
                "${targets}</html>";
        Map<String, Object> args = ImmutableMap.<String, Object>builder()
                .put("technique", hint.getTechnique())
                .put("values", valuesInOrder(hint.getValues()))
                .put("positions", positionsInOrder(hint.getForcingPositions(), hint.getHouse()))
                .put("house", hint.getHouse())
                .put("houseType", hint.getHouse().getType())
                .put("targets", positionsInOrder(hint.getTargetPositions(), hint.getHouse()))
                .build();
        String html = new StringSubstitutor(args).replace(template);
        showHintInfo(hint, html);
    }
    
    public void showHiddenMultipleInfo(HiddenMultiple multiple) {
        StringBuilder s = new StringBuilder("<html>Found a ")
                .append(multiple.getTechnique().getName())
                .append(" of ")
                .append(valuesInOrder(multiple.getHiddenValues()))
                .append("<br><br>Values that can be eliminated:<br>");
        for (Position t : multiple.getTargets()) {
            s.append(t)
                .append(": ")
                .append(valuesInOrder(multiple.getValuesToEliminate(t)))
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
    
    public void showYWingInfo(YWing yWing) {
        StringBuilder s = new StringBuilder("<html>Found a Y-Wing:<br>");
        s.append(yWing.getHinge());
        yWing.getWings().forEach(w -> s.append("<br>").append(w));
        s.append("<br><br>").append(yWing.getValue().toInt())
            .append(" can be eliminated from these cells:");
        yWing.getTargetPositions().forEach(t -> s.append("<br>").append(t));
        s.append("</html>");
        showHintInfo(yWing, s.toString());
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
                " from these cells:<br><br>" + positions(swordfish.getTargetPositions()) +
                "</html>";
        showHintInfo(swordfish, s);
    }
    
    public void showXyzWingInfo(XyzWing hint) {
        String template = "<html>An XYZ-Wing centered at ${center} and with wings at ${wing1} and ${wing2}<br>" +
                "eliminates the value ${value} from ${targets}.</html>";
        Iterator<Position> itWings = hint.getWings().iterator();
        Map<String, Object> args = ImmutableMap.of(
                "center", hint.getHinge(),
                "wing1", itWings.next(),
                "wing2", itWings.next(),
                "value", hint.getValue(),
                "targets", positions(hint.getTargetPositions()));
        String html = new StringSubstitutor(args).replace(template);
        showHintInfo(hint, html);
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

    private static String valuesInOrder(Collection<Value> values) {
        return values.stream().sorted().map(Object::toString).collect(joining(" "));
    }
    
    private static String positions(Collection<Position> positions) {
        return positions.stream()
                .map(Object::toString)
                .collect(joining(" "));
    }
    
    private static String positionsInOrder(Collection<Position> positions, House house) {
        return positions.stream()
                .sorted(house.getType().positionOrder())
                .map(Object::toString)
                .collect(joining(" "));
    }

}
