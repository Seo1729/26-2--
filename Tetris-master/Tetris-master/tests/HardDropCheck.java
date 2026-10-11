import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class HardDropCheck
{
    private static void check(boolean condition, String message)
    {
        if (!condition) throw new AssertionError(message);
    }

    private static void sameBoard(Board3D a, Board3D b)
    {
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                for (int z = 0; z < 14; z++)
                {
                    check(a.isFilled(x, y, z) == b.isFilled(x, y, z), "Different landing coordinates");
                    check(a.getType(x, y, z) == b.getType(x, y, z), "Different stored type");
                }
    }

    public static void main(String[] args) throws Exception
    {
        Field active = Game3D.class.getDeclaredField("activeBlock");
        active.setAccessible(true);
        Method tick = Game3D.class.getDeclaredMethod("fallOneStep");
        tick.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try
            {
                for (Block3D.Type type : Block3D.Type.values())
                    for (int orientation = 0; orientation < 4; orientation++)
                        for (boolean obstacle : new boolean[] { false, true })
                        {
                            Block3D block = new Block3D(type, 1, 1, 8);
                            if (orientation > 0) block = block.rotated(Block3D.Axis.values()[orientation - 1]);
                            Board3D slowBoard = new Board3D(), fastBoard = new Board3D();
                            if (!slowBoard.canPlace(block)) continue;
                            if (obstacle)
                            {
                                slowBoard.setFilled(1, 1, 3, true);
                                fastBoard.setFilled(1, 1, 3, true);
                            }
                            Game3D slow = new Game3D(slowBoard, 60000), fast = new Game3D(fastBoard, 60000);
                            try
                            {
                                slow.start(); fast.start();
                                active.set(slow, block.copy()); active.set(fast, block.copy());
                                int steps = 0;
                                while (slow.getLockedBlockCount() == 0 && steps++ < 20) tick.invoke(slow);
                                check(slow.getLockedBlockCount() == 1, "Automatic fall did not lock");
                                check(fast.hardDrop(), "Hard drop rejected");
                                check(fast.getLockedBlockCount() == 1, "Must immediately lock exactly one block");
                                sameBoard(slowBoard, fastBoard);
                                check(fast.isRunning() && fast.getActiveBlock().getZ() == 13, "Missing next block");
                                tick.invoke(fast);
                                check(fast.getActiveBlock().getZ() == 12, "Automatic fall after hard drop failed");
                                fast.stop();
                                check(!fast.hardDrop(), "Hard drop while paused");
                                check(fast.getLockedBlockCount() == 1, "Paused drop changed board");
                            }
                            finally { slow.stop(); fast.stop(); }
                        }

                Board3D board = new Board3D();
                Game3D game = new Game3D(board, 60000);
                try
                {
                    check(!game.hardDrop(), "Hard drop before start");
                    game.start();
                    Block3D floor = new Block3D(Block3D.Type.O, 1, 1, 0);
                    active.set(game, floor);
                    for (int x = 0; x < 3; x++)
                        for (int y = 0; y < 3; y++)
                            if (x == 0 || y == 0) board.setFilled(x, y, 0, true);
                    board.setFilled(0, 0, 2, true);
                    check(game.hardDrop(), "Already landed block must lock");
                    check(game.getLockedBlockCount() == 1, "Already landed block not locked");
                    for (int x = 0; x < 3; x++)
                        for (int y = 0; y < 3; y++) check(board.isEmpty(x, y, 0), "Completed layer not cleared");
                    check(board.isFilled(0, 0, 1) && board.isEmpty(0, 0, 2), "Upper data not shifted");

                    Game3DPanel panel = new Game3DPanel(game);
                    String press = (String) panel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW)
                            .get(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false));
                    String release = (String) panel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW)
                            .get(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true));
                    panel.getActionMap().get(press).actionPerformed(new ActionEvent(panel, 0, press));
                    check(game.getLockedBlockCount() == 2, "Space did not hard drop");
                    panel.getActionMap().get(press).actionPerformed(new ActionEvent(panel, 0, press));
                    check(game.getLockedBlockCount() == 2, "Held Space dropped next block");
                    panel.getActionMap().get(release).actionPerformed(new ActionEvent(panel, 0, release));
                    panel.getActionMap().get(press).actionPerformed(new ActionEvent(panel, 0, press));
                    check(game.getLockedBlockCount() == 3, "Second press did not hard drop");
                }
                finally { game.stop(); }

                Board3D blocked = new Board3D();
                Game3D ending = new Game3D(blocked, 60000);
                try
                {
                    ending.start();
                    blocked.setFilled(1, 1, 12, true);
                    check(ending.hardDrop(), "Blocked landing must lock immediately");
                    check(ending.getLockedBlockCount() == 1 && !ending.isRunning()
                            && ending.getActiveBlock() == null, "Blocked spawn must end game");
                    check(!ending.hardDrop(), "Hard drop after game over");
                }
                finally { ending.stop(); }
            }
            catch (ReflectiveOperationException exception) { throw new RuntimeException(exception); }
        });
        System.out.println("PASS: hard/automatic drop parity, rotated shapes, obstacles, immediate locking, layer clearing, next spawn, Space repeat, game over");
    }
}
