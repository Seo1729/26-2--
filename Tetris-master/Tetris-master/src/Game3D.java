import java.awt.event.*;
import java.util.Random;
import javax.swing.Timer;

// 3D 테트리스 게임 (블록 생성, 이동, 회전, 낙하)
public class Game3D
{
	public static final int DEFAULT_FALL_INTERVAL_MS = 1000;
	public enum State { READY, RUNNING, PAUSED, GAME_OVER }
	// I블록은 길이가 4라서 3x3에 안들어감 -> 뺌
	private static final Block3D.Type[] SPAWN_TYPES = {
		Block3D.Type.O, Block3D.Type.T, Block3D.Type.L,
		Block3D.Type.J, Block3D.Type.S, Block3D.Type.Z
	};

	Board3D board;
	Random random = new Random();
	private Timer fallTimer;
	private Block3D activeBlock;
	private Block3D.Type nextType;
	State state = State.READY;
	int lockedBlockCount;
	long clearedLayerCount;
	int level = 1; // 레벨 올라가면 빨라지게 하려다 못함
	public static final int POINTS_PER_LAYER = 100;

	public synchronized long getClearedLayerCount()
	{
		return clearedLayerCount;
	}

	public synchronized long getScore()
	{
		return clearedLayerCount * 100;
	}

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
		Block3D candidate = new Block3D(nextType, 1, 1, 13); // 맨 위 가운데
		if (!board.canPlace(candidate))
		{
			gameOver();
			return false;
		}
		activeBlock = candidate;
		nextType = randomSpawnType();
		return true;
	}

	private Block3D.Type randomSpawnType()
	{
		return SPAWN_TYPES[random.nextInt(SPAWN_TYPES.length)];
	}

	// 다음 블록
	public synchronized Block3D.Type getNextBlockType()
	{
		return nextType;
	}

	public Game3D()
	{
		this(DEFAULT_FALL_INTERVAL_MS);
	}

	public Game3D(int fallIntervalMs)
	{
		this(new Board3D(), fallIntervalMs);
	}

	public Game3D(Board3D board, int fallIntervalMs)
	{
		if (fallIntervalMs <= 0)
			throw new IllegalArgumentException("Fall interval must be positive");
		this.board = java.util.Objects.requireNonNull(board, "board");
		nextType = randomSpawnType();
		fallTimer = new Timer(fallIntervalMs, new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				fallOneStep();
			}
		});
	}

	// 게임 시작 (게임오버면 restart로만)
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

	// 처음부터 다시
	public synchronized void restart()
	{
		fallTimer.stop();
		board.clear();
		activeBlock = null;
		lockedBlockCount = 0;
		clearedLayerCount = 0;
		nextType = randomSpawnType();
		state = State.READY;
		start();
	}

	public synchronized Block3D getActiveBlock()
	{
		if (activeBlock == null)
			return null;
		return activeBlock.copy();
	}

	// 고스트 (떨어질 위치 미리보기)
	public synchronized Block3D getGhostBlock()
	{
		if (activeBlock == null) return null;
		Block3D ghost = activeBlock.copy();
		while (board.canPlace(ghost, ghost.getX(), ghost.getY(), ghost.getZ() - 1))
			ghost.setPosition(ghost.getX(), ghost.getY(), ghost.getZ() - 1);
		return ghost;
	}

	// 화면에 그릴 칸들 모으기
	public synchronized Game3DScene getScene()
	{
		java.util.List<Game3DScene.Cell> cells = new java.util.ArrayList<Game3DScene.Cell>();
		for (int x = 0; x < 3; x++)
			for (int y = 0; y < 3; y++)
				for (int z = 0; z < 14; z++)
					if (board.isFilled(x, y, z))
						cells.add(new Game3DScene.Cell(x, y, z, board.getType(x, y, z), false));
		if (activeBlock != null)
			for (Block3D.Cube cube : activeBlock.getAbsoluteCubes())
				cells.add(new Game3DScene.Cell(cube.getX(), cube.getY(), cube.getZ(), activeBlock.getType(), true));
		java.util.List<Game3DScene.Cell> ghostCells = new java.util.ArrayList<Game3DScene.Cell>();
		Block3D ghost = getGhostBlock();
		if (ghost != null)
			for (Block3D.Cube cube : ghost.getAbsoluteCubes())
				ghostCells.add(new Game3DScene.Cell(cube.getX(), cube.getY(), cube.getZ(), ghost.getType(), false));
		return new Game3DScene(cells, ghostCells);
	}

	// 회전 (벽이나 블록에 막히면 안돌림)
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

	// 앞뒤좌우 이동
	public synchronized boolean move(int dx, int dy)
	{
		if (state != State.RUNNING || activeBlock == null)
			return false;
		if (!((dx == -1 || dx == 1) && dy == 0
				|| (dy == -1 || dy == 1) && dx == 0))
			return false;
		return tryMoveTo(activeBlock.getX() + dx, activeBlock.getY() + dy, activeBlock.getZ());
	}

	// 하드드롭
	public synchronized boolean hardDrop()
	{
		if (state != State.RUNNING || activeBlock == null)
			return false;
		activeBlock = getGhostBlock();
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
		clearedLayerCount += board.clearCompletedLayers();
		spawnBlock();
	}

	private synchronized void fallOneStep()
	{
		if (state != State.RUNNING)
			return;
		if (!tryMoveDown())
			lockAndSpawn();
	}

	private boolean tryMoveTo(int x, int y, int z)
	{
		if (!board.canPlace(activeBlock, x, y, z))
			return false;
		activeBlock.setPosition(x, y, z);
		return true;
	}

	// 3D만 따로 실행할때 (--console 붙이면 글자로 테스트)
	public static void main(String[] args) throws InterruptedException
	{
		if (args.length == 0 || !"--console".equals(args[0]))
		{
			javax.swing.SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					new Game3DFrame().setVisible(true);
				}
			});
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
