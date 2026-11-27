package common;

/**
 * 테트리스 모드들이 함께 쓰는 베이스 규칙: 줄 점수, 레벨, 낙하 간격, 드롭 점수.
 *
 * 모드마다 값만 다르고 계산 순서는 같아서, 공통 계산은 여기에 두고
 * 모드별 값은 하위 클래스가 정한다 (예: 컴퓨터 대전의 vscpu.game.VsCpuRules).
 * Swing, 소리, 판 배열을 전혀 모르므로 화면 없이 규칙만 확인할 수 있다.
 */
public abstract class TetrisRules {

	/** 레벨에 끝이 없을 때 쓰는 최고 레벨 */
	protected static final int NO_MAX_LEVEL = Integer.MAX_VALUE;

	/** 레벨 하나를 올리는 데 필요한 줄 수 */
	protected abstract int linesPerLevel();

	/** 이 레벨에서 블록이 한 칸 떨어지는 간격(ms) */
	public abstract int fallInterval(int level);

	/**
	 * 한 번에 cleared줄을 지웠을 때 받는 점수.
	 *
	 * @param cleared 이번에 지운 줄 수 (0이면 0점)
	 * @param level   지금 레벨
	 */
	public abstract int lineScore(int cleared, int level);

	/** 가장 높은 레벨. 기본은 끝이 없다. */
	protected int maxLevel() {
		return NO_MAX_LEVEL;
	}

	/** 소프트 드롭으로 한 칸 내릴 때 받는 점수. 기본은 0점. */
	public int softDropScore() {
		return 0;
	}

	/** 하드 드롭으로 cells칸 떨어뜨렸을 때 받는 점수. 기본은 0점. */
	public int hardDropScore(int cells) {
		return 0;
	}

	/**
	 * 지금까지 지운 줄 수로 레벨을 계산한다. 레벨 1부터 시작해
	 * linesPerLevel()줄마다 하나씩 오르고 maxLevel()에서 멈춘다.
	 */
	public final int levelFor(int lines) {
		return Math.min(maxLevel(), lines / linesPerLevel() + 1);
	}
}
