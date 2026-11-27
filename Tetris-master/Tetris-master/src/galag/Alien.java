package galag;

/**
 * 외계인 한 마리. 전에는 alienX, alienY, alive, isDive 배열 4개를 같은 번호로 맞춰 썼다(Data Clumps).
 */
final class Alien
{
	static final int WIDTH = 20;
	static final int HEIGHT = 16;
	private static final int COLUMNS = 6; // 한 줄에 6마리
	private static final int LEFT = 30; // 대열 왼쪽 끝 x
	private static final int TOP = 16; // 대열 맨 윗줄 y
	private static final int GAP_X = 32; // 가로 간격
	private static final int GAP_Y = 24; // 세로 간격

	int x;
	int y;
	boolean alive;
	boolean diving; // 대열을 벗어나 아래로 내려오는 중

	/** index번째 자리(왼쪽 위부터 6마리씩 두 줄)로 돌려놓고 되살린다 */
	void placeInFormation(int index)
	{
		int column = index % COLUMNS;
		int line = index / COLUMNS;
		x = LEFT + column * GAP_X;
		y = TOP + line * GAP_Y;
		alive = true;
		diving = false;
	}

	/** 총알 사각형(왼쪽 위 bx, by, 크기 w × h)과 겹치는가 */
	boolean overlaps(int bx, int by, int w, int h)
	{
		return bx < x + WIDTH && x < bx + w && by < y + HEIGHT && y < by + h;
	}
}
