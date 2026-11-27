package vscpu.game;

import common.TetrisRules;

/**
 * 컴퓨터 대전 규칙. 베이스 규칙(TetrisRules)을 상속해 대전 모드의 값만 정한다.
 * 전에는 이 값들이 PlayerBoard 안에 상수로 섞여 있었다.
 *
 * - 줄 점수: 한 번에 지운 줄 수(0~4)별 기본 점수 × 레벨
 * - 레벨: 10줄마다 1씩, 끝없이 오른다
 * - 낙하 간격: 레벨 1에서 800ms, 레벨마다 70ms씩 빨라지고 100ms보다 빨라지지 않는다
 * - 소프트 드롭 한 칸 1점, 하드 드롭 한 칸 2점
 */
public class VsCpuRules extends TetrisRules {

	/** 한 번에 지운 줄 수(0~4)에 따른 기본 점수. 레벨을 곱한다. */
	private static final int[] LINE_SCORES = { 0, 100, 300, 500, 800 };
	private static final int LINES_PER_LEVEL = 10;
	private static final int START_FALL_MS = 800;
	private static final int FALL_MS_PER_LEVEL = 70;
	private static final int MIN_FALL_MS = 100;
	private static final int SOFT_DROP_SCORE = 1;
	/** 하드 드롭으로 내려간 한 칸마다 받는 점수 */
	private static final int HARD_DROP_SCORE = 2;

	@Override
	protected int linesPerLevel() {
		return LINES_PER_LEVEL;
	}

	@Override
	public int fallInterval(int level) {
		return Math.max(MIN_FALL_MS, START_FALL_MS - (level - 1) * FALL_MS_PER_LEVEL);
	}

	@Override
	public int lineScore(int cleared, int level) {
		return LINE_SCORES[cleared] * level;
	}

	@Override
	public int softDropScore() {
		return SOFT_DROP_SCORE;
	}

	@Override
	public int hardDropScore(int cells) {
		return HARD_DROP_SCORE * cells;
	}
}
