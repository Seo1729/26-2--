/**
 * 3D 테트리스의 논리 보드. 그래픽이나 화면 좌표에 의존하지 않는다.
 * X와 Y는 0~2, Z는 0~13이며 Z=0이 바닥이다.
 * 높이가 낮아지는 방향(-Z)이 낙하 방향이다.
 */
public class Board3D
{
	public static final int SIZE_X = 3;
	public static final int SIZE_Y = 3;
	public static final int SIZE_Z = 14;

	/** [x][y][z]: false는 빈칸, true는 채워진 칸. 생성 시 모두 빈칸이다. */
	private final boolean[][][] cells = new boolean[SIZE_X][SIZE_Y][SIZE_Z];

	/** 좌표가 보드 내부인지 검사한다. */
	public boolean isInside(int x, int y, int z)
	{
		return x >= 0 && x < SIZE_X
				&& y >= 0 && y < SIZE_Y
				&& z >= 0 && z < SIZE_Z;
	}

	/** 보드 내부의 빈칸이면 true. 범위 밖은 false를 반환한다. */
	public boolean isEmpty(int x, int y, int z)
	{
		return isInside(x, y, z) && !cells[x][y][z];
	}

	/** 보드 내부의 채워진 칸이면 true. 범위 밖은 false를 반환한다. */
	public boolean isFilled(int x, int y, int z)
	{
		return isInside(x, y, z) && cells[x][y][z];
	}

	/**
	 * 칸을 채우거나 비운다.
	 * @throws IndexOutOfBoundsException 좌표가 보드 범위 밖인 경우
	 */
	public void setFilled(int x, int y, int z, boolean filled)
	{
		if (!isInside(x, y, z))
			throw new IndexOutOfBoundsException("Outside board: (" + x + ", " + y + ", " + z + ")");
		cells[x][y][z] = filled;
	}
}
