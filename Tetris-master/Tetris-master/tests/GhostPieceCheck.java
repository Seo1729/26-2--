import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GhostPieceCheck
{
    private static void check(boolean condition, String message)
    {
        if (!condition) throw new AssertionError(message);
    }

    private static void sameShape(Block3D a, Block3D b)
    {
        check(a.getType() == b.getType() && a.getX() == b.getX() && a.getY() == b.getY(), "Type/XY changed");
        Block3D.Cube[] ac = a.getRelativeCubes(), bc = b.getRelativeCubes();
        for (int i = 0; i < ac.length; i++)
            check(ac[i].getX() == bc[i].getX() && ac[i].getY() == bc[i].getY()
                    && ac[i].getZ() == bc[i].getZ(), "Rotation changed");
    }

    private static BufferedImage render(JPanel panel)
    {
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics(); panel.paint(g); g.dispose();
        return image;
    }

    private static boolean different(BufferedImage a, BufferedImage b)
    {
        for (int y = 60; y < a.getHeight() - 40; y++)
            for (int x = 0; x < a.getWidth(); x++)
                if (a.getRGB(x, y) != b.getRGB(x, y)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception
    {
        Field active = Game3D.class.getDeclaredField("activeBlock");
        active.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try
            {
                for (Block3D.Type type : Block3D.Type.values())
                    for (int orientation = 0; orientation < 4; orientation++)
                        for (boolean obstacle : new boolean[] {false, true})
                        {
                            Board3D board = new Board3D();
                            if (obstacle) board.setFilled(1, 1, 3, true);
                            Block3D block = new Block3D(type, 1, 1, 8);
                            if (orientation > 0) block = block.rotated(Block3D.Axis.values()[orientation - 1]);
                            if (!board.canPlace(block)) continue;
                            Game3D game = new Game3D(board, 60000);
                            try
                            {
                                check(game.getGhostBlock() == null && game.getScene().ghostCells.isEmpty(), "Ghost before spawn");
                                game.start(); active.set(game, block);
                                Block3D ghost = game.getGhostBlock();
                                sameShape(block, ghost);
                                check(ghost != block && board.canPlace(ghost), "Ghost is not a valid independent block");
                                check(!board.canPlace(ghost, ghost.getX(), ghost.getY(), ghost.getZ() - 1), "Ghost above landing");
                                check(block.getZ() == 8 && game.getActiveBlock().getZ() == 8, "Preview moved actual block");
                                check(game.isRunning() && game.getLockedBlockCount() == 0, "Preview changed game state");
                                check(board.isFilled(1, 1, 3) == obstacle, "Preview changed board");
                                Game3DScene scene = game.getScene();
                                check(scene.ghostCells.size() == 4, "Missing ghost cubes");
                                try { scene.ghostCells.clear(); throw new AssertionError("Mutable ghost snapshot"); }
                                catch (UnsupportedOperationException expected) { }
                                if (type == Block3D.Type.O && orientation == 0 && obstacle)
                                {
                                    Game3DScene plain = new Game3DScene(scene.cells);
                                    Board3DRenderer renderer = new Board3DRenderer(); renderer.setSize(640, 720);
                                    renderer.setScene(plain); BufferedImage before = render(renderer);
                                    renderer.setScene(scene); check(different(before, render(renderer)), "3D ghost not visible");
                                    TopViewPanel top = new TopViewPanel(); top.setSize(280, 360);
                                    top.setScene(plain); before = render(top);
                                    top.setScene(scene); check(different(before, render(top)), "TOP VIEW ghost not visible");
                                }
                                ghost.setPosition(0, 0, 0);
                                check(game.getGhostBlock().getX() == block.getX(), "Returned ghost leaked internal state");
                                Block3D landing = game.getGhostBlock();
                                check(game.hardDrop(), "Drop failed");
                                for (Block3D.Cube cube : landing.getAbsoluteCubes())
                                    check(board.isFilled(cube.getX(), cube.getY(), cube.getZ())
                                            && board.getType(cube.getX(), cube.getY(), cube.getZ()) == type, "Ghost differs from hard drop");
                                check(scene.ghostCells.size() == 4, "Old ghost snapshot changed");
                                // Use an empty board to verify already landed pieces and pause behavior.
                                board.clear(); landing.setPosition(landing.getX(), landing.getY(), 8);
                                active.set(game, landing);
                                Block3D floor = game.getGhostBlock(); active.set(game, floor);
                                check(game.getGhostBlock().getZ() == floor.getZ(), "Already landed ghost moved");
                                game.stop(); check(game.getGhostBlock().getZ() == floor.getZ(), "Paused preview wrong");
                                active.set(game, null);
                                check(game.getGhostBlock() == null && game.getScene().ghostCells.isEmpty(), "Stale ghost without active block");
                            }
                            finally { game.stop(); }
                        }
            }
            catch (Exception e) { throw new RuntimeException(e); }
        });
        System.out.println("PASS: ghost rotation/XY preservation, collisions, nonmutation, hard-drop parity, snapshots, both views");
    }
}
