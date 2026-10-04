import java.lang.reflect.Method;
import javax.swing.SwingUtilities;

public class LayerClearCheck
{
    private static void check(boolean condition, String message)
    {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception
    {
        // 14개 층의 완성/미완성 조합을 모두 검사하고 모든 좌표의 점유 상태와 종류를 비교한다.
        for (int mask = 0; mask < (1 << Board3D.SIZE_Z); mask++)
        {
            Board3D board = new Board3D();
            boolean[][][] expectedCells = new boolean[3][3][14];
            Block3D.Type[][][] expectedTypes = new Block3D.Type[3][3][14];
            int destination = 0;
            for (int z = 0; z < Board3D.SIZE_Z; z++)
            {
                Block3D.Type type = (z % 2 == 0) ? Block3D.Type.O : Block3D.Type.T;
                board.lockBlock(new Block3D(type, 1, 1, z));
                if ((mask & (1 << z)) != 0)
                {
                    for (int x = 0; x < 3; x++)
                        for (int y = 0; y < 3; y++) board.setFilled(x, y, z, true);
                }
                else
                {
                    for (int x = 0; x < 3; x++)
                        for (int y = 0; y < 3; y++)
                        {
                            expectedCells[x][y][destination] = board.isFilled(x, y, z);
                            expectedTypes[x][y][destination] = board.getType(x, y, z);
                        }
                    destination++;
                }
            }
            check(board.clearCompletedLayers() == Integer.bitCount(mask), "Wrong removed count: " + mask);
            for (int x = 0; x < 3; x++)
                for (int y = 0; y < 3; y++)
                    for (int z = 0; z < 14; z++)
                    {
                        check(board.isFilled(x, y, z) == expectedCells[x][y][z], "Wrong occupancy: " + mask);
                        check(board.getType(x, y, z) == expectedTypes[x][y][z], "Wrong type: " + mask);
                    }
            check(board.clearCompletedLayers() == 0, "Repeated clearing changed board");
        }
        Board3D almostFull = new Board3D();
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++) almostFull.setFilled(x, y, 0, true);
        almostFull.setFilled(2, 2, 0, false);
        check(almostFull.clearCompletedLayers() == 0, "Eight cells must not clear");

        Method tick = Game3D.class.getDeclaredMethod("fallOneStep");
        tick.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            Board3D board = new Board3D();
            Game3D game = new Game3D(board, 60000);
            try
            {
                game.start();
                Block3D block = game.getActiveBlock();
                // 최상층에서 고정해 층을 완성한다. 삭제 전에 생성하면 다음 블록이 막힌다.
                for (int x = 0; x < 3; x++)
                    for (int y = 0; y < 3; y++) board.setFilled(x, y, 13, true);
                for (Block3D.Cube cube : block.getAbsoluteCubes())
                    board.setFilled(cube.getX(), cube.getY(), cube.getZ(), false);
                board.lockBlock(new Block3D(Block3D.Type.O, 1, 1, 12));
                tick.invoke(game);
                check(game.getLockedBlockCount() == 1, "Landing did not lock");
                check(game.isRunning() && game.getActiveBlock() != null, "Must clear before next spawn");
                for (int x = 0; x < 3; x++)
                    for (int y = 0; y < 3; y++)
                    {
                        check(board.isEmpty(x, y, 13) && board.getType(x, y, 13) == null, "Top not cleared");
                        check(board.isFilled(x, y, 12) == (x >= 1 && y >= 1), "Lower layer changed");
                    }
            }
            catch (ReflectiveOperationException exception) { throw new RuntimeException(exception); }
            finally { game.stop(); }
        });
        System.out.println("PASS: all 16384 layer combinations, occupancy/type preservation, eight-cell layer, lock-clear-spawn");
    }
}
