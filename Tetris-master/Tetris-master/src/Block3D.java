import java.util.Objects;

// 3D 블록 (큐브 4개)
// 위치(x,y,z)가 회전 중심이고 큐브는 거기서 상대좌표
public class Block3D
{
	public static final int CUBE_COUNT = 4;

	public enum Type
	{
		I, O, T, L, J, S, Z
	}

	public enum Axis { X, Y, Z }

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

	Type type;
	Cube[] relativeCubes;
	int x;
	int y;
	int z;

	public Block3D(Type type, int x, int y, int z)
	{
		this.type = Objects.requireNonNull(type, "type");
		this.relativeCubes = createInitialCubes(type);
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
		int[][] matrix;
		switch (axis)
		{
		case X: matrix = new int[][] { {1, 0, 0}, {0, 0, -1}, {0, 1, 0} }; break;
		case Y: matrix = new int[][] { {0, 0, 1}, {0, 1, 0}, {-1, 0, 0} }; break;
		case Z: matrix = new int[][] { {0, -1, 0}, {1, 0, 0}, {0, 0, 1} }; break;
		default: throw new IllegalArgumentException("Unknown axis: " + axis);
		}
		Block3D result = copy();
		for (int i = 0; i < 4; i++)
		{
			Cube cube = relativeCubes[i];
			result.relativeCubes[i] = new Cube(
					matrix[0][0] * cube.x + matrix[0][1] * cube.y + matrix[0][2] * cube.z,
					matrix[1][0] * cube.x + matrix[1][1] * cube.y + matrix[1][2] * cube.z,
					matrix[2][0] * cube.x + matrix[2][1] * cube.y + matrix[2][2] * cube.z);
		}
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
		Cube[] result = new Cube[4];
		for (int i = 0; i < 4; i++)
		{
			Cube cube = relativeCubes[i];
			result[i] = new Cube(x + cube.x, y + cube.y, z + cube.z);
		}
		return result;
	}

	private static Cube[] createInitialCubes(Type type)
	{
		// 2D 테트리스(Blocks)랑 같은 모양
		switch (type)
		{
		case I:
			return new Cube[] { new Cube(-1, 0, 0), new Cube(0, 0, 0), new Cube(1, 0, 0), new Cube(2, 0, 0) };
		case O:
			return new Cube[] { new Cube(0, 0, 0), new Cube(1, 0, 0), new Cube(0, 1, 0), new Cube(1, 1, 0) };
		case T:
			return new Cube[] { new Cube(0, -1, 0), new Cube(-1, 0, 0), new Cube(0, 0, 0), new Cube(1, 0, 0) };
		case L:
			return new Cube[] { new Cube(-1, 0, 0), new Cube(0, 0, 0), new Cube(1, 0, 0), new Cube(-1, 1, 0) };
		case J:
			return new Cube[] { new Cube(-1, 0, 0), new Cube(0, 0, 0), new Cube(1, 0, 0), new Cube(1, 1, 0) };
		case S:
			return new Cube[] { new Cube(0, 0, 0), new Cube(1, 0, 0), new Cube(-1, 1, 0), new Cube(0, 1, 0) };
		case Z:
			return new Cube[] { new Cube(-1, 0, 0), new Cube(0, 0, 0), new Cube(0, 1, 0), new Cube(1, 1, 0) };
		default:
			throw new IllegalArgumentException("Unknown block type: " + type);
		}
	}
}
