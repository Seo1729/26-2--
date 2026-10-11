import java.awt.Color;

/** Shared display palette for the main view and both previews. */
public final class BlockColors
{
    private static final Color[] COLORS = {
        new Color(57, 211, 230), new Color(255, 209, 70), new Color(187, 115, 245),
        new Color(255, 156, 67), new Color(83, 135, 246), new Color(91, 213, 132),
        new Color(245, 98, 121)
    };

    private BlockColors() { }

    public static Color colorFor(Block3D.Type type)
    {
        return type == null ? Color.GRAY : COLORS[type.ordinal()];
    }
}
