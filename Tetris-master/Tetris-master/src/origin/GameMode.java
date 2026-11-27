package origin;

/**
 * GameFrame 안에서 GamePanel을 쓰는 2D 모드.
 * 전에는 GameFrame.gameMode라는 static int 전역 변수에 1, 3을 넣고 곳곳에서 숫자로 비교했다(Magic Number, Global Data).
 * 이제 모드는 GamePanel을 만들 때 넘겨주고, 숫자는 HP 규칙(HpSystem)에 넘길 때만 쓴다.
 */
public enum GameMode
{
	NORMAL(1), // 일반 모드
	GALAGA(3); // 갤러그 협동 모드

	private final int number;

	GameMode(int number)
	{
		this.number = number;
	}

	/** 예전 모드 번호 (HpSystem.forMode가 받는 값) */
	public int getNumber()
	{
		return number;
	}
}
