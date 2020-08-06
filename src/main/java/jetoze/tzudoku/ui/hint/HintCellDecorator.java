package jetoze.tzudoku.ui.hint;

public interface HintCellDecorator {

    public void decorate();
    
    public void clear();
    
    
    public static final HintCellDecorator NO_DECORATION = new HintCellDecorator() {
        
        @Override
        public void decorate() {/**/}
        
        @Override
        public void clear() {/**/}
    };
    
}
