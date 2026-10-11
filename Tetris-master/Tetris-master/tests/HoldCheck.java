import java.lang.reflect.Field;
import java.util.Arrays;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import sun.misc.Unsafe;

/** Headless integration checks without starting the constructor's countdown threads. */
public class HoldCheck
{
    static class Board extends GamePanel
    {
        @Override public void drawTetris() { }
        @Override public void checkArray() { }
    }

    private static void check(boolean value, String message)
    {
        if (!value) throw new AssertionError(message);
    }

    private static Board board() throws Exception
    {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        Board b = (Board) ((Unsafe) field.get(null)).allocateInstance(Board.class);
        b.array = new int[21][12];
        b.field = new int[21][12];
        b.holdManager = new HoldManager();
        b.holdSlot = new JLabel();
        b.nextBlock = new JLabel();
        b.score = new JLabel("123");
        b.textLabel = new JLabel();
        b.blackPanel = new JPanel();
        b.tetrisArea = new JPanel();
        b.th = new Thread();
        b.timeTh = new TimeThread(b, 0);
        b.gameRun = true;
        b.Gaming = true;
        b.setArray();
        return b;
    }

    private static void active(Board b, int type, int x, int y)
    {
        for (int r = 0; r < 20; r++) Arrays.fill(b.array[r], 1, 11, 0);
        Blocks piece = new Blocks();
        piece.blockNum = type;
        b.block = piece.getBlock();
        b.blockNum = type;
        b.blockHSize = b.block.length;
        b.blockWSize = b.block[0].length;
        b.x = x;
        b.y = y;
        for (int r = 0; r < b.blockHSize; r++)
            for (int c = 0; c < b.blockWSize; c++)
                b.array[y + r][x + c] = b.block[r][c];
    }

    public static void main(String[] args) throws Exception
    {
        new ImageSource();
        SwingUtilities.invokeAndWait(() -> {
            try
            {
                GameFrame.gameMode = 3;
                Board b = board();
                active(b, 1, 2, 8);
                b.move_turn();
                check(b.blockHSize == 4, "Rotation setup failed");
                b.preBlock = 6;
                b.field[19][1] = 97;
                Thread fallThread = b.th;
                b.holdBlock();
                check(b.holdManager.getHeldBlock() == 1 && b.blockNum == 6, "Empty hold/next consumption");
                check(b.x == 5 && b.y == 0, "Default spawn position");
                check(b.array[8][2] == 0 && b.field[19][1] == 97, "Old active/fixed board");
                check(b.array[0][0] == 9 && b.array[20][5] == 9, "Boundary lost");
                check(b.th == fallThread && !fallThread.isInterrupted(), "Hold changed fall thread");
                check(b.holdSlot.getIcon() == ImageSource.block_I, "Hold preview");
                int next = b.preBlock;
                b.holdBlock();
                check(b.blockNum == 6 && b.preBlock == next, "Repeated hold allowed");
                b.fixBlock();
                check(b.holdManager.canHold(), "Lock did not unlock hold");
                active(b, 4, 3, 10);
                b.holdBlock();
                check(b.blockNum == 1 && b.holdManager.getHeldBlock() == 4, "Swap types");
                check(b.preBlock == next && b.block.length == 1 && b.block[0].length == 4,
                    "Swap consumed next or retained orientation");
                check(b.x == 4 && b.y == 0 && b.score.getText().equals("123"), "Swap position/score");
                int spawnY = b.y;
                java.lang.reflect.Method fall = GamePanel.class.getDeclaredMethod("fallStep");
                fall.setAccessible(true);
                fall.invoke(b);
                check(b.y == spawnY, "Stale fall thread moved the replacement");
                b.Gaming = false;
                b.holdManager.onBlockLocked();
                b.holdBlock();
                check(b.blockNum == 1 && b.holdManager.canHold(), "Countdown allowed hold");

                // Empty and occupied slots must both use the existing spawn-overlap rule.
                for (boolean occupied : new boolean[] { false, true })
                {
                    Board blocked = board();
                    if (occupied)
                    {
                        blocked.holdManager.hold(1);
                        blocked.holdManager.onBlockLocked();
                    }
                    active(blocked, 4, 2, 12);
                    blocked.preBlock = 1;
                    blocked.field[0][4] = 91;
                    blocked.holdBlock();
                    check(!blocked.gameRun && blocked.th.isInterrupted(), "Blocked spawn still running");
                    check(blocked.textLabel.getText().equals("GAME OVER"), "Existing game-over rule missing");
                }
                System.out.println("Hold integration checks passed.");
            }
            catch (Exception e) { throw new RuntimeException(e); }
        });
    }
}
