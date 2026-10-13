/**
 * 일반 모드의 레벨(1 ~ 5).
 * 지운 줄 수가 쌓이면 레벨이 오르고, 레벨마다 블록이 한 칸 떨어지는 기본 간격이 정해져 있다.
 */
class LevelManager
{
	/** 가장 높은 레벨 */
	static final int MAX_LEVEL = 5;

	/** 레벨 하나를 올리는 데 필요한 줄 수 */
	static final int LINES_PER_LEVEL = 5;

	/** 레벨별 기본 낙하 간격(ms). 0번 칸이 레벨 1이다. */
	private static final int[] SPEEDS = { 1000, 800, 600, 450, 300 };

	/** 지금 레벨 */
	private int level;

	/** 지금까지 지운 줄 수(시작 레벨만큼 미리 채워 둔다) */
	private int lines;

	/**
	 * 레벨 관리자를 만든다.
	 *
	 * @param startLevel 시작 레벨. 1 ~ 5 밖의 값은 범위 안으로 맞춘다.
	 */
	LevelManager(int startLevel)
	{
		level = Math.max(1, Math.min(MAX_LEVEL, startLevel));

		// 높은 레벨에서 시작하면 그 레벨까지 지운 것으로 쳐서 다음 레벨까지 남은 줄 수가 맞게 한다
		lines = (level - 1) * LINES_PER_LEVEL;
	}

	/**
	 * 지금 레벨을 알려 준다.
	 *
	 * @return 1 ~ 5
	 */
	int getLevel()
	{
		return level;
	}

	/**
	 * 지금 레벨의 기본 낙하 간격을 알려 준다.
	 *
	 * @return 블록이 한 칸 떨어지는 간격(ms)
	 */
	int getSpeed()
	{
		return SPEEDS[level - 1];
	}

	/**
	 * 지운 줄 수를 더하고 레벨을 다시 계산한다.
	 *
	 * @param count 이번에 지운 줄 수
	 * @return 레벨이 올랐으면 true
	 */
	boolean addLines(int count)
	{
		lines = lines + count;

		// 5줄마다 한 레벨씩 오르고 최고 레벨에서 멈춘다
		int newLevel = Math.min(MAX_LEVEL, 1 + lines / LINES_PER_LEVEL);
		if (newLevel > level)
		{
			level = newLevel;
			return true;
		}
		return false;
	}
}
