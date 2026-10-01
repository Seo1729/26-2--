import java.awt.Graphics;
import java.util.ArrayList;

/**
 * 갤러그 협동 모드에서 화면 상단에 줄지어 있는 외계인 편대.
 * 외계인들을 격자로 배치해 좌우로 흔들고, 한 마리씩 급강하시키며, 총알에 맞은 외계인을 없앤다.
 */
public class AlienFleet
{
	/** 편대의 줄 수 */
	private static final int ROWS = 2;

	/** 한 줄에 있는 외계인 수 */
	private static final int COLS = 6;

	/** 같은 줄에서 이웃한 외계인 사이의 가로 간격(왼쪽 끝 기준, 픽셀) */
	private static final int GAP_X = 32;

	/** 이웃한 줄 사이의 세로 간격(위쪽 끝 기준, 픽셀) */
	private static final int GAP_Y = 24;

	/** 첫 줄의 y좌표(픽셀). 화면 맨 위에 붙지 않게 조금 띄운다. */
	private static final int TOP_Y = 16;

	/** 급강하 간격(타이머 주기 횟수). 100 x 30ms = 약 3초 */
	private static final int DIVE_INTERVAL = 100;

	/** 편대에 속한 외계인 목록(급강하 중인 외계인도 포함) */
	private final ArrayList<Alien> aliens = new ArrayList<Alien>();

	/** 외계인이 움직이는 영역의 높이(픽셀). 급강하한 외계인이 바닥을 벗어났는지 판단할 때 쓴다. */
	private final int areaHeight;

	/** 급강하 중인 외계인이 없을 때부터 센 타이머 주기 횟수 */
	private int diveTimer;

	/** 처음 위치에서 편대가 가로로 벗어날 수 있는 최대 거리(픽셀) */
	private final int maxOffset;

	/** 처음 위치 기준 현재 편대의 가로 이동량(픽셀) */
	private int offset;

	/** 현재 이동 방향. 1이면 오른쪽, -1이면 왼쪽 */
	private int direction = 1;

	/**
	 * 영역 가운데 위쪽에 외계인들을 격자로 배치한다.
	 *
	 * @param areaWidth  편대가 움직일 영역의 너비(픽셀)
	 * @param areaHeight 편대가 움직일 영역의 높이(픽셀)
	 */
	public AlienFleet(int areaWidth, int areaHeight)
	{
		this.areaHeight = areaHeight;

		// 편대 전체 너비를 구해 가운데 정렬 시작점을 정한다
		int fleetWidth = (COLS - 1) * GAP_X + Alien.WIDTH;
		int startX = (areaWidth - fleetWidth) / 2;

		// 가운데에서 양쪽 벽까지 남은 거리만큼만 흔들리게 한다
		maxOffset = startX;

		// 줄과 칸 순서대로 외계인을 배치한다
		for (int row = 0; row < ROWS; row++)
			for (int col = 0; col < COLS; col++)
				aliens.add(new Alien(startX + col * GAP_X, TOP_Y + row * GAP_Y));
	}

	/**
	 * 편대를 한 주기만큼 갱신한다. 편대를 좌우로 흔들고, 급강하 중인 외계인을 내리고,
	 * 때가 되면 새로 급강하할 외계인을 고른다.
	 */
	public void update()
	{
		// 다음 이동으로 범위를 벗어나면 반대 방향으로 돌아선다
		if (offset + direction > maxOffset || offset + direction < -maxOffset)
			direction = -direction;
		offset += direction;

		// 편대에 있는 외계인은 좌우로 흔들고, 급강하 중인 외계인은 아래로 내린다
		// (바닥을 벗어난 외계인을 지워도 인덱스가 꼬이지 않게 뒤에서부터 순회)
		for (int i = aliens.size() - 1; i >= 0; i--)
		{
			Alien alien = aliens.get(i);
			if (alien.isDiving())
			{
				alien.dive();
				// 바닥 밖으로 나간 외계인은 더 이상 필요 없으므로 지운다
				if (alien.isOutOfArea(areaHeight))
					aliens.remove(i);
			}
			else
				alien.moveBy(direction);
		}

		// 급강하 중인 외계인이 없을 때만 시간을 세고, 간격이 차면 한 마리를 급강하시킨다
		if (!hasDiver())
		{
			diveTimer++;
			if (diveTimer >= DIVE_INTERVAL && !aliens.isEmpty())
			{
				aliens.get((int) (Math.random() * aliens.size())).startDive();
				diveTimer = 0;
			}
		}
	}

	/**
	 * 총알이 외계인에 맞았는지 확인하고, 맞은 외계인은 편대에서 없앤다.
	 *
	 * @param bullet 판정할 총알
	 * @return 외계인 하나를 맞혔으면 true, 아무도 맞히지 않았으면 false
	 */
	public boolean hit(Bullet bullet)
	{
		// 총알과 영역이 겹치는 첫 외계인을 찾아 없앤다(총알 하나는 한 마리만 맞힌다)
		for (int i = 0; i < aliens.size(); i++)
		{
			if (aliens.get(i).getBounds().intersects(bullet.getBounds()))
			{
				aliens.remove(i);
				return true;
			}
		}
		return false;
	}

	/**
	 * 지금 급강하 중인 외계인이 있는지 알려 준다.
	 *
	 * @return 급강하 중인 외계인이 하나라도 있으면 true
	 */
	private boolean hasDiver()
	{
		// 한 마리씩만 급강하시키기 위해 현재 급강하 중인 외계인을 찾는다
		for (int i = 0; i < aliens.size(); i++)
			if (aliens.get(i).isDiving())
				return true;
		return false;
	}

	/**
	 * 편대의 모든 외계인을 그린다.
	 *
	 * @param g 그리기에 사용할 그래픽 객체
	 */
	public void draw(Graphics g)
	{
		// 편대에 남아 있는 외계인을 하나씩 그린다
		for (int i = 0; i < aliens.size(); i++)
			aliens.get(i).draw(g);
	}
}
