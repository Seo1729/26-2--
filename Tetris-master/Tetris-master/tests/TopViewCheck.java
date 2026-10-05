import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.SwingUtilities;

public class TopViewCheck
{
    private static void check(boolean value, String message)
    {
        if (!value) throw new AssertionError(message);
    }

    private static BufferedImage render(TopViewPanel panel)
    {
        BufferedImage image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        panel.paint(g);
        g.dispose();
        return image;
    }

    public static void main(String[] args) throws Exception
    {
        SwingUtilities.invokeAndWait(() -> {
            TopViewPanel panel = new TopViewPanel();
            Game3DScene scene = new Game3DScene(Arrays.asList(
                    new Game3DScene.Cell(0, 0, 7, Block3D.Type.T, false),
                    new Game3DScene.Cell(0, 0, 0, Block3D.Type.O, false),
                    new Game3DScene.Cell(0, 0, 10, Block3D.Type.I, true),
                    new Game3DScene.Cell(2, 1, 1, Block3D.Type.O, false),
                    new Game3DScene.Cell(1, 2, 12, Block3D.Type.I, true)));
            panel.setScene(scene);
            for (int[] size : new int[][] {{280, 360}, {400, 360}, {250, 300}})
            {
                panel.setSize(size[0], size[1]);
                BufferedImage image = render(panel);
                int cell = Math.min(size[0] - 64, size[1] - 150) / 3;
                int left = (size[0] - cell * 3) / 2, top = 78;
                check(image.getRGB(left + cell / 2, top + cell / 2)
                        == Board3DRenderer.colorFor(Block3D.Type.I).getRGB(), "Falling projection missing");
                check(image.getRGB(left + 2, top + 2)
                        == Board3DRenderer.colorFor(Block3D.Type.T).darker().getRGB(),
                        "Highest fixed cube hidden or selected in list order");
                check(image.getRGB(left + 2 * cell + cell / 2, top + cell + cell / 2)
                        == Board3DRenderer.colorFor(Block3D.Type.O).darker().getRGB(), "XY axes swapped");
                check(image.getRGB(left + cell + cell / 2, top + 2 * cell + cell / 2)
                        == Board3DRenderer.colorFor(Block3D.Type.I).getRGB(), "Y direction incorrect");
                int grid = new Color(145, 166, 193).getRGB();
                for (int i = 0; i <= 3; i++)
                {
                    check(image.getRGB(left + i * cell, top + cell / 2) == grid, "Vertical boundary missing");
                    check(image.getRGB(left + cell / 2, top + i * cell) == grid, "Horizontal boundary missing");
                }
            }
            panel.setScene(new Game3DScene(Collections.emptyList()));
            BufferedImage cleared = render(panel);
            int cell = Math.min(panel.getWidth() - 64, panel.getHeight() - 150) / 3;
            int left = (panel.getWidth() - cell * 3) / 2;
            check(cleared.getRGB(left + cell / 2, 78 + cell / 2) == new Color(30, 40, 57).getRGB(),
                    "Scene refresh left stale blocks");
        });
        System.out.println("PASS: top view XY projection, overlap, highest fixed Z, square grid, refresh");
    }
}
