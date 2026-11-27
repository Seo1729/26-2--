package origin;

/**
 * 게임 시간을 재서 화면에 보여 주고, 10초마다 블록이 조금씩 빨라지게 하는 스레드.
 * 카운트다운이 끝날 때까지 기다렸다가 시작하고, 게임이 끝나면(interrupt) 멈춘다.
 */
class TimeThread extends Thread
{
	private static final int TICKS_PER_SECOND = 1000; // 1ms마다 1씩 센다
	private static final int SPEED_UP_TICKS = 10000; // 10초마다
	private static final int SPEED_UP_STEP = 20; // 낙하 간격을 20ms씩 줄인다
	private static final int SPEED_UP_LIMIT = 400; // 낙하 간격이 이 값 이하면 더 줄이지 않는다

	private final GamePanel gp;

	TimeThread(GamePanel gp)
	{
		this.gp = gp;
	}

	@Override
	public void run()
	{
		int time = 0;
		int second = 0;
		int minute = 0;
		int count = 1;

		// 카운트다운이 끝날 때까지 기다린다
		try
		{
			gp.startGate.await();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		// 게임이 끝나 interrupt되면 sleep에서 빠져나와 멈춘다
		while (true)
		{
			if (time % TICKS_PER_SECOND == 0)
			{
				minute = (time / TICKS_PER_SECOND) / 60;
				second = (time / TICKS_PER_SECOND) % 60;
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

			// 설정 화면을 보는 동안에는 시간이 흐르지 않는다
			if (gp.paused)
				continue;

			time++;

			// 10초마다 gameSpeed 20씩 줄이기
			if (time / count >= SPEED_UP_TICKS && gp.gameSpeed > SPEED_UP_LIMIT)
			{
				gp.gameSpeed -= SPEED_UP_STEP;
				count++;
			}
		}
	}
}
