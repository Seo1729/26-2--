package threed;

import java.util.Random;

// 기본 방식: 3x3에 들어가는 6종류 중 하나를 무작위로 고른다
public class RandomBlockTypeSource implements BlockTypeSource
{
	// I블록은 길이가 4라서 3x3에 안들어감 -> 뺌
	private static final Block3D.Type[] SPAWN_TYPES = {
		Block3D.Type.O, Block3D.Type.T, Block3D.Type.L,
		Block3D.Type.J, Block3D.Type.S, Block3D.Type.Z
	};

	private final Random random;

	public RandomBlockTypeSource()
	{
		this(new Random());
	}

	// 같은 순서를 다시 보고 싶을 때(테스트 등) 시드를 정한 Random을 넣는다
	public RandomBlockTypeSource(Random random)
	{
		this.random = java.util.Objects.requireNonNull(random, "random");
	}

	@Override
	public Block3D.Type next()
	{
		return SPAWN_TYPES[random.nextInt(SPAWN_TYPES.length)];
	}
}
