package origin;

import java.lang.reflect.Method;
import java.util.Arrays;
import javax.swing.SwingUtilities;

/** Exercises the original 2D movement and scoring using the existing isolated board fixture. */
public final class IntegrationGameplayCheck {
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        ImageSource.preload();
        SwingUtilities.invokeAndWait(() -> {
            try {
                Method factory = HoldCheck.class.getDeclaredMethod("board"); factory.setAccessible(true);
                HoldCheck.Board b = (HoldCheck.Board) factory.invoke(null);
                Method active = HoldCheck.class.getDeclaredMethod("active", HoldCheck.Board.class,
                    int.class, int.class, int.class); active.setAccessible(true);
                active.invoke(null, b, 1, 5, 5);
                b.moveLeft(); check(b.board.x == 4, "2D left movement");
                b.moveRight(); check(b.board.x == 5, "2D right movement");
                b.moveDown(); check(b.board.y == 6, "2D soft drop");
                b.moveTurn(); check(b.board.blockHSize == 4, "2D rotation");
                b.preBlock = 4;
                b.moveDrop();
                boolean locked = false;
                for (int[] row : b.board.field) for (int cell : row) locked |= cell > 90;
                check(locked && b.board.y == 0, "2D hard drop / next spawn");
                b.score.setText("0"); b.highestScore = new javax.swing.JLabel("0"); b.gameSpeed = 1000;
                for (int lines = 1; lines <= 4; lines++) b.addScore(lines);
                check(b.score.getText().equals("230"), "2D existing score formula");
                Arrays.fill(b.board.field[19], 1, 11, 91);
                b.board.field[18][4] = 92;
                b.board.deleteRow(19);
                check(b.board.field[19][4] == 92 && b.board.field[0][4] == 0, "2D line compaction");
                b.gameRun = false; b.gaming = false; b.th.interrupt(); b.timeTh.interrupt();
            } catch (Exception e) { throw new RuntimeException(e); }
        });
        System.out.println("Original 2D movement, rotation, soft/hard drop, scoring and line compaction passed.");
    }
}
