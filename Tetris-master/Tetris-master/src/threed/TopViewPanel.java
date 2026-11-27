package threed;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Collections;
import javax.swing.JPanel;

// 위에서 본 모습 (TOP VIEW)
// 어떤 칸을 보여줄지는 TopViewProjection이 계산하고, 이 클래스는 그리기만 한다.
public class TopViewPanel extends JPanel
{
    private static final int GRID_TOP = 78;          // 격자 윗변 y 좌표
    private static final int SIDE_MARGIN = 64;       // 좌우 여백 합
    private static final int VERTICAL_MARGIN = 150;  // 위아래 글자 영역 합

    // 화면 직렬화와 상관없는 데이터라 transient (Swing 부품은 Serializable)
    private transient Game3DScene scene = new Game3DScene(Collections.<Game3DScene.Cell>emptyList());

    public TopViewPanel()
    {
        setPreferredSize(new Dimension(280, 360));
        setBackground(Theme3D.VIEW_BACKGROUND);
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
            drawHeader(g);
            int cellSize = Math.max(0, Math.min(getWidth() - SIDE_MARGIN, getHeight() - VERTICAL_MARGIN) / Board3D.SIZE_X);
            if (cellSize < 1) return;
            int side = cellSize * Board3D.SIZE_X;
            int left = (getWidth() - side) / 2;
            TopViewProjection projection = new TopViewProjection(scene);
            for (int y = 0; y < Board3D.SIZE_Y; y++)
                for (int x = 0; x < Board3D.SIZE_X; x++)
                    drawColumn(g, projection, x, y, left + x * cellSize, GRID_TOP + y * cellSize, cellSize);
            drawGrid(g, left, side, cellSize);
            drawLegend(g, side);
        }
        finally { g.dispose(); }
    }

    private void drawHeader(Graphics2D g)
    {
        g.setColor(Theme3D.TEXT);
        g.drawString("TOP VIEW", 20, 26);
        g.drawString("X+ (D) right / Y+ (S) down", 20, 48);
    }

    // 한 칸(x, y): 고정 블록 바탕 → 떨어지는 블록 표시 → 고스트 점선 순서로 겹쳐 그린다
    private void drawColumn(Graphics2D g, TopViewProjection projection, int x, int y, int px, int py, int cellSize)
    {
        Game3DScene.Cell locked = projection.fixedAt(x, y);
        g.setColor(locked == null ? Theme3D.EMPTY_CELL : BlockColors.colorFor(locked.type).darker());
        g.fillRect(px, py, cellSize, cellSize);
        Game3DScene.Cell falling = projection.activeAt(x, y);
        if (falling != null)
            drawFallingMarker(g, falling, px, py, cellSize);
        Game3DScene.Cell ghost = projection.ghostAt(x, y);
        if (ghost != null)
            drawGhostMarker(g, ghost, px, py, cellSize);
    }

    private void drawFallingMarker(Graphics2D g, Game3DScene.Cell falling, int px, int py, int cellSize)
    {
        int inset = Math.max(3, cellSize / 6);
        int markerSize = cellSize - inset * 2;
        g.setColor(BlockColors.colorFor(falling.type));
        g.fillRect(px + inset, py + inset, markerSize, markerSize);
        g.setColor(Theme3D.ACTIVE_OUTLINE);
        g.setStroke(new BasicStroke(2f));
        g.drawRect(px + inset, py + inset, markerSize, markerSize);
    }

    private void drawGhostMarker(Graphics2D g, Game3DScene.Cell ghost, int px, int py, int cellSize)
    {
        int inset = Math.max(2, cellSize / 14);
        g.setColor(Theme3D.GHOST);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10f, new float[] {4f, 3f}, 0f));
        g.drawRect(px + inset, py + inset, cellSize - inset * 2, cellSize - inset * 2);
        g.drawString("Z=" + ghost.z, px + inset + 3, py + cellSize - inset - 3);
    }

    // 격자선과 0~2 좌표 숫자
    private void drawGrid(Graphics2D g, int left, int side, int cellSize)
    {
        g.setColor(Theme3D.MUTED_TEXT);
        g.setStroke(new BasicStroke(1f));
        for (int i = 0; i <= Board3D.SIZE_X; i++)
        {
            g.drawLine(left + i * cellSize, GRID_TOP, left + i * cellSize, GRID_TOP + side);
            g.drawLine(left, GRID_TOP + i * cellSize, left + side, GRID_TOP + i * cellSize);
        }
        for (int i = 0; i < Board3D.SIZE_X; i++)
        {
            g.drawString(Integer.toString(i), left + i * cellSize + cellSize / 2 - 3, GRID_TOP - 8);
            g.drawString(Integer.toString(i), left - 16, GRID_TOP + i * cellSize + cellSize / 2 + 4);
        }
    }

    private void drawLegend(Graphics2D g, int side)
    {
        g.setColor(Theme3D.TEXT);
        g.drawString("Falling: bright inset + white outline", 16, GRID_TOP + side + 26);
        g.drawString("Fixed: full cell (highest Z)", 16, GRID_TOP + side + 46);
        g.setColor(Theme3D.GHOST);
        g.drawString("Ghost: cyan dashed / landing Z", 16, GRID_TOP + side + 66);
    }
}
