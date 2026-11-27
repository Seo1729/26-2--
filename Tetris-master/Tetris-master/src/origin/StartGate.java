package origin;

/**
 * 카운트다운이 끝날 때까지 낙하 스레드와 시간 스레드를 세워 두는 출발 신호.
 *
 * 전에는 카운트다운 스레드가 Thread 객체 자체(th, timeTh)에 notify()를 보내고, 낙하 스레드는 그 Thread 객체를 잠근 채
 * wait()와 sleep()을 했다. Thread 객체로 wait/notify를 하면 JVM 내부 동작과 얽힐 수 있고, 잠근 채 sleep하면
 * 다른 스레드가 그 잠금을 오래 기다리게 된다(SonarLint 심각 이슈 S2236, S2274, S2276).
 * 이제 전용 객체 하나로 신호를 주고받는다. open()이 불리면 기다리던 스레드가 모두 출발한다.
 */
final class StartGate
{
	private boolean opened = false;

	/** 신호가 올 때까지 기다린다. 이미 열려 있으면 바로 돌아온다 */
	synchronized void await() throws InterruptedException
	{
		// 신호 없이 깨어나는 경우(spurious wakeup)가 있어 while로 다시 확인한다
		while (!opened)
			wait();
	}

	/** 기다리는 스레드를 모두 출발시킨다 */
	synchronized void open()
	{
		opened = true;
		notifyAll();
	}
}
