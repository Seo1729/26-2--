package threed;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.JPanel;

// 3D 그리기 (카메라 각도 계산해서 큐브 면을 직접 그림)
// 카메라 계산은 Camera3D, 마우스 입력은 CameraDragHandler가 맡고 이 클래스는 그리기만 한다.
public class Board3DRenderer extends JPanel
{
    // 보드 대각선 길이: 화면에 보드 전체가 들어가도록 배율을 정할 때 쓴다
    private static final double BOARD_DIAMETER = Math.sqrt((double) Board3D.SIZE_X * Board3D.SIZE_X
            + (double) Board3D.SIZE_Y * Board3D.SIZE_Y + (double) Board3D.SIZE_Z * Board3D.SIZE_Z);
    // 큐브 면을 칸보다 조금 작게 그려 틈을 보이게 한다 (칸 안쪽 시작/끝 위치)
    private static final double CUBE_NEAR = .035;
    private static final double CUBE_FAR = .965;
    // 고스트 점선은 조금 더 안쪽에 그린다
    private static final double GHOST_NEAR = .07;
    private static final double GHOST_FAR = .93;

    // 화면 직렬화와 상관없는 데이터라 transient (Swing 부품은 Serializable)
    private transient Game3DScene scene = new Game3DScene(java.util.Collections.<Game3DScene.Cell>emptyList());
    private final transient Camera3D camera = new Camera3D();

    public Board3DRenderer()
    {
        setPreferredSize(new Dimension(640, 720));
        setBackground(Theme3D.VIEW_BACKGROUND);
        CameraDragHandler cameraInput = new CameraDragHandler(camera, this);
        addMouseListener(cameraInput);
        addMouseMotionListener(cameraInput);
        addMouseWheelListener(cameraInput);
    }

    public void setScene(Game3DScene scene)
    {
        this.scene = java.util.Objects.requireNonNull(scene, "scene");
        repaint();
    }

    private void line(Graphics2D g, double x, double y, double z, double a, double b, double c)
    {
        g.draw(new Line2D.Double(camera.project(x, y, z), camera.project(a, b, c)));
    }

    // 큐브 면 하나
    private static final class Face
    {
        final double[][] vertices;
        final Color color;
        final boolean active;
        final double depth;

        Face(double[][] vertices, Color color, boolean active, double nx, double ny, double nz)
        {
            this.vertices = vertices;
            this.color = color;
            this.active = active;
            double sum = 0;
            for (double[] p : vertices) sum += nx * p[0] + ny * p[1] + nz * p[2];
            depth = sum / vertices.length;
        }
    }

    @Override
    protected void paintComponent(Graphics graphics)
    {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        try
        {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            camera.fitTo(getWidth(), getHeight(), BOARD_DIAMETER);
            drawBackGrid(g);
            drawFaces(g, collectVisibleFaces());
            drawFrame(g);
            drawGhosts(g);
            drawLabels(g);
        }
        finally { g.dispose(); }
    }

    // 뒤쪽 벽, 바닥 선과 높이 숫자
    private void drawBackGrid(Graphics2D g)
    {
        g.setColor(Theme3D.BACK_GRID);
        g.setStroke(new BasicStroke(1));
        for (int z = 0; z <= Board3D.SIZE_Z; z++)
        {
            line(g, 0, 0, z, Board3D.SIZE_X, 0, z);
            line(g, 0, 0, z, 0, Board3D.SIZE_Y, z);
            Point2D label = camera.project(0, 0, z);
            g.drawString(Integer.toString(z), (float) label.getX() - 24, (float) label.getY() + 4);
        }
        for (int i = 0; i <= Board3D.SIZE_X; i++)
        {
            line(g, i, 0, 0, i, 0, Board3D.SIZE_Z);
            line(g, 0, i, 0, 0, i, Board3D.SIZE_Z);
            line(g, i, 0, 0, i, Board3D.SIZE_Y, 0);
            line(g, 0, i, 0, Board3D.SIZE_X, i, 0);
        }
    }

    // 칸마다 카메라에서 보이는 면 3개만 모아서, 먼 면부터 그리도록 정렬한다
    private List<Face> collectVisibleFaces()
    {
        double nx = camera.viewX();
        double ny = camera.viewY();
        double nz = camera.viewZ();
        List<Face> faces = new ArrayList<>();
        for (Game3DScene.Cell cell : scene.cells)
        {
            double x = cell.x + CUBE_NEAR;
            double y = cell.y + CUBE_NEAR;
            double z = cell.z + CUBE_NEAR;
            double a = cell.x + CUBE_FAR;
            double b = cell.y + CUBE_FAR;
            double c = cell.z + CUBE_FAR;
            Color color = BlockColors.colorFor(cell.type);
            // 카메라에서 보이는 면만
            double sideX = nx >= 0 ? a : x;
            double sideY = ny >= 0 ? b : y;
            double sideZ = nz >= 0 ? c : z;
            faces.add(new Face(new double[][] {{sideX,y,z},{sideX,b,z},{sideX,b,c},{sideX,y,c}}, color.darker(), cell.active, nx, ny, nz));
            faces.add(new Face(new double[][] {{x,sideY,z},{a,sideY,z},{a,sideY,c},{x,sideY,c}}, color, cell.active, nx, ny, nz));
            faces.add(new Face(new double[][] {{x,y,sideZ},{a,y,sideZ},{a,b,sideZ},{x,b,sideZ}}, color.brighter(), cell.active, nx, ny, nz));
        }
        // 먼 면부터 그려야 해서 정렬
        Collections.sort(faces, new Comparator<Face>()
        {
            @Override
            public int compare(Face f1, Face f2)
            {
                return Double.compare(f1.depth, f2.depth);
            }
        });
        return faces;
    }

    private void drawFaces(Graphics2D g, List<Face> faces)
    {
        for (Face face : faces)
        {
            Path2D path = new Path2D.Double();
            for (int i = 0; i < face.vertices.length; i++)
            {
                double[] v = face.vertices[i];
                Point2D p = camera.project(v[0], v[1], v[2]);
                if (i == 0) path.moveTo(p.getX(), p.getY()); else path.lineTo(p.getX(), p.getY());
            }
            path.closePath();
            g.setColor(face.color);
            g.fill(path);
            // 떨어지는 블록은 흰 굵은 테두리, 고정 블록은 어두운 얇은 테두리
            g.setColor(face.active ? Theme3D.ACTIVE_OUTLINE : Theme3D.FACE_OUTLINE);
            g.setStroke(new BasicStroke(face.active ? 2f : 1f));
            g.draw(path);
        }
    }

    // 보드 바깥 테두리 (세로 모서리 4개 + 바닥·천장 사각형)
    private void drawFrame(Graphics2D g)
    {
        int sx = Board3D.SIZE_X;
        int sy = Board3D.SIZE_Y;
        int sz = Board3D.SIZE_Z;
        g.setColor(Theme3D.FRAME);
        g.setStroke(new BasicStroke(1.5f));
        for (int x : new int[] {0, sx})
            for (int y : new int[] {0, sy}) line(g, x, y, 0, x, y, sz);
        for (int z : new int[] {0, sz})
        {
            line(g, 0, 0, z, sx, 0, z);
            line(g, sx, 0, z, sx, sy, z);
            line(g, sx, sy, z, 0, sy, z);
            line(g, 0, sy, z, 0, 0, z);
        }
    }

    // 고스트 (점선 상자)
    private void drawGhosts(Graphics2D g)
    {
        g.setColor(Theme3D.GHOST_TRANSLUCENT);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                10f, new float[] {5f, 4f}, 0f));
        for (Game3DScene.Cell cell : scene.ghostCells)
            drawGhostBox(g, cell);
    }

    private void drawGhostBox(Graphics2D g, Game3DScene.Cell cell)
    {
        double x = cell.x + GHOST_NEAR;
        double y = cell.y + GHOST_NEAR;
        double z = cell.z + GHOST_NEAR;
        double a = cell.x + GHOST_FAR;
        double b = cell.y + GHOST_FAR;
        double c = cell.z + GHOST_FAR;
        for (double zz : new double[] {z, c})
        {
            line(g, x, y, zz, a, y, zz);
            line(g, a, y, zz, a, b, zz);
            line(g, a, b, zz, x, b, zz);
            line(g, x, b, zz, x, y, zz);
        }
        for (double xx : new double[] {x, a})
            for (double yy : new double[] {y, b}) line(g, xx, yy, z, xx, yy, c);
    }

    // 조작 안내, 축 이름, 아래쪽 블록 색 범례
    private void drawLabels(Graphics2D g)
    {
        g.setColor(Theme3D.MUTED_TEXT);
        g.drawString("Left drag: orbit | Wheel: zoom | Falling: white | Ghost: cyan dashed", 16, 22);
        Point2D px = camera.project(Board3D.SIZE_X, 0, 0);
        Point2D py = camera.project(0, Board3D.SIZE_Y, 0);
        g.drawString("X+ (D)", (float) px.getX() + 8, (float) px.getY() + 16);
        g.drawString("Y+ (S)", (float) py.getX() - 58, (float) py.getY() + 16);
        for (Block3D.Type type : Block3D.Type.values())
        {
            int left = 16 + type.ordinal() * 55;
            g.setColor(BlockColors.colorFor(type));
            g.fillRect(left, getHeight() - 27, 12, 12);
            g.drawString(type.name(), left + 18, getHeight() - 16);
        }
    }
}
