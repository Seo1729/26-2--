package dodge;

// P2 고양이 (똥피하기 하는 캐릭터)
// 상태 번호: 0 서있음, 1 걷기, 2 점프, 3 떨어짐, 4 깔림
public class Dodger {

	GamePanel game;

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

	public Dodger(GamePanel g) {
		game = g;
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

	// 매 프레임마다 그림 위치를 실제 칸 쪽으로 조금씩 옮김
	public void updateDraw(int elapsedMs) {
		drawX = slide(drawX, x, elapsedMs / 90.0);
		drawY = slide(drawY, y, elapsedMs / 80.0);
	}

	static double slide(double current, int target, double step) {
		double diff = target - current;
		double distance = Math.abs(diff);
		if (distance > 3)
			return target;
		double move = step * Math.max(1, distance);
		if (distance <= move)
			return target;
		return current + Math.signum(diff) * move;
	}

	void stepped() {
		walkFrame ^= 1;
		lastMoveTime = System.currentTimeMillis();
	}

	boolean isSolid(int cx, int cy) {
		return !game.isFree(cx, cy) || game.occupies(cx, cy);
	}

	public boolean isOnGround() {
		return isSolid(x, y - 1);
	}

	public void moveLeft() {
		if (crushed)
			return;
		facingRight = false;
		if (!isSolid(x - 1, y)) {
			--x;
			stepped();
		}
	}

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
			jumpLeft = 2; // 2칸까지 점프
	}

	// 점프중이면 올라가고 아니면 떨어짐
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
//		System.out.println("dodger " + x + ", " + y);
	}

	// 블록이랑 겹치면 위로 올려줌
	public void resolveOverlap() {
		while (y < 22 - 1 && !game.isFree(x, y))
			++y;
	}

	public void crush() {
		crushed = true;
		drawX = x;
		drawY = y;
	}

	public int getState() {
		if (crushed)
			return 4;
		if (jumpLeft > 0)
			return 2;
		if (!isOnGround())
			return 3;
		if (drawX != x || System.currentTimeMillis() - lastMoveTime < 200)
			return 1;
		return 0;
	}
}
