import java.awt.*;
import java.awt.event.*;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import javax.swing.*;
import settings.GameControls;
import settings.GameSettings.Game;

/** 실제 창/키 이벤트로 ESC 설정창과 닫은 뒤 두 게임의 입력을 검사한다. */
public class SettingsWindowCheck {
    static Robot robot;
    static <T> T edt(Callable<T> task) throws Exception {
        java.util.concurrent.FutureTask<T> result = new java.util.concurrent.FutureTask<>(task);
        SwingUtilities.invokeAndWait(result); return result.get();
    }
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    static void key(int key) throws Exception {
        Component focus = edt(() -> KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner());
        check(focus != null, "Test window focus");
        EventQueue queue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        queue.postEvent(new KeyEvent(focus, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, key, KeyEvent.CHAR_UNDEFINED));
        queue.postEvent(new KeyEvent(focus, KeyEvent.KEY_RELEASED, System.currentTimeMillis(), 0, key, KeyEvent.CHAR_UNDEFINED));
        robot.delay(200);
    }
    static JDialog settingsDialog() throws Exception {
        for (int i = 0; i < 30; i++) {
            JDialog dialog = edt(() -> {
                for (Window window : Window.getWindows())
                    if (window instanceof JDialog && window.isShowing() && ((JDialog) window).getTitle().equals("환경 설정"))
                        return (JDialog) window;
                return null;
            });
            if (dialog != null) return dialog;
            robot.delay(100);
        }
        throw new AssertionError("ESC did not open settings");
    }
    static JButton button(Container root, String text) {
        for (Component component : root.getComponents()) {
            if (component instanceof JButton && ((JButton) component).getText().equals(text)) return (JButton) component;
            if (component instanceof Container) {
                JButton result = button((Container) component, text); if (result != null) return result;
            }
        }
        return null;
    }
    public static void main(String[] args) throws Exception {
        robot = new Robot(); robot.setAutoDelay(40);
        try {
            new ImageSource();
            GameControls.reset(Game.TETRIS);
            Method factory = HoldCheck.class.getDeclaredMethod("board"); factory.setAccessible(true);
            HoldCheck.Board board = (HoldCheck.Board) factory.invoke(null);
            Method active = HoldCheck.class.getDeclaredMethod("active", HoldCheck.Board.class, int.class, int.class, int.class);
            active.setAccessible(true); active.invoke(null, board, 4, 5, 5);
            GameFrame frame = edt(() -> {
                GameFrame f = new GameFrame(); f.fgp = board; f.MyKey1(board);
                f.revalidate(); f.repaint(); f.toFront(); f.requestFocusInWindow(); return f;
            });
            robot.delay(500);
            Point position = edt(frame::getLocationOnScreen);
            robot.mouseMove(position.x + 180, position.y + 100);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK); robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            edt(() -> { frame.requestFocusInWindow(); return null; }); robot.delay(300);
            int before = edt(() -> board.x);
            key(KeyEvent.VK_LEFT);
            check(edt(() -> board.x) == before - 1, "Initial Tetris key input; focus="
                    + edt(() -> KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner())
                    + "; x=" + edt(() -> board.x));
            key(KeyEvent.VK_ESCAPE);
            JDialog dialog = settingsDialog();
            int blockedX = edt(() -> board.x);
            key(KeyEvent.VK_LEFT);
            check(edt(() -> board.x) == blockedX, "Tetris input blocked in settings");
            edt(() -> { GameControls.setKey(Game.TETRIS, "LEFT", KeyEvent.VK_H); button(dialog, "저장").doClick(); return null; });
            key(KeyEvent.VK_ESCAPE); robot.delay(300);
            check(!edt(dialog::isShowing) && !GameControls.isInputBlocked(), "ESC closes/unblocks settings");
            key(KeyEvent.VK_H);
            check(edt(() -> board.x) == blockedX - 1, "Tetris remapped key after close");
            edt(() -> { board.preBlock = 1; GameControls.setKey(Game.TETRIS, "HOLD", KeyEvent.VK_J); return null; });
            int originalBlock = edt(() -> board.blockNum);
            key(KeyEvent.VK_C);
            check(edt(() -> board.holdManager.getHeldBlock()) == 0, "Old hold key still active");
            key(KeyEvent.VK_J);
            check(edt(() -> board.holdManager.getHeldBlock()) == originalBlock,
                "Custom hold key did not reach real Tetris input handler");
            edt(() -> { frame.dispose(); return null; });

            GameControls.reset(Game.DODGE);
            dodge.GamePanel dodge = edt(() -> new dodge.GamePanel());
            java.lang.reflect.Field started = dodge.GamePanel.class.getDeclaredField("isStarted"); started.setAccessible(true);
            java.lang.reflect.Field dodgerField = dodge.GamePanel.class.getDeclaredField("dodger"); dodgerField.setAccessible(true);
            Object dodger = dodgerField.get(dodge);
            java.lang.reflect.Field x = dodge.Dodger.class.getDeclaredField("x"); x.setAccessible(true);
            JFrame dodgeFrame = edt(() -> {
                started.setBoolean(dodge, true); x.setInt(dodger, 5);
                JFrame f = new JFrame("Dodge settings test"); f.add(dodge); f.setSize(640, 700);
                f.setLocationRelativeTo(null); f.setVisible(true); f.toFront(); dodge.requestFocusInWindow(); return f;
            });
            robot.delay(500); key(KeyEvent.VK_LEFT);
            check(edt(() -> x.getInt(dodger)) == 4, "Initial dodge key");
            key(KeyEvent.VK_ESCAPE); JDialog dodgeDialog = settingsDialog();
            key(KeyEvent.VK_RIGHT);
            check(edt(() -> x.getInt(dodger)) == 4, "Dodge input blocked in settings");
            edt(() -> { GameControls.setKey(Game.DODGE, "LEFT", KeyEvent.VK_H); button(dodgeDialog, "닫기").doClick(); return null; });
            robot.delay(300); key(KeyEvent.VK_H);
            check(edt(() -> x.getInt(dodger)) == 3 && !GameControls.isInputBlocked(), "Dodge remapped key after close");
            edt(() -> { dodge.stop(); dodgeFrame.dispose(); return null; });
            System.out.println("Actual settings window/ESC/focus/Tetris/Dodge checks passed.");
        } finally {
            edt(() -> { for (Window window : Window.getWindows()) window.dispose(); return null; });
        }
    }
}
