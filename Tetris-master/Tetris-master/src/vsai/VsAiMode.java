package vsai;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 * 사람 vs AI 테트리스 화면 하나. 왼쪽은 플레이어, 오른쪽은 AI.
 * 두 판에 같은 순서의 블록이 나오고, 먼저 꼭대기까지 쌓인 쪽이 진다.
 * JPanel이라 단독 창(VsAiMain)에도, 나중에 메인 메뉴에도 그대로 붙일 수 있다.
 *
 * 조작: ←/→ 이동, ↑ 회전, ↓ 한 칸 내리기, Space 바로 떨어뜨리기, P 일시정지, R 다시 시작(끝난 뒤)
 */
public class VsAiMode extends JPanel implements ActionListener {

	private static final int FRAME_MS = 20;
	// AI 실력 (보통 수준): 0.23초마다 한 동작, 블록마다 0.25초 생각,
	// 25% 확률로 가장 좋은 자리 대신 2~6등 자리 중 하나에 둔다
	private static final int AI_STEP_MS = 230;
	private static final int AI_THINK_MS = 250;
	private static final double AI_MISTAKE_RATE = 0.25;
	private static final int AI_MISTAKE_CHOICES = 6;
	private static final Color PLAYER_COLOR = new Color(110, 170, 255);
	private static final Color AI_COLOR = new Color(255, 120, 120);

	private final PlayerBoard player = new PlayerBoard("플레이어");
	private final PlayerBoard ai = new PlayerBoard("AI");
	private final BoardView playerView = new BoardView(player, PLAYER_COLOR);
	private final BoardView aiView = new BoardView(ai, AI_COLOR);
	private final AiController aiController;
	private final JLabel status = new JLabel("", SwingConstants.CENTER);
	private final Timer timer = new Timer(FRAME_MS, this);
	private boolean running;
	private boolean paused;
	private boolean finished;
	private int elapsedMs;

	public VsAiMode() {
		this(new HeuristicBrain(AI_MISTAKE_RATE, AI_MISTAKE_CHOICES, new Random()));
	}

	/** brain을 바꿔 끼우면 다른 AI와 대결한다 (예: 나중에 ClaudeBrain) */
	public VsAiMode(AiBrain brain) {
		super(new BorderLayout());
		setBackground(BoardView.BACKGROUND);
		setFocusable(true);
		aiController = new AiController(ai, brain);
		aiController.setStepMs(AI_STEP_MS);
		aiController.setThinkMs(AI_THINK_MS);

		status.setForeground(new Color(235, 236, 242));
		status.setFont(BoardView.font(Font.BOLD, 16f));
		status.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		add(status, BorderLayout.NORTH);

		JPanel boards = new JPanel(new GridLayout(1, 2, 20, 0));
		boards.setOpaque(false);
		boards.add(playerView);
		boards.add(aiView);
		add(boards, BorderLayout.CENTER);

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				handleKey(e.getKeyCode());
			}
		});
	}

	/** 새 대결 시작 (화면에 붙인 뒤 호출) */
	public void start() {
		PieceSequence sequence = new PieceSequence(System.nanoTime());
		player.reset(sequence);
		ai.reset(sequence);
		aiController.reset();
		playerView.setOverlay(null, null);
		aiView.setOverlay(null, null);
		elapsedMs = 0;
		running = true;
		paused = false;
		finished = false;
		requestFocusInWindow();
		timer.start();
		refresh();
	}

	/** 모드 종료 (다른 화면으로 넘어갈 때 호출) */
	public void stop() {
		timer.stop();
		running = false;
		aiController.reset();
	}

	/** AI 동작 간격(ms). 작을수록 어렵다. */
	public void setAiStepMs(int stepMs) {
		aiController.setStepMs(stepMs);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!running || paused)
			return;
		elapsedMs += FRAME_MS;
		player.tick(FRAME_MS);
		ai.tick(FRAME_MS);
		aiController.update(FRAME_MS);
		checkResult();
		refresh();
	}

	private void handleKey(int code) {
		if (code == KeyEvent.VK_R && finished) {
			start();
			return;
		}
		if (code == KeyEvent.VK_P && running && !finished) {
			paused = !paused;
			String sub = paused ? "P: 계속하기" : null;
			playerView.setOverlay(paused ? "일시정지" : null, sub);
			aiView.setOverlay(paused ? "일시정지" : null, sub);
			refresh();
			return;
		}
		if (!running || paused || finished)
			return;
		switch (code) {
		case KeyEvent.VK_LEFT -> player.moveLeft();
		case KeyEvent.VK_RIGHT -> player.moveRight();
		case KeyEvent.VK_UP -> player.rotate();
		case KeyEvent.VK_DOWN -> player.softDrop();
		case KeyEvent.VK_SPACE -> player.hardDrop();
		default -> {
			return;
		}
		}
		checkResult();
		refresh();
	}

	/** 한쪽이라도 꼭대기까지 쌓이면 승패를 정한다 */
	private void checkResult() {
		if (finished || !(player.isGameOver() || ai.isGameOver()))
			return;
		finished = true;
		timer.stop();
		boolean playerLost = player.isGameOver();
		playerView.setOverlay(playerLost ? "패배" : "승리!", "R: 다시 시작");
		aiView.setOverlay(playerLost ? "승리!" : "패배", "R: 다시 시작");
	}

	private void refresh() {
		int sec = elapsedMs / 1000;
		String time = String.format("%d:%02d", sec / 60, sec % 60);
		if (finished)
			status.setText((player.isGameOver() ? "AI 승리" : "플레이어 승리") + "  ·  " + time);
		else
			status.setText("사람 vs AI  ·  " + time + (paused ? "  ·  일시정지" : ""));
		repaint();
	}
}
