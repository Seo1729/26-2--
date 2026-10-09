import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

/**
 * 사용 중인 아이템의 남은 시간(쿨타임)을 보여 주는 30x30 아이콘.
 * 아이콘 위를 시계 방향으로 검게 덮어 가면서 남은 초를 숫자로 표시하고, 시간이 끝나면 효과를 되돌린다.
 * 원본에서는 GamePanel 안의 내부 클래스(UsingItemLabel)였는데, 따로 파일로 분리했다.
 */
public class CoolTimeLabel extends JLabel implements Runnable
{
	/** 아이콘 크기(픽셀) */
	private static final int SIZE = 30;

	/** 한 번 그릴 때 검은 영역이 늘어나는 거리(픽셀) */
	private static final int SPEED = 1;

	/** 표시할 아이템 아이콘 */
	private ImageIcon icon;

	/** 효과를 받고 있는 게임 화면. 쿨타임이 끝나면 여기서 효과를 되돌린다. */
	private GamePanel target;

	/** 검은 다각형의 꼭짓점 x좌표 7개(위 가운데에서 시작해 시계 방향으로 돈다) */
	private int x1, x2, x3, x4, x5, x6, x7;

	/** 검은 다각형의 꼭짓점 y좌표 7개 */
	private int y1, y2, y3, y4, y5, y6, y7;

	/** 지금 몇 번째 변을 덮고 있는지(1: 위 오른쪽, 2: 오른쪽, 3: 아래, 4: 왼쪽, 5: 위 왼쪽) */
	private int step = 1;

	/** 화면에 표시할 남은 시간(초) */
	private int remainSeconds = 10;

	/** 쿨타임이 진행 중인지 여부 */
	private boolean running = true;

	/**
	 * 쿨타임 아이콘을 만들고 바로 시간을 재기 시작한다.
	 *
	 * @param icon   표시할 아이템 아이콘
	 * @param target 효과를 받고 있는 게임 화면
	 */
	public CoolTimeLabel(ImageIcon icon, GamePanel target)
	{
		this.icon = icon;
		this.target = target;
		setSize(SIZE, SIZE);

		// 다각형은 위쪽 가운데에서 시작한다
		x1 = SIZE / 2;
		x2 = SIZE / 2;
		x3 = SIZE;
		x4 = SIZE;
		x5 = 0;
		x6 = SIZE / 2;
		x7 = 0;

		y1 = 0;
		y2 = 0;
		y3 = 0;
		y4 = SIZE;
		y5 = SIZE;
		y6 = SIZE / 2;
		y7 = 0;

		Thread coolTimeThread = new Thread(this);
		coolTimeThread.start();
	}

	/**
	 * 0.08초마다 검은 영역을 조금씩 넓히고, 한 바퀴를 다 돌면 아이템 효과를 되돌린다.
	 */
	public void run()
	{
		int count = 0;

		while (running)
		{
			// 위 오른쪽 → 오른쪽 → 아래 → 왼쪽 → 위 왼쪽 순서로 한 변씩 덮어 간다
			if (x2 < SIZE)
			{
				x2 = x2 + SPEED;
			}
			else
			{
				step = 2;
				if (y3 < SIZE)
				{
					y3 = y3 + SPEED;
				}
				else
				{
					step = 3;
					if (x4 > 0)
					{
						x4 = x4 - SPEED;
					}
					else
					{
						step = 4;
						if (y5 > 0)
						{
							y5 = y5 - SPEED;
						}
						else
						{
							step = 5;
							if (x7 < SIZE / 2)
							{
								x7 = x7 + SPEED;
							}
							else
							{
								running = false;
							}
						}
					}
				}
			}
			count++;
			repaint();

			// 12번 그릴 때마다 남은 시간을 1초 줄인다
			if (count % 12 == 0)
			{
				remainSeconds--;
			}

			try
			{
				Thread.sleep(80);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		// 쿨타임이 끝났으므로 바꿔 두었던 속도를 원래대로 돌린다
		if (icon == ImageSource.item_fast)
		{
			target.gameSpeed = target.gameSpeed + 200;
		}
		else if (icon == ImageSource.item_slow)
		{
			target.gameSpeed = target.gameSpeed - 200;
		}

		// 목록에서 나를 빼고 남은 쿨타임 아이콘들을 다시 왼쪽부터 놓는다
		target.usingItemPanel.removeAll();
		target.coolTimeList.remove(this);
		for (int i = 0; i < target.coolTimeList.size(); i++)
		{
			target.usingItemPanel.add(target.coolTimeList.get(i));
			target.coolTimeList.get(i).setLocation(i * 30, 0);
		}
		target.usingItemPanel.repaint();
	}

	/**
	 * 아이콘을 그리고, 그 위에 지나간 시간만큼 검은 다각형을 덮은 뒤 남은 초를 쓴다.
	 *
	 * @param g 그리기에 사용할 그래픽 객체
	 */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		g.drawImage(icon.getImage(), 0, 0, this);
		g.setColor(Color.BLACK);

		// 지금 덮고 있는 변에 따라 꼭짓점 개수가 늘어난다
		if (step == 1)
		{
			g.fillPolygon(new int[] { x1, x2, x6 }, new int[] { y1, y2, y6 }, 3);
		}
		else if (step == 2)
		{
			g.fillPolygon(new int[] { x1, x2, x3, x6 }, new int[] { y1, y2, y3, y6 }, 4);
		}
		else if (step == 3)
		{
			g.fillPolygon(new int[] { x1, x2, x3, x4, x6 }, new int[] { y1, y2, y3, y4, y6 }, 5);
		}
		else if (step == 4)
		{
			g.fillPolygon(new int[] { x1, x2, x3, x4, x5, x6 }, new int[] { y1, y2, y3, y4, y5, y6 }, 6);
		}
		else
		{
			g.fillPolygon(new int[] { x1, x2, x3, x4, x5, x7, x6 }, new int[] { y1, y2, y3, y4, y5, y7, y6 }, 7);
		}

		// 남은 초를 흰 글씨로 쓴다
		g.setColor(Color.WHITE);
		g.setFont(new Font("Verdana", Font.BOLD, 20));
		g.drawString(Integer.toString(remainSeconds), 7, 22);
	}
}
