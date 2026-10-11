import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class GameUICheck
{
    private static void check(boolean value, String message)
    {
        if (!value) throw new AssertionError(message);
    }

    private static JLabel label(Game3DPanel panel, String name) throws Exception
    {
        Field field = Game3DPanel.class.getDeclaredField(name); field.setAccessible(true);
        return (JLabel) field.get(panel);
    }

    private static void layout(Container parent)
    {
        parent.doLayout();
        for (Component child : parent.getComponents())
            if (child instanceof Container) layout((Container) child);
    }

    public static void main(String[] args) throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            Board3D board = new Board3D();
            Game3D game = new Game3D(board, 60000);
            try
            {
                // A vertical O fills the last two cells of both bottom layers.
                for (int z = 0; z < 2; z++)
                    for (int x = 0; x < 3; x++)
                        for (int y = 0; y < 3; y++)
                            if (!(y == 1 && (x == 1 || x == 2))) board.setFilled(x, y, z, true);
                game.start();
                Field active = Game3D.class.getDeclaredField("activeBlock"); active.setAccessible(true);
                active.set(game, new Block3D(Block3D.Type.O, 1, 1, 8).rotated(Block3D.Axis.X));
                Game3DPanel panel = new Game3DPanel(game);
                check(label(panel, "score").getText().equals("0"), "Initial score");
                check(label(panel, "nextBlock").getText().equals(game.getNextBlockType().name()), "Next UI differs from reservation");
                game.hardDrop(); panel.refreshStatus();
                check(label(panel, "nextBlock").getText().equals(game.getNextBlockType().name()), "Next UI did not refresh");
                check(label(panel, "clearedLayers").getText().equals("2"), "Multiple clears not counted");
                check(label(panel, "score").getText().equals("200"), "UI scoring rule");
                check(game.getClearedLayerCount() == 2 && game.getScore() == 200, "Game stats missing");
                Game3DPanel recreated = new Game3DPanel(game);
                check(label(recreated, "score").getText().equals("200"), "New UI lost score");
                game.restart();
                panel.refreshStatus();
                check(label(panel, "score").getText().equals("0"), "External restart retained score");
                // Exercise key release while modifiers differ from the press.
                panel.getActionMap().get("pressSpace").actionPerformed(new ActionEvent(panel, 0, "Space"));
                Object release = panel.getInputMap(javax.swing.JPanel.WHEN_IN_FOCUSED_WINDOW).get(
                        javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE,
                                java.awt.event.InputEvent.CTRL_DOWN_MASK | java.awt.event.InputEvent.ALT_DOWN_MASK, true));
                check("releaseSpace".equals(release), "Modified release not bound");
                panel.getActionMap().get(release).actionPerformed(new ActionEvent(panel, 0, "Space"));
                int previousLocks = game.getLockedBlockCount();
                panel.getActionMap().get("pressSpace").actionPerformed(new ActionEvent(panel, 0, "Space"));
                check(game.getLockedBlockCount() == previousLocks + 1, "Modifier change left key stuck");
                panel.refreshStatus();
                check(label(panel, "score").getText().equals("0"), "Refresh counted twice");
                // Occupy the shared spawn pivot without making a completed layer.
                board.setFilled(1, 1, 13, true);
                game.hardDrop(); panel.refreshStatus();
                check(label(panel, "gameState").getText().equals("GAME OVER"), "Game over hidden");
                panel.getActionMap().get("pressR").actionPerformed(new ActionEvent(panel, 0, "R"));
                check(label(panel, "clearedLayers").getText().equals("0")
                        && label(panel, "score").getText().equals("0"), "Restart retained UI stats");
                check(label(panel, "gameState").getText().equals("RUNNING"), "Stale game over");
                int z = game.getActiveBlock().getZ(), locked = game.getLockedBlockCount();
                for (int[] size : new int[][] {{1024, 880}, {940, 600}})
                {
                    panel.setSize(size[0], size[1]); layout(panel);
                    BufferedImage image = new BufferedImage(size[0], size[1], BufferedImage.TYPE_INT_RGB);
                    Graphics2D g = image.createGraphics(); panel.paint(g); g.dispose();
                    if (args.length > 0 && size[0] == 1024)
                        javax.imageio.ImageIO.write(image, "png", new java.io.File(args[0]));
                    Field topField = Game3DPanel.class.getDeclaredField("topView"); topField.setAccessible(true);
                    Component top = (Component) topField.get(panel);
                    check(top.getWidth() >= 280 && top.getHeight() >= 280, "Top view clipped");
                }
                check(game.getActiveBlock().getZ() == z && game.getLockedBlockCount() == locked, "UI changed game");
            }
            catch (Exception e) { throw new RuntimeException(e); }
            finally { game.stop(); }
        });
        System.out.println("PASS: UI layer counting/scoring, refresh, game over/restart, layout, unchanged game");
    }
}
