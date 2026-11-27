package vscpu.cpu;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import vscpu.cpu.CpuBrain.Move;
import vscpu.game.Board;
import vscpu.game.PlayerBoard;
import vscpu.game.Tetromino;

/**
 * CPU 쪽 판을 조작한다.
 * 새 블록이 나오면 CpuBrain에게 별도 스레드에서 둘 자리를 묻고(화면은 계속 움직임),
 * 답을 받으면 사람처럼 한 동작씩(회전 → 좌우 이동 → 떨어뜨리기) 실행한다.
 * 답이 너무 늦거나 실패하면 예비 머리(fallback)가 대신 정한다.
 *
 * 스레드: update()와 reset()은 Swing 이벤트 스레드(게임 타이머)에서만 불린다.
 * 작업 스레드는 판 복사본만 받으므로 두 스레드가 같은 객체를 함께 바꾸는 일이 없다.
 */
public class CpuController {

	/**
	 * CPU 세기 설정.
	 *
	 * @param stepMs         한 동작 사이 간격(ms). 작을수록 빠르고 어렵다.
	 * @param thinkMs        새 블록이 나온 뒤 움직이기 시작할 때까지 기다리는 시간(ms)
	 * @param mistakeRate    가장 좋은 자리 대신 다른 자리에 둘 확률 (0~1)
	 * @param mistakeChoices 실수할 때 고르는 범위: 점수 순위 2등부터 이 등수까지 중 하나
	 */
	public record Settings(int stepMs, int thinkMs, double mistakeRate, int mistakeChoices) {
	}

	private static final Logger LOG = Logger.getLogger(CpuController.class.getName());
	/** 이 시간(ms) 안에 답이 없으면 예비 머리로 둔다 */
	private static final long TIMEOUT_MS = 2000;
	/** 예비 머리마저 자리를 못 찾으면 이렇게 둔다 (회전 없이 가운데) */
	private static final Move DEFAULT_MOVE = new Move(0, 4);
	private static final int NOT_HANDLED = -1;

	private final PlayerBoard side;
	private final CpuBrain brain;
	private final CpuBrain fallback;
	private final Settings settings;
	private ExecutorService worker = newWorker();

	private int handledPieceId = NOT_HANDLED;
	private Future<Move> pending;
	private long askedAt;
	private Board askedBoard;
	private Move plan;
	private int stepElapsed;

	public CpuController(PlayerBoard side, CpuBrain brain, CpuBrain fallback, Settings settings) {
		this.side = side;
		this.brain = brain;
		this.fallback = fallback;
		this.settings = settings;
	}

	private static ExecutorService newWorker() {
		return Executors.newSingleThreadExecutor(r -> {
			Thread t = new Thread(r, "vscpu-brain");
			t.setDaemon(true);
			return t;
		});
	}

	/** 생각 중이던 것을 버린다 (새 판을 시작할 때) */
	public void reset() {
		cancelPending();
		plan = null;
		handledPieceId = NOT_HANDLED;
		stepElapsed = 0;
		if (worker.isShutdown())
			worker = newWorker();
	}

	/** 모드를 끝낼 때: 생각을 멈추고 작업 스레드를 정리한다 */
	public void shutdown() {
		reset();
		worker.shutdownNow();
	}

	/** 게임 루프가 매 프레임 호출 */
	public void update(int elapsedMs) {
		if (side.isGameOver())
			return;

		if (side.getPieceId() != handledPieceId)
			ask();

		if (plan == null && !receivePlan())
			return;
		if (System.currentTimeMillis() - askedAt < settings.thinkMs())
			return;

		stepElapsed += elapsedMs;
		if (stepElapsed < settings.stepMs())
			return;
		stepElapsed = 0;
		step();
	}

	/** 새 블록에 대해 둘 자리를 묻기 시작한다 */
	private void ask() {
		handledPieceId = side.getPieceId();
		plan = null;
		stepElapsed = 0;
		cancelPending();
		askedBoard = side.snapshotBoard();
		Board snapshot = askedBoard.copy();
		Tetromino current = side.getCurrent();
		Tetromino next = side.getNext();
		askedAt = System.currentTimeMillis();
		pending = worker.submit(() -> brain.decide(snapshot, current, next));
	}

	/** 답이 왔거나 시간이 지났으면 plan을 정한다. 아직 기다리는 중이면 false */
	private boolean receivePlan() {
		Move move = null;
		if (pending != null && pending.isDone()) {
			move = answerOf(pending);
		} else if (System.currentTimeMillis() - askedAt < TIMEOUT_MS) {
			return false;
		} else {
			LOG.info("CPU 머리가 " + TIMEOUT_MS + "ms 안에 답하지 않아 예비 머리로 둡니다.");
			cancelPending();
		}
		pending = null;
		if (move == null)
			move = fallback.decide(askedBoard.copy(), side.getCurrent(), side.getNext());
		plan = move != null ? move : DEFAULT_MOVE;
		return true;
	}

	/** 끝난 작업의 답. 실패했으면 이유를 남기고 null (예비 머리가 대신 둔다) */
	private static Move answerOf(Future<Move> done) {
		try {
			return done.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (CancellationException e) {
			// 새 블록이 나와 일부러 취소한 경우라 남길 것이 없다
		} catch (ExecutionException e) {
			LOG.log(Level.WARNING, "CPU 머리가 계산 중 실패해 예비 머리로 둡니다.", e.getCause());
		}
		return null;
	}

	private void cancelPending() {
		if (pending != null)
			pending.cancel(true);
		pending = null;
	}

	/** 한 동작 실행: 회전이 맞을 때까지 돌리고, 열이 맞을 때까지 옮기고, 떨어뜨린다. 막히면 그 자리에 떨어뜨린다. */
	private void step() {
		BooleanSupplier action = nextAction();
		if (action == null || !action.getAsBoolean())
			side.hardDrop();
	}

	/** 다음에 할 한 동작. 목표 자리에 도착했으면 null */
	private BooleanSupplier nextAction() {
		int targetX = plan.column() - side.getCurrent().minX(plan.rotation());
		if (side.getRotation() != plan.rotation())
			return side::rotate;
		if (side.getX() < targetX)
			return side::moveRight;
		if (side.getX() > targetX)
			return side::moveLeft;
		return null;
	}
}
