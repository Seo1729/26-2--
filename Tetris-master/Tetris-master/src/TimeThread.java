/**
 * 일반 테트리스의 경과 시간을 표시하고, 시간이 지날수록 블록 낙하 속도를 높이는 스레드.
 * (원래 GameFrame.java 안에 있던 클래스를 파일로 분리했다)
 */
class TimeThread extends Thread
{
	GamePanel gp;
	static boolean gameState = true;
	int n;

	TimeThread(GamePanel gp, int n)
	{
		this.gp = gp;
		this.n = n;
	}

	public void run()
	{
		int time = 0;
		int second = 0;
		int minute = 0;
		int count = 1;

		synchronized (this)
		{
			try
			{
				this.wait();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		while (gameState)
		{
			if (time % 1000 == 0)
			{
				minute = (time / 1000) / 60;
				second = (time / 1000) % 60;
				String str = String.format("%2d m %02d s", minute, second);
				gp.time.setText(str);
			}
			try
			{
				sleep(1);
			}
			catch (InterruptedException e)
			{
				return;
			}

			time++;

			// 10초마다 gameSpeed 20씩 줄이기
			if (time / count >= 10000 && gp.gameSpeed > 400)
			{
				gp.gameSpeed -= 20;
				count++;
			}
		}
	}
}
