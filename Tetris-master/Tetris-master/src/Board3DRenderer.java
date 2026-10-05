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

    public Board3DRenderer()
    {
        setPreferredSize(new Dimension(640, 720));
        setBackground(new Color(19, 25, 38));
    }

    public void setScene(Game3DScene scene)
    {
        this.scene = java.util.Objects.requireNonNull(scene, "scene");
        repaint();
    }

    private Point2D project(double x, double y, double z)
    {
        return new Point2D.Double(originX + scale * .70710678 * (x - y),
                originY + scale * (.24184476 * (x + y) - .93969262 * z));
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
        Face(double[][] vertices, Color color, boolean active)
        {
            this.vertices = vertices; this.color = color; this.active = active;
            double sum = 0;
            for (double[] p : vertices) sum += .664463 * (p[0] + p[1]) + .342020 * p[2];
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
            double width = .70710678 * (Board3D.SIZE_X + Board3D.SIZE_Y);
            double height = .93969262 * Board3D.SIZE_Z + .24184476 * (Board3D.SIZE_X + Board3D.SIZE_Y);
            scale = Math.max(1, Math.min((getWidth() - 100.0) / width, (getHeight() - 110.0) / height));
            originX = getWidth() / 2.0;
            originY = (getHeight() - scale * height) / 2 + scale * .93969262 * Board3D.SIZE_Z;
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
                faces.add(new Face(new double[][] {{a,y,z},{a,b,z},{a,b,c},{a,y,c}}, color.darker(), cell.active));
                faces.add(new Face(new double[][] {{x,b,z},{a,b,z},{a,b,c},{x,b,c}}, color, cell.active));
                faces.add(new Face(new double[][] {{x,y,c},{a,y,c},{a,b,c},{x,b,c}}, color.brighter(), cell.active));
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
            g.drawString("3 x 3 x 14   |   Z up   |   falling: white outline", 16, 22);
            Point2D px = project(3, 0, 0), py = project(0, 3, 0);
            g.drawString("X+ (D)", (float) px.getX() + 8, (float) px.getY() + 16);
            g.drawString("Y+ (W)", (float) py.getX() - 58, (float) py.getY() + 16);
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
