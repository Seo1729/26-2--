package vsai;

/**
 * 인터넷 없이 코드로 계산하는 AI.
 * 가능한 모든 회전과 열에 블록을 놓아 보고, 판의 모양을 점수로 매겨 가장 좋은 자리를 고른다.
 * 다음 블록까지 한 수 앞을 내다본다. API가 느리거나 실패할 때 대신 쓰는 예비 AI이기도 하다.
 */
public class HeuristicBrain implements AiBrain {

	// 널리 쓰이는 테트리스 AI 가중치 (높이 합, 지운 줄, 구멍, 울퉁불퉁함)
	private static final double HEIGHT = -0.510066;
	private static final double LINES = 0.760666;
	private static final double HOLES = -0.35663;
	private static final double BUMPINESS = -0.184483;

	@Override
	public Move decide(Board board, Tetromino current, Tetromino next) {
		Move best = null;
		double bestScore = Double.NEGATIVE_INFINITY;
		for (int rot = 0; rot < 4; ++rot) {
			for (int px = -3; px < Board.WIDTH; ++px) {
				Board after = drop(board, current, rot, px);
				if (after == null)
					continue;
				int cleared = after.clearLines();
				double score = next == null ? evaluate(after, cleared)
						: LINES * cleared + bestPlacement(after, next);
				if (score > bestScore) {
					bestScore = score;
					best = new Move(rot, px + current.minX(rot));
				}
			}
		}
		return best;
	}

	/** 다음 블록을 둘 수 있는 가장 좋은 자리의 점수 */
	private double bestPlacement(Board board, Tetromino t) {
		double best = -1e9;
		for (int rot = 0; rot < 4; ++rot) {
			for (int px = -3; px < Board.WIDTH; ++px) {
				Board after = drop(board, t, rot, px);
				if (after == null)
					continue;
				int cleared = after.clearLines();
				best = Math.max(best, evaluate(after, cleared));
			}
		}
		return best;
	}

	/** 맨 위(블록이 나오는 높이)에서 떨어뜨린 결과 판. 놓을 수 없으면 null */
	private static Board drop(Board board, Tetromino t, int rot, int px) {
		if (!board.canPlace(t, rot, px, 0))
			return null;
		int y = board.dropY(t, rot, px, 0);
		Board after = board.copy();
		if (!after.place(t, rot, px, y))
			return null;
		return after;
	}

	private static double evaluate(Board b, int cleared) {
		int aggregate = 0;
		int bumpiness = 0;
		int prev = -1;
		for (int x = 0; x < Board.WIDTH; ++x) {
			int h = b.columnHeight(x);
			aggregate += h;
			if (prev >= 0)
				bumpiness += Math.abs(h - prev);
			prev = h;
		}
		return HEIGHT * aggregate + LINES * cleared + HOLES * b.holes() + BUMPINESS * bumpiness;
	}
}
