package jetoze.tzudoku.model;

public enum CellColor {
    BLACK,
    GRAY,
    WHITE,
    GREEN,
    PURPLE,
    ORANGE,
    RED,
    YELLOW,
    BLUE;
    
    public static CellColor fromValue(Value value) {
        return values()[value.ordinal()];
    }
}
