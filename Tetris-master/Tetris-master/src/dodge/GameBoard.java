package dodge;

/**
 * 보드 격자만 관리한다. 쌓인 블록 저장, 칸 비었는지 확인, 줄 삭제.
 * 좌표: x는 왼쪽 0부터, y는 바닥 0부터 위로 증가. 칸 값은 Shape 모양 번호(0은 빈칸).
 */
public class GameBoard implements CollisionMap {

	public static final int WIDTH = 10;
	public static final int HEIGHT = 22;

	private final int[] cells = new int[WIDTH * HEIGHT];

	public GameBoard() {
		clear();
	}

	public void clear() {
		java.util.Arrays.fill(cells, Shape.NONE);
	}

	private static int index(int x, int y) {
		return (y * WIDTH) + x;
	}

	public int shapeAt(int x, int y) {
		return cells[index(x, y)];
	}

	/** 한 칸에 블록을 직접 놓는다 (테스트와 디버그용) */
	public void set(int x, int y, int shape) {
		cells[index(x, y)] = shape;
	}

	public boolean isInside(int x, int y) {
		return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
	}

	/** 보드 안이고 블록이 없으면 true */
	public boolean isFree(int x, int y) {
		return isInside(x, y) && shapeAt(x, y) == Shape.NONE;
	}

	/** 보드 밖이거나 쌓인 블록이 있으면 true */
	@Override
	public boolean isBlocked(int x, int y) {
		return !isFree(x, y);
	}

	/** 조각이 (posX, posY)에 놓일 수 있는지 */
	public boolean canPlace(Shape piece, int posX, int posY) {
		for (int i = 0; i < Shape.CELLS; ++i) {
			if (!isFree(posX + piece.x(i), posY - piece.y(i)))
				return false;
		}
		return true;
	}

	/** 조각을 보드에 고정한다 */
	public void place(Shape piece, int posX, int posY) {
		for (int i = 0; i < Shape.CELLS; ++i)
			set(posX + piece.x(i), posY - piece.y(i), piece.getShape());
	}

	/** 꽉 찬 줄을 지우고 지운 줄 수를 돌려준다 */
	public int removeFullLines() {
		int numFullLines = 0;
		for (int y = HEIGHT - 1; y >= 0; --y) {
			if (isLineFull(y)) {
				++numFullLines;
				shiftDownFrom(y);
			}
		}
		return numFullLines;
	}

	private boolean isLineFull(int y) {
		for (int x = 0; x < WIDTH; ++x) {
			if (shapeAt(x, y) == Shape.NONE)
				return false;
		}
		return true;
	}

	// y줄을 지우고 그 위 줄들을 한 칸씩 내린다
	private void shiftDownFrom(int y) {
		for (int k = y; k < HEIGHT - 1; ++k) {
			for (int x = 0; x < WIDTH; ++x)
				set(x, k, shapeAt(x, k + 1));
		}
		for (int x = 0; x < WIDTH; ++x)
			set(x, HEIGHT - 1, Shape.NONE);
	}
}
