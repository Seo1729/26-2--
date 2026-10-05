import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.JPanel;

/** Swing software 3D renderer: orthographic camera, shaded faces and depth sorting. */
public final class Board3DRenderer extends JPanel
{
    private static final Color[] COLORS = {
        new Color(57, 211, 230), new Color(255, 209, 70), new Color(187, 115, 245),
        new Color(255, 156, 67), new Color(83, 135, 246), new Color(91, 213, 132),
        new Color(245, 98, 121)
    };
    private Game3DScene scene = new Game3DScene(java.util.Collections.emptyList());
    private double scale, originX, originY;
    private double azimuth = Math.PI / 4, elevation = Math.toRadians(20), zoom = 1;
    private int dragX, dragY;
    private boolean dragging;

    public Board3DRenderer()
    {
        setPreferredSize(new Dimension(640, 720));
        setBackground(new Color(19, 25, 38));
        MouseAdapter cameraInput = new MouseAdapter()
        {
            @Override public void mousePressed(MouseEvent event)
            {
                if (event.getButton() != MouseEvent.BUTTON1) return;
                dragging = true;
                dragX = event.getX(); dragY = event.getY();
            }
            @Override public void mouseReleased(MouseEvent event)
            {
                if (event.getButton() == MouseEvent.BUTTON1) dragging = false;
            }
            @Override public void mouseDragged(MouseEvent event)
            {
                if (!dragging || (event.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == 0) return;
                azimuth = Math.IEEEremainder(azimuth - (event.getX() - dragX) * .008, Math.PI * 2);
                elevation = Math.max(Math.toRadians(-85), Math.min(Math.toRadians(85),
                        elevation + (event.getY() - dragY) * .008));
                dragX = event.getX(); dragY = event.getY();
                repaint();
            }
            @Override public void mouseWheelMoved(MouseWheelEvent event)
            {
                zoom = Math.max(.3, Math.min(4, zoom * Math.pow(1.12, -event.getPreciseWheelRotation())));
                repaint();
            }
        };
        addMouseListener(cameraInput);
        addMouseMotionListener(cameraInput);
        addMouseWheelListener(cameraInput);
    }

    public void setScene(Game3DScene scene)
    {
        this.scene = java.util.Objects.requireNonNull(scene, "scene");
        repaint();
    }

    private Point2D project(double x, double y, double z)
    {
        x -= Board3D.SIZE_X / 2.0;
        y -= Board3D.SIZE_Y / 2.0;
        z -= Board3D.SIZE_Z / 2.0;
        return new Point2D.Double(originX + scale * (Math.sin(azimuth) * x - Math.cos(azimuth) * y),
                originY + scale * (Math.sin(elevation) * (Math.cos(azimuth) * x + Math.sin(azimuth) * y)
                        - Math.cos(elevation) * z));
    }

    private void line(Graphics2D g, double x, double y, double z, double a, double b, double c)
    {
        g.draw(new Line2D.Double(project(x, y, z), project(a, b, c)));
    }

    private static final class Face
    {
        final double[][] vertices;
        final Color color;
        final boolean active;
        final double depth;
        Face(double[][] vertices, Color color, boolean active, double nx, double ny, double nz)
        {
            this.vertices = vertices; this.color = color; this.active = active;
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
            // A constant bounding sphere keeps orbiting from changing the zoom.
            double diameter = Math.sqrt(Board3D.SIZE_X * Board3D.SIZE_X
                    + Board3D.SIZE_Y * Board3D.SIZE_Y + Board3D.SIZE_Z * Board3D.SIZE_Z);
            scale = Math.max(1, Math.min(getWidth() - 100.0, getHeight() - 110.0) / diameter) * zoom;
            originX = getWidth() / 2.0;
            originY = getHeight() / 2.0;
            double nx = Math.cos(elevation) * Math.cos(azimuth);
            double ny = Math.cos(elevation) * Math.sin(azimuth);
            double nz = Math.sin(elevation);
            g.setColor(new Color(65, 79, 101));
            g.setStroke(new BasicStroke(1));
            // Rear walls and floor identify every cell without covering the cubes.
            for (int z = 0; z <= Board3D.SIZE_Z; z++)
            {
                line(g, 0, 0, z, 3, 0, z);
                line(g, 0, 0, z, 0, 3, z);
                Point2D label = project(0, 0, z);
                g.drawString(Integer.toString(z), (float) label.getX() - 24, (float) label.getY() + 4);
            }
            for (int i = 0; i <= 3; i++)
            {
                line(g, i, 0, 0, i, 0, 14); line(g, 0, i, 0, 0, i, 14);
                line(g, i, 0, 0, i, 3, 0); line(g, 0, i, 0, 3, i, 0);
            }
            List<Face> faces = new ArrayList<Face>();
            for (Game3DScene.Cell cell : scene.cells)
            {
                double x = cell.x + .035, y = cell.y + .035, z = cell.z + .035;
                double a = cell.x + .965, b = cell.y + .965, c = cell.z + .965;
                Color color = cell.type == null ? Color.GRAY : COLORS[cell.type.ordinal()];
                // Choose the visible side on each axis for the current camera.
                double sideX = nx >= 0 ? a : x, sideY = ny >= 0 ? b : y, sideZ = nz >= 0 ? c : z;
                faces.add(new Face(new double[][] {{sideX,y,z},{sideX,b,z},{sideX,b,c},{sideX,y,c}}, color.darker(), cell.active, nx, ny, nz));
                faces.add(new Face(new double[][] {{x,sideY,z},{a,sideY,z},{a,sideY,c},{x,sideY,c}}, color, cell.active, nx, ny, nz));
                faces.add(new Face(new double[][] {{x,y,sideZ},{a,y,sideZ},{a,b,sideZ},{x,b,sideZ}}, color.brighter(), cell.active, nx, ny, nz));
            }
            faces.sort(Comparator.comparingDouble(face -> face.depth));
            for (Face face : faces)
            {
                Path2D path = new Path2D.Double();
                for (int i = 0; i < face.vertices.length; i++)
                {
                    double[] v = face.vertices[i];
                    Point2D p = project(v[0], v[1], v[2]);
                    if (i == 0) path.moveTo(p.getX(), p.getY()); else path.lineTo(p.getX(), p.getY());
                }
                path.closePath();
                g.setColor(face.color); g.fill(path);
                g.setColor(face.active ? new Color(255, 255, 240) : new Color(25, 32, 47));
                g.setStroke(new BasicStroke(face.active ? 2f : 1f)); g.draw(path);
            }
            g.setColor(new Color(145, 166, 193, 160));
            g.setStroke(new BasicStroke(1.5f));
            for (int x : new int[] {0, 3})
                for (int y : new int[] {0, 3}) line(g, x, y, 0, x, y, 14);
            for (int z : new int[] {0, 14})
            {
                line(g, 0, 0, z, 3, 0, z); line(g, 3, 0, z, 3, 3, z);
                line(g, 3, 3, z, 0, 3, z); line(g, 0, 3, z, 0, 0, z);
            }
            g.drawString("Left drag: orbit   |   Wheel: zoom   |   falling: white outline", 16, 22);
            Point2D px = project(3, 0, 0), py = project(0, 3, 0);
            g.drawString("X+ (D)", (float) px.getX() + 8, (float) px.getY() + 16);
            g.drawString("Y+ (S)", (float) py.getX() - 58, (float) py.getY() + 16);
            for (Block3D.Type type : Block3D.Type.values())
            {
                int left = 16 + type.ordinal() * 55;
                g.setColor(COLORS[type.ordinal()]); g.fillRect(left, getHeight() - 27, 12, 12);
                g.drawString(type.name(), left + 18, getHeight() - 16);
            }
        }
        finally { g.dispose(); }
    }
}
