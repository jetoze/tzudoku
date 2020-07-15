package jetoze.tzudoku;

import static com.google.common.base.Preconditions.checkArgument;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import jetoze.tzudoku.model.Puzzle;
import jetoze.tzudoku.model.PuzzleInfo;
import jetoze.tzudoku.model.PuzzleState;
import jetoze.tzudoku.model.PuzzleStorageRepresentation;

public class PuzzleInventory {
    // TODO: Add utilities for cleaning up old progress files.
    
    private static final String FILE_EXTENSION = ".json";
    private static final String PROPERTIES_FILE = "properties";
    private static final String PROGRESS_FOLDER = "progress";
    private static final String ARCHIVE_FOLDER = "archive";
    
    private final File directory;
    private final Map<String, PuzzleInfo> puzzleInfos;
    private final PuzzleProperties puzzleProperties;
    
    public PuzzleInventory(File directory) {
        this.directory = requireNonNull(directory);
        checkArgument(directory.isDirectory(), "Not a directory: " + directory.toPath());
        checkArgument(directory.exists(), "Directory does not exist: " + directory.toPath());
        puzzleProperties = new PuzzleProperties(new File(directory, PROPERTIES_FILE));
        puzzleProperties.load();
        this.puzzleInfos = load();
    }
    
    public ImmutableList<PuzzleInfo> listAvailablePuzzles() {
        return ImmutableList.copyOf(puzzleInfos.values());
    }

    private Map<String, PuzzleInfo> load() {
        File[] files = directory.listFiles((dir, name) -> name.endsWith(FILE_EXTENSION));
        if (files == null) {
            return new HashMap<>();
        }
        return Stream.of(files)
                .map(this::getPuzzleId)
                .map(this::toPuzzleInfo)
                .collect(Collectors.toMap(PuzzleInfo::getName, i -> i));

    }
    
    private String getPuzzleId(File file) {
        String name = file.getName();
        assert name.endsWith(FILE_EXTENSION);
        return name.substring(0, name.length() - FILE_EXTENSION.length());
    }
    
    private PuzzleInfo toPuzzleInfo(String id) {
        String name = puzzleProperties.getName(id);
        PuzzleState state = puzzleProperties.getState(id);
        ZonedDateTime lastUpdated = puzzleProperties.getLastUpdated(id);
        return new PuzzleInfo(name, state, lastUpdated);
    }
    
    public void addNewPuzzle(Puzzle puzzle) throws IOException {
        checkArgument(!puzzleInfos.containsKey(puzzle.getName()),
                "A puzzle with the same name already exists: %s", puzzle.getName());
        String id = getPuzzleId(puzzle.getName());
        File file = getPuzzleFile(id);
        savePuzzleToDisk(puzzle, file);
        puzzleInfos.put(puzzle.getName(), new PuzzleInfo(puzzle.getName(), PuzzleState.NEW, null));
        puzzleProperties.addPuzzle(puzzle, id);
        puzzleProperties.save();
    }
    
    private String getPuzzleId(String puzzleName) {
        return puzzleName.replace(' ', '_');
    }

    private void savePuzzleToDisk(Puzzle puzzle, File target) throws IOException {
        PuzzleStorageRepresentation gridState = new PuzzleStorageRepresentation(puzzle);
        String json = gridState.toJson();
        Files.writeString(target.toPath(), json);
    }
    
    public Puzzle loadPuzzle(PuzzleInfo info) throws IOException {
        File file = getPuzzleFile(info);
        String json = Files.readString(file.toPath());
        PuzzleStorageRepresentation p = PuzzleStorageRepresentation.fromJson(json);
        return p.restorePuzzle(info.getName());
    }
    
    private File getPuzzleFile(PuzzleInfo info) {
        String id = getPuzzleId(info.getName());
        if (info.getState() == PuzzleState.PROGRESS) {
            File file = getProgressFile(id);
            if (file.canRead()) {
                return file;
            }
        }
        return getPuzzleFile(id);
    }

    private File getPuzzleFile(String id) {
        return new File(directory, id + FILE_EXTENSION);
    }

    public void markAsCompleted(Puzzle puzzle) {
        String id = getPuzzleId(puzzle.getName());
        updatePuzzleState(id, PuzzleState.SOLVED);
        // TODO: Delete progress file, if one exists?
    }
    
    public void saveProgress(Puzzle puzzle) throws IOException {
        if (puzzle.isSolved()) {
            markAsCompleted(puzzle);
        } else {
            // TODO: Do not overwrite previous saves. For example, append timestamp to
            // the file name. Then add utilities for loading an earlier save.
            String id = getPuzzleId(puzzle.getName());
            File progressFile = getProgressFile(id);
            savePuzzleToDisk(puzzle, progressFile);
            updatePuzzleState(id, PuzzleState.PROGRESS);
        }
    }

    private File getProgressFile(String id) {
        File progressFolder = new File(directory, PROGRESS_FOLDER);
        return new File(progressFolder, id + "_progress" + FILE_EXTENSION);
    }

    private void updatePuzzleState(String id, PuzzleState state) {
        puzzleProperties.setState(id, state);
        puzzleProperties.save();
    }
    
    public void archive(PuzzleInfo puzzleInfo) throws IOException {
        Path source = new File(directory, puzzleInfo.getName() + FILE_EXTENSION).toPath();
        Path archiveFolder = new File(directory, ARCHIVE_FOLDER).toPath();
        Files.move(source, archiveFolder.resolve(source.getFileName()), StandardCopyOption.REPLACE_EXISTING);
        String id = getPuzzleId(puzzleInfo.getName());
        puzzleProperties.remove(id);
        puzzleProperties.save();
    }
    
    
    
    private static class PuzzleProperties {
        private static final String NAME_PROPERTY = ".name";
        private static final String STATE_PROPERTY = ".state";
        private static final String LAST_UPDATED_PROPERTY = ".lastUpdated";
        
        private final File storageFile;
        private final Properties properties = new Properties();
        
        public PuzzleProperties(File storageFile) {
            this.storageFile = storageFile;
        }
        
        public String getName(String id) {
            return properties.getProperty(id + NAME_PROPERTY, id);
        }
        
        public void addPuzzle(Puzzle puzzle, String id) {
            properties.setProperty(id + NAME_PROPERTY, puzzle.getName());
            properties.setProperty(id + STATE_PROPERTY, PuzzleState.NEW.name());
        }
        
        public void setState(String id, PuzzleState state) {
            properties.setProperty(id + STATE_PROPERTY, state.name());
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.SECONDS);
            properties.setProperty(id + LAST_UPDATED_PROPERTY, 
                    DateTimeFormatter.ISO_DATE_TIME.format(now));
        }
        
        public PuzzleState getState(String id) {
            String key = id + STATE_PROPERTY;
            try {
                return properties.containsKey(key)
                        ? PuzzleState.valueOf(properties.getProperty(key))
                        : PuzzleState.NEW;
            } catch (Exception e) {
                e.printStackTrace();
                return PuzzleState.NEW;
            }
        }
        
        @Nullable
        public ZonedDateTime getLastUpdated(String id) {
            String key = id + LAST_UPDATED_PROPERTY;
            if (properties.containsKey(key)) {
                try {
                    String value = properties.getProperty(key);
                    return ZonedDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
                } catch (DateTimeParseException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        
        public void remove(String id) {
            properties.remove(id + NAME_PROPERTY);
            properties.remove(id + STATE_PROPERTY);
            properties.remove(id + LAST_UPDATED_PROPERTY);
        }
        
        public void load() {
            if (!storageFile.canRead()) {
                return;
            }
            try {
                try (FileReader fr = new FileReader(storageFile)) {
                    properties.load(fr);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        public void save() {
            if (!storageFile.canWrite()) {
                return;
            }
            try {
                try (FileWriter fw = new FileWriter(storageFile)) {
                    properties.store(fw, "");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}