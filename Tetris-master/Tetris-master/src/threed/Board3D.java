package threed;

// 3D 보드 (가로3 세로3 높이14, z=0이 바닥)
public class Board3D
{
	public static final int SIZE_X = 3;
	public static final int SIZE_Y = 3;
	public static final int SIZE_Z = 14;

	// 칸 정보는 보드만 직접 바꾼다 (밖에서는 메서드로만 읽고 쓴다)
	private final boolean[][][] cells = new boolean[SIZE_X][SIZE_Y][SIZE_Z];
	private final Block3D.Type[][][] types = new Block3D.Type[SIZE_X][SIZE_Y][SIZE_Z]; // 칸 색깔용

	public Block3D.Type getType(int x, int y, int z)
	{
		if (!isInside(x, y, z))
			throw new IndexOutOfBoundsException("Outside board");
		return types[x][y][z];
	}

	// 블록 고정
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

	// 꽉찬 층 지우기 (지운 층 수 리턴)
	// 꽉 차지 않은 층만 아래부터 차례로 내려 담고, 남은 위쪽 층은 비운다.
	public int clearCompletedLayers()
	{
		int destinationZ = 0;
		for (int sourceZ = 0; sourceZ < SIZE_Z; sourceZ++)
		{
			if (isLayerComplete(sourceZ))
				continue;
			if (destinationZ != sourceZ)
				copyLayer(sourceZ, destinationZ);
			destinationZ++;
		}
		for (int z = destinationZ; z < SIZE_Z; z++)
			clearLayer(z);
		return SIZE_Z - destinationZ;
	}

	// z층의 9칸이 모두 찼는지
	private boolean isLayerComplete(int z)
	{
		for (int x = 0; x < SIZE_X; x++)
			for (int y = 0; y < SIZE_Y; y++)
				if (!cells[x][y][z])
					return false;
		return true;
	}

	// fromZ층의 칸과 색을 toZ층으로 그대로 옮겨 적는다
	private void copyLayer(int fromZ, int toZ)
	{
		for (int x = 0; x < SIZE_X; x++)
			for (int y = 0; y < SIZE_Y; y++)
			{
				cells[x][y][toZ] = cells[x][y][fromZ];
				types[x][y][toZ] = types[x][y][fromZ];
			}
	}

	// z층을 빈 칸으로 만든다
	private void clearLayer(int z)
	{
		for (int x = 0; x < SIZE_X; x++)
			for (int y = 0; y < SIZE_Y; y++)
			{
				cells[x][y][z] = false;
				types[x][y][z] = null;
			}
	}

	public void clear()
	{
		for (int z = 0; z < SIZE_Z; z++)
			clearLayer(z);
	}

	public boolean isInside(int x, int y, int z)
	{
		return isInside((long) x, (long) y, (long) z);
	}

	// long으로 범위 검사 (좌표 + 상대좌표가 int 범위를 넘쳐도 잘못 통과하지 않게)
	private static boolean isInside(long x, long y, long z)
	{
		return x >= 0 && x < SIZE_X && y >= 0 && y < SIZE_Y && z >= 0 && z < SIZE_Z;
	}

	public boolean isEmpty(int x, int y, int z)
	{
		return isInside(x, y, z) && !cells[x][y][z];
	}

	public boolean isFilled(int x, int y, int z)
	{
		return isInside(x, y, z) && cells[x][y][z];
	}

	// 블록 놓을수 있나
	public boolean canPlace(Block3D block)
	{
		java.util.Objects.requireNonNull(block, "block");
		return canPlace(block, block.getX(), block.getY(), block.getZ());
	}

	public boolean canPlace(Block3D block, int x, int y, int z)
	{
		java.util.Objects.requireNonNull(block, "block");
		for (Block3D.Cube cube : block.getRelativeCubes())
		{
			// long으로 (숫자 너무 크면 넘쳐서)
			long cubeX = (long) x + cube.getX();
			long cubeY = (long) y + cube.getY();
			long cubeZ = (long) z + cube.getZ();
			if (!isInside(cubeX, cubeY, cubeZ))
				return false;
			if (!isEmpty((int) cubeX, (int) cubeY, (int) cubeZ))
				return false;
		}
		return true;
	}

	// 테스트용으로 칸 직접 채우기
	public void setFilled(int x, int y, int z, boolean filled)
	{
		if (!isInside(x, y, z))
			throw new IndexOutOfBoundsException("Outside board: (" + x + ", " + y + ", " + z + ")");
		cells[x][y][z] = filled;
		if (!filled)
			types[x][y][z] = null;
	}
}
