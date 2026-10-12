package dodge;

import java.nio.file.Files;
import javax.swing.SwingUtilities;

public final class DodgeOutcomeCheck {
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        CharacterManager manager = new CharacterManager(Files.createTempDirectory("dodge-outcome-").resolve("appearance.properties"));
        SwingUtilities.invokeAndWait(() -> {
            GamePanel game = new GamePanel(manager);
            try {
                game.start(); game.timer.stop();
                game.dodger.x = 9; game.dodger.moveRight();
                check(game.dodger.x == 9, "Right boundary");
                game.dodger.x = 8; game.cells[7] = 1; game.dodger.moveLeft();
                check(game.dodger.x == 8, "Left fixed-block collision");
                game.dodger.x = 9;
                game.curPiece.setShape(5); game.curX = 4; game.curY = 1;
                java.util.Arrays.fill(game.cells, 0, 10, 1); game.cells[4] = game.cells[5] = 0;
                game.oneLineDown();
                check(game.numLinesRemoved == 1, "Dodge cleared-line count");
                game.numLinesRemoved = 10; game.refresh();
                check(game.result == 2 && game.tetrisWins == 1 && !game.isStarted, "Line-goal game over");
                game.refresh(); check(game.tetrisWins == 1, "Win counted twice");
                game.restart(); game.timer.stop(); game.playElapsed = 120000; game.refresh();
                check(game.result == 4 && game.dodgerWins == 1, "Survival game over");
                game.restart(); game.timer.stop(); game.dodger.y = 18; game.refresh();
                check(game.result == 3 && game.dodgerWins == 2, "Goal game over");
                game.restart(); game.timer.stop(); game.isGameOver = true; game.refresh();
                check(game.result == 5 && game.dodgerWins == 3, "Stack-full game over");
            } finally { game.stop(); }
        });
        System.out.println("Dodge boundaries, collision, line count, victory totals and game-over paths passed.");
    }
}
