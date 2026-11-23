package threed;

// 3D 테트리스를 화면 없이 글자로 돌려 보는 실행기 (java threed.Game3DMain --console)
// 게임 규칙(Game3D)에서 콘솔 출력 코드를 분리했다.
final class Game3DConsole
{
	private static final int POLL_INTERVAL_MS = 50;

	private Game3DConsole() { }

	// 새 게임을 만들어 블록이 내려갈 때마다 위치를, 고정될 때마다 고정 개수를 출력한다
	static void run() throws InterruptedException
	{
		Game3D game = new Game3D();
		game.start();
		if (game.getActiveBlock() == null)
		{
			System.out.println("Cannot spawn block: occupied spawn position.");
			return;
		}
		int previousZ = -1;
		int previousLocked = 0;
		try
		{
			do
			{
				synchronized (game)
				{
					int locked = game.getLockedBlockCount();
					if (locked != previousLocked)
					{
						System.out.println("Locked blocks: " + locked);
						previousLocked = locked;
						previousZ = -1;
					}
					Block3D block = game.getActiveBlock();
					if (block != null && block.getZ() != previousZ)
					{
						System.out.println(block.getType() + " position: (" + block.getX()
								+ ", " + block.getY() + ", " + block.getZ() + ")");
						previousZ = block.getZ();
					}
				}
				Thread.sleep(POLL_INTERVAL_MS);
			} while (game.isRunning());
			System.out.println("Game over. Locked blocks: " + game.getLockedBlockCount());
		}
		finally
		{
			game.stop();
		}
	}
}
