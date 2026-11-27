package threed;

// 게임에서 일어난 일을 알려 받는 쪽 (효과음, 기록 등)
// Game3D는 "무슨 일이 일어났는지"만 알리고, 그때 무엇을 할지는 이 인터페이스를 구현한 쪽이 정한다.
public interface Game3DListener
{
	// 블록이 실제로 한 칸 움직였을 때 (막혀서 못 움직이면 부르지 않음)
	void blockMoved();

	// 블록이 실제로 회전했을 때
	void blockRotated();

	// 블록이 보드에 고정됐을 때
	void blockLocked();

	// 꽉 찬 층이 지워졌을 때 (count는 1 이상)
	void layersCleared(int count);

	// 새 블록을 놓을 수 없어 게임이 끝났을 때
	void gameOver();

	// 처음부터 다시 시작할 때
	void gameRestarted();
}
