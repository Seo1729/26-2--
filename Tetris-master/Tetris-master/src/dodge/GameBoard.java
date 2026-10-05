package dodge;

/**
 * 보드 격자만 관리한다. 쌓인 블록 저장, 칸 비었는지 확인, 줄 삭제.
 * 좌표: x는 왼쪽 0부터, y는 바닥 0부터 위로 증가.
 */
public class GameBoard {

	public static final int WIDTH = 10;
	public static final int HEIGHT = 22;

	private final Tetrominoes[] cells = new Tetrominoes[WIDTH * HEIGHT];

	public GameBoard() {
		clear();
	}

	public void clear() {
		for (int i = 0; i < WIDTH * HEIGHT; ++i)
			cells[i] = Tetrominoes.NoShape;
	}

	public Tetrominoes shapeAt(int x, int y) {
		return cells[(y * WIDTH) + x];
	}

	public boolean isInside(int x, int y) {
		return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
	}

	/** 보드 안이고 블록이 없으면 true */
	public boolean isFree(int x, int y) {
		return isInside(x, y) && shapeAt(x, y) == Tetrominoes.NoShape;
	}

	/** 조각이 (posX, posY)에 놓일 수 있는지 */
	public boolean canPlace(Shape piece, int posX, int posY) {
		for (int i = 0; i < 4; ++i) {
			if (!isFree(posX + piece.x(i), posY - piece.y(i)))
				return false;
		}
		return true;
	}

	/** 조각을 보드에 고정한다 */
	public void place(Shape piece, int posX, int posY) {
		for (int i = 0; i < 4; ++i) {
			int x = posX + piece.x(i);
			int y = posY - piece.y(i);
			cells[(y * WIDTH) + x] = piece.getShape();
		}
	}

	/** 꽉 찬 줄을 지우고 지운 줄 수를 돌려준다 */
	public int removeFullLines() {
		int numFullLines = 0;

		for (int i = HEIGHT - 1; i >= 0; --i) {
			boolean lineIsFull = true;

			for (int j = 0; j < WIDTH; ++j) {
				if (shapeAt(j, i) == Tetrominoes.NoShape) {
					lineIsFull = false;
					break;
				}
			}

			if (lineIsFull) {
				++numFullLines;
				for (int k = i; k < HEIGHT - 1; ++k) {
					for (int j = 0; j < WIDTH; ++j)
						cells[(k * WIDTH) + j] = shapeAt(j, k + 1);
				}
				for (int j = 0; j < WIDTH; ++j)
					cells[((HEIGHT - 1) * WIDTH) + j] = Tetrominoes.NoShape;
			}
		}
		return numFullLines;
	}
}
