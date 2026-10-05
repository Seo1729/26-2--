import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Collections;
import javax.swing.JPanel;

/** Fixed world-space XY projection, independent of the main camera. */
public final class TopViewPanel extends JPanel
{
    private Game3DScene scene = new Game3DScene(Collections.emptyList());

    public TopViewPanel()
    {
        setPreferredSize(new Dimension(280, 360));
        setBackground(new Color(19, 25, 38));
    }

    public void setScene(Game3DScene scene)
    {
        this.scene = java.util.Objects.requireNonNull(scene, "scene");
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics)
    {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try
        {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(220, 230, 245));
            g.drawString("TOP VIEW", 20, 26);
            g.drawString("X+ (D) right / Y+ (S) down", 20, 48);
            int cellSize = Math.max(0, Math.min(getWidth() - 64, getHeight() - 150) / 3);
            if (cellSize < 1) return;
            int side = cellSize * 3;
            int left = (getWidth() - side) / 2;
            int top = 78;
            Game3DScene.Cell[][] fixed = new Game3DScene.Cell[3][3];
            Game3DScene.Cell[][] active = new Game3DScene.Cell[3][3];
            Game3DScene.Cell[][] ghost = new Game3DScene.Cell[3][3];
            for (Game3DScene.Cell cell : scene.ghostCells)
            {
                if (cell.x < 0 || cell.x >= 3 || cell.y < 0 || cell.y >= 3) continue;
                Game3DScene.Cell previous = ghost[cell.x][cell.y];
                if (previous == null || cell.z < previous.z) ghost[cell.x][cell.y] = cell;
            }
            for (Game3DScene.Cell cell : scene.cells)
            {
                if (cell.x < 0 || cell.x >= 3 || cell.y < 0 || cell.y >= 3) continue;
                Game3DScene.Cell[][] target = cell.active ? active : fixed;
                Game3DScene.Cell previous = target[cell.x][cell.y];
                if (previous == null || cell.z > previous.z) target[cell.x][cell.y] = cell;
            }
            for (int y = 0; y < 3; y++)
                for (int x = 0; x < 3; x++)
                {
                    int px = left + x * cellSize, py = top + y * cellSize;
                    Game3DScene.Cell locked = fixed[x][y], falling = active[x][y];
                    g.setColor(locked == null ? new Color(30, 40, 57)
                            : Board3DRenderer.colorFor(locked.type).darker());
                    g.fillRect(px, py, cellSize, cellSize);
                    if (falling != null)
                    {
                        int inset = Math.max(3, cellSize / 6);
                        int markerSize = cellSize - inset * 2;
                        g.setColor(Board3DRenderer.colorFor(falling.type));
                        g.fillRect(px + inset, py + inset, markerSize, markerSize);
                        g.setColor(new Color(255, 255, 240));
                        g.setStroke(new BasicStroke(2f));
                        g.drawRect(px + inset, py + inset, markerSize, markerSize);
                    }
                    if (ghost[x][y] != null)
                    {
                        int inset = Math.max(2, cellSize / 14);
                        g.setColor(new Color(114, 233, 255));
                        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                                10f, new float[] {4f, 3f}, 0f));
                        g.drawRect(px + inset, py + inset, cellSize - inset * 2, cellSize - inset * 2);
                        g.drawString("Z=" + ghost[x][y].z, px + inset + 3, py + cellSize - inset - 3);
                    }
                }
            g.setColor(new Color(145, 166, 193));
            g.setStroke(new BasicStroke(1f));
            for (int i = 0; i <= 3; i++)
            {
                g.drawLine(left + i * cellSize, top, left + i * cellSize, top + side);
                g.drawLine(left, top + i * cellSize, left + side, top + i * cellSize);
            }
            for (int i = 0; i < 3; i++)
            {
                g.drawString(Integer.toString(i), left + i * cellSize + cellSize / 2 - 3, top - 8);
                g.drawString(Integer.toString(i), left - 16, top + i * cellSize + cellSize / 2 + 4);
            }
            g.setColor(new Color(220, 230, 245));
            g.drawString("Falling: bright inset + white outline", 16, top + side + 26);
            g.drawString("Fixed: full cell (highest Z)", 16, top + side + 46);
            g.setColor(new Color(114, 233, 255));
            g.drawString("Ghost: cyan dashed / landing Z", 16, top + side + 66);
        }
        finally { g.dispose(); }
    }
}
