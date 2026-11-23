package vsai.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 한쪽(플레이어 또는 CPU)의 게임 상태: 판, 떨어지는 블록, 다음 블록, 점수, 줄 수, 레벨.
 * 조작 방법은 사람과 CPU가 같다 (좌우 이동, 회전, 소프트 드롭, 하드 드롭).
 */
public class PlayerBoard implements BoardState {

	/** 한 번에 지운 줄 수(0~4)에 따른 기본 점수. 레벨을 곱한다. */
	private static final int[] LINE_SCORES = { 0, 100, 300, 500, 800 };
	/** 회전이 벽이나 블록에 막히면 이만큼 옆으로 밀어서 다시 시도한다 */
	private static final int[] KICKS = { 0, -1, 1, -2, 2 };
	private static final int SPAWN_X = 3;
	private static final int SPAWN_Y = 0;
	private static final int SOFT_DROP_SCORE = 1;
	/** 하드 드롭으로 내려간 한 칸마다 받는 점수 */
	private static final int HARD_DROP_SCORE = 2;
	private static final int LINES_PER_LEVEL = 10;
	/** 낙하 간격: 레벨 1에서 800ms, 레벨마다 70ms씩 빨라지고, 100ms보다 빨라지지 않는다 */
	private static final int START_FALL_MS = 800;
	private static final int FALL_MS_PER_LEVEL = 70;
	private static final int MIN_FALL_MS = 100;

	/**
	 * 두 판이 함께 쓰는 블록 순서. 같은 순서로 블록이 나와서 공정하다.
	 * 7종을 한 묶음으로 섞어 내보내는 방식(7-bag)이라 같은 블록만 계속 나오지 않는다.
	 */
	public static class PieceSequence {

		private final Random random;
		private final List<Tetromino> pieces = new ArrayList<>();

		public PieceSequence(long seed) {
			random = new Random(seed);
		}

		/** index번째 블록 (0부터) */
		public synchronized Tetromino get(int index) {
			while (pieces.size() <= index) {
				List<Tetromino> bag = new ArrayList<>(List.of(Tetromino.values()));
				Collections.shuffle(bag, random);
				pieces.addAll(bag);
			}
			return pieces.get(index);
		}
	}

	private final String name;
	private final Board board = new Board();
	private PieceSequence sequence;
	private int pieceIndex;
	private Tetromino current;
	private Tetromino next;
	private int rotation;
	private int x;
	private int y;
	/** 지금까지 나온 블록 수. CPU가 새 블록이 나왔는지 알아챌 때 쓴다. */
	private int pieceId;
	private int score;
	private int lines;
	private boolean gameOver;
	private int fallElapsed;

	public PlayerBoard(String name) {
		this.name = name;
	}

	public void reset(PieceSequence sequence) {
		this.sequence = sequence;
		board.clear();
		pieceIndex = 0;
		pieceId = 0;
		score = 0;
		lines = 0;
		gameOver = false;
		fallElapsed = 0;
		spawn();
	}

	private void spawn() {
		current = sequence.get(pieceIndex);
		next = sequence.get(pieceIndex + 1);
		++pieceIndex;
		++pieceId;
		rotation = 0;
		x = SPAWN_X;
		y = SPAWN_Y;
		if (!board.canPlace(current, rotation, x, y))
			gameOver = true;
	}

	/** 게임 루프가 매 프레임 호출. 레벨에 맞는 간격마다 한 칸 떨어진다. */
	public void tick(int elapsedMs) {
		if (gameOver)
			return;
		fallElapsed += elapsedMs;
		if (fallElapsed >= fallInterval()) {
			fallElapsed = 0;
			if (!tryMove(rotation, x, y + 1))
				lock();
		}
	}

	public boolean moveLeft() {
		return shift(-1);
	}

	public boolean moveRight() {
		return shift(1);
	}

	/** 옆으로 dx칸 옮긴다 */
	private boolean shift(int dx) {
		return !gameOver && tryMove(rotation, x + dx, y);
	}

	/** 시계 방향 회전. 막히면 옆으로 조금 밀어서 다시 시도한다. */
	public boolean rotate() {
		if (gameOver)
			return false;
		int nr = (rotation + 1) & (Tetromino.ROTATIONS - 1);
		for (int k : KICKS) {
			if (tryMove(nr, x + k, y))
				return true;
		}
		return false;
	}

	/** 한 칸 내리기. 더 못 내려가면 고정한다. */
	public void softDrop() {
		if (gameOver)
			return;
		fallElapsed = 0;
		if (tryMove(rotation, x, y + 1))
			score += SOFT_DROP_SCORE;
		else
			lock();
	}

	/** 바로 바닥까지 떨어뜨려 고정한다 */
	public void hardDrop() {
		if (gameOver)
			return;
		int ny = ghostY();
		score += HARD_DROP_SCORE * (ny - y);
		y = ny;
		lock();
	}

	private boolean tryMove(int nr, int nx, int ny) {
		if (!board.canPlace(current, nr, nx, ny))
			return false;
		rotation = nr;
		x = nx;
		y = ny;
		return true;
	}

	private void lock() {
		if (!board.place(current, rotation, x, y)) {
			gameOver = true;
			return;
		}
		int cleared = board.clearLines();
		lines += cleared;
		score += LINE_SCORES[cleared] * level();
		fallElapsed = 0;
		spawn();
	}

	@Override
	public int level() {
		return lines / LINES_PER_LEVEL + 1;
	}

	/** 레벨이 오를수록 빨리 떨어진다 (ms) */
	private int fallInterval() {
		return Math.max(MIN_FALL_MS, START_FALL_MS - (level() - 1) * FALL_MS_PER_LEVEL);
	}

	@Override
	public int ghostY() {
		return board.dropY(current, rotation, x, y);
	}

	/** 지금 판의 복사본. CPU가 생각할 때 원본을 건드리지 않도록 복사본만 준다. */
	public Board snapshotBoard() {
		return board.copy();
	}

	@Override
	public Tetromino cellAt(int x, int y) {
		return board.get(x, y);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Tetromino getCurrent() {
		return current;
	}

	@Override
	public Tetromino getNext() {
		return next;
	}

	@Override
	public int getRotation() {
		return rotation;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	public int getPieceId() {
		return pieceId;
	}

	@Override
	public int getScore() {
		return score;
	}

	@Override
	public int getLines() {
		return lines;
	}

	@Override
	public boolean isGameOver() {
		return gameOver;
	}
}
