import settings.EnvironmentSettingsPanel;
import settings.GameControls;
import settings.GameSettings;
import settings.GameSettings.Game;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

/** 실제 사용자 설정을 건드리지 않도록 임시 user.home에서 실행한다. */
public class EnvironmentSettingsCheck {
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    static List<Component> components(Container parent) {
        List<Component> all = new ArrayList<>();
        for (Component child : parent.getComponents()) {
            all.add(child);
            if (child instanceof Container) all.addAll(components((Container) child));
        }
        return all;
    }
    static JButton button(Container parent, String text) {
        for (Component component : components(parent))
            if (component instanceof JButton && ((JButton) component).getText().equals(text)) return (JButton) component;
        throw new AssertionError("Missing button: " + text);
    }
    static void layout(Container parent) {
        parent.doLayout();
        for (Component child : parent.getComponents()) if (child instanceof Container) layout((Container) child);
    }
    public static void main(String[] args) throws Exception {
        Settings.load();
        SwingUtilities.invokeAndWait(() -> {
            boolean[] closed = {false};
            EnvironmentSettingsPanel panel = new EnvironmentSettingsPanel(() -> closed[0] = true);
            List<JSlider> sliders = new ArrayList<>();
            for (Component component : components(panel)) if (component instanceof JSlider) sliders.add((JSlider) component);
            check(sliders.size() == 2, "Both volume sliders");
            sliders.get(0).setValue(31); sliders.get(1).setValue(67);
            check(Settings.getBgmVolume() == 31 && Settings.getSfxVolume() == 67, "Shared sound setting application");
            GameControls.setKey(Game.TETRIS, "HOLD", KeyEvent.VK_H);
            button(panel, "저장").doClick();
            GameSettings saved = new GameSettings();
            check(saved.getBgmVolume() == 31 && saved.getSfxVolume() == 67
                    && saved.getTetrisKey(GameSettings.TetrisAction.HOLD) == KeyEvent.VK_H, "Combined save");
            button(panel, "기본값 복원").doClick();
            check(sliders.get(0).getValue() == 40 && sliders.get(1).getValue() == 70, "Restore slider display");
            check(GameControls.physical(Game.TETRIS, KeyEvent.VK_C) == KeyEvent.VK_C, "Restore keys");
            check(new GameSettings().getBgmVolume() == 40, "Restore persisted");
            panel.setSize(panel.getPreferredSize()); layout(panel);
            Point footer = SwingUtilities.convertPoint(button(panel, "닫기"), 0, 0, panel);
            check(footer.y >= 0 && footer.y + button(panel, "닫기").getHeight() <= panel.getHeight(), "Close button visible");
            if (args.length > 0) {
                try {
                    BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics = image.createGraphics(); panel.printAll(graphics); graphics.dispose();
                    javax.imageio.ImageIO.write(image, "png", new java.io.File(args[0]));
                } catch (java.io.IOException e) { throw new RuntimeException(e); }
            }
            button(panel, "닫기").doClick(); check(closed[0], "Close callback");

            Game3D game = new Game3D(new Board3D(), 60000); game.start();
            Game3DPanel gamePanel = new Game3DPanel(game); gamePanel.addNotify();
            try {
                javax.swing.Action drop = gamePanel.getActionMap().get("pressSpace");
                int locks = game.getLockedBlockCount();
                GameControls.beginSettings();
                check(GameControls.canonical(Game.TETRIS, KeyEvent.VK_LEFT) == KeyEvent.VK_UNDEFINED, "Listener input blocked");
                drop.actionPerformed(new ActionEvent(gamePanel, 0, ""));
                check(game.getLockedBlockCount() == locks, "Swing binding input blocked");
                GameControls.beginSettings(); GameControls.endSettings();
                check(GameControls.isInputBlocked(), "Nested modal remains blocked");
                GameControls.endSettings();
                check(!GameControls.isInputBlocked() && gamePanel.pressedKeys.isEmpty(), "Close releases input state");
                gamePanel.getActionMap().get("pressSpace").actionPerformed(new ActionEvent(gamePanel, 0, ""));
                check(game.getLockedBlockCount() == locks + 1, "Game input resumes after close");
            } finally { gamePanel.removeNotify(); game.stop(); }
        });
        System.out.println("Environment settings UI checks passed.");
    }
}
