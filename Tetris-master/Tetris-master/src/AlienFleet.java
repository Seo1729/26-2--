import java.awt.Graphics;
import java.util.ArrayList;

/**
 * 갤러그 협동 모드에서 화면 상단에 줄지어 있는 외계인 편대.
 * 외계인들을 격자로 배치하고, 편대 전체를 좌우로 흔들며 이동시킨다.
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

	/** 편대에 속한 외계인 목록 */
	private final ArrayList<Alien> aliens = new ArrayList<Alien>();

	/** 처음 위치에서 편대가 가로로 벗어날 수 있는 최대 거리(픽셀) */
	private final int maxOffset;

	/** 처음 위치 기준 현재 편대의 가로 이동량(픽셀) */
	private int offset;

	/** 현재 이동 방향. 1이면 오른쪽, -1이면 왼쪽 */
	private int direction = 1;

	/**
	 * 영역 가운데 위쪽에 외계인들을 격자로 배치한다.
	 *
	 * @param areaWidth 편대가 움직일 영역의 너비(픽셀)
	 */
	public AlienFleet(int areaWidth)
	{
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
	 * 편대 전체를 한 칸(1픽셀) 이동시킨다. 벽에 닿으면 방향을 바꾼다.
	 */
	public void update()
	{
		// 다음 이동으로 범위를 벗어나면 반대 방향으로 돌아선다
		if (offset + direction > maxOffset || offset + direction < -maxOffset)
			direction = -direction;

		// 편대 이동량을 기록하고 모든 외계인을 같은 방향으로 옮긴다
		offset += direction;
		for (int i = 0; i < aliens.size(); i++)
			aliens.get(i).moveBy(direction);
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
