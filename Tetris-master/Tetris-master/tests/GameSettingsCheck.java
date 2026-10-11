import settings.GameSettings;
import settings.GameSettings.TetrisAction;
import settings.GameSettings.DodgeAction;
import java.awt.event.KeyEvent;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** 실제 사용자 설정 대신 임시 파일로 저장/복구를 검증한다. */
public class GameSettingsCheck {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("game-settings-check-");
        Path file = directory.resolve("nested/settings.properties");
        GameSettings settings = new GameSettings(file);
        check(!Files.exists(file), "Constructor must not create a file");
        check(settings.getBgmVolume() == 40 && settings.getSfxVolume() == 70, "Missing file defaults");
        for (TetrisAction action : TetrisAction.values())
            check(settings.getTetrisKey(action) == action.getDefaultKey(), "Tetris default");
        for (DodgeAction action : DodgeAction.values())
            check(settings.getDodgeKey(action) == action.getDefaultKey(), "Dodge default");
        settings.setBgmVolume(0);
        settings.setSfxVolume(100);
        settings.setTetrisKey(TetrisAction.LEFT, KeyEvent.VK_J);
        settings.setDodgeKey(DodgeAction.LEFT, KeyEvent.VK_L);
        settings.save();
        GameSettings loaded = new GameSettings(file);
        check(loaded.getBgmVolume() == 0 && loaded.getSfxVolume() == 100, "Volume round trip");
        check(loaded.getTetrisKey(TetrisAction.LEFT) == KeyEvent.VK_J
                && loaded.getDodgeKey(DodgeAction.LEFT) == KeyEvent.VK_L, "Independent key round trip");
        Files.write(file, ("bgmVolume=-1\nsfxVolume=garbage\ntetris.key.LEFT=2147483647\n"
                + "dodge.key.LEFT=" + KeyEvent.VK_H + "\ntetris.key.RIGHT=0\n").getBytes(StandardCharsets.UTF_8));
        check(loaded.load(), "Readable properties");
        check(loaded.getBgmVolume() == 40 && loaded.getSfxVolume() == 70, "Invalid volume fallback");
        check(loaded.getTetrisKey(TetrisAction.LEFT) == KeyEvent.VK_LEFT
                && loaded.getTetrisKey(TetrisAction.RIGHT) == KeyEvent.VK_RIGHT, "Invalid key fallback");
        check(loaded.getDodgeKey(DodgeAction.LEFT) == KeyEvent.VK_H, "Valid item preserved");
        loaded.setBgmVolume(101);
        loaded.setSfxVolume(-1);
        loaded.setDodgeKey(DodgeAction.JUMP, -5);
        check(loaded.getBgmVolume() == 40 && loaded.getSfxVolume() == 70
                && loaded.getDodgeKey(DodgeAction.JUMP) == KeyEvent.VK_UP, "Setter validation");
        Files.write(file, "bgmVolume=25\nbroken=\\uZZZZ\n".getBytes(StandardCharsets.UTF_8));
        check(!loaded.load() && loaded.getBgmVolume() == 40, "Malformed file fallback");
        GameSettings unreadable = new GameSettings(directory);
        check(!unreadable.load() && unreadable.getSfxVolume() == 70, "Unreadable file fallback");
        try { unreadable.save(); throw new AssertionError("Save failure must be reported"); }
        catch (java.io.IOException expected) { }
        loaded.resetToDefaults();
        loaded.save();
        check(new GameSettings(file).getDodgeKey(DodgeAction.LEFT) == KeyEvent.VK_LEFT, "Reset persistence");
        Files.delete(file);
        check(!loaded.load() && loaded.getTetrisKey(TetrisAction.LEFT) == KeyEvent.VK_LEFT, "Deleted file fallback");
        Files.delete(file.getParent());
        Files.delete(directory);
        System.out.println("GameSettings checks passed.");
    }
}
