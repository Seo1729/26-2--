package origin;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JLabel;

/**
 * 시간이 걸리는 아이템(fast, slow, blackout)을 쓰면 화면 위 칸에 나오는 쿨타임 그림.
 * 아이콘 위를 검은 다각형이 시계 방향으로 덮어 가고, 다 덮으면(약 8초) 사라지면서 속도 아이템 효과를 되돌린다.
 * 전에는 GamePanel 안의 내부 클래스(UsingItemLabel)였고, 꼭짓점 좌표를 x1~x7, y1~y7 변수 14개로 들고 있었다(Data Clumps).
 */
final class CooldownLabel extends JLabel implements Runnable
{
	private static final int SIZE = 30; // 그림 한 칸 크기(px)
	private static final int FRAME_DELAY = 80; // 한 단계 그리는 간격(ms)
	private static final int FRAMES_PER_COUNT = 12; // 숫자가 1 줄어드는 단계 수
	private static final int SPEED = 1; // 한 단계마다 꼭짓점이 움직이는 거리(px)

	// 다각형 꼭짓점 7개. 0~4는 테두리를 따라 움직이는 점, 5는 가운데, 6은 마지막에 위쪽 가운데로 돌아오는 점
	private static final int CENTER = 5;
	// 단계(phase)별로 앞에서부터 phase + 1개 점을 이 순서로 잇고 마지막에 가운데 점을 잇는다
	private static final int[] ORDER = { 0, 1, 2, 3, 4, 6 };

	private final transient GamePanel target;
	private final ItemType type;
	private final int[] px = { SIZE / 2, SIZE / 2, SIZE, SIZE, 0, SIZE / 2, 0 };
	private final int[] py = { 0, 0, 0, SIZE, SIZE, SIZE / 2, 0 };
	private int phase = 1;
	private int count = 10; // 남은 시간(초 비슷한 숫자)으로 그림 위에 보여 준다

	CooldownLabel(GamePanel target, ItemType type)
	{
		this.target = target;
		this.type = type;
		setSize(SIZE, SIZE);
		new Thread(this).start();
	}

	/** 쿨타임이 끝났을 때 되돌릴 아이템 종류 */
	ItemType getType()
	{
		return type;
	}

	@Override
	public void run()
	{
		int frame = 0;
		boolean running = true;
		while (running)
		{
			running = advance();
			frame++;
			repaint();

			if (frame % FRAMES_PER_COUNT == 0)
				count--;

			try
			{
				Thread.sleep(FRAME_DELAY);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		target.finishCooldown(this);
	}

	/**
	 * 덮는 다각형을 한 단계 키운다. 위쪽 → 오른쪽 → 아래쪽 → 왼쪽 → 위쪽 가운데 순서로 꼭짓점 하나씩 움직인다.
	 * 다 덮었으면 false
	 */
	private boolean advance()
	{
		if (px[1] < SIZE)
		{
			px[1] += SPEED;
			return true;
		}
		phase = 2;
		if (py[2] < SIZE)
		{
			py[2] += SPEED;
			return true;
		}
		phase = 3;
		if (px[3] > 0)
		{
			px[3] -= SPEED;
			return true;
		}
		phase = 4;
		if (py[4] > 0)
		{
			py[4] -= SPEED;
			return true;
		}
		phase = 5;
		if (px[6] < SIZE / 2)
		{
			px[6] += SPEED;
			return true;
		}
		return false;
	}

	@Override
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.drawImage(type.getItemIcon().getImage(), 0, 0, this);
		g.setColor(Color.BLACK);

		int n = phase + 2;
		int[] xs = new int[n];
		int[] ys = new int[n];
		for (int i = 0; i < n - 1; i++)
		{
			xs[i] = px[ORDER[i]];
			ys[i] = py[ORDER[i]];
		}
		xs[n - 1] = px[CENTER];
		ys[n - 1] = py[CENTER];
		g.fillPolygon(xs, ys, n);

		g.setColor(Color.WHITE);
		g.setFont(new Font("Verdana", Font.BOLD, 20));
		g.drawString(Integer.toString(count), 7, 22);
	}
}
