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
	private final Block3D.Type[][][] types = new Block3D.Type[SIZE_X][SIZE_Y][SIZE_Z];

	/** 고정된 블록 종류. 빈칸 또는 setFilled로 만든 종류 없는 칸은 null이다. */
	public Block3D.Type getType(int x, int y, int z)
	{
		if (!isInside(x, y, z))
			throw new IndexOutOfBoundsException("Outside board");
		return types[x][y][z];
	}

	/** 네 절대좌표를 검증한 뒤 종류와 함께 저장한다. 실패 시 보드를 변경하지 않는다. */
	public void lockBlock(Block3D block)
	{
		if (!canPlace(block))
			throw new IllegalArgumentException("Cannot lock block at occupied or outside cells");
		for (Block3D.Cube cube : block.getAbsoluteCubes())
		{
			cells[cube.getX()][cube.getY()][cube.getZ()] = true;
			types[cube.getX()][cube.getY()][cube.getZ()] = block.getType();
		}
	}

	/** 완성된 층을 제거하고 남은 층을 순서대로 압축한다. 삭제한 층 수를 반환한다. */
	public int clearCompletedLayers()
	{
		int destinationZ = 0;
		for (int sourceZ = 0; sourceZ < SIZE_Z; sourceZ++)
		{
			boolean complete = true;
			for (int x = 0; x < SIZE_X; x++)
				for (int y = 0; y < SIZE_Y; y++)
					if (!cells[x][y][sourceZ])
						complete = false;
			if (complete)
				continue;
			if (destinationZ != sourceZ)
			{
				for (int x = 0; x < SIZE_X; x++)
					for (int y = 0; y < SIZE_Y; y++)
					{
						cells[x][y][destinationZ] = cells[x][y][sourceZ];
						types[x][y][destinationZ] = types[x][y][sourceZ];
					}
			}
			destinationZ++;
		}
		for (int z = destinationZ; z < SIZE_Z; z++)
			for (int x = 0; x < SIZE_X; x++)
				for (int y = 0; y < SIZE_Y; y++)
				{
					cells[x][y][z] = false;
					types[x][y][z] = null;
				}
		return SIZE_Z - destinationZ;
	}

	/** 새 게임을 위해 점유 상태와 저장된 블록 종류를 모두 초기화한다. */
	public void clear()
	{
		for (int x = 0; x < SIZE_X; x++)
			for (int y = 0; y < SIZE_Y; y++)
				for (int z = 0; z < SIZE_Z; z++)
				{
					cells[x][y][z] = false;
					types[x][y][z] = null;
				}
	}

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

	/** 블록의 현재 위치에 네 큐브를 모두 배치할 수 있는지 검사한다. */
	public boolean canPlace(Block3D block)
	{
		java.util.Objects.requireNonNull(block, "block");
		return canPlace(block, block.getX(), block.getY(), block.getZ());
	}

	/**
	 * 블록을 변경하지 않고 후보 중심 좌표의 배치 가능 여부를 검사한다.
	 * 네 큐브 중 하나라도 경계 밖이거나 고정된 칸과 겹치면 false이다.
	 */
	public boolean canPlace(Block3D block, int x, int y, int z)
	{
		java.util.Objects.requireNonNull(block, "block");
		for (Block3D.Cube cube : block.getRelativeCubes())
		{
			// 큰 후보 좌표를 더할 때 정수 오버플로로 경계 검사가 우회되지 않도록 한다.
			long cubeX = (long) x + cube.getX();
			long cubeY = (long) y + cube.getY();
			long cubeZ = (long) z + cube.getZ();
			if (cubeX < 0 || cubeX >= SIZE_X
					|| cubeY < 0 || cubeY >= SIZE_Y
					|| cubeZ < 0 || cubeZ >= SIZE_Z)
				return false;
			if (!isEmpty((int) cubeX, (int) cubeY, (int) cubeZ))
				return false;
		}
		return true;
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
		if (!filled)
			types[x][y][z] = null;
	}
}
