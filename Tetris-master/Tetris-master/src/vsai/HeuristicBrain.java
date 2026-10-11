package vsai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * 인터넷 없이 코드로 계산하는 AI.
 * 가능한 모든 회전과 열에 블록을 놓아 보고, 판의 모양을 점수로 매겨 가장 좋은 자리를 고른다.
 * 다음 블록까지 한 수 앞을 내다본다. API가 느리거나 실패할 때 대신 쓰는 예비 AI이기도 하다.
 *
 * 실수 확률을 주면 그 확률로 가장 좋은 자리 대신 상위 몇 개 중 하나를 무작위로 고른다 (난이도 조절용).
 */
public class HeuristicBrain implements AiBrain {

	// 널리 쓰이는 테트리스 AI 가중치 (높이 합, 지운 줄, 구멍, 울퉁불퉁함)
	private static final double HEIGHT = -0.510066;
	private static final double LINES = 0.760666;
	private static final double HOLES = -0.35663;
	private static final double BUMPINESS = -0.184483;

	private final double mistakeRate;
	private final int mistakeChoices;
	private final Random random;

	/** 실수 없이 항상 가장 좋은 자리를 고르는 AI */
	public HeuristicBrain() {
		this(0, 1, new Random());
	}

	/**
	 * @param mistakeRate    실수할 확률 (0~1)
	 * @param mistakeChoices 실수할 때 고르는 범위: 점수 순위 2등부터 이 등수까지 중 하나
	 */
	public HeuristicBrain(double mistakeRate, int mistakeChoices, Random random) {
		this.mistakeRate = mistakeRate;
		this.mistakeChoices = mistakeChoices;
		this.random = random;
	}

	@Override
	public Move decide(Board board, Tetromino current, Tetromino next) {
		List<Move> ranked = rank(board, current, next);
		if (ranked.isEmpty())
			return null;
		int choices = Math.min(mistakeChoices, ranked.size());
		if (choices > 1 && random.nextDouble() < mistakeRate)
			return ranked.get(1 + random.nextInt(choices - 1));
		return ranked.get(0);
	}

	/** 놓을 수 있는 자리를 좋은 순서로. 결과 모양이 같은 자리(회전만 다른 경우)는 하나만 남긴다. */
	public List<Move> rank(Board board, Tetromino current, Tetromino next) {
		record Scored(Move move, double score) {
		}
		List<Scored> scored = new ArrayList<>();
		Set<String> seen = new HashSet<>();
		for (int rot = 0; rot < 4; ++rot) {
			for (int px = -3; px < Board.WIDTH; ++px) {
				if (!board.canPlace(current, rot, px, 0))
					continue;
				int y = board.dropY(current, rot, px, 0);
				if (!seen.add(placementKey(current, rot, px, y)))
					continue;
				Board after = board.copy();
				if (!after.place(current, rot, px, y))
					continue;
				int cleared = after.clearLines();
				double score = next == null ? evaluate(after, cleared)
						: LINES * cleared + bestPlacement(after, next);
				scored.add(new Scored(new Move(rot, px + current.minX(rot)), score));
			}
		}
		scored.sort(Comparator.comparingDouble(Scored::score).reversed());
		List<Move> moves = new ArrayList<>();
		for (Scored s : scored)
			moves.add(s.move());
		return moves;
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

	/** 놓인 칸들의 위치로 만든 이름. 같은 이름이면 결과가 같은 자리다. */
	private static String placementKey(Tetromino t, int rot, int px, int py) {
		List<Integer> cells = new ArrayList<>();
		for (int[] c : t.cells(rot))
			cells.add((py + c[1]) * Board.WIDTH + px + c[0]);
		cells.sort(null);
		return cells.toString();
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
