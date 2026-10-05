package dodge;

/**
 * P2 똥피하기 플레이어 캐릭터. 보드 1칸 크기로 칸 단위로 움직인다.
 * 쌓인 블록과 떨어지는 조각은 벽/발판으로 취급한다.
 */
public class Dodger {

	/** 점프로 올라갈 수 있는 최대 칸 수 */
	public static final int JUMP_HEIGHT = 2;

	private final GameBoard board;
	private final TetrisPlayer tetris;
	private int x;
	private int y;
	private int jumpLeft;
	private boolean crushed;

	public Dodger(GameBoard board, TetrisPlayer tetris) {
		this.board = board;
		this.tetris = tetris;
	}

	public void reset() {
		x = 0;
		y = 0;
		jumpLeft = 0;
		crushed = false;
	}

	private boolean isSolid(int cx, int cy) {
		return !board.isFree(cx, cy) || tetris.occupies(cx, cy);
	}

	public boolean isOnGround() {
		return isSolid(x, y - 1);
	}

	public void moveLeft() {
		if (!crushed && !isSolid(x - 1, y))
			--x;
	}

	public void moveRight() {
		if (!crushed && !isSolid(x + 1, y))
			++x;
	}

	public void jump() {
		if (!crushed && isOnGround())
			jumpLeft = JUMP_HEIGHT;
	}

	/** 일정 간격마다 호출. 점프 중이면 1칸 올라가고, 아니면 중력으로 1칸 떨어진다. */
	public void physicsStep() {
		if (crushed)
			return;

		if (jumpLeft > 0) {
			if (!isSolid(x, y + 1)) {
				++y;
				--jumpLeft;
			} else {
				jumpLeft = 0;
			}
		} else if (!isOnGround()) {
			--y;
		}
	}

	/** 줄 삭제 등으로 캐릭터 칸이 블록과 겹치면 위쪽 빈칸으로 밀어 올린다 */
	public void resolveOverlap() {
		while (y < GameBoard.HEIGHT - 1 && !board.isFree(x, y))
			++y;
	}

	public void crush() {
		crushed = true;
	}

	public boolean isCrushed() {
		return crushed;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
