/**
 * 일반 테트리스의 경과 시간을 표시하고, 시간이 지날수록 블록 낙하 속도를 높이는 스레드.
 */
class TimeThread extends Thread
{
	/** 시간을 표시하고 속도를 바꿀 게임 화면 */
	GamePanel gp;

	/** 시간 재기를 계속할지 여부 */
	static boolean gameState = true;

	/**
	 * 시간 스레드를 만든다. start()를 불러도 카운트다운이 끝나 깨워 줄 때까지 기다린다.
	 *
	 * @param gp 시간을 표시할 게임 화면
	 */
	TimeThread(GamePanel gp)
	{
		this.gp = gp;
	}

	/**
	 * 1밀리초마다 시간을 세서 1초마다 화면에 표시하고, 10초마다 낙하 간격을 20밀리초씩 줄인다.
	 */
	public void run()
	{
		int time = 0;
		int second = 0;
		int minute = 0;
		int count = 1;

		// 카운트다운이 끝날 때까지 기다린다
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
			// 1초(1000밀리초)가 될 때마다 "분 m 초 s" 형식으로 표시한다
			if (time % 1000 == 0)
			{
				minute = (time / 1000) / 60;
				second = (time / 1000) % 60;
				String str = String.format("%2d m %02d s", minute, second);
				gp.timeLabel.setText(str);
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

			// 10초마다 블록 낙하 간격을 20밀리초씩 줄인다(0.4초보다 빨라지지는 않는다)
			if (time / count >= 10000 && gp.gameSpeed > 400)
			{
				gp.gameSpeed = gp.gameSpeed - 20;
				count++;
			}
		}
	}
}
