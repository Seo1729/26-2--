package kr.ac.jbnu.se.tetris.dodge;

import kr.ac.jbnu.se.tetris.Shape;
import kr.ac.jbnu.se.tetris.Tetrominoes;

/**
 * P1 테트리스 플레이어의 상태와 동작. 현재 조각, 이동, 회전, 낙하, 줄 수.
 * 그리기와 입력은 GamePanel / InputHandler가 맡는다.
 */
public class TetrisPlayer {

	private final GameBoard board;
	private Shape curPiece = new Shape();
	/** 다음에 나올 조각 (정보판 미리보기용) */
	private final Shape nextPiece = new Shape();
	private int curX = 0;
	private int curY = 0;
	private boolean isFallingFinished = false;
	private boolean isGameOver = false;
	private int numLinesRemoved = 0;
	private Dodger dodger;

	public TetrisPlayer(GameBoard board) {
		this.board = board;
	}

	public void setDodger(Dodger dodger) {
		this.dodger = dodger;
	}

	public void start() {
		isFallingFinished = false;
		isGameOver = false;
		numLinesRemoved = 0;
		nextPiece.setRandomShape();
		newPiece();
	}

	/** 타이머 한 번마다 호출된다 */
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
		tryMove(curPiece, curX - 1, curY);
	}

	public void moveRight() {
		tryMove(curPiece, curX + 1, curY);
	}

	public void rotateLeft() {
		tryMove(curPiece.rotateLeft(), curX, curY);
	}

	public void rotateRight() {
		tryMove(curPiece.rotateRight(), curX, curY);
	}

	/** 한 칸 내리기. 즉시 낙하(하드드롭)는 설계상 없음. */
	public void oneLineDown() {
		if (isGameOver || !hasPiece())
			return;
		// 아래로 내려가는 자리에 P2가 있으면 압사
		if (board.canPlace(curPiece, curX, curY - 1) && covers(curPiece, curX, curY - 1, dodger)) {
			dodger.crush();
			return;
		}
		if (!tryMove(curPiece, curX, curY - 1))
			pieceDropped();
	}

	private void pieceDropped() {
		board.place(curPiece, curX, curY);

		int lines = board.removeFullLines();
		if (lines > 0) {
			numLinesRemoved += lines;
			isFallingFinished = true;
			curPiece.setShape(Tetrominoes.NoShape);
		} else {
			newPiece();
		}
	}

	private void newPiece() {
		curPiece.setShape(nextPiece.getShape());
		nextPiece.setRandomShape();
		curX = GameBoard.WIDTH / 2 + 1;
		curY = GameBoard.HEIGHT - 1 + curPiece.minY();

		if (!board.canPlace(curPiece, curX, curY)) {
			curPiece.setShape(Tetrominoes.NoShape);
			isGameOver = true;
		}
	}

	private boolean tryMove(Shape newPiece, int newX, int newY) {
		if (isGameOver || !hasPiece())
			return false;
		// 좌우 이동·회전으로 P2를 덮는 건 막는다 (밀어서 압사 없음)
		if (!board.canPlace(newPiece, newX, newY) || covers(newPiece, newX, newY, dodger))
			return false;
		curPiece = newPiece;
		curX = newX;
		curY = newY;
		return true;
	}

	private static boolean covers(Shape piece, int posX, int posY, Dodger d) {
		if (d == null)
			return false;
		for (int i = 0; i < 4; ++i) {
			if (posX + piece.x(i) == d.getX() && posY - piece.y(i) == d.getY())
				return true;
		}
		return false;
	}

	/** 현재 떨어지는 조각이 (x, y) 칸을 차지하고 있는지 */
	public boolean occupies(int x, int y) {
		if (!hasPiece())
			return false;
		for (int i = 0; i < 4; ++i) {
			if (curX + curPiece.x(i) == x && curY - curPiece.y(i) == y)
				return true;
		}
		return false;
	}

	public boolean hasPiece() {
		return curPiece.getShape() != Tetrominoes.NoShape;
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
