import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.swing.SwingUtilities;

public class NextBlockCheck
{
    private static void check(boolean value, String message)
    {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception
    {
        Field next = Game3D.class.getDeclaredField("nextType"); next.setAccessible(true);
        Field active = Game3D.class.getDeclaredField("activeBlock"); active.setAccessible(true);
        Method tick = Game3D.class.getDeclaredMethod("fallOneStep"); tick.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> {
            try
            {
                for (Block3D.Type type : Block3D.Type.values())
                {
                    if (type == Block3D.Type.I) continue;
                    for (boolean hardDrop : new boolean[] {false, true})
                    {
                        Board3D board = new Board3D(); Game3D game = new Game3D(board, 60000);
                        try
                        {
                            Block3D.Type first = game.getNextBlockType();
                            check(first != null && first != Block3D.Type.I, "Invalid initial reservation");
                            game.start();
                            check(game.getActiveBlock().getType() == first, "Initial spawn ignored preview");
                            next.set(game, type);
                            game.getScene(); game.getGhostBlock(); game.move(-1, 0); game.rotate(Block3D.Axis.Z);
                            check(game.getNextBlockType() == type, "Movement/read changed reservation");
                            game.stop(); game.start();
                            check(game.getNextBlockType() == type, "Pause consumed next");
                            if (hardDrop) game.hardDrop();
                            else
                            {
                                int steps = 0;
                                while (game.getLockedBlockCount() == 0 && steps++ < 20) tick.invoke(game);
                            }
                            check(game.getActiveBlock().getType() == type, "Spawn differs from displayed next");
                            check(game.getNextBlockType() != null && game.getNextBlockType() != Block3D.Type.I,
                                    "Invalid replacement reservation");
                            next.set(game, type);
                            board.setFilled(1, 1, 13, true);
                            active.set(game, new Block3D(Block3D.Type.O, 1, 1, 0));
                            board.clear(); board.setFilled(1, 1, 13, true);
                            game.hardDrop();
                            check(game.getState() == Game3D.State.GAME_OVER && game.getNextBlockType() == type,
                                    "Failed spawn consumed reservation");
                            game.start();
                            check(game.getNextBlockType() == type, "Game over start changed next");
                            game.restart();
                            check(game.isRunning() && game.getNextBlockType() != null && game.getActiveBlock() != null,
                                    "Restart did not rebuild active/next");
                        }
                        finally { game.stop(); }
                    }
                }
            }
            catch (Exception e) { throw new RuntimeException(e); }
        });
        System.out.println("PASS: reserved next matches initial/automatic/hard-drop spawn, pause, failed spawn, restart");
    }
}
