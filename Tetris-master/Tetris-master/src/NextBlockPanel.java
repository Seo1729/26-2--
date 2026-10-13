import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

// 다음 블록 미리보기
public class NextBlockPanel extends JPanel
{
    Block3D.Type type;

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
                g.setColor(getColor(type));
                g.fillRect(x + 1, y + 1, cell - 2, cell - 2);
                g.setColor(getColor(type).brighter());
                g.drawRect(x + 1, y + 1, cell - 3, cell - 3);
            }
        }
        finally { g.dispose(); }
    }

    // 블록 색 (BlockColors꺼 복사)
    static Color getColor(Block3D.Type type)
    {
        Color[] colors = {
            new Color(57, 211, 230), new Color(255, 209, 70), new Color(187, 115, 245),
            new Color(255, 156, 67), new Color(83, 135, 246), new Color(91, 213, 132),
            new Color(245, 98, 121)
        };
        if (type == null)
            return Color.GRAY;
        return colors[type.ordinal()];
    }
}
