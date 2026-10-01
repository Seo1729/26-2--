import java.awt.Color;
import java.awt.Graphics;

/**
 * 갤러그 협동 모드에서 전투기가 쏘는 총알.
 * 위쪽으로 일정 속도로 날아가며, 자기 위치를 관리하고 자기 자신을 그린다.
 */
public class Bullet
{
	/** 총알 가로 크기(픽셀) */
	private static final int WIDTH = 2;

	/** 총알 세로 크기(픽셀) */
	private static final int HEIGHT = 8;

	/** 한 번 이동할 때 위로 올라가는 거리(픽셀) */
	private static final int SPEED = 10;

	/** 총알 왼쪽 위 x좌표(갤러그 레이어 기준 픽셀). 위로만 날아가므로 변하지 않는다. */
	private final int x;

	/** 총알 왼쪽 위 y좌표(갤러그 레이어 기준 픽셀) */
	private int y;

	/**
	 * 발사 지점 바로 위에 총알을 만든다.
	 *
	 * @param centerX 총알 가운데가 놓일 x좌표(픽셀)
	 * @param bottomY 총알 아래쪽 끝이 놓일 y좌표(픽셀)
	 */
	public Bullet(int centerX, int bottomY)
	{
		// 발사 지점 가운데에 맞추고, 기체와 겹치지 않게 그 위에 놓는다
		x = centerX - WIDTH / 2;
		y = bottomY - HEIGHT;
	}

	/**
	 * 총알을 위로 한 번 이동시킨다.
	 */
	public void move()
	{
		// 화면 좌표는 위로 갈수록 작아지므로 y를 줄인다
		y -= SPEED;
	}

	/**
	 * 총알이 영역 위쪽 밖으로 완전히 나갔는지 알려 준다.
	 *
	 * @return 아래쪽 끝까지 영역 위로 벗어났으면 true
	 */
	public boolean isOutOfArea()
	{
		// 아래쪽 끝이 0보다 위에 있으면 더 이상 화면에 보이지 않는다
		return y + HEIGHT < 0;
	}

	/**
	 * 총알을 노란 막대로 그린다.
	 *
	 * @param g 그리기에 사용할 그래픽 객체
	 */
	public void draw(Graphics g)
	{
		// 검은 배경과 하늘색 전투기 위에서 잘 보이도록 노란색으로 그린다
		g.setColor(Color.YELLOW);
		g.fillRect(x, y, WIDTH, HEIGHT);
	}
}
