import java.lang.reflect.Method;
import javax.swing.SwingUtilities;

/** 실행: java -Djava.awt.headless=true -cp <컴파일 경로> BlockLockCheck */
public class BlockLockCheck
{
    private static void check(boolean condition, String message)
    {
        if (!condition) throw new AssertionError(message);
    }

    private static int filledCount(Board3D board)
    {
        int count = 0;
        for (int x = 0; x < Board3D.SIZE_X; x++)
            for (int y = 0; y < Board3D.SIZE_Y; y++)
                for (int z = 0; z < Board3D.SIZE_Z; z++)
                    if (board.isFilled(x, y, z)) count++;
        return count;
    }

    public static void main(String[] args) throws Exception
    {
        for (Block3D.Type type : Block3D.Type.values())
        {
            Board3D board = new Board3D();
            Block3D block = new Block3D(type, 1, 1, 0);
            if (type == Block3D.Type.I)
            {
                try { board.lockBlock(block); throw new AssertionError("Outside block accepted"); }
                catch (IllegalArgumentException expected) { }
                check(filledCount(board) == 0, "Failed lock changed board");
                continue;
            }
            board.lockBlock(block);
            check(filledCount(board) == 4, "Must store four cubes");
            for (Block3D.Cube cube : block.getAbsoluteCubes())
                check(board.getType(cube.getX(), cube.getY(), cube.getZ()) == type,
                        "Wrong absolute coordinate or type");
            check(!board.canPlace(block), "Locked cubes must collide");
            try { board.lockBlock(block); throw new AssertionError("Overlap accepted"); }
            catch (IllegalArgumentException expected) { }
            check(filledCount(board) == 4, "Overlap changed board");
            for (Block3D.Cube cube : block.getAbsoluteCubes())
                board.setFilled(cube.getX(), cube.getY(), cube.getZ(), false);
            check(board.getType(1, 1, 0) == null, "Cleared type retained");
        }

        Method tick = Game3D.class.getDeclaredMethod("fallOneStep");
        tick.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            Board3D board = new Board3D();
            Game3D game = new Game3D(board, 60000);
            try
            {
                game.start();
                int ticks = 0;
                while (game.isRunning() && ticks++ < 300)
                {
                    Block3D before = game.getActiveBlock();
                    int locked = game.getLockedBlockCount();
                    boolean canFall = board.canPlace(before, before.getX(), before.getY(), before.getZ() - 1);
                    tick.invoke(game);
                    if (canFall)
                    {
                        check(game.getActiveBlock().getZ() == before.getZ() - 1, "Fall failed");
                        check(game.getLockedBlockCount() == locked, "Locked too early");
                    }
                    else
                    {
                        check(game.getLockedBlockCount() == locked + 1, "Landing did not lock");
                        for (Block3D.Cube cube : before.getAbsoluteCubes())
                            check(board.getType(cube.getX(), cube.getY(), cube.getZ()) == before.getType(),
                                    "Landing stored wrong type or coordinate");
                        check(filledCount(board) == (locked + 1) * 4, "Wrong stored cube count");
                        Block3D next = game.getActiveBlock();
                        if (next != null)
                            check(next.getZ() == Board3D.SIZE_Z - 1 && board.canPlace(next), "Invalid next spawn");
                        System.out.println("Landing Z=" + before.getZ() + ": locked #" + (locked + 1)
                                + ", next=" + (next == null ? "game over" : next.getType()));
                    }
                }
                check(!game.isRunning() && game.getActiveBlock() == null, "Blocked spawn must stop");
                check(game.getLockedBlockCount() >= 14, "Cycle did not repeat");
                check(!game.move(1, 0), "Movement after game over");
            }
            catch (ReflectiveOperationException exception) { throw new RuntimeException(exception); }
            finally { game.stop(); }
        });

        Board3D timerBoard = new Board3D();
        Game3D timedGame = new Game3D(timerBoard, 5);
        try
        {
            timedGame.start();
            long deadline = System.nanoTime() + 5_000_000_000L;
            while (timedGame.getLockedBlockCount() < 3 && System.nanoTime() < deadline)
                Thread.sleep(10);
            check(timedGame.getLockedBlockCount() >= 3, "Timer did not repeat spawning and locking");
            System.out.println("PASS: absolute coordinates, types, collisions, repeated cycles, game over, Swing timer");
        }
        finally { timedGame.stop(); }
    }
}
