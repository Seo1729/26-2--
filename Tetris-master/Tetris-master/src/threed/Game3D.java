package threed;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

// 3D 테트리스 게임 규칙 (블록 생성, 이동, 회전, 낙하, 층 삭제, 점수)
// 효과음은 Game3DListener, 다음 블록 고르기는 BlockTypeSource, 화면 데이터는 Game3DSceneBuilder,
// 글자 모드 실행은 Game3DConsole, 프로그램 시작은 Game3DMain이 맡는다.
public class Game3D implements Game3DControl, Game3DStatus
{
	public static final int DEFAULT_FALL_INTERVAL_MS = 1000;
	public static final int POINTS_PER_LAYER = 100;
	public enum State { READY, RUNNING, PAUSED, GAME_OVER }

	// 새 블록이 나오는 위치: 맨 위 가운데 (1, 1, 13)
	private static final int SPAWN_X = Board3D.SIZE_X / 2;
	private static final int SPAWN_Y = Board3D.SIZE_Y / 2;
	private static final int SPAWN_Z = Board3D.SIZE_Z - 1;

	// 게임 상태는 이 클래스만 직접 바꾼다 (밖에서는 get 메서드로만 읽는다)
	private final Board3D board;
	// 다음 블록 종류를 고르는 방법은 밖에서 받는다 (기본은 RandomBlockTypeSource)
	private final BlockTypeSource typeSource;
	// 일어난 일을 알릴 곳 (기본은 효과음을 내는 Game3DSoundEffects)
	private final Game3DListener listener;
	private final Timer fallTimer;
	private Block3D activeBlock;
	private Block3D.Type nextType;
	private State state = State.READY;
	private int lockedBlockCount;
	private long clearedLayerCount;

	@Override
	public synchronized long getClearedLayerCount()
	{
		return clearedLayerCount;
	}

	@Override
	public synchronized long getScore()
	{
		return clearedLayerCount * POINTS_PER_LAYER;
	}

	@Override
	public synchronized State getState()
	{
		return state;
	}

	private void gameOver()
	{
		fallTimer.stop();
		activeBlock = null;
		state = State.GAME_OVER;
		listener.gameOver();
	}

	@Override
	public synchronized int getLockedBlockCount()
	{
		return lockedBlockCount;
	}

	private boolean spawnBlock()
	{
		Block3D candidate = new Block3D(nextType, SPAWN_X, SPAWN_Y, SPAWN_Z); // 맨 위 가운데
		if (!board.canPlace(candidate))
		{
			gameOver();
			return false;
		}
		activeBlock = candidate;
		nextType = typeSource.next();
		return true;
	}

	// 다음 블록
	@Override
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
		this(board, fallIntervalMs, new RandomBlockTypeSource(), new Game3DSoundEffects());
	}

	// 블록 종류 고르는 방법과 알림 받을 곳을 직접 정할 때 (구체 클래스 대신 인터페이스에 의존)
	public Game3D(Board3D board, int fallIntervalMs, BlockTypeSource typeSource, Game3DListener listener)
	{
		if (fallIntervalMs <= 0)
			throw new IllegalArgumentException("Fall interval must be positive");
		this.board = java.util.Objects.requireNonNull(board, "board");
		this.typeSource = java.util.Objects.requireNonNull(typeSource, "typeSource");
		this.listener = java.util.Objects.requireNonNull(listener, "listener");
		nextType = typeSource.next();
		fallTimer = new Timer(fallIntervalMs, new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				fallOneStep();
			}
		});
	}

	// 게임 시작 (게임오버면 restart로만)
	@Override
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

	@Override
	public synchronized void stop()
	{
		if (state == State.RUNNING)
			state = State.PAUSED;
		fallTimer.stop();
	}

	@Override
	public synchronized boolean isRunning()
	{
		return state == State.RUNNING;
	}

	// 처음부터 다시
	@Override
	public synchronized void restart()
	{
		fallTimer.stop();
		board.clear();
		activeBlock = null;
		lockedBlockCount = 0;
		clearedLayerCount = 0;
		nextType = typeSource.next();
		state = State.READY;
		listener.gameRestarted();
		start();
	}

	@Override
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

	// 화면에 그릴 칸들 모으기 (만드는 방법은 Game3DSceneBuilder가 맡는다)
	@Override
	public synchronized Game3DScene getScene()
	{
		return Game3DSceneBuilder.build(board, activeBlock, getGhostBlock());
	}

	// 회전 (벽이나 블록에 막히면 안돌림)
	@Override
	public synchronized boolean rotate(Block3D.Axis axis)
	{
		if (state != State.RUNNING || activeBlock == null)
			return false;
		Block3D candidate = activeBlock.rotated(axis);
		if (!board.canPlace(candidate))
			return false;
		activeBlock = candidate;
		listener.blockRotated();
		return true;
	}

	// 앞뒤좌우 이동
	@Override
	public synchronized boolean move(int dx, int dy)
	{
		if (state != State.RUNNING || activeBlock == null)
			return false;
		if (!isOneStep(dx, dy))
			return false;
		boolean moved = tryMoveTo(activeBlock.getX() + dx, activeBlock.getY() + dy, activeBlock.getZ());

		// 실제로 움직였을 때만 알린다
		if (moved)
			listener.blockMoved();
		return moved;
	}

	// 앞뒤좌우 중 한 방향으로 한 칸인지 (대각선, 제자리, 두 칸 이상은 안 됨)
	private static boolean isOneStep(int dx, int dy)
	{
		return (dx == -1 || dx == 1) && dy == 0
				|| (dy == -1 || dy == 1) && dx == 0;
	}

	// 하드드롭
	@Override
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
		listener.blockLocked();
		int cleared = board.clearCompletedLayers();
		clearedLayerCount += cleared;

		// 층이 지워졌을 때만 알린다
		if (cleared > 0)
			listener.layersCleared(cleared);
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
}
