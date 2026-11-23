package vsai.game;

/**
 * 테트리스 블록 7종. 회전 상태 4개의 칸 좌표를 미리 계산해 둔다.
 * 좌표는 블록 상자 왼쪽 위 기준이고, y는 아래로 갈수록 커진다.
 * 색은 화면 쪽(view.BoardView)이 정하므로 여기에는 모양만 있다.
 */
public enum Tetromino {
	I(4, true, 0, 1, 1, 1, 2, 1, 3, 1),
	O(4, false, 1, 0, 2, 0, 1, 1, 2, 1),
	T(3, true, 1, 0, 0, 1, 1, 1, 2, 1),
	S(3, true, 1, 0, 2, 0, 0, 1, 1, 1),
	Z(3, true, 0, 0, 1, 0, 1, 1, 2, 1),
	J(3, true, 0, 0, 0, 1, 1, 1, 2, 1),
	L(3, true, 2, 0, 0, 1, 1, 1, 2, 1);

	/** 회전 상태 수 */
	public static final int ROTATIONS = 4;
	/** 블록 하나를 이루는 칸 수 */
	public static final int CELLS = 4;

	/** [회전][칸 번호][0=x, 1=y] */
	private final int[][][] cells = new int[ROTATIONS][CELLS][2];

	Tetromino(int boxSize, boolean rotates, int... xy) {
		for (int i = 0; i < CELLS; ++i) {
			cells[0][i][0] = xy[i * 2];
			cells[0][i][1] = xy[i * 2 + 1];
		}
		// 시계 방향 회전: 상자 안에서 (x, y) -> (상자크기-1-y, x)
		for (int r = 1; r < ROTATIONS; ++r) {
			for (int i = 0; i < CELLS; ++i) {
				int x = cells[r - 1][i][0];
				int y = cells[r - 1][i][1];
				cells[r][i][0] = rotates ? boxSize - 1 - y : x;
				cells[r][i][1] = rotates ? x : y;
			}
		}
	}

	/** 회전 상태 rotation(0~3)의 칸 좌표 4개 */
	public int[][] cells(int rotation) {
		return cells[rotation & (ROTATIONS - 1)];
	}

	/** 그 회전 상태에서 가장 왼쪽 칸의 x (상자 기준) */
	public int minX(int rotation) {
		int m = Integer.MAX_VALUE;
		for (int[] c : cells(rotation))
			m = Math.min(m, c[0]);
		return m;
	}
}
