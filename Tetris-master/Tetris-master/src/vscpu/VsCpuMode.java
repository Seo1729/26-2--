package vscpu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import vscpu.cpu.CpuController;
import vscpu.cpu.HeuristicBrain;
import vscpu.game.PlayerBoard;
import vscpu.game.PlayerBoard.PieceSequence;
import vscpu.view.BoardView;

/**
 * 컴퓨터와 대전하는 테트리스 화면 하나. 왼쪽은 플레이어, 오른쪽은 CPU.
 * 두 판에 같은 순서의 블록이 나오고, 먼저 꼭대기까지 쌓인 쪽이 진다.
 * JPanel이라 메인 메뉴(GameFrame)에 붙고, main()으로 단독 실행도 된다.
 *
 * 조작: ←/→ 이동, ↑ 회전, ↓ 한 칸 내리기, Space 바로 떨어뜨리기, P 일시정지, R 다시 시작(끝난 뒤)
 * 모든 동작은 Swing 이벤트 스레드(키 입력, Swing Timer)에서만 일어난다.
 */
public class VsCpuMode extends JPanel implements ActionListener {

	private static final int FRAME_MS = 20;
	/**
	 * CPU 실력 (보통 수준): 0.23초마다 한 동작, 블록마다 0.25초 생각,
	 * 25% 확률로 가장 좋은 자리 대신 2~6등 자리 중 하나에 둔다
	 */
	private static final CpuController.Settings CPU_SETTINGS = new CpuController.Settings(230, 250, 0.25, 6);
	private static final Color PLAYER_COLOR = new Color(110, 170, 255);
	private static final Color CPU_COLOR = new Color(255, 120, 120);
	private static final int WINDOW_WIDTH = 1100;
	private static final int WINDOW_HEIGHT = 800;
	private static final int BOARD_GAP = 20;
	private static final int STATUS_TOP_PADDING = 10;
	private static final String RESTART_HINT = "R: 다시 시작";

	// 화면 부품(JPanel)은 직렬화할 수 있는 타입이지만 게임 상태 객체는 저장할 필요가 없어 transient로 둔다
	private final transient PlayerBoard player = new PlayerBoard("플레이어");
	private final transient PlayerBoard cpu = new PlayerBoard("컴퓨터");
	private final BoardView playerView = new BoardView(player, PLAYER_COLOR);
	private final BoardView cpuView = new BoardView(cpu, CPU_COLOR);
	private final transient CpuController cpuController = new CpuController(cpu,
			new HeuristicBrain(CPU_SETTINGS.mistakeRate(), CPU_SETTINGS.mistakeChoices(), new Random()),
			new HeuristicBrain(), CPU_SETTINGS);
	/** 게임 중 플레이어 키 → 동작 */
	private final transient Map<Integer, Runnable> playerKeys = Map.of(
			KeyEvent.VK_LEFT, player::moveLeft,
			KeyEvent.VK_RIGHT, player::moveRight,
			KeyEvent.VK_UP, player::rotate,
			KeyEvent.VK_DOWN, player::softDrop,
			KeyEvent.VK_SPACE, player::hardDrop);
	private final JLabel status = new JLabel("", SwingConstants.CENTER);
	private final Timer timer = new Timer(FRAME_MS, this);
	private boolean running;
	private boolean paused;
	private boolean finished;
	private int elapsedMs;

	/** 컴퓨터와 대전 모드만 단독으로 실행해 보는 진입점 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Tetris - 컴퓨터와 대전");
			VsCpuMode mode = new VsCpuMode();
			frame.add(mode);
			// 작업 표시줄을 뺀 화면 크기보다 크면 화면에 맞춘다
			Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
			frame.setSize(Math.min(WINDOW_WIDTH, screen.width), Math.min(WINDOW_HEIGHT, screen.height));
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			mode.start();
		});
	}

	public VsCpuMode() {
		super(new BorderLayout());
		setBackground(BoardView.BACKGROUND);
		setFocusable(true);

		status.setForeground(BoardView.TEXT);
		status.setFont(BoardView.font(Font.BOLD, 16f));
		status.setBorder(BorderFactory.createEmptyBorder(STATUS_TOP_PADDING, 0, 0, 0));
		add(status, BorderLayout.NORTH);

		JPanel boards = new JPanel(new GridLayout(1, 2, BOARD_GAP, 0));
		boards.setOpaque(false);
		boards.add(playerView);
		boards.add(cpuView);
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
		cpu.reset(sequence);
		cpuController.reset();
		setOverlays(null, null, null);
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
		cpuController.shutdown();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (!running || paused)
			return;
		elapsedMs += FRAME_MS;
		player.tick(FRAME_MS);
		cpu.tick(FRAME_MS);
		cpuController.update(FRAME_MS);
		checkResult();
		refresh();
	}

	private void handleKey(int code) {
		if (code == KeyEvent.VK_R && finished) {
			start();
			return;
		}
		if (code == KeyEvent.VK_P && running && !finished) {
			togglePause();
			return;
		}
		Runnable action = playerKeys.get(code);
		if (!running || paused || finished || action == null)
			return;
		action.run();
		checkResult();
		refresh();
	}

	private void togglePause() {
		paused = !paused;
		String title = paused ? "일시정지" : null;
		setOverlays(title, title, paused ? "P: 계속하기" : null);
		refresh();
	}

	/** 두 판에 같은 문구 줄(sub)과 각자의 제목을 함께 띄운다 */
	private void setOverlays(String playerTitle, String cpuTitle, String sub) {
		playerView.setOverlay(playerTitle, sub);
		cpuView.setOverlay(cpuTitle, sub);
	}

	/** 한쪽이라도 꼭대기까지 쌓이면 승패를 정한다 */
	private void checkResult() {
		if (finished || !(player.isGameOver() || cpu.isGameOver()))
			return;
		finished = true;
		timer.stop();
		boolean playerLost = player.isGameOver();
		setOverlays(playerLost ? "패배" : "승리!", playerLost ? "승리!" : "패배", RESTART_HINT);
	}

	private void refresh() {
		int sec = elapsedMs / 1000;
		String time = String.format("%d:%02d", sec / 60, sec % 60);
		if (finished)
			status.setText((player.isGameOver() ? "컴퓨터 승리" : "플레이어 승리") + "  ·  " + time);
		else
			status.setText("컴퓨터와 대전  ·  " + time + (paused ? "  ·  일시정지" : ""));
		repaint();
	}
}
