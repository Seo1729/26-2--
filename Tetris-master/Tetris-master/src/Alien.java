import java.awt.Color;
import java.awt.Graphics;

/**
 * 갤러그 협동 모드에서 화면 상단 편대에 속한 외계인 한 마리.
 * 자기 위치를 관리하고 자기 자신을 그린다.
 */
public class Alien
{
	/** 외계인 가로 크기(픽셀). 테트리스 한 칸(24px) 안에 들어가게 한다. */
	public static final int WIDTH = 20;

	/** 외계인 세로 크기(픽셀) */
	private static final int HEIGHT = 16;

	/** 몸체 색. 테트리스 블록 색과 겹치지 않는 분홍색을 쓴다. */
	private static final Color BODY_COLOR = new Color(255, 105, 180);

	/** 외계인 왼쪽 위 x좌표(갤러그 레이어 기준 픽셀) */
	private int x;

	/** 외계인 왼쪽 위 y좌표(갤러그 레이어 기준 픽셀) */
	private int y;

	/**
	 * 지정한 위치에 외계인을 만든다.
	 *
	 * @param x 왼쪽 위 x좌표(픽셀)
	 * @param y 왼쪽 위 y좌표(픽셀)
	 */
	public Alien(int x, int y)
	{
		this.x = x;
		this.y = y;
	}

	/**
	 * 외계인을 가로로 지정한 거리만큼 옮긴다. 편대가 함께 좌우로 흔들릴 때 쓴다.
	 *
	 * @param dx 옮길 거리(픽셀). 양수면 오른쪽, 음수면 왼쪽
	 */
	public void moveBy(int dx)
	{
		x += dx;
	}

	/**
	 * 외계인을 분홍색 몸체와 검은 눈 두 개로 그린다.
	 *
	 * @param g 그리기에 사용할 그래픽 객체
	 */
	public void draw(Graphics g)
	{
		// 둥근 몸체
		g.setColor(BODY_COLOR);
		g.fillOval(x, y, WIDTH, HEIGHT);

		// 몸체 위쪽 양옆에 눈을 찍어 외계인처럼 보이게 한다
		g.setColor(Color.BLACK);
		g.fillRect(x + 5, y + 5, 3, 3);
		g.fillRect(x + WIDTH - 8, y + 5, 3, 3);
	}
}
