package vscpu.game;

import common.TetrisRules;

/**
 * 컴퓨터 대전 규칙(VsCpuRules)이 베이스 규칙(TetrisRules)을 상속한 뒤에도
 * 리팩토링 전 PlayerBoard 상수와 같은 값을 내는지 확인한다.
 */
public final class VsCpuRulesCheck {

	private VsCpuRulesCheck() {
	}

	private static void check(boolean ok, String message) {
		if (!ok)
			throw new AssertionError(message);
	}

	public static void main(String[] args) {
		TetrisRules rules = new VsCpuRules();
		int[] oldScores = { 0, 100, 300, 500, 800 };

		for (int lines = 0; lines <= 300; ++lines)
			check(rules.levelFor(lines) == lines / 10 + 1, "level for " + lines + " lines");
		for (int level = 1; level <= 30; ++level) {
			check(rules.fallInterval(level) == Math.max(100, 800 - (level - 1) * 70), "fall interval at level " + level);
			for (int cleared = 0; cleared <= 4; ++cleared)
				check(rules.lineScore(cleared, level) == oldScores[cleared] * level,
						"line score " + cleared + " at level " + level);
		}
		check(rules.softDropScore() == 1, "soft drop score");
		for (int cells = 0; cells <= 20; ++cells)
			check(rules.hardDropScore(cells) == 2 * cells, "hard drop score " + cells);

		// 판에 규칙이 실제로 쓰이는지: 하드 드롭 점수가 떨어진 칸 수 × 2
		PlayerBoard board = new PlayerBoard("test");
		board.reset(new PlayerBoard.PieceSequence(1));
		int startY = board.getY();
		int drop = board.ghostY() - startY;
		board.hardDrop();
		check(board.getScore() == 2 * drop, "hard drop score on board");
		check(board.level() == 1, "board starts at level 1");

		System.out.println("VsCpuRules values match the old PlayerBoard rules and are used by the board.");
	}
}
