import java.awt.*;
import java.awt.event.KeyEvent;
import javax.swing.*;

/** Actual menu/dialog transitions; isolated process exits to stop legacy countdown threads. */
public final class MenuWindowCheck {
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    static JDialog dialog(String title) throws Exception {
        for (int i = 0; i < 30; i++) {
            JDialog found = SettingsWindowCheck.edt(() -> {
                for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isShowing()
                    && ((JDialog) w).getTitle().equals(title)) return (JDialog) w;
                return null;
            });
            if (found != null) return found;
            Thread.sleep(100);
        }
        throw new AssertionError("Dialog missing: " + title);
    }
    public static void main(String[] args) throws Exception {
        int exit = 1;
        try {
            GameFrame.main(new String[0]); Thread.sleep(500);
            GameFrame frame = SettingsWindowCheck.edt(() -> {
                for (Window w : Window.getWindows()) if (w instanceof GameFrame && w.isShowing()) return (GameFrame) w;
                throw new AssertionError("GameFrame main did not show menu");
            });
            for (String title : new String[] {"환경 설정", "캐릭터 관리"}) {
                SwingUtilities.invokeLater(() -> SettingsWindowCheck.button(frame.mainMenu, title).doClick());
                JDialog d = dialog(title);
                SettingsWindowCheck.edt(() -> { SettingsWindowCheck.button(d,
                    title.equals("환경 설정") ? "닫기" : "뒤로 가기").doClick(); return null; });
                Thread.sleep(400);
                check(SettingsWindowCheck.edt(() -> {
                    Component focus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                    return focus != null && SwingUtilities.getWindowAncestor(focus) == frame;
                }), "Menu focus not restored after " + title);
            }
            SettingsWindowCheck.edt(() -> { frame.mainMenu.btn3.doClick(); return null; });
            Thread.sleep(400);
            check(SettingsWindowCheck.edt(() -> frame.dodgeMode != null
                && SwingUtilities.isDescendingFrom(KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner(),
                    frame.dodgeMode)), "Menu to dodge / keyboard focus");
            SettingsWindowCheck.edt(() -> { frame.dodgeMode.stop(); frame.dispose(); return null; });
            GameFrame normal = SettingsWindowCheck.edt(GameFrame::new);
            SettingsWindowCheck.edt(() -> { normal.mainMenu.btn1.doClick(); return null; });
            // The original countdown includes START!! for one more second after 3, 2, 1.
            Thread.sleep(4500);
            check(SettingsWindowCheck.edt(() -> normal.fgp != null && normal.fgp.Gaming
                && KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == normal),
                "Menu to original Tetris / countdown / focus");
            System.out.println("Actual main/menu/settings/character dialog/Tetris/Dodge transitions and focus passed.");
            exit = 0;
        } catch (Throwable e) { e.printStackTrace(); }
        finally {
            SettingsWindowCheck.edt(() -> { for (Window w : Window.getWindows()) w.dispose(); return null; });
            System.exit(exit);
        }
    }
}
