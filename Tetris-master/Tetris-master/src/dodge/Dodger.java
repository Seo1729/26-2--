package dodge;

/**
 * P2 똥피하기 플레이어 캐릭터. 보드 1칸 크기로 칸 단위로 움직인다.
 * 쌓인 블록과 떨어지는 조각은 벽/발판으로 취급한다.
 */
public class Dodger {

	/** 점프로 올라갈 수 있는 최대 칸 수 */
	public static final int JUMP_HEIGHT = 2;
	/**
	 * 마지막으로 한 칸 걸은 뒤 이 시간(ms) 동안은 걷는 모습으로 그린다.
	 * 키를 꾹 눌렀을 때 첫 반복까지 기다리는 시간(InputHandler.REPEAT_DELAY 170ms)보다 길어야
	 * 그 사이에 잠깐 서 있는 모습으로 바뀌지 않는다.
	 */
	private static final long WALK_POSE_MS = 200;
	/** 그리는 위치가 좌우로 1칸 미끄러지는 데 걸리는 시간(ms). 키를 꾹 눌렀을 때 반복 간격과 같게 해서 끊기지 않게 한다. */
	private static final double SLIDE_X_MS = 90;
	/** 그리는 위치가 위아래로 1칸 움직이는 데 걸리는 시간(ms). 점프/중력 간격과 같다. */
	private static final double SLIDE_Y_MS = 80;
	/** 그리는 위치가 이보다 멀리 떨어지면(다시 시작 등) 미끄러지지 않고 바로 옮긴다 */
	private static final double SNAP_DISTANCE = 3;

	/** 그리기용 캐릭터 상태 */
	public enum State {
		IDLE, WALK, JUMP, FALL, CRUSHED
	}

	private final GameBoard board;
	private final TetrisPlayer tetris;
	private int x;
	private int y;
	private int jumpLeft;
	private boolean crushed;
	private boolean facingRight;
	/** 걸을 때마다 0, 1을 번갈아 바꿔 발을 교대로 내딛는 모습을 만든다 */
	private int walkFrame;
	private long lastMoveTime;
	/**
	 * 화면에 그리는 위치(칸 단위, 소수). 게임 판정은 칸 단위 x, y로 하고,
	 * 그림만 이 값을 따라 부드럽게 미끄러지게 해서 순간이동처럼 보이지 않게 한다.
	 */
	private double drawX;
	private double drawY;

	public Dodger(GameBoard board, TetrisPlayer tetris) {
		this.board = board;
		this.tetris = tetris;
	}

	public void reset() {
		x = 0;
		y = 0;
		jumpLeft = 0;
		crushed = false;
		facingRight = true;
		walkFrame = 0;
		lastMoveTime = 0;
		drawX = x;
		drawY = y;
	}

	/** 게임 루프가 매 프레임 호출. 그리는 위치를 실제 칸 쪽으로 조금씩 옮긴다. */
	public void updateDraw(int elapsedMs) {
		drawX = slide(drawX, x, elapsedMs / SLIDE_X_MS);
		drawY = slide(drawY, y, elapsedMs / SLIDE_Y_MS);
	}

	/** current를 target 쪽으로 step칸만큼 옮긴다. 1칸보다 많이 뒤처지면 그만큼 빨리 따라간다. */
	private static double slide(double current, int target, double step) {
		double diff = target - current;
		double distance = Math.abs(diff);
		if (distance > SNAP_DISTANCE)
			return target;
		double move = step * Math.max(1, distance);
		if (distance <= move)
			return target;
		return current + Math.signum(diff) * move;
	}

	/** 한 칸 걸었을 때 걷기 애니메이션을 한 프레임 넘긴다 */
	private void stepped() {
		walkFrame ^= 1;
		lastMoveTime = System.currentTimeMillis();
	}

	private boolean isSolid(int cx, int cy) {
		return !board.isFree(cx, cy) || tetris.occupies(cx, cy);
	}

	public boolean isOnGround() {
		return isSolid(x, y - 1);
	}

	/** 왼쪽으로 한 칸. 막혀 있어도 방향은 왼쪽으로 돌아본다. */
	public void moveLeft() {
		if (crushed)
			return;
		facingRight = false;
		if (!isSolid(x - 1, y)) {
			--x;
			stepped();
		}
	}

	/** 오른쪽으로 한 칸. 막혀 있어도 방향은 오른쪽으로 돌아본다. */
	public void moveRight() {
		if (crushed)
			return;
		facingRight = true;
		if (!isSolid(x + 1, y)) {
			++x;
			stepped();
		}
	}

	public void jump() {
		if (!crushed && isOnGround())
			jumpLeft = JUMP_HEIGHT;
	}

	/** 일정 간격마다 호출. 점프 중이면 1칸 올라가고, 아니면 중력으로 1칸 떨어진다. */
	public void physicsStep() {
		if (crushed)
			return;

		if (jumpLeft > 0) {
			if (!isSolid(x, y + 1)) {
				++y;
				--jumpLeft;
			} else {
				jumpLeft = 0;
			}
		} else if (!isOnGround()) {
			--y;
		}
	}

	/** 줄 삭제 등으로 캐릭터 칸이 블록과 겹치면 위쪽 빈칸으로 밀어 올린다 */
	public void resolveOverlap() {
		while (y < GameBoard.HEIGHT - 1 && !board.isFree(x, y))
			++y;
	}

	public void crush() {
		crushed = true;
		// 깔린 자리에 바로 그려서 블록과 어긋나 보이지 않게 한다
		drawX = x;
		drawY = y;
	}

	public boolean isCrushed() {
		return crushed;
	}

	/** 지금 어떤 모습으로 그려야 하는지 */
	public State getState() {
		if (crushed)
			return State.CRUSHED;
		if (jumpLeft > 0)
			return State.JUMP;
		if (!isOnGround())
			return State.FALL;
		// 옆 칸으로 미끄러지는 동안에도 걷는 모습
		if (drawX != x || System.currentTimeMillis() - lastMoveTime < WALK_POSE_MS)
			return State.WALK;
		return State.IDLE;
	}

	public boolean isFacingRight() {
		return facingRight;
	}

	public int getWalkFrame() {
		return walkFrame;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	/** 화면에 그릴 x (칸 단위, 소수) */
	public double getDrawX() {
		return drawX;
	}

	/** 화면에 그릴 y (칸 단위, 소수) */
	public double getDrawY() {
		return drawY;
	}
}
