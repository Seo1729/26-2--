package galag;

/** 전투기 총알 하나. 전에는 bx, by 리스트 2개를 같은 번호로 맞춰 썼다(Data Clumps) */
final class Bullet
{
	static final int WIDTH = 2;
	static final int HEIGHT = 8;
	static final int SPEED = 10; // 한 번에 올라가는 거리(px)

	int x;
	int y;

	Bullet(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
}
