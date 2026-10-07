import java.util.Objects;

/**
 * 네 개의 큐브로 구성된 논리 블록. 보드나 그래픽에 의존하지 않는다.
 * 블록 위치는 정수 격자의 중심 기준점(회전 피벗)이며 기하학적 무게중심과는 다를 수 있다.
 * 큐브의 보드 좌표 = 블록 위치 + 상대좌표. 초기 형태는 모두 XY 평면(z=0)에 있다.
 */
public class Block3D
{
	public static final int CUBE_COUNT = 4;

	public enum Type
	{
		I, O, T, L, J, S, Z
	}

	public enum Axis { X, Y, Z }

	/** 상대좌표와 보드 좌표에 공통으로 사용하는 불변 좌표 값. */
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

	private final Type type;
	// 회전된 형태도 블록의 기준점에 대한 상대좌표로 유지한다.
	private Cube[] relativeCubes;
	private int x;
	private int y;
	private int z;

	public Block3D(Type type, int x, int y, int z)
	{
		this.type = Objects.requireNonNull(type, "type");
		this.relativeCubes = createInitialCubes(type);
		setPosition(x, y, z);
	}

	public Type getType() { return type; }

	/** 현재 회전 상태를 보존하는 독립적인 복사본. */
	public Block3D copy()
	{
		Block3D result = new Block3D(type, x, y, z);
		result.relativeCubes = relativeCubes.clone();
		return result;
	}

	/** 90도 회전 후보를 반환한다. 원본의 좌표와 형태는 변경하지 않는다. */
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
		for (int i = 0; i < CUBE_COUNT; i++)
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

	/** 중심 기준점의 보드 위치를 설정한다. 배치 가능 여부는 보드 측에서 검사한다. */
	public void setPosition(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/** 내부 배열을 보호하는 복사본. 각 Cube는 불변이다. */
	public Cube[] getRelativeCubes()
	{
		return relativeCubes.clone();
	}

	/** 현재 블록 위치를 더한 네 큐브의 보드 좌표를 반환한다. */
	public Cube[] getAbsoluteCubes()
	{
		Cube[] result = new Cube[CUBE_COUNT];
		for (int i = 0; i < CUBE_COUNT; i++)
		{
			Cube cube = relativeCubes[i];
			result[i] = new Cube(x + cube.x, y + cube.y, z + cube.z);
		}
		return result;
	}

	private static Cube[] createInitialCubes(Type type)
	{
		// 기존 Blocks.getBlock()과 같은 모양과 방향을 사용한다.
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
