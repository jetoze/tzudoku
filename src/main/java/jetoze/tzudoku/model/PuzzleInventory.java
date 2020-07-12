package jetoze.tzudoku.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import tzeth.exceptions.NotImplementedYetException;

public class PuzzleInventory {
    private static final String FILE_EXTENSION = ".json";
    private static final String PROPERTIES_FILE = ".properties";
    private static final String PROGRESS_FOLDER = "progress";
    
    private final File directory;
    private final Properties puzzleProperties = new Properties();
    
    public PuzzleInventory(File directory) {
        this.directory = requireNonNull(directory);
        checkArgument(directory.isDirectory(), "Not a directory: " + directory.toPath());
        checkArgument(!directory.exists(), "Directory does not exist: " + directory.toPath());
        loadProperties();
    }
    
    private void loadProperties() {
        File file = new File(directory, PROPERTIES_FILE);
        if (!file.canRead()) {
            return;
        }
        try {
            try (FileReader fr = new FileReader(file)) {
                puzzleProperties.load(fr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public ImmutableList<PuzzleInfo> listAvailablePuzzles() {
        File[] files = directory.listFiles((dir, name) -> name.endsWith(FILE_EXTENSION));
        if (files == null) {
            return ImmutableList.of();
        }
        return Stream.of(files)
                .map(this::getPuzzleName)
                .map(this::toPuzzleInfo)
                .collect(toImmutableList());
                
    }
    
    private String getPuzzleName(File file) {
        String name = file.getName();
        assert name.endsWith(FILE_EXTENSION);
        return name.substring(0, name.length() - FILE_EXTENSION.length());
    }
    
    private PuzzleInfo toPuzzleInfo(String name) {
        PuzzleState state = getPuzzleState(name);
        return new PuzzleInfo(name, state);
    }
    
    private PuzzleState getPuzzleState(String name) {
        try {
            return puzzleProperties.containsKey(name)
                    ? PuzzleState.valueOf(puzzleProperties.getProperty(name))
                    : PuzzleState.NEW;
        } catch (Exception e) {
            e.printStackTrace();
            return PuzzleState.NEW;
        }
    }
    
    public Puzzle loadPuzzle(PuzzleInfo info) {
        throw new NotImplementedYetException();
    }
    
    public void saveProgress(Puzzle puzzle) throws IOException {
        // TODO: Needs to change once we have more information in the puzzle, e.g.
        // sandwiches, killer cages, and thermos.
        GridState gridState = new GridState(puzzle.getGrid());
        String json = gridState.toJson();
        File progressFolder = new File(directory, PROGRESS_FOLDER);
        File progressFile = new File(progressFolder, puzzle.getName() + "_progress" + FILE_EXTENSION);
        Files.writeString(progressFile.toPath(), json);
    }
}
