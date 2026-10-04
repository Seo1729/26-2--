import java.util.Random;
import javax.swing.Timer;

/** 블록 생성, 충돌 검사, 자동 낙하 및 수평 이동을 실행하는 3D 게임 로직. */
public class Game3D
{
	public static final int DEFAULT_FALL_INTERVAL_MS = 1000;
	// XY 평면의 I는 폭이 4이므로 현재 3x3 보드에 배치할 수 없다.
	private static final Block3D.Type[] SPAWN_TYPES = {
		Block3D.Type.O, Block3D.Type.T, Block3D.Type.L,
		Block3D.Type.J, Block3D.Type.S, Block3D.Type.Z
	};

	private final Board3D board;
	private final Random random = new Random();
	private final Timer fallTimer;
	private Block3D activeBlock;
	private boolean running;
	private int lockedBlockCount;

	public synchronized int getLockedBlockCount()
	{
		return lockedBlockCount;
	}

	private boolean spawnBlock()
	{
		Block3D candidate = new Block3D(SPAWN_TYPES[random.nextInt(SPAWN_TYPES.length)],
				Board3D.SIZE_X / 2, Board3D.SIZE_Y / 2, Board3D.SIZE_Z - 1);
		activeBlock = board.canPlace(candidate) ? candidate : null;
		return activeBlock != null;
	}

	public Game3D()
	{
		this(DEFAULT_FALL_INTERVAL_MS);
	}

	/** 검증이나 속도 설정을 위해 낙하 간격을 지정할 수 있다. */
	public Game3D(int fallIntervalMs)
	{
		this(new Board3D(), fallIntervalMs);
	}

	/** 미리 고정된 칸이 있는 보드에서도 같은 충돌 검사를 사용한다. */
	public Game3D(Board3D board, int fallIntervalMs)
	{
		if (fallIntervalMs <= 0)
			throw new IllegalArgumentException("Fall interval must be positive");
		this.board = java.util.Objects.requireNonNull(board, "board");
		fallTimer = new Timer(fallIntervalMs, event -> fallOneStep());
	}

	/** 최초 시작 시 랜덤 블록을 생성한다. 정지 후 재시작하면 기존 블록을 이어서 낙하시킨다. */
	public synchronized void start()
	{
		if (running)
			return;
		if (activeBlock == null && !spawnBlock())
			return;
		if (!board.canPlace(activeBlock))
			return;
		running = true;
		fallTimer.start();
	}

	public synchronized void stop()
	{
		running = false;
		fallTimer.stop();
	}

	public synchronized boolean isRunning()
	{
		return running;
	}

	/** 외부에서 낙하 중인 블록을 변경하지 못하도록 현재 상태의 복사본을 반환한다. */
	public synchronized Block3D getActiveBlock()
	{
		if (activeBlock == null)
			return null;
		return activeBlock.copy();
	}

	/** 회전 후보가 경계와 고정 블록 검사를 통과한 경우만 반영한다. 기준점은 움직이지 않는다. */
	public synchronized boolean rotate(Block3D.Axis axis)
	{
		if (!running || activeBlock == null)
			return false;
		Block3D candidate = activeBlock.rotated(axis);
		if (!board.canPlace(candidate))
			return false;
		activeBlock = candidate;
		return true;
	}

	/** X 또는 Y 방향으로 한 칸 이동한다. 정지 상태이거나 충돌하면 위치를 유지한다. */
	public synchronized boolean move(int dx, int dy)
	{
		if (!running || activeBlock == null)
			return false;
		if (!((dx == -1 || dx == 1) && dy == 0
				|| (dy == -1 || dy == 1) && dx == 0))
			return false;
		return tryMoveTo(activeBlock.getX() + dx, activeBlock.getY() + dy, activeBlock.getZ());
	}

	private synchronized void fallOneStep()
	{
		if (!running)
			return;
		if (!tryMoveTo(activeBlock.getX(), activeBlock.getY(), activeBlock.getZ() - 1))
		{
			board.lockBlock(activeBlock);
			lockedBlockCount++;
			board.clearCompletedLayers();
			if (!spawnBlock())
				stop();
		}
	}

	/** 위치 갱신은 반드시 후보 위치의 충돌 검사를 통과한 뒤 수행한다. */
	private boolean tryMoveTo(int x, int y, int z)
	{
		if (!board.canPlace(activeBlock, x, y, z))
			return false;
		activeBlock.setPosition(x, y, z);
		return true;
	}

	/** 기본 실행은 키 입력 창을 연다. --console 옵션은 생성부터 게임 종료까지 확인한다. */
	public static void main(String[] args) throws InterruptedException
	{
		if (args.length == 0 || !"--console".equals(args[0]))
		{
			javax.swing.SwingUtilities.invokeLater(() -> new Game3DFrame().setVisible(true));
			return;
		}
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
				Thread.sleep(50);
			} while (game.isRunning());
			System.out.println("Game over. Locked blocks: " + game.getLockedBlockCount());
		}
		finally
		{
			game.stop();
		}
	}
}
