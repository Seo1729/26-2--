import java.io.IOException;
import settings.GameSettings;

/** 기존 API를 유지하면서 공통 파일 설정을 사운드에 연결한다. */
public final class Settings {
    public static final int DEFAULT_SFX_VOLUME = GameSettings.DEFAULT_SFX_VOLUME;
    public static final int DEFAULT_BGM_VOLUME = GameSettings.DEFAULT_BGM_VOLUME;
    private static final GameSettings data = new GameSettings();
    private static String saveError;
    private Settings() { }
    public static synchronized void load() { data.load(); applyToSound(); }
    public static synchronized int getSfxVolume() { return data.getSfxVolume(); }
    public static synchronized int getBgmVolume() { return data.getBgmVolume(); }
    public static synchronized boolean isMuted() { return data.isMuted(); }
    public static synchronized String getSaveError() { return saveError; }
    public static synchronized void setSfxVolume(int percent) {
        data.setSfxVolume(Math.max(0, Math.min(100, percent)));
        SoundManager.setSfxVolume(data.getSfxVolume() / 100f);
        save();
    }
    public static synchronized void setBgmVolume(int percent) {
        data.setBgmVolume(Math.max(0, Math.min(100, percent)));
        SoundManager.setBgmVolume(data.getBgmVolume() / 100f);
        save();
    }
    public static synchronized void setMuted(boolean mute) {
        data.setMuted(mute); SoundManager.setMuted(mute); save();
    }
    public static synchronized void resetToDefaults() {
        data.resetToDefaults(); applyToSound(); save();
    }
    private static void applyToSound() {
        SoundManager.setSfxVolume(data.getSfxVolume() / 100f);
        SoundManager.setBgmVolume(data.getBgmVolume() / 100f);
        SoundManager.setMuted(data.isMuted());
    }
    private static void save() {
        try { data.save(); saveError = null; }
        catch (IOException | SecurityException e) {
            saveError = "설정을 저장하지 못했습니다. 이번 실행에는 적용되지만 재실행 시 유지되지 않습니다.";
        }
    }
}
