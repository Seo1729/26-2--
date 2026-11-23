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

        checkSuperJump();
    }

    // 고양이를 땅에 내려놓는다 (점프가 끝날 때까지 물리를 돌림)
    static void land(Dodger d) {
        while (d.jumpLeft > 0 || !d.isOnGround()) d.physicsStep();
    }

    // 점프 키를 누르고, ms 뒤(그 사이 물리 한 번) 한 번 더 누른다
    static void doubleTap(Dodger d, int ms) {
        d.jump(); d.physicsStep(); d.updateTimers(ms); d.jump();
    }

    // 3칸 점프: 점프 키 두 번, 최대 3회, 20초마다 1회 충전, 가득 차면 충전 시간 안 쌓임
    static void checkSuperJump() {
        Dodger d = new Dodger(new GameBoard(), GameEvents.NONE);
        d.reset();
        check(d.getSuperJumps() == Dodger.MAX_SUPER_JUMPS && d.getSuperJumpRechargeMs() == 0, "Starts full");
        d.updateTimers(60_000);
        check(d.getSuperJumps() == 3 && d.getSuperJumpRechargeMs() == 0, "Full charge kept time");

        // 한 번만 누르면 보통 2칸 점프, 횟수는 그대로
        d.jump(); land(d);
        check(d.getSuperJumps() == 3, "Single tap used a charge");
        d.jump(); int top = 0;
        while (d.jumpLeft > 0 || !d.isOnGround()) { d.physicsStep(); top = Math.max(top, d.y); }
        check(top == Dodger.JUMP_HEIGHT, "Normal jump height");

        // 빨리 두 번 누르면 출발점에서 3칸
        doubleTap(d, 100); top = 0;
        while (d.jumpLeft > 0 || !d.isOnGround()) { d.physicsStep(); top = Math.max(top, d.y); }
        check(top == Dodger.SUPER_JUMP_HEIGHT && d.getSuperJumps() == 2, "Double tap super jump height");

        // 공중에서 세 번째로 눌러도 더 안 올라가고 횟수도 안 씀
        doubleTap(d, 100); d.jump();
        check(d.getSuperJumps() == 1, "Third tap used another charge");
        land(d);

        // 늦게 누르면 3칸 점프 안 됨
        doubleTap(d, Dodger.DOUBLE_TAP_MS + 1);
        check(d.getSuperJumps() == 1, "Slow second tap counted");
        land(d);

        doubleTap(d, 100); land(d);
        check(d.getSuperJumps() == 0, "Used all charges");
        doubleTap(d, 100); top = 0;
        while (d.jumpLeft > 0 || !d.isOnGround()) { d.physicsStep(); top = Math.max(top, d.y); }
        check(top == Dodger.JUMP_HEIGHT, "Super jump with no charges");

        // 위에서 두 번 누르기 시간을 재느라 충전 시간도 조금 흘렀으니 남은 시간 기준으로 본다
        d.updateTimers(d.getSuperJumpRechargeMs() - 20);
        check(d.getSuperJumps() == 0 && d.getSuperJumpRechargeMs() == 20, "Recharged early");
        d.updateTimers(20);
        check(d.getSuperJumps() == 1, "Recharge after 20s");
        d.updateTimers(Dodger.SUPER_JUMP_RECHARGE_MS * 2);
        check(d.getSuperJumps() == 2, "Recharge one step at a time");
        d.updateTimers(Dodger.SUPER_JUMP_RECHARGE_MS);
        d.updateTimers(Dodger.SUPER_JUMP_RECHARGE_MS);
        check(d.getSuperJumps() == Dodger.MAX_SUPER_JUMPS, "Charges above max");

        d.jump(); d.crushed = true; d.jump();
        check(d.getSuperJumps() == Dodger.MAX_SUPER_JUMPS, "Crushed cat super jumped");
    }

    // 화면: 시작하면 돌고, 승부가 나면 멈추고, R 재시작은 게임 중·일시정지·승부 뒤 언제나
    // 실제 키 입력 경로(InputHandler)로 재시작 키를 눌렀다 뗀다
    static void pressR(GamePanel panel) {
        int code = common.KeySettings.getKey(common.KeyAction.DODGE_RESTART);
        for (java.awt.event.KeyListener l : panel.getKeyListeners()) {
            l.keyPressed(new java.awt.event.KeyEvent(panel, java.awt.event.KeyEvent.KEY_PRESSED, 0, 0, code, 'r'));
            l.keyReleased(new java.awt.event.KeyEvent(panel, java.awt.event.KeyEvent.KEY_RELEASED, 0, 0, code, 'r'));
        }
    }

    static void checkPanel() throws Exception {
        CharacterManager manager = new CharacterManager(Files.createTempDirectory("dodge-outcome-").resolve("appearance.properties"));
        SwingUtilities.invokeAndWait(() -> {
            GamePanel panel = new GamePanel(manager);
            try {
                panel.restart();
                check(!panel.isStarted(), "Restart before start");
                panel.start(); panel.timer.stop();
                check(panel.isStarted(), "Panel not started");
                panel.logic().tick(5000); panel.getDodger().x = 5;
                pressR(panel); panel.timer.stop();
                check(panel.isStarted() && panel.logic().getElapsedMs() == 0 && panel.getDodger().x == 0
                        && panel.getStatus().getTetrisWins() + panel.getStatus().getDodgerWins() == 0, "R mid-game");
                panel.togglePause(); pressR(panel);
                check(panel.isStarted() && !panel.isPaused() && panel.timer.isRunning(), "R while paused");
                panel.timer.stop();
                panel.getDodger().crush(); panel.refresh();
                check(!panel.isStarted() && panel.getStatus().getTetrisWins() == 1, "Panel did not stop on game over");
                pressR(panel); panel.timer.stop();
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
