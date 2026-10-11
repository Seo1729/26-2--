package settings;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;
import settings.GameSettings.Game;

/** 모든 게임과 사운드가 공유하는 설정 및 키 코드 변환 경로. */
public final class GameControls {
    private static final GameSettings SETTINGS = new GameSettings();
    private static final CopyOnWriteArrayList<Runnable> listeners = new CopyOnWriteArrayList<>();
    private static long revision;
    private static String saveError;
    private static int settingsWindows;
    private static Runnable soundUpdater = () -> { };
    private GameControls() { }
    public static GameSettings settings() { return SETTINGS; }
    public static synchronized long revision() { return revision; }
    public static synchronized String getSaveError() { return saveError; }
    public static synchronized boolean isInputBlocked() { return settingsWindows > 0; }
    public static void beginSettings() {
        synchronized (GameControls.class) { settingsWindows++; }
        changed();
    }
    public static void endSettings() {
        synchronized (GameControls.class) { settingsWindows = Math.max(0, settingsWindows - 1); }
        changed();
    }
    public static void setSoundUpdater(Runnable updater) { soundUpdater = java.util.Objects.requireNonNull(updater); }
    public static void setBgmVolume(int value) { SETTINGS.setBgmVolume(value); soundUpdater.run(); save(); }
    public static void setSfxVolume(int value) { SETTINGS.setSfxVolume(value); soundUpdater.run(); save(); }
    public static void resetAll() { SETTINGS.resetToDefaults(); soundUpdater.run(); changed(); save(); }
    public static void addListener(Runnable listener) { listeners.addIfAbsent(listener); }
    public static void removeListener(Runnable listener) { listeners.remove(listener); }
    public static int canonical(Game game, int physicalCode) {
        if (isInputBlocked()) return java.awt.event.KeyEvent.VK_UNDEFINED;
        Map<String, Integer> defaults = GameSettings.defaultKeys(game);
        for (Map.Entry<String, Integer> entry : SETTINGS.getKeys(game).entrySet())
            if (entry.getValue() == physicalCode) return defaults.get(entry.getKey());
        return java.awt.event.KeyEvent.VK_UNDEFINED;
    }
    public static int physical(Game game, int defaultCode) {
        for (Map.Entry<String, Integer> entry : GameSettings.defaultKeys(game).entrySet())
            if (entry.getValue() == defaultCode) return SETTINGS.getKeys(game).get(entry.getKey());
        throw new IllegalArgumentException("알 수 없는 기본 키입니다.");
    }
    public static void setKey(Game game, String action, int code) {
        SETTINGS.setKey(game, action, code); changed(); save();
    }
    public static void reset(Game game) { SETTINGS.resetKeys(game); changed(); save(); }
    public static void changed() {
        synchronized (GameControls.class) { revision++; }
        for (Runnable listener : listeners)
            if (SwingUtilities.isEventDispatchThread()) listener.run();
            else SwingUtilities.invokeLater(listener);
    }
    public static synchronized void save() {
        try { SETTINGS.save(); saveError = null; }
        catch (IOException | SecurityException e) {
            saveError = "저장하지 못했습니다. 이번 실행에만 적용됩니다.";
        }
    }
}
