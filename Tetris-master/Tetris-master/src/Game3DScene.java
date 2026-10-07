import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Immutable world-space data; contains no graphics or game rules. */
public final class Game3DScene
{
    public static final class Cell
    {
        public final int x, y, z;
        public final Block3D.Type type;
        public final boolean active;

        public Cell(int x, int y, int z, Block3D.Type type, boolean active)
        {
            this.x = x; this.y = y; this.z = z;
            this.type = type; this.active = active;
        }
    }

    public final List<Cell> cells;
    public final List<Cell> ghostCells;

    public Game3DScene(List<Cell> cells)
    {
        this(cells, Collections.emptyList());
    }

    public Game3DScene(List<Cell> cells, List<Cell> ghostCells)
    {
        this.cells = Collections.unmodifiableList(new ArrayList<Cell>(cells));
        this.ghostCells = Collections.unmodifiableList(new ArrayList<Cell>(ghostCells));
    }
}
