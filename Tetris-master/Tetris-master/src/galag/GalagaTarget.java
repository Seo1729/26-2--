package galag;

/**
 * 갤러그 층(GalagaLayer)이 아래의 테트리스 보드에게 요구하는 기능만 모은 인터페이스.
 *
 * 전에는 GalagaLayer가 GamePanel이라는 구체 클래스를 직접 들고 그 필드(paused)까지 읽었다
 * (Inappropriate Intimacy, DIP 위반). 이제 갤러그는 이 약속만 알고, GamePanel이 이 인터페이스를 구현한다.
 * 그래서 갤러그 묶음(galag)은 테트리스 쪽 클래스를 하나도 import하지 않는다.
 */
public interface GalagaTarget
{
	/** 테트리스 게임이 아직 진행 중인가 */
	boolean isRunning();

	/** 설정 화면을 보는 중이라 멈춰 있는가 */
	boolean isPaused();

	/** 화면 기준 (row, col) 칸이 바닥이거나 블록으로 막혀 있는가 */
	boolean isBlocked(int row, int col);

	/** 화면 기준 (row, col)이 비어 있으면 회색 블록을 놓는다 */
	void placeGarbage(int row, int col);

	/** 게임을 끝내고 가운데에 message를 보여 준다 (CLEAR, GAME OVER) */
	void endGame(String message);
}
