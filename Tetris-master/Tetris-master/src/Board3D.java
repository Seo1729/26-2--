// 3D 보드 (가로3 세로3 높이14, z=0이 바닥)
public class Board3D
{
	public static final int SIZE_X = 3;
	public static final int SIZE_Y = 3;
	public static final int SIZE_Z = 14;

	boolean[][][] cells = new boolean[3][3][14];
	Block3D.Type[][][] types = new Block3D.Type[3][3][14]; // 칸 색깔용

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
	public int clearCompletedLayers()
	{
		int destinationZ = 0;
		for (int sourceZ = 0; sourceZ < 14; sourceZ++)
		{
			boolean complete = true;
			for (int x = 0; x < 3; x++)
				for (int y = 0; y < 3; y++)
					if (!cells[x][y][sourceZ])
						complete = false;
			if (complete)
				continue;
			if (destinationZ != sourceZ)
			{
				for (int x = 0; x < 3; x++)
					for (int y = 0; y < 3; y++)
					{
						cells[x][y][destinationZ] = cells[x][y][sourceZ];
						types[x][y][destinationZ] = types[x][y][sourceZ];
					}
			}
			destinationZ++;
		}
		for (int z = destinationZ; z < 14; z++)
			for (int x = 0; x < 3; x++)
				for (int y = 0; y < 3; y++)
				{
					cells[x][y][z] = false;
					types[x][y][z] = null;
				}
		return 14 - destinationZ;
	}

	public void clear()
	{
		for (int x = 0; x < 3; x++)
			for (int y = 0; y < 3; y++)
				for (int z = 0; z < 14; z++)
				{
					cells[x][y][z] = false;
					types[x][y][z] = null;
				}
	}

	public boolean isInside(int x, int y, int z)
	{
		if (x >= 0 && x < 3 && y >= 0 && y < 3 && z >= 0 && z < 14)
			return true;
		else
			return false;
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
			if (cubeX < 0 || cubeX >= 3 || cubeY < 0 || cubeY >= 3 || cubeZ < 0 || cubeZ >= 14)
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
