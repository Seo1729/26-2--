package vsai;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * AI 쪽 판을 조작한다.
 * 새 블록이 나오면 AiBrain에게 별도 스레드에서 둘 자리를 묻고(화면은 계속 움직임),
 * 답을 받으면 사람처럼 한 동작씩(회전 → 좌우 이동 → 떨어뜨리기) 실행한다.
 * 답이 너무 늦거나 실패하면 예비 AI(HeuristicBrain)가 대신 정한다.
 */
public class AiController {

	/** 이 시간(ms) 안에 답이 없으면 예비 AI로 둔다 */
	private static final long TIMEOUT_MS = 2000;

	private final PlayerBoard side;
	private AiBrain brain;
	private final AiBrain fallback = new HeuristicBrain();
	private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
		Thread t = new Thread(r, "vsai-brain");
		t.setDaemon(true);
		return t;
	});

	/** 한 동작 사이 간격(ms). 작을수록 AI가 빠르고 어렵다. */
	private int stepMs = 120;
	/** 새 블록이 나온 뒤 움직이기 시작할 때까지 기다리는 시간(ms). 사람이 생각하는 것처럼 보인다. */
	private int thinkMs = 0;
	private int handledPieceId = -1;
	private Future<Move> pending;
	private long askedAt;
	private Board askedBoard;
	private Move plan;
	private int stepElapsed;

	public AiController(PlayerBoard side, AiBrain brain) {
		this.side = side;
		this.brain = brain;
	}

	public void setStepMs(int stepMs) {
		this.stepMs = stepMs;
	}

	public void setThinkMs(int thinkMs) {
		this.thinkMs = thinkMs;
	}

	/** 다음 블록부터 이 AI가 둘 자리를 정한다 */
	public void setBrain(AiBrain brain) {
		this.brain = brain;
	}

	public void reset() {
		if (pending != null)
			pending.cancel(true);
		pending = null;
		plan = null;
		handledPieceId = -1;
		stepElapsed = 0;
	}

	/** 게임 루프가 매 프레임 호출 */
	public void update(int elapsedMs) {
		if (side.isGameOver())
			return;

		if (side.getPieceId() != handledPieceId)
			ask();

		if (plan == null && !receivePlan())
			return;
		if (System.currentTimeMillis() - askedAt < thinkMs)
			return;

		stepElapsed += elapsedMs;
		if (stepElapsed < stepMs)
			return;
		stepElapsed = 0;
		step();
	}

	/** 새 블록에 대해 둘 자리를 묻기 시작한다 */
	private void ask() {
		handledPieceId = side.getPieceId();
		plan = null;
		stepElapsed = 0;
		if (pending != null)
			pending.cancel(true);
		askedBoard = side.getBoard().copy();
		Board snapshot = askedBoard.copy();
		Tetromino current = side.getCurrent();
		Tetromino next = side.getNext();
		AiBrain asked = brain;
		askedAt = System.currentTimeMillis();
		pending = worker.submit(() -> asked.decide(snapshot, current, next));
	}

	/** 답이 왔거나 시간이 지났으면 plan을 정한다. 아직 기다리는 중이면 false */
	private boolean receivePlan() {
		Move move = null;
		if (pending != null && pending.isDone()) {
			try {
				move = pending.get();
			} catch (Exception e) {
				move = null; // 실패하면 아래에서 예비 AI로
			}
		} else if (System.currentTimeMillis() - askedAt < TIMEOUT_MS) {
			return false;
		} else if (pending != null) {
			pending.cancel(true);
		}
		pending = null;
		if (move == null)
			move = fallback.decide(askedBoard.copy(), side.getCurrent(), side.getNext());
		plan = move != null ? move : new Move(0, 4);
		return true;
	}

	/** 한 동작 실행: 회전이 맞을 때까지 돌리고, 열이 맞을 때까지 옮기고, 떨어뜨린다 */
	private void step() {
		Tetromino t = side.getCurrent();
		int targetX = plan.column() - t.minX(plan.rotation());
		if (side.getRotation() != plan.rotation()) {
			if (!side.rotate())
				side.hardDrop();
		} else if (side.getX() < targetX) {
			if (!side.moveRight())
				side.hardDrop();
		} else if (side.getX() > targetX) {
			if (!side.moveLeft())
				side.hardDrop();
		} else {
			side.hardDrop();
		}
	}

	public void shutdown() {
		worker.shutdownNow();
	}
}
