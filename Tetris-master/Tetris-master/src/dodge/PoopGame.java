package dodge;

/**
 * 똥피하기 한 판의 로직 (Swing 없음). 보드, P1 테트리스, P2 고양이, 승패를 묶고 시간을 흘린다.
 * 화면(GamePanel)은 타이머마다 tick()을 부르고 결과를 그리기만 한다.
 */
public class PoopGame implements GameStatus {

	/** P1 블록이 저절로 한 칸 내려오는 간격(ms) */
	public static final int FALL_INTERVAL_MS = 400;
	/** P2 고양이 점프·중력이 한 칸 움직이는 간격(ms) */
	public static final int DODGER_INTERVAL_MS = 80;

	private final GameEvents events;
	private final GameBoard board = new GameBoard();
	private final Dodger dodger;
	private final TetrisPlayer tetris;
	private final GameRules rules = new GameRules();

	private int playElapsed = 0;
	private int fallElapsed = 0;
	private int dodgerElapsed = 0;
	private GameRules.Result result = GameRules.Result.NONE;

	public PoopGame(GameEvents events) {
		this.events = events;
		// 고양이에게는 "쌓인 블록 + 떨어지는 블록"이 막힌 칸이다
		dodger = new Dodger(this::isSolidForDodger, events);
		tetris = new TetrisPlayer(board, dodger, events);
	}

	private boolean isSolidForDodger(int x, int y) {
		return board.isBlocked(x, y) || tetris.occupies(x, y);
	}

	/** 새 판 시작. 전적은 그대로 둔다. */
	public void start() {
		playElapsed = 0;
		fallElapsed = 0;
		dodgerElapsed = 0;
		result = GameRules.Result.NONE;
		board.clear();
		dodger.reset();
		tetris.start();
	}

	/** 시간을 elapsedMs만큼 흘린다. 승부가 난 뒤에는 아무것도 하지 않는다. */
	public void tick(int elapsedMs) {
		if (isOver())
			return;

		playElapsed += elapsedMs;
		fallElapsed += elapsedMs;
		if (fallElapsed >= FALL_INTERVAL_MS) {
			fallElapsed = 0;
			tetris.tick();
			dodger.resolveOverlap(board);
		}

		dodgerElapsed += elapsedMs;
		if (dodgerElapsed >= DODGER_INTERVAL_MS) {
			dodgerElapsed = 0;
			dodger.physicsStep();
		}
		dodger.updateDraw(elapsedMs);
	}

	/**
	 * 승패를 확인한다. 이번에 처음 승부가 났으면 전적에 더하고 이벤트를 알린다.
	 * 같은 판에서 여러 번 불러도 전적은 한 번만 오른다.
	 */
	public GameRules.Result checkResult() {
		if (isOver())
			return result;
		result = GameRules.judge(tetris, dodger, playElapsed);
		if (isOver()) {
			rules.record(result);
			events.onGameEnd(result);
		}
		return result;
	}

	public boolean isOver() {
		return result.isDecided();
	}

	public GameRules.Result getResult() {
		return result;
	}

	public GameBoard getBoard() {
		return board;
	}

	public TetrisPlayer getTetris() {
		return tetris;
	}

	public Dodger getDodger() {
		return dodger;
	}

	// ---------------- GameStatus (정보판용) ----------------

	@Override
	public int getElapsedMs() {
		return playElapsed;
	}

	@Override
	public Shape getNextPiece() {
		return tetris.getNextPiece();
	}

	@Override
	public int getLinesRemoved() {
		return tetris.getLinesRemoved();
	}

	@Override
	public int getDodgerHeight() {
		return dodger.getY();
	}

	@Override
	public int getTetrisWins() {
		return rules.getTetrisWins();
	}

	@Override
	public int getDodgerWins() {
		return rules.getDodgerWins();
	}
}
