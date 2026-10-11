package settings;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * 공통 설정 데이터와 Properties 파일 저장소. 입력 처리나 사운드를 변경하지 않는다.
 * 생성 시 불러오며, 변경은 save() 호출 시에만 저장한다.
 */
public final class GameSettings {
    public enum Game { TETRIS, DODGE, THREE_D, GALAGA }
    private final EnumMap<Game, Map<String, Integer>> bindings = new EnumMap<>(Game.class);
    public static Map<String, Integer> defaultKeys(Game game) {
        Map<String, Integer> keys = new LinkedHashMap<>();
        if (game == Game.TETRIS || game == Game.GALAGA) {
            for (TetrisAction a : TetrisAction.values()) keys.put(a.name(), a.defaultKey);
            if (game == Game.GALAGA) {
                keys.put("FIGHTER_LEFT", KeyEvent.VK_A); keys.put("FIGHTER_RIGHT", KeyEvent.VK_D);
                keys.put("SHOOT", KeyEvent.VK_W);
            }
        } else if (game == Game.DODGE) {
            for (DodgeAction a : DodgeAction.values()) keys.put(a.name(), a.defaultKey);
        } else {
            keys.put("FORWARD", KeyEvent.VK_W); keys.put("BACK", KeyEvent.VK_S);
            keys.put("LEFT", KeyEvent.VK_A); keys.put("RIGHT", KeyEvent.VK_D);
            keys.put("ROTATE_X", KeyEvent.VK_J); keys.put("ROTATE_Y", KeyEvent.VK_K);
            keys.put("ROTATE_Z", KeyEvent.VK_L); keys.put("HARD_DROP", KeyEvent.VK_SPACE);
            keys.put("RESTART", KeyEvent.VK_R);
        }
        return keys;
    }
    public synchronized Map<String, Integer> getKeys(Game game) {
        return java.util.Collections.unmodifiableMap(new LinkedHashMap<>(bindings.get(Objects.requireNonNull(game))));
    }
    /** 중복/예약 키는 거부하며 기존 설정을 유지한다. */
    public synchronized void setKey(Game game, String action, int code) {
        Map<String, Integer> keys = bindings.get(Objects.requireNonNull(game));
        if (!keys.containsKey(action)) throw new IllegalArgumentException("알 수 없는 동작입니다.");
        if (!isAssignableKey(code)) throw new IllegalArgumentException("시스템 예약 키 또는 사용할 수 없는 키입니다.");
        for (Map.Entry<String, Integer> entry : keys.entrySet())
            if (!entry.getKey().equals(action) && entry.getValue() == code)
                throw new IllegalArgumentException("이 게임의 다른 동작에 지정된 키입니다.");
        keys.put(action, code);
    }
    public synchronized void resetKeys(Game game) { bindings.put(game, defaultKeys(game)); }
    public static boolean isAssignableKey(int code) {
        return VALID_KEYS.contains(code) && code != KeyEvent.VK_ESCAPE && code != KeyEvent.VK_TAB
            && code != KeyEvent.VK_SHIFT && code != KeyEvent.VK_CONTROL && code != KeyEvent.VK_ALT
            && code != KeyEvent.VK_META && code != KeyEvent.VK_WINDOWS && code != KeyEvent.VK_ALT_GRAPH
            && code != KeyEvent.VK_F1 && code != KeyEvent.VK_F4 && code != KeyEvent.VK_CONTEXT_MENU;
    }
    public static final int DEFAULT_BGM_VOLUME = 40;
    public static final int DEFAULT_SFX_VOLUME = 70;

    public enum TetrisAction {
        DOWN(KeyEvent.VK_DOWN), LEFT(KeyEvent.VK_LEFT), RIGHT(KeyEvent.VK_RIGHT),
        USE_ITEM(KeyEvent.VK_1), ATTACK_ITEM(KeyEvent.VK_2), DELETE_ITEM(KeyEvent.VK_3),
        HARD_DROP(KeyEvent.VK_SPACE), ROTATE(KeyEvent.VK_UP), HOLD(KeyEvent.VK_C);

        private final int defaultKey;
        TetrisAction(int defaultKey) { this.defaultKey = defaultKey; }
        public int getDefaultKey() { return defaultKey; }
    }

    /** 똥피하기 모드의 두 플레이어와 공통 명령을 함께 관리한다. */
    public enum DodgeAction {
        TETRIS_LEFT(KeyEvent.VK_A), TETRIS_RIGHT(KeyEvent.VK_D),
        TETRIS_ROTATE(KeyEvent.VK_W), TETRIS_DOWN(KeyEvent.VK_S),
        LEFT(KeyEvent.VK_LEFT), RIGHT(KeyEvent.VK_RIGHT), JUMP(KeyEvent.VK_UP),
        PAUSE(KeyEvent.VK_P), RESTART(KeyEvent.VK_R);

        private final int defaultKey;
        DodgeAction(int defaultKey) { this.defaultKey = defaultKey; }
        public int getDefaultKey() { return defaultKey; }
    }

    private static final Set<Integer> VALID_KEYS = validKeys();
    private final Path file;
    private int bgmVolume;
    private int sfxVolume;
    private boolean muted;

    /** 기본 파일: 사용자 홈/.jbnu-tetris/game-settings.properties */
    public GameSettings() {
        this(Paths.get(System.getProperty("user.home"), ".jbnu-tetris", "game-settings.properties"));
    }

    public GameSettings(Path file) {
        this.file = Objects.requireNonNull(file, "file").toAbsolutePath();
        load();
    }

    public Path getFile() { return file; }
    public synchronized int getBgmVolume() { return bgmVolume; }
    public synchronized int getSfxVolume() { return sfxVolume; }
    public synchronized boolean isMuted() { return muted; }
    public synchronized void setMuted(boolean value) { muted = value; }
    public synchronized void setBgmVolume(int value) { bgmVolume = volume(value, DEFAULT_BGM_VOLUME); }
    public synchronized void setSfxVolume(int value) { sfxVolume = volume(value, DEFAULT_SFX_VOLUME); }
    public synchronized int getTetrisKey(TetrisAction action) { return bindings.get(Game.TETRIS).get(Objects.requireNonNull(action).name()); }
    public synchronized int getDodgeKey(DodgeAction action) { return bindings.get(Game.DODGE).get(Objects.requireNonNull(action).name()); }
    public synchronized void setTetrisKey(TetrisAction action, int key) {
        Objects.requireNonNull(action);
        setKey(Game.TETRIS, action.name(), key(key, action.defaultKey));
    }
    public synchronized void setDodgeKey(DodgeAction action, int key) {
        Objects.requireNonNull(action);
        setKey(Game.DODGE, action.name(), key(key, action.defaultKey));
    }

    /** 메모리만 초기화한다. 파일 반영은 save()로 명시한다. */
    public synchronized void resetToDefaults() {
        bgmVolume = DEFAULT_BGM_VOLUME;
        sfxVolume = DEFAULT_SFX_VOLUME;
        muted = false;
        for (Game game : Game.values()) resetKeys(game);
    }

    /** 파일이 없거나 읽을 수 없으면 기본값 유지. 개별 오류는 해당 항목만 복구한다. */
    public synchronized boolean load() {
        resetToDefaults();
        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (IOException | IllegalArgumentException | SecurityException e) {
            return false;
        }
        setBgmVolume(integer(properties, "bgmVolume", DEFAULT_BGM_VOLUME));
        setSfxVolume(integer(properties, "sfxVolume", DEFAULT_SFX_VOLUME));
        muted = "true".equalsIgnoreCase(properties.getProperty("muted", "false").trim());
        for (Game game : Game.values()) {
            Map<String, Integer> candidate = defaultKeys(game);
            Set<Integer> used = new HashSet<>();
            boolean valid = true;
            for (Map.Entry<String, Integer> entry : candidate.entrySet()) {
                int code = integer(properties, prefix(game) + entry.getKey(), entry.getValue());
                if (!isAssignableKey(code)) code = entry.getValue();
                entry.setValue(code);
                if (!used.add(code)) valid = false;
            }
            // 손상된 중복 설정은 해당 게임 전체를 기본값으로 복구한다.
            if (valid) bindings.put(game, candidate);
        }
        return true;
    }

    /** 저장 실패는 호출자가 처리할 수 있게 IOException으로 전달한다. 메모리 값은 유지한다. */
    public synchronized void save() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("bgmVolume", Integer.toString(bgmVolume));
        properties.setProperty("sfxVolume", Integer.toString(sfxVolume));
        properties.setProperty("muted", Boolean.toString(muted));
        for (Game game : Game.values())
            for (Map.Entry<String, Integer> entry : bindings.get(game).entrySet())
                properties.setProperty(prefix(game) + entry.getKey(), Integer.toString(entry.getValue()));
        Files.createDirectories(file.getParent());
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            properties.store(writer, "Common game settings (Java KeyEvent key codes)");
        }
    }

    private static int integer(Properties properties, String name, int fallback) {
        try { return Integer.parseInt(properties.getProperty(name, "").trim()); }
        catch (NumberFormatException e) { return fallback; }
    }
    private static int volume(int value, int fallback) { return value >= 0 && value <= 100 ? value : fallback; }
    private static int key(int value, int fallback) { return VALID_KEYS.contains(value) ? value : fallback; }
    private static String prefix(Game game) { return game.name().toLowerCase(java.util.Locale.ROOT) + ".key."; }

    /** 확장 키 코드도 포함하여 실제 KeyEvent VK_* 상수만 허용한다. */
    private static Set<Integer> validKeys() {
        Set<Integer> keys = new HashSet<>();
        for (Field field : KeyEvent.class.getFields()) {
            if (field.getName().startsWith("VK_") && field.getType() == int.class
                    && Modifier.isStatic(field.getModifiers())) {
                try { keys.add(field.getInt(null)); }
                catch (IllegalAccessException e) { throw new ExceptionInInitializerError(e); }
            }
        }
        keys.remove(KeyEvent.VK_UNDEFINED);
        return keys;
    }
}
