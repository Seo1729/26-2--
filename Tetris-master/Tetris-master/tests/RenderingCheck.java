import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

public class RenderingCheck
{
    private static void check(boolean value, String message)
    {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            Board3D board = new Board3D();
            board.lockBlock(new Block3D(Block3D.Type.O, 0, 0, 0));
            board.lockBlock(new Block3D(Block3D.Type.T, 1, 1, 2));
            board.setFilled(2, 2, 1, true);
            Game3D game = new Game3D(board, 60000);
            try
            {
                game.start();
                Game3DScene before = game.getScene();
                check(before.cells.size() == 13, "Missing fixed or active cells");
                check(before.cells.stream().filter(cell -> cell.active).count() == 4, "Missing falling cubes");
                check(before.cells.stream().filter(cell -> !cell.active && cell.type == Block3D.Type.O).count() == 4,
                        "Fixed type lost");
                try { before.cells.clear(); throw new AssertionError("Mutable snapshot"); }
                catch (UnsupportedOperationException expected) { }
                Board3DRenderer renderer = new Board3DRenderer();
                renderer.setScene(before);
                for (int[] size : new int[][] {{640, 720}, {420, 480}, {1000, 800}})
                {
                    renderer.setSize(size[0], size[1]);
                    BufferedImage image = new BufferedImage(size[0], size[1], BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics = image.createGraphics();
                    renderer.paint(graphics);
                    graphics.dispose();
                    int colored = 0;
                    for (int y = 40; y < size[1] - 40; y++)
                        for (int x = 0; x < size[0]; x++)
                        {
                            java.awt.Color color = new java.awt.Color(image.getRGB(x, y));
                            if (Math.max(color.getRed(), Math.max(color.getGreen(), color.getBlue())) > 180) colored++;
                        }
                    check(colored > 200, "Cubes not painted");
                    if (args.length > 0 && size[0] == 640)
                        try { ImageIO.write(image, "png", new java.io.File(args[0])); }
                        catch (java.io.IOException exception) { throw new RuntimeException(exception); }
                }
                renderer.setSize(640, 720);
                BufferedImage initial = render(renderer);
                renderer.dispatchEvent(new MouseEvent(renderer, MouseEvent.MOUSE_PRESSED, 0, 0,
                        100, 100, 1, false, MouseEvent.BUTTON1));
                renderer.dispatchEvent(new MouseEvent(renderer, MouseEvent.MOUSE_DRAGGED, 1,
                        MouseEvent.BUTTON1_DOWN_MASK, 450, 20, 0, false, MouseEvent.NOBUTTON));
                renderer.dispatchEvent(new MouseEvent(renderer, MouseEvent.MOUSE_RELEASED, 2, 0,
                        450, 20, 1, false, MouseEvent.BUTTON1));
                BufferedImage rotated = render(renderer);
                check(different(initial, rotated), "Orbit did not change the image");
                renderer.dispatchEvent(new MouseWheelEvent(renderer, MouseEvent.MOUSE_WHEEL, 3, 0,
                        320, 360, 0, false, MouseWheelEvent.WHEEL_UNIT_SCROLL, 1, -3));
                check(different(rotated, render(renderer)), "Zoom did not change the image");
                Game3DScene afterCamera = game.getScene();
                for (int i = 0; i < before.cells.size(); i++)
                {
                    Game3DScene.Cell oldCell = before.cells.get(i), newCell = afterCamera.cells.get(i);
                    check(oldCell.x == newCell.x && oldCell.y == newCell.y && oldCell.z == newCell.z
                            && oldCell.type == newCell.type && oldCell.active == newCell.active,
                            "Camera changed world coordinates");
                }
                check(game.isRunning() && game.getLockedBlockCount() == 0,
                        "Camera changed game progression");
                check(game.getScene().cells.size() == before.cells.size(), "Rendering mutated game");
                check(game.hardDrop(), "Drop failed");
                check(before.cells.size() == 13, "Snapshot changed with game");
                check(game.getScene().cells.stream().filter(cell -> !cell.active).count() == 13,
                        "Newly locked cubes absent");
            }
            finally { game.stop(); }
        });
        System.out.println("PASS: rendering, orbit, zoom, unchanged game coordinates/progression, drop refresh");
    }

    private static BufferedImage render(Board3DRenderer renderer)
    {
        BufferedImage image = new BufferedImage(640, 720, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        renderer.paint(graphics);
        graphics.dispose();
        return image;
    }

    private static boolean different(BufferedImage first, BufferedImage second)
    {
        for (int y = 40; y < 680; y++)
            for (int x = 0; x < 640; x++)
                if (first.getRGB(x, y) != second.getRGB(x, y)) return true;
        return false;
    }
}
