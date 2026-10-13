package dodge;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Classpath PNGs, facing right, aligned to a full character-sized canvas. */
public final class CharacterItem {
    public enum Category { SKIN, HAT, OUTFIT }
    private final Category category;
    private final String id, name;
    private final Map<String, String> frames;

    public CharacterItem(Category category, String id, String name, Map<String, String> frames) {
        this.category = Objects.requireNonNull(category);
        if (id == null || id.trim().isEmpty() || CharacterAppearance.NONE.equals(id))
            throw new IllegalArgumentException("Invalid cosmetic ID");
        this.id = id;
        this.name = Objects.requireNonNull(name);
        Map<String, String> copy = new LinkedHashMap<>();
        Objects.requireNonNull(frames).forEach((frame, path) -> {
            if (!java.util.Arrays.asList("idle", "walk1", "walk2", "jump", "fall", "crushed").contains(frame))
                throw new IllegalArgumentException("Unknown animation frame: " + frame);
            if (path == null || !path.startsWith("/") || !path.endsWith(".png"))
                throw new IllegalArgumentException("Use an absolute classpath PNG path");
            copy.put(frame, path);
        });
        if (!copy.containsKey("idle")) throw new IllegalArgumentException("idle is required");
        this.frames = Collections.unmodifiableMap(copy);
    }
    public Category getCategory() { return category; }
    public String getId() { return id; }
    public String getName() { return name; }
    public Map<String, String> getFrames() { return frames; }
}
