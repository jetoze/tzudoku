package jetoze.tzudoku;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import jetoze.tzudoku.model.Grid;
import jetoze.tzudoku.model.GridState;
import jetoze.tzudoku.model.Puzzle;
import jetoze.tzudoku.model.PuzzleInfo;
import jetoze.tzudoku.model.PuzzleState;

public class PuzzleInventory {
    // TODO: Add utilities for cleaning up old progress files.
    
    private static final String FILE_EXTENSION = ".json";
    private static final String PROPERTIES_FILE = ".properties";
    private static final String STATE_PROPERTY = ".state";
    private static final String LAST_UPDATED_PROPERTY = ".lastUpdated";
    private static final String PROGRESS_FOLDER = "progress";
    private static final String ARCHIVE_FOLDER = "archive";
    
    private final File directory;
    private final Properties puzzleProperties = new Properties();
    
    public PuzzleInventory(File directory) {
        this.directory = requireNonNull(directory);
        checkArgument(directory.isDirectory(), "Not a directory: " + directory.toPath());
        checkArgument(directory.exists(), "Directory does not exist: " + directory.toPath());
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
        ZonedDateTime lastUpdated = getPuzzleLastUpdated(name);
        return new PuzzleInfo(name, state, lastUpdated);
    }
    
    private PuzzleState getPuzzleState(String name) {
        String key = name + STATE_PROPERTY;
        try {
            return puzzleProperties.containsKey(key)
                    ? PuzzleState.valueOf(puzzleProperties.getProperty(key))
                    : PuzzleState.NEW;
        } catch (Exception e) {
            e.printStackTrace();
            return PuzzleState.NEW;
        }
    }
    
    @Nullable
    private ZonedDateTime getPuzzleLastUpdated(String name) {
        String key = name + LAST_UPDATED_PROPERTY;
        if (puzzleProperties.containsKey(key)) {
            try {
                String value = puzzleProperties.getProperty(key);
                return ZonedDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    public void addNewPuzzle(Puzzle puzzle) throws IOException {
        File file = getPuzzleFile(puzzle.getName());
        if (file.exists()) {
            throw new IllegalArgumentException("A puzzle with the same name already exists: " + puzzle.getName());
        }
        GridState gridState = new GridState(puzzle.getGrid());
        String json = gridState.toJson();
        Files.writeString(file.toPath(), json);
        puzzleProperties.setProperty(puzzle.getName() + STATE_PROPERTY, PuzzleState.NEW.name());
        saveProperties();
    }
    
    public Puzzle loadPuzzle(PuzzleInfo info) throws IOException {
        File file = getPuzzleFile(info);
        String json = Files.readString(file.toPath());
        GridState gridState = GridState.fromJson(json);
        Grid grid = gridState.restoreGrid();
        return new Puzzle(info.getName(), grid);
    }
    
    private File getPuzzleFile(PuzzleInfo info) {
        if (info.getState() == PuzzleState.PROGRESS) {
            File file = getProgressFile(info.getName());
            if (file.canRead()) {
                return file;
            }
        }
        return getPuzzleFile(info.getName());
    }

    private File getPuzzleFile(String name) {
        return new File(directory, name + FILE_EXTENSION);
    }

    public void markAsCompleted(Puzzle puzzle) {
        updatePuzzleState(puzzle, PuzzleState.SOLVED);
        // TODO: Delete progress file, if one exists?
    }
    
    public void saveProgress(Puzzle puzzle) throws IOException {
        if (puzzle.isSolved()) {
            markAsCompleted(puzzle);
        } else {
            savePuzzleProgressToDisk(puzzle);
            updatePuzzleState(puzzle, PuzzleState.PROGRESS);
        }
    }

    private void savePuzzleProgressToDisk(Puzzle puzzle) throws IOException {
        // TODO: Do not overwrite previous saves. For example, append timestamp to
        // the file name. Then add utilities for loading an earlier save.
        GridState gridState = new GridState(puzzle.getGrid());
        String json = gridState.toJson();
        File progressFile = getProgressFile(puzzle.getName());
        Files.writeString(progressFile.toPath(), json);
    }

    private File getProgressFile(String puzzleName) {
        File progressFolder = new File(directory, PROGRESS_FOLDER);
        File progressFile = new File(progressFolder, puzzleName + "_progress" + FILE_EXTENSION);
        return progressFile;
    }

    private void updatePuzzleState(Puzzle puzzle, PuzzleState state) {
        puzzleProperties.setProperty(puzzle.getName() + STATE_PROPERTY, state.name());
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.SECONDS);
        puzzleProperties.setProperty(puzzle.getName() + LAST_UPDATED_PROPERTY, 
                DateTimeFormatter.ISO_DATE_TIME.format(now));
        saveProperties();
    }
    
    private void saveProperties() {
        File file = new File(directory, PROPERTIES_FILE);
        if (!file.canWrite()) {
            return;
        }
        try {
            try (FileWriter fw = new FileWriter(file)) {
                puzzleProperties.store(fw, "");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void archive(PuzzleInfo puzzleInfo) {
        try {
            Path source = new File(directory, puzzleInfo.getName() + FILE_EXTENSION).toPath();
            Path archiveFolder = new File(directory, ARCHIVE_FOLDER).toPath();
            Files.move(source, archiveFolder.resolve(source.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            puzzleProperties.remove(puzzleInfo.getName() + STATE_PROPERTY);
            puzzleProperties.remove(puzzleInfo.getName() + LAST_UPDATED_PROPERTY);
            saveProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
