package origin;

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
        Board() { super(GameMode.NORMAL, 1); } // 컴파일용 (테스트는 생성자를 부르지 않고 allocateInstance로 만든다)
        @Override public void drawTetris() { }
        @Override public void checkArray() { }
    }

    static GameMode mode = GameMode.NORMAL; // 테스트가 만드는 판의 모드

    private static void check(boolean value, String message)
    {
        if (!value) throw new AssertionError(message);
    }

    private static Board board() throws Exception
    {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        Unsafe unsafe = (Unsafe) field.get(null);
        Board b = (Board) unsafe.allocateInstance(Board.class);
        b.hp = (HpSystem) unsafe.allocateInstance(HpSystem.class); // 생성자 없이 만든 판에도 HP 객체가 있어야 게임 오버 판정이 돈다
        b.mode = mode;
        b.board = new TetrisBoard(); // 보드 데이터·규칙은 TetrisBoard로 옮겨 갔다
        b.scoreKeeper = new ScoreKeeper(0); // 점수 값은 ScoreKeeper가 들고 있다
        b.holdManager = new HoldManager();
        b.holdSlot = new JLabel();
        b.nextBlock = new JLabel();
        b.score = new JLabel("123");
        b.textLabel = new JLabel();
        b.blackPanel = new JPanel();
        b.tetrisArea = new JPanel();
        b.th = new Thread();
        b.timeTh = new TimeThread(b);
        b.gameRun = true;
        b.gaming = true;
        b.setArray();
        return b;
    }

    private static void active(Board b, int type, int x, int y)
    {
        for (int r = 0; r < 20; r++) Arrays.fill(b.board.array[r], 1, 11, 0);
        b.board.block = BlockType.fromNumber(type).newShape();
        b.board.blockNum = type;
        b.board.blockHSize = b.board.block.length;
        b.board.blockWSize = b.board.block[0].length;
        b.board.x = x;
        b.board.y = y;
        for (int r = 0; r < b.board.blockHSize; r++)
            for (int c = 0; c < b.board.blockWSize; c++)
                b.board.array[y + r][x + c] = b.board.block[r][c];
    }

    public static void main(String[] args) throws Exception
    {
        ImageSource.preload();
        SwingUtilities.invokeAndWait(() -> {
            try
            {
                mode = GameMode.GALAGA; // 갤러그 모드 게임 오버 규칙을 확인한다 (모드는 이제 판마다 가진다)
                Board b = board();
                active(b, 1, 2, 8);
                b.moveTurn();
                check(b.board.blockHSize == 4, "Rotation setup failed");
                b.preBlock = 6;
                b.board.field[19][1] = 97;
                Thread fallThread = b.th;
                b.holdBlock();
                check(b.holdManager.getHeldBlock() == 1 && b.board.blockNum == 6, "Empty hold/next consumption");
                check(b.board.x == 5 && b.board.y == 0, "Default spawn position");
                check(b.board.array[8][2] == 0 && b.board.field[19][1] == 97, "Old active/fixed board");
                check(b.board.array[0][0] == 9 && b.board.array[20][5] == 9, "Boundary lost");
                check(b.th == fallThread && !fallThread.isInterrupted(), "Hold changed fall thread");
                check(b.holdSlot.getIcon() == ImageSource.BLOCK_I, "Hold preview");
                int next = b.preBlock;
                b.holdBlock();
                check(b.board.blockNum == 6 && b.preBlock == next, "Repeated hold allowed");
                b.fixBlock();
                check(b.holdManager.canHold(), "Lock did not unlock hold");
                active(b, 4, 3, 10);
                b.holdBlock();
                check(b.board.blockNum == 1 && b.holdManager.getHeldBlock() == 4, "Swap types");
                check(b.preBlock == next && b.board.block.length == 1 && b.board.block[0].length == 4,
                    "Swap consumed next or retained orientation");
                check(b.board.x == 4 && b.board.y == 0 && b.score.getText().equals("123"), "Swap position/score");
                int spawnY = b.board.y;
                java.lang.reflect.Method fall = GamePanel.class.getDeclaredMethod("fallStep");
                fall.setAccessible(true);
                fall.invoke(b);
                check(b.board.y == spawnY, "Stale fall thread moved the replacement");
                b.gaming = false;
                b.holdManager.onBlockLocked();
                b.holdBlock();
                check(b.board.blockNum == 1 && b.holdManager.canHold(), "Countdown allowed hold");

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
                    blocked.board.field[0][4] = 91;
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
