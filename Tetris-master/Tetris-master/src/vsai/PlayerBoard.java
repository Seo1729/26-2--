package vsai;

/**
 * 한쪽(플레이어 또는 AI)의 게임 상태: 판, 떨어지는 블록, 다음 블록, 점수, 줄 수, 레벨.
 * 조작 방법은 사람과 AI가 같다 (좌우 이동, 회전, 소프트 드롭, 하드 드롭).
 */
public class PlayerBoard {

	/** 한 번에 지운 줄 수(0~4)에 따른 기본 점수. 레벨을 곱한다. */
	private static final int[] LINE_SCORES = { 0, 100, 300, 500, 800 };
	/** 회전이 벽이나 블록에 막히면 이만큼 옆으로 밀어서 다시 시도한다 */
	private static final int[] KICKS = { 0, -1, 1, -2, 2 };
	private static final int SPAWN_X = 3;

	private final String name;
	private final Board board = new Board();
	private PieceSequence sequence;
	private int pieceIndex;
	private Tetromino current;
	private Tetromino next;
	private int rotation;
	private int x;
	private int y;
	/** 지금까지 나온 블록 수. AI가 새 블록이 나왔는지 알아챌 때 쓴다. */
	private int pieceId;
	private int score;
	private int lines;
	private boolean gameOver;
	private int fallElapsed;

	public PlayerBoard(String name) {
		this.name = name;
	}

	public void reset(PieceSequence sequence) {
		this.sequence = sequence;
		board.clear();
		pieceIndex = 0;
		pieceId = 0;
		score = 0;
		lines = 0;
		gameOver = false;
		fallElapsed = 0;
		spawn();
	}

	private void spawn() {
		current = sequence.get(pieceIndex);
		next = sequence.get(pieceIndex + 1);
		++pieceIndex;
		++pieceId;
		rotation = 0;
		x = SPAWN_X;
		y = 0;
		if (!board.canPlace(current, rotation, x, y))
			gameOver = true;
	}

	/** 게임 루프가 매 프레임 호출. 레벨에 맞는 간격마다 한 칸 떨어진다. */
	public void tick(int elapsedMs) {
		if (gameOver)
			return;
		fallElapsed += elapsedMs;
		if (fallElapsed >= fallInterval()) {
			fallElapsed = 0;
			if (!tryMove(rotation, x, y + 1))
				lock();
		}
	}

	public boolean moveLeft() {
		return !gameOver && tryMove(rotation, x - 1, y);
	}

	public boolean moveRight() {
		return !gameOver && tryMove(rotation, x + 1, y);
	}

	/** 시계 방향 회전. 막히면 옆으로 조금 밀어서 다시 시도한다. */
	public boolean rotate() {
		if (gameOver)
			return false;
		int nr = (rotation + 1) & 3;
		for (int k : KICKS) {
			if (tryMove(nr, x + k, y))
				return true;
		}
		return false;
	}

	/** 한 칸 내리기. 더 못 내려가면 고정한다. */
	public void softDrop() {
		if (gameOver)
			return;
		fallElapsed = 0;
		if (tryMove(rotation, x, y + 1))
			score += 1;
		else
			lock();
	}

	/** 바로 바닥까지 떨어뜨려 고정한다 */
	public void hardDrop() {
		if (gameOver)
			return;
		int ny = board.dropY(current, rotation, x, y);
		score += 2 * (ny - y);
		y = ny;
		lock();
	}

	private boolean tryMove(int nr, int nx, int ny) {
		if (!board.canPlace(current, nr, nx, ny))
			return false;
		rotation = nr;
		x = nx;
		y = ny;
		return true;
	}

	private void lock() {
		if (!board.place(current, rotation, x, y)) {
			gameOver = true;
			return;
		}
		int cleared = board.clearLines();
		lines += cleared;
		score += LINE_SCORES[cleared] * level();
		fallElapsed = 0;
		spawn();
	}

	public int level() {
		return lines / 10 + 1;
	}

	/** 레벨이 오를수록 빨리 떨어진다 (ms) */
	private int fallInterval() {
		return Math.max(100, 800 - (level() - 1) * 70);
	}

	/** 지금 블록을 떨어뜨리면 멈출 y (그림자 표시용) */
	public int ghostY() {
		return board.dropY(current, rotation, x, y);
	}

	public String getName() {
		return name;
	}

	public Board getBoard() {
		return board;
	}

	public Tetromino getCurrent() {
		return current;
	}

	public Tetromino getNext() {
		return next;
	}

	public int getRotation() {
		return rotation;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getPieceId() {
		return pieceId;
	}

	public int getScore() {
		return score;
	}

	public int getLines() {
		return lines;
	}

	public boolean isGameOver() {
		return gameOver;
	}
}
