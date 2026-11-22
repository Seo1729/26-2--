package dodge;

import java.nio.file.Files;
import javax.swing.SwingUtilities;

/** 똥피하기 로직(PoopGame)은 Swing 없이, 화면(GamePanel)은 시작·재시작 흐름만 확인한다. */
public final class DodgeOutcomeCheck {
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }

    public static void main(String[] args) throws Exception {
        checkLogic();
        checkPanel();
        System.out.println("Dodge boundaries, collision, line count, victory totals and game-over paths passed.");
    }

    // 로직만: 화면, 타이머, 소리 없이 돌아가야 한다
    static void checkLogic() {
        PoopGame game = new PoopGame(GameEvents.NONE);
        GameBoard board = game.getBoard();
        TetrisPlayer tetris = game.getTetris();
        Dodger dodger = game.getDodger();

        game.start();
        dodger.x = 9; dodger.moveRight();
        check(dodger.x == 9, "Right boundary");
        dodger.x = 8; board.set(7, 0, 1); dodger.moveLeft();
        check(dodger.x == 8, "Left fixed-block collision");

        dodger.x = 9;
        for (int x = 0; x < GameBoard.WIDTH; x++) if (x != 4 && x != 5) board.set(x, 0, 1);
        tetris.spawn(Shape.SQUARE, 4, 1);
        tetris.oneLineDown();
        check(tetris.getLinesRemoved() == 1, "Dodge cleared-line count");

        for (int i = 1; i < GameRules.TARGET_LINES; i++) {
            board.clear();
            for (int x = 0; x < GameBoard.WIDTH; x++) if (x != 4 && x != 5) board.set(x, 0, 1);
            tetris.spawn(Shape.SQUARE, 4, 1);
            tetris.oneLineDown();
        }
        check(game.checkResult() == GameRules.Result.LINES && game.getTetrisWins() == 1, "Line-goal game over");
        game.checkResult(); check(game.getTetrisWins() == 1, "Win counted twice");
        int before = game.getElapsedMs(); game.tick(GamePanel.FRAME_MS);
        check(game.getElapsedMs() == before, "Finished game kept ticking");

        game.start(); game.tick(GameRules.TIME_LIMIT_MS);
        check(game.checkResult() == GameRules.Result.TIME_OVER && game.getDodgerWins() == 1, "Survival game over");

        game.start(); dodger.y = GameRules.GOAL_ROW;
        check(game.checkResult() == GameRules.Result.REACHED_TOP && game.getDodgerWins() == 2, "Goal game over");

        game.start();
        for (int x = 0; x < GameBoard.WIDTH; x++) board.set(x, GameBoard.HEIGHT - 1, 1);
        tetris.start();
        check(tetris.isGameOver(), "Blocked spawn not game over");
        check(game.checkResult() == GameRules.Result.STACK_FULL && game.getDodgerWins() == 3, "Stack-full game over");

        // 조건이 겹치면 압사가 먼저
        game.start(); dodger.y = GameRules.GOAL_ROW; dodger.crushed = true;
        check(game.checkResult() == GameRules.Result.CRUSHED, "Crush has priority");
    }

    // 화면: 시작하면 돌고, 승부가 나면 멈추고, 재시작은 승부가 난 뒤에만
    static void checkPanel() throws Exception {
        CharacterManager manager = new CharacterManager(Files.createTempDirectory("dodge-outcome-").resolve("appearance.properties"));
        SwingUtilities.invokeAndWait(() -> {
            GamePanel panel = new GamePanel(manager);
            try {
                panel.start(); panel.timer.stop();
                check(panel.isStarted(), "Panel not started");
                panel.restart();
                check(panel.isStarted() && !panel.logic().isOver(), "Restart before game over");
                panel.getDodger().crush(); panel.refresh();
                check(!panel.isStarted() && panel.getStatus().getTetrisWins() == 1, "Panel did not stop on game over");
                panel.restart(); panel.timer.stop();
                check(panel.isStarted() && !panel.getDodger().isCrushed(), "Restart after game over");
                panel.togglePause();
                check(panel.isPaused() && !panel.timer.isRunning(), "Pause did not stop loop");
                panel.togglePause();
                check(!panel.isPaused() && panel.timer.isRunning(), "Resume did not restart loop");
            } finally { panel.stop(); }
            check(!panel.timer.isRunning(), "Stop left timer running");
        });
    }
}
