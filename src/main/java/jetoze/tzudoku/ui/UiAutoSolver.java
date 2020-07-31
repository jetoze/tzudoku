package jetoze.tzudoku.ui;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import com.google.common.collect.ImmutableSet;

import jetoze.gunga.UiThread;
import jetoze.tzudoku.hint.Hint;
import jetoze.tzudoku.hint.Multiple;
import jetoze.tzudoku.hint.PointingPair;
import jetoze.tzudoku.hint.Single;
import jetoze.tzudoku.hint.XyWing;
import jetoze.tzudoku.model.Grid;

/**
 * Attempts to auto-solve the puzzle currently loaded into the UI, giving visual feedback
 * of each individual step.
 * <p>
 * This auto solver only applies classic sudoku techniques.
 */
public class UiAutoSolver {
    
    // TODO: Place the app frame in blocking wait-state while the auto solver is running.
    // TODO: Allow the auto-solver to be stopped.

    // TODO: We need to blank out the current undo-redo state before starting.
    // TODO: Alternatively, rewrite the apply() method to work with the GridUiModel.
    //       Then we get undo-redo for free.

    private static final Duration DELAY = Duration.ofMillis(750L);
    
    private final JFrame appFrame;
    private final GridUiModel gridModel;
    
    public UiAutoSolver(JFrame appFrame, GridUiModel gridModel) {
        this.appFrame = requireNonNull(appFrame);
        this.gridModel = requireNonNull(gridModel);
    }

    public void start() {
        // First fill in all candidates.
        // Then find and apply hints in this order:
        //   1. Hidden Single
        //   2. Pointing Pair
        //   3. Triple
        //   4. XY wing
        //
        // If a hint is found, the next hint will *always* be Hidden Single. If no hint is
        // found, move to the next hint. If we reach the end of the hint list without finding
        // anything, we give up.
        //
        // We run this on a timer, that fires every second or so, for a more pleasant 
        // user experience.
        Controller controller = new Controller(appFrame, gridModel);
        controller.start();
    }
    
    
    private static class Controller {
        
        private final JFrame appFrame;
        private final GridUiModel model;
        
        public Controller(JFrame appFrame, GridUiModel model) {
            this.appFrame = appFrame;
            this.model = model;
        }

        public GridUiModel getModel() {
            return model;
        }
        
        public void start() {
            // TODO: Restore the original setting when we are done?
            model.getEliminateCandidatesProperty().set(true);
            new FillInCandidates().run(this);
        }
        
        public void stop() {
            UiThread.throwIfNotUiThread();
            if (model.getGrid().isSolved()) {
                showSuccessMessage();
            } else {
                JOptionPane.showMessageDialog(appFrame, "This puzzle proved too difficult for me to solve :(", 
                        "No Solution Found", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void showSuccessMessage() {
            JOptionPane.showMessageDialog(appFrame, "Ta-da!", "Solved", JOptionPane.INFORMATION_MESSAGE);
        }
        
        public void runNextStep(Step step) {
            UiThread.throwIfNotUiThread();
            requireNonNull(step);
            if (model.getGrid().isSolved()) {
                showSuccessMessage();
            } else {
                runStep(step);
            }
        }

        private void runStep(Step step) {
            Timer timer = new Timer((int) DELAY.toMillis(), e -> step.run(this));
            timer.setRepeats(false);
            timer.start();
        }

        public void updateUi(Hint hint) {
            // FIXME: Refactor the Step implementation so that we can pass Hints of
            // specific types, without having to cast.
            UiThread.run(() -> {
                if (hint instanceof Single) {
                    applyHint((Single) hint);
                } else if (hint instanceof PointingPair) {
                    applyHint((PointingPair) hint);
                } else if (hint instanceof Multiple) {
                    applyHint((Multiple) hint);
                } else if (hint instanceof XyWing) {
                    applyHint((XyWing) hint);
                }
            });
        }
        
        private void applyHint(Single single) {
            model.setEnterValueMode(EnterValueMode.NORMAL);
            model.selectCellAt(single.getPosition());
            model.enterValue(single.getValue());
        }
        
        private void applyHint(PointingPair pointingPair) {
            model.removeCandidatesFromCells(pointingPair.getTargets(), ImmutableSet.of(pointingPair.getValue()));
        }
        
        private void applyHint(Multiple multiple) {
            model.removeCandidatesFromCells(multiple.getTargets(), multiple.getValues());
        }
        
        private void applyHint(XyWing xyWing) {
            model.removeCandidatesFromCells(xyWing.getTargets(), ImmutableSet.of(xyWing.getValueThatCanBeEliminated()));
        }
    }
    
    
    private interface Step {
        
        void run(Controller controller);
        
    }
    
    
    private static class FillInCandidates implements Step {

        @Override
        public void run(Controller controller) {
            controller.getModel().showRemainingCandidates();
            controller.runNextStep(HintStep.HIDDEN_SINGLE);
        }
    }
    
    
    private static enum HintStep implements Step {
        
        HIDDEN_SINGLE(Single::findNext) {

            @Override
            protected Optional<HintStep> getNextStep() {
                return Optional.of(POINTING_PAIR);
            }

        },
        
        POINTING_PAIR(PointingPair::findNext) {

            @Override
            protected Optional<HintStep> getNextStep() {
                return Optional.of(TRIPLE);
            }
        },
        
        TRIPLE(Multiple::findNextTriple) {

            @Override
            protected Optional<HintStep> getNextStep() {
                return Optional.of(XY_WING);
            }
        },
        
        XY_WING(XyWing::findNext) {

            @Override
            protected Optional<HintStep> getNextStep() {
                // This is the last step.
                return Optional.empty();
            }
        };
        
        private final Function<Grid, Optional<? extends Hint>> hintFinder;
        
        private HintStep(Function<Grid, Optional<? extends Hint>> hintFinder) {
            this.hintFinder = hintFinder;
        }

        @Override
        public final void run(Controller controller) {
            Callable<Optional<HintStep>> job = () -> {
                Optional<? extends Hint> opt = hintFinder.apply(controller.getModel().getGrid());
                if (opt.isPresent()) {
                    controller.updateUi(opt.get());
                    return Optional.of(HIDDEN_SINGLE);
                } else {
                    return getNextStep();
                }
            };
            UiThread.offload(job, next -> next.ifPresentOrElse(controller::runNextStep, controller::stop));
        }
        
        protected abstract Optional<HintStep> getNextStep();
        
    }
    
}
