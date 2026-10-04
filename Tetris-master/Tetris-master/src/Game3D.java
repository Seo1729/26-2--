import java.util.Random;
import javax.swing.Timer;

/** 블록 생성, 충돌 검사, 자동 낙하 및 수평 이동을 실행하는 3D 게임 로직. */
public class Game3D
{
	public static final int DEFAULT_FALL_INTERVAL_MS = 1000;
	public enum State { READY, RUNNING, PAUSED, GAME_OVER }
	// XY 평면의 I는 폭이 4이므로 현재 3x3 보드에 배치할 수 없다.
	private static final Block3D.Type[] SPAWN_TYPES = {
		Block3D.Type.O, Block3D.Type.T, Block3D.Type.L,
		Block3D.Type.J, Block3D.Type.S, Block3D.Type.Z
	};

	private final Board3D board;
	private final Random random = new Random();
	private final Timer fallTimer;
	private Block3D activeBlock;
	private State state = State.READY;
	private int lockedBlockCount;

	public synchronized State getState()
	{
		return state;
	}

	private void gameOver()
	{
		fallTimer.stop();
		activeBlock = null;
		state = State.GAME_OVER;
	}

	public synchronized int getLockedBlockCount()
	{
		return lockedBlockCount;
	}

	private boolean spawnBlock()
	{
		Block3D candidate = new Block3D(SPAWN_TYPES[random.nextInt(SPAWN_TYPES.length)],
				Board3D.SIZE_X / 2, Board3D.SIZE_Y / 2, Board3D.SIZE_Z - 1);
		if (!board.canPlace(candidate))
		{
			gameOver();
			return false;
		}
		activeBlock = candidate;
		return true;
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

	/** 최초 시작 또는 일시 정지에서 이어서 진행한다. GAME_OVER는 restart로만 재시작한다. */
	public synchronized void start()
	{
		if (state == State.RUNNING || state == State.GAME_OVER)
			return;
		if (activeBlock == null && !spawnBlock())
			return;
		if (!board.canPlace(activeBlock))
		{
			gameOver();
			return;
		}
		state = State.RUNNING;
		fallTimer.start();
	}

	public synchronized void stop()
	{
		if (state == State.RUNNING)
			state = State.PAUSED;
		fallTimer.stop();
	}

	public synchronized boolean isRunning()
	{
		return state == State.RUNNING;
	}

	/** 보드, 회전 중인 블록 및 카운터를 초기화하고 새 게임을 시작한다. */
	public synchronized void restart()
	{
		fallTimer.stop();
		board.clear();
		activeBlock = null;
		lockedBlockCount = 0;
		state = State.READY;
		start();
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
		if (state != State.RUNNING || activeBlock == null)
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
		if (state != State.RUNNING || activeBlock == null)
			return false;
		if (!((dx == -1 || dx == 1) && dy == 0
				|| (dy == -1 || dy == 1) && dx == 0))
			return false;
		return tryMoveTo(activeBlock.getX() + dx, activeBlock.getY() + dy, activeBlock.getZ());
	}

	/** 가능한 최저 위치까지 내려가 즉시 고정한다. 정지 상태에서는 아무것도 변경하지 않는다. */
	public synchronized boolean hardDrop()
	{
		if (state != State.RUNNING || activeBlock == null)
			return false;
		while (tryMoveDown())
		{
			// 자동 낙하와 같은 한 칸 이동 및 충돌 검사를 반복한다.
		}
		lockAndSpawn();
		return true;
	}

	private boolean tryMoveDown()
	{
		return tryMoveTo(activeBlock.getX(), activeBlock.getY(), activeBlock.getZ() - 1);
	}

	private void lockAndSpawn()
	{
		board.lockBlock(activeBlock);
		lockedBlockCount++;
		board.clearCompletedLayers();
		spawnBlock();
	}

	private synchronized void fallOneStep()
	{
		if (state != State.RUNNING)
			return;
		if (!tryMoveDown())
			lockAndSpawn();
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
