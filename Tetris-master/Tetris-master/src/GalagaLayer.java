import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;

// 갤러그 협동 모드 (테트리스 영역 위에 겹쳐서 그림)
public class GalagaLayer extends JPanel
{
	GamePanel board;
	Timer timer;
	KeyListener keyListener;

	// 전투기
	int fighterX;
	int fighterY;

	// 총알
	ArrayList<Integer> bx = new ArrayList<Integer>();
	ArrayList<Integer> by = new ArrayList<Integer>();
	int cool = 0;

	// 외계인 (12마리)
	int[] alienX = new int[12];
	int[] alienY = new int[12];
	boolean[] alive = new boolean[12];
	boolean[] isDive = new boolean[12];
	int diveSpeed;
	int diveTime;
	int diveTimer = 0;
	int offset = 0;
	int dir = 1;

	int wave = 1;
	int score = 0; // 점수 표시 하려다가 안함

	// 키
	boolean left = false;
	boolean right = false;
	boolean shoot = false;

	public GalagaLayer(int x, int y, int width, int height, GamePanel b)
	{
		setOpaque(false);
		setBounds(x, y, width, height);
		board = b;

		// 전투기 위치 (아래 가운데)
		fighterX = (240 - 24) / 2;
		fighterY = 480 - 20;

		// 외계인 배치
		for (int i = 0; i < 12; i++)
		{
			alienX[i] = 30 + (i % 6) * 32;
			alienY[i] = 16 + (i / 6) * 24;
			alive[i] = true;
			isDive[i] = false;
		}
		diveTime = 100;
		diveSpeed = 2;

		keyListener = new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_A)
					left = true;
				else if (e.getKeyCode() == KeyEvent.VK_D)
					right = true;
				else if (e.getKeyCode() == KeyEvent.VK_W)
					shoot = true;
			}

			public void keyReleased(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_A)
					left = false;
				else if (e.getKeyCode() == KeyEvent.VK_D)
					right = false;
				else if (e.getKeyCode() == KeyEvent.VK_W)
					shoot = false;
			}
		};

		timer = new Timer(30, new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// 테트리스 끝나면 같이 멈춤
				if (!board.isRunning())
				{
					timer.stop();
					setVisible(false);
					return;
				}
				update();
				repaint();
			}
		});
		timer.start();
	}

	public KeyListener getKeyListener()
	{
		return keyListener;
	}

	public void update()
	{
		// 외계인 좌우로 흔들기
		if (offset + dir > 30 || offset + dir < -30)
			dir = -dir;
		offset = offset + dir;

		for (int i = 11; i >= 0; i--)
		{
			if (alive[i] == false)
				continue;

			if (isDive[i] == true)
			{
				alienY[i] = alienY[i] + diveSpeed;

				// 바닥이나 블록에 닿았는지
				int col = (alienX[i] + 10) / 24;
				int row = (alienY[i] + 16) / 24;
				if (board.isBlocked(row, col))
				{
					if (row - 1 >= 0)
						board.placeGarbage(row - 1, col);
					alive[i] = false;
				}
			}
			else
			{
				alienX[i] = alienX[i] + dir;
			}
		}

		// 지금 내려오는 애가 없으면 시간 재다가 한마리 내려보냄
		boolean someoneDiving = false;
		for (int i = 0; i < 12; i++)
		{
			if (alive[i] && isDive[i])
				someoneDiving = true;
		}
		if (someoneDiving == false)
		{
			diveTimer++;

			int cnt = 0;
			for (int i = 0; i < 12; i++)
			{
				if (alive[i])
					cnt++;
			}

			if (diveTimer >= diveTime && cnt > 0)
			{
				int pick = (int) (Math.random() * cnt);
				int n = 0;
				for (int i = 0; i < 12; i++)
				{
					if (alive[i])
					{
						if (n == pick)
						{
							isDive[i] = true;
						}
						n++;
					}
				}
				diveTimer = 0;
			}
		}

		// 전투기 이동
		if (left)
		{
			fighterX = fighterX - 4;
			if (fighterX < 0)
				fighterX = 0;
		}
		if (right)
		{
			fighterX = fighterX + 4;
			if (fighterX > 216)
				fighterX = 216;
		}

		// 총 쏘기
		if (cool > 0)
			cool--;
		if (shoot && cool == 0)
		{
			bx.add(fighterX + 12 - 1);
			by.add(fighterY - 8);
			cool = 7;
		}

		// 총알 이동, 맞았는지 확인
		for (int i = bx.size() - 1; i >= 0; i--)
		{
			by.set(i, by.get(i) - 10);

			boolean remove = false;
			if (by.get(i) + 8 < 0)
			{
				remove = true;
			}
			else
			{
				for (int j = 0; j < 12; j++)
				{
					if (alive[j] == false)
						continue;
					// 사각형 겹치는지
					if (bx.get(i) < alienX[j] + 20 && alienX[j] < bx.get(i) + 2
							&& by.get(i) < alienY[j] + 16 && alienY[j] < by.get(i) + 8)
					{
						alive[j] = false;
						remove = true;
						break;
					}
				}
			}

			if (remove)
			{
				bx.remove(i);
				by.remove(i);
			}
		}

		// 다 잡았는지
		int left2 = 0;
		for (int i = 0; i < 12; i++)
		{
			if (alive[i])
				left2++;
		}
		if (left2 == 0)
		{
			if (wave == 3)
			{
				board.endGame("CLEAR");
			}
			else
			{
				wave++;

				// 다음 웨이브 (위에꺼 복사)
				for (int i = 0; i < 12; i++)
				{
					alienX[i] = 30 + (i % 6) * 32;
					alienY[i] = 16 + (i / 6) * 24;
					alive[i] = true;
					isDive[i] = false;
				}
				diveTime = 100 - (wave - 1) * 20;
				diveSpeed = 2 + (wave - 1);
				diveTimer = 0;
				offset = 0;
				dir = 1;
			}
		}
	}

	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		// 외계인
		for (int i = 0; i < 12; i++)
		{
			if (alive[i])
			{
				g.setColor(new Color(255, 105, 180));
				g.fillOval(alienX[i], alienY[i], 20, 16);
				g.setColor(Color.BLACK);
				g.fillRect(alienX[i] + 5, alienY[i] + 5, 3, 3);
				g.fillRect(alienX[i] + 12, alienY[i] + 5, 3, 3);
			}
		}

		// 전투기
		g.setColor(Color.CYAN);
		g.fillPolygon(new int[] { fighterX + 12, fighterX + 24, fighterX },
				new int[] { fighterY, fighterY + 20, fighterY + 20 }, 3);

		// 총알
		g.setColor(Color.YELLOW);
		for (int i = 0; i < bx.size(); i++)
		{
			g.fillRect(bx.get(i), by.get(i), 2, 8);
		}

		g.setColor(Color.WHITE);
		g.setFont(new Font("Verdana", Font.PLAIN, 10));
		g.drawString("WAVE " + wave + "/" + 3, 4, 12);

//		g.drawString("SCORE " + score, 180, 12);
	}

	// 외계인도 총 쏘게 하기 (시간 없어서 못함)
//	public void alienShoot()
//	{
//	}
}
