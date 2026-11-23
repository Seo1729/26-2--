package dodge;

/**
 * 고양이(Dodger)가 움직일 때 "이 칸이 막혔나?"만 묻는 통로.
 * Dodger는 GamePanel이나 TetrisPlayer를 몰라도 되고, 테스트에서는 람다 하나로 대신할 수 있다.
 */
@FunctionalInterface
public interface CollisionMap {

	/** (x, y) 칸이 벽, 쌓인 블록, 떨어지는 블록 중 하나로 막혀 있으면 true */
	boolean isBlocked(int x, int y);
}
