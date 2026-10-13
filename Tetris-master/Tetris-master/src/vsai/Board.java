package vsai;

/**
 * 한쪽 판의 쌓인 블록 격자. 좌표: x는 왼쪽 0부터, y는 맨 위 0부터 아래로 증가.
 * AI가 생각할 때 쓸 수 있도록 복사(copy)가 가볍다.
 */
public class Board {

	public static final int WIDTH = 10;
	public static final int HEIGHT = 20;

	private final Tetromino[][] grid = new Tetromino[HEIGHT][WIDTH];

	public Board copy() {
		Board b = new Board();
		for (int y = 0; y < HEIGHT; ++y)
			System.arraycopy(grid[y], 0, b.grid[y], 0, WIDTH);
		return b;
	}

	public void clear() {
		for (int y = 0; y < HEIGHT; ++y)
			for (int x = 0; x < WIDTH; ++x)
				grid[y][x] = null;
	}

	/** (x, y) 칸의 블록. 비어 있으면 null */
	public Tetromino get(int x, int y) {
		return grid[y][x];
	}

	/** 블록을 (px, py) 위치(상자 왼쪽 위)에 놓을 수 있는지. 판 위쪽 바깥은 비어 있는 것으로 본다. */
	public boolean canPlace(Tetromino t, int rotation, int px, int py) {
		for (int[] c : t.cells(rotation)) {
			int x = px + c[0];
			int y = py + c[1];
			if (x < 0 || x >= WIDTH || y >= HEIGHT)
				return false;
			if (y >= 0 && grid[y][x] != null)
				return false;
		}
		return true;
	}

	/** 블록을 고정한다. 판 위쪽 바깥에 걸친 칸이 있으면 false (꼭대기를 넘음) */
	public boolean place(Tetromino t, int rotation, int px, int py) {
		boolean inside = true;
		for (int[] c : t.cells(rotation)) {
			int x = px + c[0];
			int y = py + c[1];
			if (y < 0)
				inside = false;
			else
				grid[y][x] = t;
		}
		return inside;
	}

	/** (px, py)에서 그대로 떨어뜨렸을 때 멈추는 y */
	public int dropY(Tetromino t, int rotation, int px, int py) {
		int y = py;
		while (canPlace(t, rotation, px, y + 1))
			++y;
		return y;
	}

	/** 꽉 찬 줄을 지우고 지운 줄 수를 돌려준다 */
	public int clearLines() {
		int cleared = 0;
		for (int y = HEIGHT - 1; y >= 0; --y) {
			boolean full = true;
			for (int x = 0; x < WIDTH && full; ++x)
				full = grid[y][x] != null;
			if (!full)
				continue;
			++cleared;
			for (int yy = y; yy > 0; --yy)
				System.arraycopy(grid[yy - 1], 0, grid[yy], 0, WIDTH);
			for (int x = 0; x < WIDTH; ++x)
				grid[0][x] = null;
			++y; // 내려온 줄을 다시 검사
		}
		return cleared;
	}

	/** x열의 높이 (비어 있으면 0, 맨 위까지 차면 HEIGHT) */
	public int columnHeight(int x) {
		for (int y = 0; y < HEIGHT; ++y)
			if (grid[y][x] != null)
				return HEIGHT - y;
		return 0;
	}

	/** 위에 블록이 있는데 비어 있는 칸(구멍) 수 */
	public int holes() {
		int holes = 0;
		for (int x = 0; x < WIDTH; ++x) {
			boolean covered = false;
			for (int y = 0; y < HEIGHT; ++y) {
				if (grid[y][x] != null)
					covered = true;
				else if (covered)
					++holes;
			}
		}
		return holes;
	}
}
