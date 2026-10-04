import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.awt.event.ActionEvent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class GameOverCheck
{
    private static void check(boolean condition, String message)
    {
        if (!condition) throw new AssertionError(message);
    }

    private static void empty(Board3D board)
    {
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                for (int z = 0; z < 14; z++)
                    check(board.isEmpty(x, y, z) && board.getType(x, y, z) == null, "Restart retained board data");
    }

    public static void main(String[] args) throws Exception
    {
        Field timerField = Game3D.class.getDeclaredField("fallTimer");
        timerField.setAccessible(true);
        Method tick = Game3D.class.getDeclaredMethod("fallOneStep");
        tick.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try
            {
                Board3D board = new Board3D();
                board.lockBlock(new Block3D(Block3D.Type.O, 1, 1, 13));
                Game3D game = new Game3D(board, 60000);
                Timer timer = (Timer) timerField.get(game);
                try
                {
                    check(game.getState() == Game3D.State.READY, "Initial state");
                    game.start();
                    check(game.getState() == Game3D.State.GAME_OVER, "Initial overlap did not end game");
                    check(!timer.isRunning() && game.getActiveBlock() == null, "Game-over timer/block retained");
                    check(!game.move(1, 0) && !game.rotate(Block3D.Axis.Z) && !game.hardDrop(), "Game-over input accepted");
                    tick.invoke(game);
                    check(board.getType(1, 1, 13) == Block3D.Type.O && game.getLockedBlockCount() == 0,
                            "Game over changed fixed data");
                    Game3DPanel panel = new Game3DPanel(game);
                    for (String key : new String[] { "W", "A", "S", "D", "J", "K", "L", "Space" })
                        panel.getActionMap().get("press" + key).actionPerformed(new ActionEvent(panel, 0, key));
                    check(game.getState() == Game3D.State.GAME_OVER && game.getLockedBlockCount() == 0,
                            "Key actions changed game-over state");
                    game.stop();
                    game.start();
                    check(game.getState() == Game3D.State.GAME_OVER, "Start/stop erased game over");
                    board.clear();
                    game.start();
                    check(game.getState() == Game3D.State.GAME_OVER, "Must explicitly restart");
                    board.lockBlock(new Block3D(Block3D.Type.O, 1, 1, 4));
                    check("pressR".equals(panel.getInputMap(javax.swing.JPanel.WHEN_IN_FOCUSED_WINDOW)
                            .get(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, 0, false))),
                            "R key missing");
                    panel.getActionMap().get("pressR").actionPerformed(new ActionEvent(panel, 0, "R"));
                    empty(board);
                    check(game.getState() == Game3D.State.RUNNING && timer.isRunning()
                            && game.getLockedBlockCount() == 0 && board.canPlace(game.getActiveBlock()), "Restart failed");
                    int z = game.getActiveBlock().getZ();
                    tick.invoke(game);
                    panel.getActionMap().get("pressR").actionPerformed(new ActionEvent(panel, 0, "R"));
                    check(game.getActiveBlock().getZ() == z - 1, "R restarted a running game");
                    z--;
                    game.stop();
                    check(game.getState() == Game3D.State.PAUSED && !timer.isRunning(), "Pause failed");
                    tick.invoke(game);
                    check(game.getActiveBlock().getZ() == z, "Paused timer changed block");
                    game.start();
                    tick.invoke(game);
                    check(game.getState() == Game3D.State.RUNNING && game.getActiveBlock().getZ() == z - 1,
                            "Resume failed");
                    game.hardDrop();
                    check(game.getLockedBlockCount() == 1, "Restarted game cannot lock");
                    game.restart();
                    empty(board);
                    check(game.getLockedBlockCount() == 0, "Restart did not reset counter");
                }
                finally { game.stop(); }

                for (boolean hardDrop : new boolean[] { false, true })
                {
                    Board3D blocked = new Board3D();
                    Game3D ending = new Game3D(blocked, 60000);
                    try
                    {
                        ending.start();
                        blocked.setFilled(1, 1, 12, true);
                        if (hardDrop) ending.hardDrop(); else tick.invoke(ending);
                        check(ending.getState() == Game3D.State.GAME_OVER && ending.getLockedBlockCount() == 1,
                                "Next spawn overlap did not end game");
                        check(!((Timer) timerField.get(ending)).isRunning(), "Timer not stopped");
                        tick.invoke(ending);
                        check(ending.getLockedBlockCount() == 1, "Queued tick locked again");
                        ending.restart();
                        empty(blocked);
                        check(ending.isRunning(), "Restart after next-spawn failure failed");
                    }
                    finally { ending.stop(); }
                }

                // 충돌 이외의 배치 실패도 같은 GAME_OVER 경로를 사용한다.
                Board3D rejectingBoard = new Board3D() {
                    @Override public boolean canPlace(Block3D block) { return false; }
                };
                Game3D invalid = new Game3D(rejectingBoard, 60000);
                invalid.start();
                check(invalid.getState() == Game3D.State.GAME_OVER
                        && !((Timer) timerField.get(invalid)).isRunning(), "Invalid placement accepted");
                invalid.restart();
                check(invalid.getState() == Game3D.State.GAME_OVER, "Restart must revalidate spawn");
            }
            catch (ReflectiveOperationException exception) { throw new RuntimeException(exception); }
        });
        System.out.println("PASS: states, initial/next spawn failure, timer/input blocking, game-over persistence, pause/resume, restart/reset");
    }
}
