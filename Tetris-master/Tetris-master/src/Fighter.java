import java.awt.Color;
import java.awt.Graphics;

/**
 * 갤러그 협동 모드에서 전투기 플레이어가 조종하는 전투기.
 * 전투기의 위치를 관리하고 자기 자신을 그린다.
 */
public class Fighter
{
	/** 전투기 가로 크기(픽셀). 테트리스 한 칸 너비(24px)와 맞춘다. */
	private static final int WIDTH = 24;

	/** 전투기 세로 크기(픽셀) */
	private static final int HEIGHT = 20;

	/** 한 번 이동할 때 움직이는 거리(픽셀) */
	private static final int SPEED = 4;

	/** 전투기 왼쪽 위 x좌표(갤러그 레이어 기준 픽셀) */
	private int x;

	/** 전투기 왼쪽 위 y좌표(갤러그 레이어 기준 픽셀). 하단에 고정된다. */
	private final int y;

	/** x좌표의 최댓값. 이 값을 넘으면 기체가 영역 오른쪽 밖으로 나간다. */
	private final int maxX;

	/**
	 * 전투기를 영역 하단 가운데에 배치한다.
	 *
	 * @param areaWidth  전투기가 움직일 영역의 너비(픽셀)
	 * @param areaHeight 전투기가 움직일 영역의 높이(픽셀)
	 */
	public Fighter(int areaWidth, int areaHeight)
	{
		// 가운데에서 시작해야 좌우 이동 여유가 같다
		x = (areaWidth - WIDTH) / 2;
		// 맨 아래에 붙여 쌓인 블록 위에 겹쳐 보이게 한다
		y = areaHeight - HEIGHT;
		// 기체 오른쪽 끝이 영역 오른쪽 끝에 닿는 위치까지만 허용한다
		maxX = areaWidth - WIDTH;
	}

	/**
	 * 전투기를 왼쪽으로 한 번 이동시킨다. 영역 왼쪽 끝에서는 더 가지 않는다.
	 */
	public void moveLeft()
	{
		// 왼쪽 벽(0)을 넘지 않도록 막는다
		x = Math.max(0, x - SPEED);
	}

	/**
	 * 전투기를 오른쪽으로 한 번 이동시킨다. 영역 오른쪽 끝에서는 더 가지 않는다.
	 */
	public void moveRight()
	{
		// 오른쪽 벽(maxX)을 넘지 않도록 막는다
		x = Math.min(maxX, x + SPEED);
	}

	/**
	 * 전투기를 위쪽이 뾰족한 삼각형 기체로 그린다.
	 *
	 * @param g 그리기에 사용할 그래픽 객체
	 */
	public void draw(Graphics g)
	{
		// 블록 색(회색, 원색)과 구분되도록 하늘색으로 그린다
		g.setColor(Color.CYAN);
		g.fillPolygon(new int[] { x + WIDTH / 2, x + WIDTH, x },
				new int[] { y, y + HEIGHT, y + HEIGHT }, 3);
	}
}
