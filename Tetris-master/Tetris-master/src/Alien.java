import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

/**
 * 갤러그 협동 모드에서 화면 상단 편대에 속한 외계인 한 마리.
 * 편대에 머물거나 아래로 급강하하며, 자기 위치를 관리하고 자기 자신을 그린다.
 */
public class Alien
{
	/** 외계인 가로 크기(픽셀). 테트리스 한 칸(24px) 안에 들어가게 한다. */
	public static final int WIDTH = 20;

	/** 외계인 세로 크기(픽셀) */
	private static final int HEIGHT = 16;

	/** 급강하할 때 한 번에 내려가는 거리(픽셀) */
	private static final int DIVE_SPEED = 2;

	/** 몸체 색. 테트리스 블록 색과 겹치지 않는 분홍색을 쓴다. */
	private static final Color BODY_COLOR = new Color(255, 105, 180);

	/** 외계인 왼쪽 위 x좌표(갤러그 레이어 기준 픽셀) */
	private int x;

	/** 외계인 왼쪽 위 y좌표(갤러그 레이어 기준 픽셀) */
	private int y;

	/** 편대를 벗어나 아래로 급강하하는 중인지 여부 */
	private boolean diving;

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
	 * 편대를 벗어나 급강하를 시작한다.
	 */
	public void startDive()
	{
		diving = true;
	}

	/**
	 * 급강하 중인지 알려 준다.
	 *
	 * @return 급강하 중이면 true, 편대에 있으면 false
	 */
	public boolean isDiving()
	{
		return diving;
	}

	/**
	 * 급강하 중인 외계인을 아래로 한 번 이동시킨다.
	 */
	public void dive()
	{
		// 화면 좌표는 아래로 갈수록 커지므로 y를 늘린다
		y += DIVE_SPEED;
	}

	/**
	 * 외계인이 영역 아래쪽 밖으로 완전히 나갔는지 알려 준다.
	 *
	 * @param areaHeight 외계인이 움직이는 영역의 높이(픽셀)
	 * @return 위쪽 끝까지 영역 아래로 벗어났으면 true
	 */
	public boolean isOutOfArea(int areaHeight)
	{
		// 위쪽 끝이 영역 높이보다 아래에 있으면 더 이상 보이지 않는다
		return y > areaHeight;
	}

	/**
	 * 충돌 판정에 쓸 외계인의 사각 영역을 돌려준다.
	 *
	 * @return 현재 위치와 크기를 담은 사각형
	 */
	public Rectangle getBounds()
	{
		return new Rectangle(x, y, WIDTH, HEIGHT);
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
