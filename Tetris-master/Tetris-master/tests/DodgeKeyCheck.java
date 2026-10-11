package dodge;

import settings.GameControls;
import settings.GameSettings.Game;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.SwingUtilities;

public class DodgeKeyCheck {
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    static void press(GamePanel panel, int key, boolean release) {
        KeyEvent event = new KeyEvent(panel, release ? KeyEvent.KEY_RELEASED : KeyEvent.KEY_PRESSED,
                System.currentTimeMillis(), 0, key, KeyEvent.CHAR_UNDEFINED);
        for (KeyListener listener : panel.getKeyListeners())
            if (release) listener.keyReleased(event); else listener.keyPressed(event);
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            GameControls.reset(Game.DODGE);
            GamePanel panel = new GamePanel();
            panel.isStarted = true;
            panel.dodger.reset(); panel.dodger.x = 5;
            GameControls.setKey(Game.DODGE, "LEFT", KeyEvent.VK_H);
            press(panel, KeyEvent.VK_LEFT, false);
            check(panel.dodger.x == 5, "Old dodge key disabled");
            press(panel, KeyEvent.VK_H, false);
            check(panel.dodger.x == 4 && panel.nextRepeat.containsKey(KeyEvent.VK_LEFT), "Remapped dodge movement/repeat");
            press(panel, KeyEvent.VK_H, false);
            check(panel.dodger.x == 4, "OS repeat still suppressed");
            GameControls.setKey(Game.DODGE, "LEFT", KeyEvent.VK_J);
            press(panel, KeyEvent.VK_J, false);
            check(panel.dodger.x == 3 && panel.held.size() == 1, "Live rebind clears held keys");
            press(panel, KeyEvent.VK_J, true);
            check(panel.nextRepeat.isEmpty() && panel.held.isEmpty(), "Remapped release clears repeat");
            panel.stop();
            GameControls.setKey(Game.DODGE, "LEFT", KeyEvent.VK_H);
        });
        System.out.println("Dodge key checks passed.");
    }
}
