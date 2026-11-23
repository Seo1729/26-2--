package dodge;

import java.util.Random;

// 테트리스 블록 모양 (학교 tetris2026 코드 가져옴)
// 모양 번호: 0 없음, 1 Z, 2 S, 3 일자, 4 T, 5 네모, 6 L, 7 반대L
public class Shape {

	public static final int NONE = 0;
	public static final int SQUARE = 5;
	/** 실제 블록 종류 수 (없음 제외) */
	public static final int KINDS = 7;
	/** 블록 한 개를 이루는 칸 수 */
	public static final int CELLS = 4;

	// 모양별 기본 좌표 (모양 번호가 인덱스)
	private static final int[][][] COORDS_TABLE = { { { 0, 0 }, { 0, 0 }, { 0, 0 }, { 0, 0 } },
			{ { 0, -1 }, { 0, 0 }, { -1, 0 }, { -1, 1 } }, { { 0, -1 }, { 0, 0 }, { 1, 0 }, { 1, 1 } },
			{ { 0, -1 }, { 0, 0 }, { 0, 1 }, { 0, 2 } }, { { -1, 0 }, { 0, 0 }, { 1, 0 }, { 0, 1 } },
			{ { 0, 0 }, { 1, 0 }, { 0, 1 }, { 1, 1 } }, { { -1, -1 }, { 0, -1 }, { 0, 0 }, { 0, 1 } },
			{ { 1, -1 }, { 0, -1 }, { 0, 0 }, { 0, 1 } } };

	private static final Random RANDOM = new Random();

	private int pieceShape;
	private final int[][] coords = new int[CELLS][2];

	public Shape() {
		setShape(NONE);
	}

	public void setShape(int shape) {
		for (int i = 0; i < CELLS; i++) {
			coords[i][0] = COORDS_TABLE[shape][i][0];
			coords[i][1] = COORDS_TABLE[shape][i][1];
		}
		pieceShape = shape;
	}

	public void setRandomShape() {
		setShape(RANDOM.nextInt(KINDS) + 1);
	}

	public int x(int index) {
		return coords[index][0];
	}

	public int y(int index) {
		return coords[index][1];
	}

	public int getShape() {
		return pieceShape;
	}

	public boolean isEmpty() {
		return pieceShape == NONE;
	}

	public int minX() {
		int m = coords[0][0];
		for (int i = 1; i < CELLS; i++)
			m = Math.min(m, coords[i][0]);
		return m;
	}

	public int maxX() {
		int m = coords[0][0];
		for (int i = 1; i < CELLS; i++)
			m = Math.max(m, coords[i][0]);
		return m;
	}

	public int minY() {
		int m = coords[0][1];
		for (int i = 1; i < CELLS; i++)
			m = Math.min(m, coords[i][1]);
		return m;
	}

	public int maxY() {
		int m = coords[0][1];
		for (int i = 1; i < CELLS; i++)
			m = Math.max(m, coords[i][1]);
		return m;
	}

	/** 왼쪽으로 돌린 새 모양 (네모는 그대로) */
	public Shape rotateLeft() {
		if (pieceShape == SQUARE)
			return this;

		Shape result = new Shape();
		result.pieceShape = pieceShape;
		for (int i = 0; i < CELLS; ++i) {
			result.coords[i][0] = y(i);
			result.coords[i][1] = -x(i);
		}
		return result;
	}
}
