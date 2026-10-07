import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/** Camera-independent preview of the next piece's initial XY shape. */
public final class NextBlockPanel extends JPanel
{
    private Block3D.Type type;

    public NextBlockPanel()
    {
        setOpaque(false);
        setPreferredSize(new Dimension(160, 58));
    }

    public void setType(Block3D.Type type)
    {
        if (this.type != type)
        {
            this.type = type;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics graphics)
    {
        super.paintComponent(graphics);
        if (type == null) return;
        Block3D.Cube[] cubes = new Block3D(type, 0, 0, 0).getRelativeCubes();
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (Block3D.Cube cube : cubes)
        {
            minX = Math.min(minX, cube.getX()); minY = Math.min(minY, cube.getY());
            maxX = Math.max(maxX, cube.getX()); maxY = Math.max(maxY, cube.getY());
        }
        int columns = maxX - minX + 1, rows = maxY - minY + 1;
        int cell = Math.min(24, Math.min(getWidth() / columns, getHeight() / rows));
        if (cell < 4) return;
        int left = (getWidth() - columns * cell) / 2, top = (getHeight() - rows * cell) / 2;
        Graphics2D g = (Graphics2D) graphics.create();
        try
        {
            g.setStroke(new BasicStroke(1f));
            for (Block3D.Cube cube : cubes)
            {
                int x = left + (cube.getX() - minX) * cell;
                int y = top + (cube.getY() - minY) * cell;
                g.setColor(BlockColors.colorFor(type));
                g.fillRect(x + 1, y + 1, cell - 2, cell - 2);
                g.setColor(BlockColors.colorFor(type).brighter());
                g.drawRect(x + 1, y + 1, cell - 3, cell - 3);
            }
        }
        finally { g.dispose(); }
    }
}
