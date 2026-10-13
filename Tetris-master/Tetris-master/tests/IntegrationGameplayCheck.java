import java.lang.reflect.Method;
import java.util.Arrays;
import javax.swing.SwingUtilities;

/** Exercises the original 2D movement and scoring using the existing isolated board fixture. */
public final class IntegrationGameplayCheck {
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        new ImageSource();
        SwingUtilities.invokeAndWait(() -> {
            try {
                Method factory = HoldCheck.class.getDeclaredMethod("board"); factory.setAccessible(true);
                HoldCheck.Board b = (HoldCheck.Board) factory.invoke(null);
                Method active = HoldCheck.class.getDeclaredMethod("active", HoldCheck.Board.class,
                    int.class, int.class, int.class); active.setAccessible(true);
                active.invoke(null, b, 1, 5, 5);
                b.move_left(); check(b.x == 4, "2D left movement");
                b.move_right(); check(b.x == 5, "2D right movement");
                b.move_down(); check(b.y == 6, "2D soft drop");
                b.move_turn(); check(b.blockHSize == 4, "2D rotation");
                b.preBlock = 4;
                b.move_drop();
                boolean locked = false;
                for (int[] row : b.field) for (int cell : row) locked |= cell > 90;
                check(locked && b.y == 0, "2D hard drop / next spawn");
                b.score.setText("0"); b.gameSpeed = 1000;
                for (int lines = 1; lines <= 4; lines++) b.addScore(lines);
                check(b.score.getText().equals("230"), "2D existing score formula");
                Arrays.fill(b.field[19], 1, 11, 91);
                b.field[18][4] = 92;
                b.lineDelete(19);
                check(b.field[19][4] == 92 && b.field[0][4] == 0, "2D line compaction");
                b.gameRun = false; b.Gaming = false; b.th.interrupt(); b.timeTh.interrupt();
            } catch (Exception e) { throw new RuntimeException(e); }
        });
        System.out.println("Original 2D movement, rotation, soft/hard drop, scoring and line compaction passed.");
    }
}
