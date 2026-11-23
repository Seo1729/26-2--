package threed;

import java.util.Objects;

// 3D 블록 (큐브 4개)
// 위치(x,y,z)가 회전 중심이고 큐브는 거기서 상대좌표
public class Block3D
{
	public static final int CUBE_COUNT = 4;

	// 블록 종류: 종류마다 자기 모양(큐브 4개의 상대좌표)을 직접 들고 있다.
	// 새 종류를 추가할 때 switch를 고칠 필요 없이 상수 한 줄만 추가하면 된다. (OCP)
	// 순서(I O T L J S Z)는 BlockColors의 색 순서와 맞물려 있으므로 바꾸지 않는다.
	public enum Type
	{
		// 2D 테트리스(Blocks)랑 같은 모양
		I(new int[][] { {-1, 0, 0}, {0, 0, 0}, {1, 0, 0}, {2, 0, 0} }),
		O(new int[][] { {0, 0, 0}, {1, 0, 0}, {0, 1, 0}, {1, 1, 0} }),
		T(new int[][] { {0, -1, 0}, {-1, 0, 0}, {0, 0, 0}, {1, 0, 0} }),
		L(new int[][] { {-1, 0, 0}, {0, 0, 0}, {1, 0, 0}, {-1, 1, 0} }),
		J(new int[][] { {-1, 0, 0}, {0, 0, 0}, {1, 0, 0}, {1, 1, 0} }),
		S(new int[][] { {0, 0, 0}, {1, 0, 0}, {-1, 1, 0}, {0, 1, 0} }),
		Z(new int[][] { {-1, 0, 0}, {0, 0, 0}, {0, 1, 0}, {1, 1, 0} });

		private final int[][] offsets;

		Type(int[][] offsets)
		{
			this.offsets = offsets;
		}

		// 이 종류의 처음 모양을 새 배열로 만들어 준다 (호출한 쪽이 바꿔도 원본은 그대로)
		Cube[] createCubes()
		{
			Cube[] cubes = new Cube[offsets.length];
			for (int i = 0; i < offsets.length; i++)
				cubes[i] = new Cube(offsets[i][0], offsets[i][1], offsets[i][2]);
			return cubes;
		}
	}

	// 회전 축: 축마다 90도 회전 행렬을 직접 들고 있다. (switch 대신 축이 스스로 회전을 계산)
	public enum Axis
	{
		X(new int[][] { {1, 0, 0}, {0, 0, -1}, {0, 1, 0} }),
		Y(new int[][] { {0, 0, 1}, {0, 1, 0}, {-1, 0, 0} }),
		Z(new int[][] { {0, -1, 0}, {1, 0, 0}, {0, 0, 1} });

		private final int[][] matrix;

		Axis(int[][] matrix)
		{
			this.matrix = matrix;
		}

		// 큐브 하나를 이 축으로 90도 돌린 새 큐브
		Cube rotate(Cube cube)
		{
			return new Cube(
					matrix[0][0] * cube.getX() + matrix[0][1] * cube.getY() + matrix[0][2] * cube.getZ(),
					matrix[1][0] * cube.getX() + matrix[1][1] * cube.getY() + matrix[1][2] * cube.getZ(),
					matrix[2][0] * cube.getX() + matrix[2][1] * cube.getY() + matrix[2][2] * cube.getZ());
		}
	}

	// 큐브 하나 좌표
	public static final class Cube
	{
		private final int x;
		private final int y;
		private final int z;

		public Cube(int x, int y, int z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public int getX() { return x; }
		public int getY() { return y; }
		public int getZ() { return z; }
	}

	// 밖에서 직접 바꾸지 못하게 모두 private (위치는 setPosition으로만 바꾼다)
	private final Type type;
	private Cube[] relativeCubes;
	private int x;
	private int y;
	private int z;

	public Block3D(Type type, int x, int y, int z)
	{
		this.type = Objects.requireNonNull(type, "type");
		this.relativeCubes = type.createCubes();
		setPosition(x, y, z);
	}

	public Type getType() { return type; }

	// 복사
	public Block3D copy()
	{
		Block3D result = new Block3D(type, x, y, z);
		result.relativeCubes = relativeCubes.clone();
		return result;
	}

	// 90도 돌린 새 블록 리턴 (원래꺼는 그대로)
	public Block3D rotated(Axis axis)
	{
		Objects.requireNonNull(axis, "axis");
		Block3D result = copy();
		for (int i = 0; i < CUBE_COUNT; i++)
			result.relativeCubes[i] = axis.rotate(relativeCubes[i]);
		return result;
	}

	public int getX() { return x; }
	public int getY() { return y; }
	public int getZ() { return z; }

	public void setPosition(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Cube[] getRelativeCubes()
	{
		return relativeCubes.clone();
	}

	// 보드 기준 좌표
	public Cube[] getAbsoluteCubes()
	{
		Cube[] result = new Cube[CUBE_COUNT];
		for (int i = 0; i < CUBE_COUNT; i++)
		{
			Cube cube = relativeCubes[i];
			result[i] = new Cube(x + cube.getX(), y + cube.getY(), z + cube.getZ());
		}
		return result;
	}
}
