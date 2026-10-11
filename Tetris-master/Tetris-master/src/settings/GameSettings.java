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

/**
 * 공통 설정 데이터와 Properties 파일 저장소. 입력 처리나 사운드를 변경하지 않는다.
 * 생성 시 불러오며, 변경은 save() 호출 시에만 저장한다.
 */
public final class GameSettings {
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
    private final EnumMap<TetrisAction, Integer> tetrisKeys = new EnumMap<>(TetrisAction.class);
    private final EnumMap<DodgeAction, Integer> dodgeKeys = new EnumMap<>(DodgeAction.class);

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
    public synchronized int getTetrisKey(TetrisAction action) { return tetrisKeys.get(Objects.requireNonNull(action)); }
    public synchronized int getDodgeKey(DodgeAction action) { return dodgeKeys.get(Objects.requireNonNull(action)); }
    public synchronized void setTetrisKey(TetrisAction action, int key) {
        Objects.requireNonNull(action);
        tetrisKeys.put(action, key(key, action.defaultKey));
    }
    public synchronized void setDodgeKey(DodgeAction action, int key) {
        Objects.requireNonNull(action);
        dodgeKeys.put(action, key(key, action.defaultKey));
    }

    /** 메모리만 초기화한다. 파일 반영은 save()로 명시한다. */
    public synchronized void resetToDefaults() {
        bgmVolume = DEFAULT_BGM_VOLUME;
        sfxVolume = DEFAULT_SFX_VOLUME;
        muted = false;
        for (TetrisAction action : TetrisAction.values()) tetrisKeys.put(action, action.defaultKey);
        for (DodgeAction action : DodgeAction.values()) dodgeKeys.put(action, action.defaultKey);
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
        for (TetrisAction action : TetrisAction.values())
            setTetrisKey(action, integer(properties, "tetris.key." + action.name(), action.defaultKey));
        for (DodgeAction action : DodgeAction.values())
            setDodgeKey(action, integer(properties, "dodge.key." + action.name(), action.defaultKey));
        return true;
    }

    /** 저장 실패는 호출자가 처리할 수 있게 IOException으로 전달한다. 메모리 값은 유지한다. */
    public synchronized void save() throws IOException {
        Properties properties = new Properties();
        properties.setProperty("bgmVolume", Integer.toString(bgmVolume));
        properties.setProperty("sfxVolume", Integer.toString(sfxVolume));
        properties.setProperty("muted", Boolean.toString(muted));
        for (TetrisAction action : TetrisAction.values())
            properties.setProperty("tetris.key." + action.name(), Integer.toString(tetrisKeys.get(action)));
        for (DodgeAction action : DodgeAction.values())
            properties.setProperty("dodge.key." + action.name(), Integer.toString(dodgeKeys.get(action)));
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
