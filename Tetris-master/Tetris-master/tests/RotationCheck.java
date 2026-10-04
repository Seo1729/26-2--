import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

public class RotationCheck
{
    private static void check(boolean condition, String message)
    {
        if (!condition) throw new AssertionError(message);
    }

    private static void same(Block3D expected, Block3D actual)
    {
        check(expected.getType() == actual.getType(), "Type changed");
        check(expected.getX() == actual.getX() && expected.getY() == actual.getY()
                && expected.getZ() == actual.getZ(), "Pivot moved");
        Block3D.Cube[] left = expected.getRelativeCubes(), right = actual.getRelativeCubes();
        for (int i = 0; i < left.length; i++)
            check(left[i].getX() == right[i].getX() && left[i].getY() == right[i].getY()
                    && left[i].getZ() == right[i].getZ(), "Shape changed at cube " + i);
    }

    private static void formula(Block3D before, Block3D after, Block3D.Axis axis)
    {
        Block3D.Cube[] a = before.getRelativeCubes(), b = after.getRelativeCubes();
        for (int i = 0; i < a.length; i++)
        {
            int x = a[i].getX(), y = a[i].getY(), z = a[i].getZ();
            int nx = axis == Block3D.Axis.Y ? z : axis == Block3D.Axis.Z ? -y : x;
            int ny = axis == Block3D.Axis.X ? -z : axis == Block3D.Axis.Z ? x : y;
            int nz = axis == Block3D.Axis.X ? y : axis == Block3D.Axis.Y ? -x : z;
            check(b[i].getX() == nx && b[i].getY() == ny && b[i].getZ() == nz, "Wrong matrix");
        }
    }

    private static boolean contains(Block3D block, Block3D.Cube target)
    {
        for (Block3D.Cube cube : block.getAbsoluteCubes())
            if (cube.getX() == target.getX() && cube.getY() == target.getY() && cube.getZ() == target.getZ())
                return true;
        return false;
    }

    public static void main(String[] args) throws Exception
    {
        for (Block3D.Type type : Block3D.Type.values())
        {
            Block3D original = new Block3D(type, 1, 1, 6);
            Block3D snapshot = original.copy();
            for (Block3D.Axis axis : Block3D.Axis.values())
            {
                Block3D rotated = original;
                for (int turn = 0; turn < 4; turn++)
                {
                    Block3D next = rotated.rotated(axis);
                    formula(rotated, next, axis);
                    rotated = next;
                }
                same(original, rotated);
                same(snapshot, original);
            }
            Block3D mixed = original;
            for (Block3D.Axis axis : new Block3D.Axis[] { Block3D.Axis.X, Block3D.Axis.Y, Block3D.Axis.Z })
            {
                Block3D next = mixed.rotated(axis);
                formula(mixed, next, axis);
                mixed = next;
            }
            same(mixed, mixed.copy());
        }
        Field active = Game3D.class.getDeclaredField("activeBlock");
        active.setAccessible(true);
        Method tick = Game3D.class.getDeclaredMethod("fallOneStep");
        tick.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try
            {
                for (Block3D.Axis axis : Block3D.Axis.values())
                {
                    Board3D board = new Board3D();
                    Game3D game = new Game3D(board, 60000);
                    try
                    {
                        game.start();
                        Block3D original = new Block3D(Block3D.Type.O, 1, 1, 6);
                        active.set(game, original);
                        check(game.rotate(axis), "Valid rotation rejected");
                        formula(original, game.getActiveBlock(), axis);
                        Block3D snapshot = game.getActiveBlock();
                        snapshot.setPosition(0, 0, 0);
                        same(original.rotated(axis), game.getActiveBlock());
                        active.set(game, original);
                        for (Block3D.Cube cube : original.rotated(axis).getAbsoluteCubes())
                            if (!contains(original, cube))
                            {
                                board.setFilled(cube.getX(), cube.getY(), cube.getZ(), true);
                                check(!game.rotate(axis), "Collision accepted");
                                same(original, game.getActiveBlock());
                                check(board.isFilled(cube.getX(), cube.getY(), cube.getZ()), "Obstacle changed");
                                board.setFilled(cube.getX(), cube.getY(), cube.getZ(), false);
                                break;
                            }
                        Block3D edge = axis == Block3D.Axis.X ? new Block3D(Block3D.Type.O, 1, 1, 13)
                                : axis == Block3D.Axis.Y ? new Block3D(Block3D.Type.O, 1, 1, 0)
                                : new Block3D(Block3D.Type.O, 0, 0, 6);
                        active.set(game, edge);
                        check(board.canPlace(edge), "Invalid boundary fixture");
                        check(!game.rotate(axis), "Outside rotation accepted");
                        same(edge, game.getActiveBlock());
                        active.set(game, original.copy());
                        Game3DPanel panel = new Game3DPanel(game);
                        int key = axis == Block3D.Axis.X ? KeyEvent.VK_J : axis == Block3D.Axis.Y ? KeyEvent.VK_K : KeyEvent.VK_L;
                        String press = (String) panel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW)
                                .get(KeyStroke.getKeyStroke(key, 0, false));
                        String release = (String) panel.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW)
                                .get(KeyStroke.getKeyStroke(key, 0, true));
                        panel.getActionMap().get(press).actionPerformed(new ActionEvent(panel, 0, press));
                        same(original.rotated(axis), game.getActiveBlock());
                        panel.getActionMap().get(press).actionPerformed(new ActionEvent(panel, 0, press));
                        same(original.rotated(axis), game.getActiveBlock());
                        panel.getActionMap().get(release).actionPerformed(new ActionEvent(panel, 0, release));
                        panel.getActionMap().get(press).actionPerformed(new ActionEvent(panel, 0, press));
                        same(original.rotated(axis).rotated(axis), game.getActiveBlock());
                        game.stop();
                        Block3D stopped = game.getActiveBlock();
                        check(!game.rotate(axis), "Rotation while stopped");
                        same(stopped, game.getActiveBlock());
                    }
                    finally { game.stop(); }
                }
                Board3D board = new Board3D();
                Game3D game = new Game3D(board, 60000);
                try
                {
                    game.start();
                    active.set(game, new Block3D(Block3D.Type.O, 1, 1, 6));
                    check(game.rotate(Block3D.Axis.X), "Rotation before fall failed");
                    Block3D expected = game.getActiveBlock();
                    expected.setPosition(1, 1, 0);
                    for (int step = 0; step < 7; step++) tick.invoke(game);
                    check(game.getLockedBlockCount() == 1, "Rotated block did not lock");
                    for (Block3D.Cube cube : expected.getAbsoluteCubes())
                        check(board.getType(cube.getX(), cube.getY(), cube.getZ()) == Block3D.Type.O,
                                "Rotated absolute coordinates not stored");
                }
                finally { game.stop(); }
            }
            catch (ReflectiveOperationException exception) { throw new RuntimeException(exception); }
        });
        System.out.println("PASS: seven types, three matrices, mixed/four-turn rotations, boundaries, collisions, snapshots, keys, rotated landing");
    }
}
