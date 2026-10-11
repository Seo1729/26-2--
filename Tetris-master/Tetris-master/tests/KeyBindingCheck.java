import settings.GameSettings;
import settings.GameSettings.Game;
import settings.GameControls;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import javax.swing.*;

public class KeyBindingCheck {
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    static void reject(Runnable action) {
        try { action.run(); throw new AssertionError("Invalid binding accepted"); }
        catch (IllegalArgumentException expected) { }
    }
    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            check(GameControls.physical(Game.TETRIS, KeyEvent.VK_C) == KeyEvent.VK_H, "Restart hold binding");
            check(GameControls.physical(Game.DODGE, KeyEvent.VK_LEFT) == KeyEvent.VK_H, "Restart dodge binding");
            System.out.println("Key restart checks passed."); return;
        }
        GameSettings data = GameControls.settings();
        for (Game game : Game.values()) GameControls.reset(game);
        GameControls.setKey(Game.TETRIS, "HOLD", KeyEvent.VK_H);
        GameControls.setKey(Game.DODGE, "LEFT", KeyEvent.VK_H);
        check(GameControls.canonical(Game.TETRIS, KeyEvent.VK_H) == KeyEvent.VK_C, "Canonical hold action");
        check(GameControls.canonical(Game.TETRIS, KeyEvent.VK_C) == KeyEvent.VK_UNDEFINED, "Old key disabled");
        reject(() -> GameControls.setKey(Game.TETRIS, "LEFT", KeyEvent.VK_H));
        reject(() -> GameControls.setKey(Game.GALAGA, "HOLD", KeyEvent.VK_A));
        for (int code : new int[] {KeyEvent.VK_ESCAPE, KeyEvent.VK_TAB, KeyEvent.VK_ALT, KeyEvent.VK_F1, KeyEvent.VK_F4, -1})
            reject(() -> GameControls.setKey(Game.TETRIS, "HOLD", code));
        check(data.getTetrisKey(GameSettings.TetrisAction.HOLD) == KeyEvent.VK_H, "Rejected changes preserve value");
        Settings.setBgmVolume(22);
        check(new GameSettings().getTetrisKey(GameSettings.TetrisAction.HOLD) == KeyEvent.VK_H, "Volume save preserves keys");
        SwingUtilities.invokeAndWait(() -> {
            Game3D game = new Game3D(new Board3D(), 60000);
            game.start();
            Game3DPanel panel = new Game3DPanel(game);
            panel.addNotify();
            try {
                panel.getActionMap().get("pressSpace").actionPerformed(new ActionEvent(panel, 0, ""));
                check(!panel.pressedKeys.isEmpty(), "Pressed key setup");
                GameControls.setKey(Game.THREE_D, "HARD_DROP", KeyEvent.VK_H);
                check(panel.pressedKeys.isEmpty(), "Rebinding clears pressed keys");
                InputMap map = panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
                check(map.get(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0)) == null, "Old 3D mapping removed");
                check("pressSpace".equals(map.get(KeyStroke.getKeyStroke(KeyEvent.VK_H, 0))), "New 3D mapping immediate");
                int locks = game.getLockedBlockCount();
                panel.getActionMap().get(map.get(KeyStroke.getKeyStroke(KeyEvent.VK_H, 0))).actionPerformed(new ActionEvent(panel, 0, ""));
                check(game.getLockedBlockCount() == locks + 1, "Remapped hard drop executes");
                GameControls.reset(Game.THREE_D);
                check("pressSpace".equals(map.get(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0))), "Default restore immediate");
                new settings.KeySettingsPanel();
                try {
                    java.lang.reflect.Method boardFactory = HoldCheck.class.getDeclaredMethod("board");
                    boardFactory.setAccessible(true);
                    GalagaLayer galaga = new GalagaLayer(0, 0, 240, 480, (GamePanel) boardFactory.invoke(null));
                    galaga.timer.stop();
                    GameControls.setKey(Game.GALAGA, "FIGHTER_LEFT", KeyEvent.VK_H);
                    java.awt.event.KeyEvent press = new java.awt.event.KeyEvent(galaga, KeyEvent.KEY_PRESSED,
                            System.currentTimeMillis(), 0, KeyEvent.VK_H, KeyEvent.CHAR_UNDEFINED);
                    galaga.getKeyListener().keyPressed(press);
                    check(galaga.left, "Remapped fighter movement");
                    GameControls.setKey(Game.GALAGA, "FIGHTER_LEFT", KeyEvent.VK_J);
                    galaga.getKeyListener().keyReleased(new java.awt.event.KeyEvent(galaga, KeyEvent.KEY_RELEASED,
                            System.currentTimeMillis(), 0, KeyEvent.VK_H, KeyEvent.CHAR_UNDEFINED));
                    check(!galaga.left, "Fighter held state cleared after remap");
                    GameControls.reset(Game.GALAGA);
                } catch (Exception e) { throw new RuntimeException(e); }
            } finally { panel.removeNotify(); game.stop(); }
        });
        java.nio.file.Path corrupt = Files.createTempFile("keys-corrupt", ".properties");
        Files.write(corrupt, "tetris.key.LEFT=39\ndodge.key.LEFT=27\n".getBytes(StandardCharsets.UTF_8));
        GameSettings restored = new GameSettings(corrupt);
        check(restored.getTetrisKey(GameSettings.TetrisAction.LEFT) == KeyEvent.VK_LEFT, "Duplicate file repair");
        check(restored.getDodgeKey(GameSettings.DodgeAction.LEFT) == KeyEvent.VK_LEFT, "Reserved file repair");
        Files.delete(corrupt);
        check(GameControls.getSaveError() == null, "Settings saved");
        System.out.println("Key binding checks passed.");
    }
}
