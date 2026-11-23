package threed;

// 게임을 "조작"하는 쪽(키 입력, 창)이 쓰는 기능만 모은 인터페이스
// 키 입력 처리는 점수·장면 같은 조회 기능을 알 필요가 없으므로 이것만 받는다. (ISP)
public interface Game3DControl
{
	void start();

	void stop();

	void restart();

	boolean move(int dx, int dy);

	boolean rotate(Block3D.Axis axis);

	boolean hardDrop();

	// 키가 지금 동작해도 되는지(게임 중 / 게임 오버) 판단할 때 쓴다
	Game3D.State getState();
}
