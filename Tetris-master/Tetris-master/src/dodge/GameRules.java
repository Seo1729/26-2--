package dodge;

/**
 * 승패 판정과 전적. 제한시간, 목표 줄 수, 골 높이는 여기 한곳에만 둔다.
 */
public class GameRules {

	/** P1 승리: 이 줄 수만큼 지우면 승리 */
	public static final int TARGET_LINES = 10;
	/** P2 승리: 이 시간(ms) 동안 버티면 승리 */
	public static final int TIME_LIMIT_MS = 120_000;
	/**
	 * P2 승리: 캐릭터가 이 줄(바닥 0부터) 이상에 도달하면 승리.
	 * 맨 위 3줄은 새 블록이 나타나는 자리라 그 바로 아래를 골 라인으로 둔다.
	 */
	public static final int GOAL_ROW = GameBoard.HEIGHT - 4;

	public enum Result {
		NONE(null),
		CRUSHED("압사! 테트리스 승리"),
		LINES("목표 " + TARGET_LINES + "줄 달성! 테트리스 승리"),
		REACHED_TOP("꼭대기 도달! 똥피하기 승리"),
		TIME_OVER("시간 종료까지 생존! 똥피하기 승리"),
		STACK_FULL("블록이 꼭대기까지 쌓임! 똥피하기 승리");

		private final String message;

		Result(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

		public String getTitle() {
			return isTetrisWin() ? "테트리스 승리!" : "똥피하기 승리!";
		}

		public boolean isTetrisWin() {
			return this == CRUSHED || this == LINES;
		}

		public boolean isDecided() {
			return this != NONE;
		}
	}

	private int tetrisWins = 0;
	private int dodgerWins = 0;

	/** 현재 상태로 승패를 판정한다. 아직 안 끝났으면 NONE. 위에 있는 조건이 먼저다. */
	public static Result judge(TetrisPlayer tetris, Dodger dodger, int elapsedMs) {
		if (dodger.isCrushed())
			return Result.CRUSHED;
		if (tetris.getLinesRemoved() >= TARGET_LINES)
			return Result.LINES;
		if (dodger.getY() >= GOAL_ROW)
			return Result.REACHED_TOP;
		if (tetris.isGameOver())
			return Result.STACK_FULL;
		if (elapsedMs >= TIME_LIMIT_MS)
			return Result.TIME_OVER;
		return Result.NONE;
	}

	/** 끝난 판 결과를 전적에 더한다 */
	public void record(Result result) {
		if (!result.isDecided())
			return;
		if (result.isTetrisWin())
			++tetrisWins;
		else
			++dodgerWins;
	}

	public int getTetrisWins() {
		return tetrisWins;
	}

	public int getDodgerWins() {
		return dodgerWins;
	}

	public static int remainingMs(int elapsedMs) {
		return Math.max(0, TIME_LIMIT_MS - elapsedMs);
	}

	public static int remainingSeconds(int elapsedMs) {
		return (remainingMs(elapsedMs) + 999) / 1000;
	}
}
