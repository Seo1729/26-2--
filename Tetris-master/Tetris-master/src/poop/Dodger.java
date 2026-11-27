package poop;

/**
 * P2 고양이 (똥피하기 하는 캐릭터). 칸 단위 위치, 점프, 중력, 압사.
 * 주변이 막혔는지는 CollisionMap에만 묻고, 소리는 GameEvents로 알린다.
 */
public class Dodger {

	/** 그림에 쓰는 고양이 모습 */
	public enum Pose {
		IDLE, WALK, JUMP, FALL, CRUSHED
	}

	/** 한 번 점프로 올라가는 칸 수 */
	public static final int JUMP_HEIGHT = 2;
	/** 3칸 점프: 갇혔을 때 빠져나오라고 주는 높은 점프 */
	public static final int SUPER_JUMP_HEIGHT = 3;
	/** 3칸 점프를 모아 둘 수 있는 최대 횟수 (판 시작 때 가득 참) */
	public static final int MAX_SUPER_JUMPS = 3;
	/** 3칸 점프 한 번이 충전되는 시간(ms). 가득 차 있으면 충전 시간이 흐르지 않는다. */
	public static final int SUPER_JUMP_RECHARGE_MS = 20_000;
	/** 점프 키를 이 시간(ms) 안에 한 번 더 누르면 3칸 점프가 된다 */
	public static final int DOUBLE_TAP_MS = 350;
	/** 걷기 그림을 유지하는 시간(ms) */
	private static final long WALK_POSE_MS = 200;
	/** 그림 위치가 한 칸 따라가는 데 걸리는 시간(ms) */
	private static final double SLIDE_X_MS = 90.0;
	private static final double SLIDE_Y_MS = 80.0;
	/** 이보다 멀리 떨어지면 미끄러지지 않고 바로 옮긴다 (칸) */
	private static final double SNAP_DISTANCE = 3;

	private final CollisionMap world;
	private final GameEvents events;

	// 칸 위치와 상태 (테스트가 같은 패키지에서 직접 바꿔 볼 수 있게 package-private)
	int x;
	int y;
	int jumpLeft;
	boolean crushed;
	boolean facingRight;
	int walkFrame;
	long lastMoveTime;
	// 그림 그리는 위치 (부드럽게 움직이게)
	double drawX;
	double drawY;
	// 3칸 점프 남은 횟수와 다음 충전까지 쌓인 시간
	int superJumps;
	int rechargeElapsed;
	// 마지막 땅 점프의 출발 높이와 그 뒤로 흐른 시간 (-1이면 두 번 누르기 대기 아님)
	int takeoffY;
	int sinceJumpMs = -1;

	public Dodger(CollisionMap world, GameEvents events) {
		this.world = world;
		this.events = events;
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
		superJumps = MAX_SUPER_JUMPS;
		rechargeElapsed = 0;
		takeoffY = 0;
		sinceJumpMs = -1;
	}

	// 매 프레임마다 그림 위치를 실제 칸 쪽으로 조금씩 옮김
	public void updateDraw(int elapsedMs) {
		drawX = slide(drawX, x, elapsedMs / SLIDE_X_MS);
		drawY = slide(drawY, y, elapsedMs / SLIDE_Y_MS);
	}

	static double slide(double current, int target, double step) {
		double diff = target - current;
		double distance = Math.abs(diff);
		if (distance > SNAP_DISTANCE)
			return target;
		double move = step * Math.max(1, distance);
		if (distance <= move)
			return target;
		return current + Math.signum(diff) * move;
	}

	public boolean isOnGround() {
		return world.isBlocked(x, y - 1);
	}

	public void moveLeft() {
		walk(-1);
	}

	public void moveRight() {
		walk(1);
	}

	private void walk(int dx) {
		if (crushed)
			return;
		facingRight = dx > 0;
		if (!world.isBlocked(x + dx, y)) {
			x += dx;
			walkFrame ^= 1;
			lastMoveTime = System.currentTimeMillis();
		}
	}

	/**
	 * 점프 키. 땅에서 누르면 2칸 점프.
	 * 점프한 뒤 DOUBLE_TAP_MS 안에 한 번 더 누르면 (남은 횟수가 있을 때) 뛰어오른 자리에서 3칸까지 올라간다.
	 */
	public void jump() {
		if (crushed)
			return;
		if (isOnGround()) {
			jumpLeft = JUMP_HEIGHT;
			takeoffY = y;
			sinceJumpMs = 0;
			events.onJump();
		} else if (canUpgradeJump()) {
			jumpLeft = takeoffY + SUPER_JUMP_HEIGHT - y;
			--superJumps;
			sinceJumpMs = -1; // 한 번 점프에 한 번만
			events.onSuperJump();
		}
	}

	private boolean canUpgradeJump() {
		return sinceJumpMs >= 0 && sinceJumpMs <= DOUBLE_TAP_MS && superJumps > 0
				&& takeoffY + SUPER_JUMP_HEIGHT > y && !world.isBlocked(x, y + 1);
	}

	/**
	 * 시간이 흐른 만큼 두 번 누르기 시간을 재고, 3칸 점프를 충전한다.
	 * 가득 차 있으면 충전 시간을 쌓지 않는다.
	 */
	public void updateTimers(int elapsedMs) {
		if (sinceJumpMs >= 0) {
			sinceJumpMs += elapsedMs;
			if (sinceJumpMs > DOUBLE_TAP_MS)
				sinceJumpMs = -1;
		}

		if (superJumps >= MAX_SUPER_JUMPS) {
			rechargeElapsed = 0;
			return;
		}
		rechargeElapsed += elapsedMs;
		if (rechargeElapsed >= SUPER_JUMP_RECHARGE_MS) {
			rechargeElapsed -= SUPER_JUMP_RECHARGE_MS;
			++superJumps;
			if (superJumps >= MAX_SUPER_JUMPS)
				rechargeElapsed = 0;
		}
	}

	public int getSuperJumps() {
		return superJumps;
	}

	/** 다음 3칸 점프가 충전될 때까지 남은 시간(ms). 가득 차 있으면 0. */
	public int getSuperJumpRechargeMs() {
		return superJumps >= MAX_SUPER_JUMPS ? 0 : SUPER_JUMP_RECHARGE_MS - rechargeElapsed;
	}

	// 점프중이면 올라가고 아니면 떨어짐
	public void physicsStep() {
		if (crushed)
			return;

		if (jumpLeft > 0) {
			if (!world.isBlocked(x, y + 1)) {
				++y;
				--jumpLeft;
			} else {
				jumpLeft = 0;
			}
		} else if (!isOnGround()) {
			--y;
		}
	}

	/** 쌓인 블록이랑 겹치면 위로 올려줌 (블록 고정이나 줄 삭제 뒤) */
	public void resolveOverlap(CollisionMap fixedBlocks) {
		while (y < GameBoard.HEIGHT - 1 && fixedBlocks.isBlocked(x, y))
			++y;
	}

	public void crush() {
		crushed = true;
		events.onCrushed();
		drawX = x;
		drawY = y;
	}

	public boolean isAt(int cx, int cy) {
		return x == cx && y == cy;
	}

	public Pose getPose() {
		if (crushed)
			return Pose.CRUSHED;
		if (jumpLeft > 0)
			return Pose.JUMP;
		if (!isOnGround())
			return Pose.FALL;
		if (drawX != x || System.currentTimeMillis() - lastMoveTime < WALK_POSE_MS)
			return Pose.WALK;
		return Pose.IDLE;
	}

	/**
	 * DodgerSprite(팀원 코드)가 아직 숫자 상태(0 서있음, 1 걷기, 2 점프, 3 떨어짐, 4 깔림)를 쓰기 때문에
	 * 남겨 둔 연결용 메서드. DodgerSprite를 Pose로 바꾸면 지운다.
	 */
	public int getState() {
		return getPose().ordinal();
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public double getDrawX() {
		return drawX;
	}

	public double getDrawY() {
		return drawY;
	}

	public boolean isCrushed() {
		return crushed;
	}
}
