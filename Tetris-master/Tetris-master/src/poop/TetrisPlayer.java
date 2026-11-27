package poop;

/**
 * P1 테트리스 플레이어의 상태와 동작. 현재 조각, 이동, 회전, 낙하, 줄 수.
 * 그리기와 입력은 GamePanel / InputHandler가 맡고, 소리는 GameEvents로 알린다.
 */
public class TetrisPlayer {

	/** 새 조각이 나타나는 가로 위치 */
	public static final int SPAWN_X = GameBoard.WIDTH / 2 + 1;

	private final GameBoard board;
	private final Dodger dodger;
	private final GameEvents events;

	private Shape curPiece = new Shape();
	/** 다음에 나올 조각 (정보판 미리보기용) */
	private final Shape nextPiece = new Shape();
	private int curX = 0;
	private int curY = 0;
	/** 줄을 지운 직후 한 박자 쉬었다가 새 조각을 꺼내기 위한 표시 */
	private boolean isFallingFinished = false;
	private boolean isGameOver = false;
	private int numLinesRemoved = 0;

	public TetrisPlayer(GameBoard board, Dodger dodger, GameEvents events) {
		this.board = board;
		this.dodger = dodger;
		this.events = events;
	}

	public void start() {
		isFallingFinished = false;
		isGameOver = false;
		numLinesRemoved = 0;
		nextPiece.setRandomShape();
		newPiece();
	}

	/** 낙하 박자마다 호출된다 */
	public void tick() {
		if (isGameOver)
			return;
		if (isFallingFinished) {
			isFallingFinished = false;
			newPiece();
		} else {
			oneLineDown();
		}
	}

	public void moveLeft() {
		if (tryMove(curPiece, curX - 1, curY))
			events.onPieceMoved();
	}

	public void moveRight() {
		if (tryMove(curPiece, curX + 1, curY))
			events.onPieceMoved();
	}

	public void rotate() {
		if (tryMove(curPiece.rotateLeft(), curX, curY))
			events.onPieceRotated();
	}

	/** 한 칸 내리기. 즉시 낙하(하드드롭)는 설계상 없음. */
	public void oneLineDown() {
		if (isGameOver || !hasPiece())
			return;
		// 아래로 내려가는 자리에 P2가 있으면 압사
		if (board.canPlace(curPiece, curX, curY - 1) && covers(curPiece, curX, curY - 1)) {
			dodger.crush();
			return;
		}
		if (!tryMove(curPiece, curX, curY - 1))
			pieceDropped();
	}

	private void pieceDropped() {
		board.place(curPiece, curX, curY);
		events.onPieceLocked();

		int lines = board.removeFullLines();
		if (lines > 0) {
			events.onLinesCleared(lines);
			numLinesRemoved += lines;
			isFallingFinished = true;
			curPiece.setShape(Shape.NONE);
		} else {
			newPiece();
		}
	}

	private void newPiece() {
		spawn(nextPiece.getShape(), SPAWN_X, GameBoard.HEIGHT - 1);
		nextPiece.setRandomShape();
	}

	/**
	 * shape 조각을 x 위치, 맨 윗칸이 topY인 자리에 꺼낸다. 놓을 자리가 없으면 블록이 꽉 찬 것.
	 * 테스트에서 원하는 조각을 원하는 자리에 두는 데도 쓴다.
	 */
	void spawn(int shape, int x, int topY) {
		curPiece = new Shape();
		curPiece.setShape(shape);
		curX = x;
		curY = topY + curPiece.minY();

		if (!board.canPlace(curPiece, curX, curY)) {
			curPiece.setShape(Shape.NONE);
			isGameOver = true;
		}
	}

	private boolean tryMove(Shape newPiece, int newX, int newY) {
		if (isGameOver || !hasPiece())
			return false;
		// 좌우 이동·회전으로 P2를 덮는 건 막는다 (밀어서 압사 없음)
		if (!board.canPlace(newPiece, newX, newY) || covers(newPiece, newX, newY))
			return false;
		curPiece = newPiece;
		curX = newX;
		curY = newY;
		return true;
	}

	private boolean covers(Shape piece, int posX, int posY) {
		for (int i = 0; i < Shape.CELLS; ++i) {
			if (dodger.isAt(posX + piece.x(i), posY - piece.y(i)))
				return true;
		}
		return false;
	}

	/** 현재 떨어지는 조각이 (x, y) 칸을 차지하고 있는지 */
	public boolean occupies(int x, int y) {
		if (!hasPiece())
			return false;
		for (int i = 0; i < Shape.CELLS; ++i) {
			if (curX + curPiece.x(i) == x && curY - curPiece.y(i) == y)
				return true;
		}
		return false;
	}

	public boolean hasPiece() {
		return !curPiece.isEmpty();
	}

	public Shape getPiece() {
		return curPiece;
	}

	public Shape getNextPiece() {
		return nextPiece;
	}

	public int getX() {
		return curX;
	}

	public int getY() {
		return curY;
	}

	public boolean isGameOver() {
		return isGameOver;
	}

	public int getLinesRemoved() {
		return numLinesRemoved;
	}
}
