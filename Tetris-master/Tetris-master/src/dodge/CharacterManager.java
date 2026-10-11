package dodge;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.util.*;
import javax.imageio.ImageIO;

/** Validated cosmetic catalog and explicit save/load, independent of game physics. */
public final class CharacterManager {
    private static final class Shared {
        static final CharacterManager INSTANCE = new CharacterManager();
    }
    public static CharacterManager getInstance() { return Shared.INSTANCE; }
    private final Path file;
    private final EnumMap<CharacterItem.Category, Map<String, CharacterItem>> catalog =
        new EnumMap<>(CharacterItem.Category.class);
    private final EnumMap<CharacterItem.Category, Map<String, Map<String, BufferedImage>>> images =
        new EnumMap<>(CharacterItem.Category.class);
    private CharacterAppearance appearance = CharacterAppearance.defaults();
    private long revision;

    public CharacterManager() {
        this(Paths.get(System.getProperty("user.home"), ".jbnu-tetris", "character-appearance.properties"));
    }
    public CharacterManager(Path file) { this(file, Collections.emptyList()); }

    /** Register extensions before loading their persisted IDs. */
    public CharacterManager(Path file, Collection<CharacterItem> items) {
        this.file = Objects.requireNonNull(file).toAbsolutePath();
        for (CharacterItem.Category category : CharacterItem.Category.values()) {
            catalog.put(category, new LinkedHashMap<>());
            images.put(category, new LinkedHashMap<>());
        }
        Map<String, String> frames = new LinkedHashMap<>();
        for (String frame : DodgerSprite.NAMES)
            frames.put(frame, "/dodge/images/dodger_" + frame + ".png");
        CharacterItem base = new CharacterItem(CharacterItem.Category.SKIN, "default", "기본 캐릭터", frames);
        catalog.get(base.getCategory()).put(base.getId(), base);
        // Retain the old procedural fallback when packaged default images are missing.
        images.get(base.getCategory()).put(base.getId(), readImages(base, false));
        for (CharacterItem item : items) register(item);
        try { load(); }
        catch (IOException e) { System.err.println("캐릭터 설정을 읽지 못함: " + e.getMessage()); }
    }

    /** Missing or invalid supplied PNGs are rejected; no placeholder items are registered. */
    public synchronized void register(CharacterItem item) {
        Objects.requireNonNull(item);
        if (catalog.get(item.getCategory()).containsKey(item.getId()))
            throw new IllegalArgumentException("Duplicate cosmetic ID: " + item.getId());
        Map<String, BufferedImage> loaded = readImages(item, true);
        catalog.get(item.getCategory()).put(item.getId(), item);
        images.get(item.getCategory()).put(item.getId(), loaded);
    }
    private static Map<String, BufferedImage> readImages(CharacterItem item, boolean strict) {
        Map<String, BufferedImage> result = new LinkedHashMap<>();
        item.getFrames().forEach((frame, path) -> {
            try {
                java.net.URL url = CharacterManager.class.getResource(path);
                BufferedImage image = url == null ? null : ImageIO.read(url);
                if (image == null) throw new IOException("Missing or invalid PNG: " + path);
                result.put(frame, image);
            } catch (IOException e) {
                if (strict) throw new IllegalArgumentException(e.getMessage(), e);
            }
        });
        return Collections.unmodifiableMap(result);
    }
    public synchronized List<CharacterItem> getItems(CharacterItem.Category category) {
        return Collections.unmodifiableList(new ArrayList<>(catalog.get(category).values()));
    }
    public Path getFile() { return file; }
    public synchronized CharacterAppearance getAppearance() { return appearance; }
    public synchronized long getRevision() { return revision; }
    public synchronized void selectSkin(String id) {
        select(new CharacterAppearance(id, appearance.getHatId(), appearance.getOutfitId()));
    }
    public synchronized void selectHat(String id) {
        select(new CharacterAppearance(appearance.getSkinId(), id, appearance.getOutfitId()));
    }
    public synchronized void selectOutfit(String id) {
        select(new CharacterAppearance(appearance.getSkinId(), appearance.getHatId(), id));
    }
    public synchronized void select(CharacterAppearance selection) {
        require(CharacterItem.Category.SKIN, selection.getSkinId());
        require(CharacterItem.Category.HAT, selection.getHatId());
        require(CharacterItem.Category.OUTFIT, selection.getOutfitId());
        appearance = selection;
        revision++;
    }
    private void require(CharacterItem.Category category, String id) {
        if (category != CharacterItem.Category.SKIN && CharacterAppearance.NONE.equals(id)) return;
        if (!catalog.get(category).containsKey(id)) throw new IllegalArgumentException("Unknown cosmetic: " + id);
    }
    private String validOrDefault(CharacterItem.Category category, String id, String fallback) {
        return catalog.get(category).containsKey(id) ? id : fallback;
    }
    public synchronized boolean load() throws IOException {
        Properties properties = new Properties();
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                try { properties.load(reader); }
                catch (IllegalArgumentException e) { throw new IOException("Invalid appearance settings", e); }
            }
        } else { select(CharacterAppearance.defaults()); return false; }
        select(new CharacterAppearance(
            validOrDefault(CharacterItem.Category.SKIN, properties.getProperty("skin"), "default"),
            validOrDefault(CharacterItem.Category.HAT, properties.getProperty("hat"), "none"),
            validOrDefault(CharacterItem.Category.OUTFIT, properties.getProperty("outfit"), "none")));
        return true;
    }
    public synchronized void save() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("skin", appearance.getSkinId());
        properties.setProperty("hat", appearance.getHatId());
        properties.setProperty("outfit", appearance.getOutfitId());
        Files.createDirectories(file.getParent());
        Path temporary = Files.createTempFile(file.getParent(), "appearance-", ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
                properties.store(writer, "Dodge character cosmetics");
            }
            try { Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException e) {
                Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally { Files.deleteIfExists(temporary); }
    }
    synchronized Map<String, BufferedImage> frames(CharacterItem.Category category, String id) {
        return images.get(category).getOrDefault(id, Collections.emptyMap());
    }
}
