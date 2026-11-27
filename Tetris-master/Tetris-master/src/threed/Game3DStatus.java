package threed;

// 게임 상태를 "보여 주는" 쪽(3D 화면, 오른쪽 정보창)이 쓰는 조회 기능만 모은 인터페이스
// 화면은 게임을 움직일 수 없고 읽기만 한다. (ISP)
public interface Game3DStatus
{
	Game3DScene getScene();

	Block3D.Type getNextBlockType();

	long getScore();

	long getClearedLayerCount();

	int getLockedBlockCount();

	Game3D.State getState();

	boolean isRunning();

	// 떨어지는 블록의 복사본 (없으면 null)
	Block3D getActiveBlock();
}
