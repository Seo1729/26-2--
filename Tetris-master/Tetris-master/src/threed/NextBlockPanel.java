package threed;

import java.awt.BasicStroke;
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

    @Override
    protected void paintComponent(Graphics graphics)
    {
        super.paintComponent(graphics);
        if (type == null) return;
        Block3D.Cube[] cubes = new Block3D(type, 0, 0, 0).getRelativeCubes();
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (Block3D.Cube cube : cubes)
        {
            minX = Math.min(minX, cube.getX()); minY = Math.min(minY, cube.getY());
            maxX = Math.max(maxX, cube.getX()); maxY = Math.max(maxY, cube.getY());
        }
        int columns = maxX - minX + 1;
        int rows = maxY - minY + 1;
        int cell = Math.min(24, Math.min(getWidth() / columns, getHeight() / rows));
        if (cell < 4) return;
        int left = (getWidth() - columns * cell) / 2;
        int top = (getHeight() - rows * cell) / 2;
        Graphics2D g = (Graphics2D) graphics.create();
        try
        {
            g.setStroke(new BasicStroke(1f));
            for (Block3D.Cube cube : cubes)
            {
                int x = left + (cube.getX() - minX) * cell;
                int y = top + (cube.getY() - minY) * cell;
                // 색은 메인 화면과 같은 BlockColors 한 곳에서 가져온다 (복사본 제거)
                g.setColor(BlockColors.colorFor(type));
                g.fillRect(x + 1, y + 1, cell - 2, cell - 2);
                g.setColor(BlockColors.colorFor(type).brighter());
                g.drawRect(x + 1, y + 1, cell - 3, cell - 3);
            }
        }
        finally { g.dispose(); }
    }
}
