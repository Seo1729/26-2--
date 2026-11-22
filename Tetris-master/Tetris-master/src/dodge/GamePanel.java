package dodge;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.*;
import javax.swing.Timer;

import common.Bgm;
import common.KeyAction;
import common.KeySettings;
import common.SettingsPanel;
import common.SoundManager;

/**
 * 똥피하기 게임 화면. 게임 루프(타이머), 시작·일시정지·설정창, 보드 그리기만 맡는다.
 * 게임 규칙은 PoopGame, 키 처리는 InputHandler, 효과음은 PoopSounds가 한다.
 */
public class GamePanel extends JPanel implements ActionListener {

	/** 게임 루프 한 프레임(ms) */
	public static final int FRAME_MS = 20;
	/** 꾹 눌렀을 때 반복 간격(ms): 블록은 빠르게, 고양이는 조금 느리게 */
	private static final long BLOCK_REPEAT_MS = 50;
	private static final long CAT_REPEAT_MS = 90;

	private final PoopGame game = new PoopGame(new PoopSounds());
	private final CharacterManager characterManager;
	final DodgerSprite dodgerSprite;
	private final InputHandler input;
	/** 상태가 바뀌어 다시 그려야 할 때 알릴 곳 (정보판) */
	private final List<Runnable> changeListeners = new ArrayList<>();

	final Timer timer;
	private boolean isStarted = false;
	private boolean isPaused = false;

	public GamePanel() {
		this(CharacterManager.getInstance());
	}

	public GamePanel(CharacterManager characterManager) {
		this.characterManager = Objects.requireNonNull(characterManager);
		dodgerSprite = new DodgerSprite(characterManager);
		setFocusable(true);
		setBackground(Theme.BACKGROUND);
		timer = new Timer(FRAME_MS, this);

		TetrisPlayer tetris = game.getTetris();
		Dodger dodger = game.getDodger();
		input = new InputHandler(this::isPlaying, this::repaint)
				.control(KeyAction.DODGE_PAUSE, this::togglePause)
				.control(KeyAction.DODGE_RESTART, this::restart)
				.fixedKey(KeyEvent.VK_ESCAPE, this::openSettings)
				.repeatingAction(KeyAction.DODGE_BLOCK_LEFT, BLOCK_REPEAT_MS, tetris::moveLeft)
				.repeatingAction(KeyAction.DODGE_BLOCK_RIGHT, BLOCK_REPEAT_MS, tetris::moveRight)
				.action(KeyAction.DODGE_BLOCK_ROTATE, tetris::rotate)
				.repeatingAction(KeyAction.DODGE_BLOCK_DOWN, BLOCK_REPEAT_MS, tetris::oneLineDown)
				.repeatingAction(KeyAction.DODGE_CAT_LEFT, CAT_REPEAT_MS, dodger::moveLeft)
				.repeatingAction(KeyAction.DODGE_CAT_RIGHT, CAT_REPEAT_MS, dodger::moveRight)
				.action(KeyAction.DODGE_CAT_JUMP, dodger::jump);
		addKeyListener(input);

		// 포커스 잃으면 키 다 뗀걸로
		addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				input.clear();
			}
		});
	}

	public CharacterManager getCharacterManager() {
		return characterManager;
	}

	/** 정보판처럼 읽기만 하는 쪽에 넘겨줄 게임 상태 */
	public GameStatus getStatus() {
		return game;
	}

	public Dodger getDodger() {
		return game.getDodger();
	}

	/** 같은 패키지 테스트가 로직을 직접 조작할 때 쓴다 */
	PoopGame logic() {
		return game;
	}

	public void addChangeListener(Runnable listener) {
		changeListeners.add(listener);
	}

	public boolean isStarted() {
		return isStarted;
	}

	public boolean isPaused() {
		return isPaused;
	}

	private boolean isPlaying() {
		return isStarted && !isPaused;
	}

	// ---------------- 진행 ----------------

	public void start() {
		if (isPaused)
			return;

		requestFocusInWindow();
		SoundManager.startBgm(Bgm.GAME);
		isStarted = true;
		game.start();
		input.clear();
		timer.start();
		refresh();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		input.update();
		game.tick(FRAME_MS);
		refresh();
	}

	public void togglePause() {
		if (!isStarted)
			return;

		isPaused = !isPaused;
		if (isPaused)
			timer.stop();
		else
			timer.start();
		input.clear();
		refresh();
	}

	// 게임을 멈추고 설정 화면을 띄운다 (닫으면 원래 상태로 돌아감)
	void openSettings() {
		if (!isStarted)
			return;

		// 이미 P로 멈춰 있었다면 설정 화면을 닫아도 계속 멈춘 채로 둔다
		boolean wasPaused = isPaused;
		if (!wasPaused)
			togglePause();
		SettingsPanel.showDialog(SwingUtilities.getWindowAncestor(this));
		if (!wasPaused)
			togglePause();
		requestFocusInWindow(); // 설정 창이 닫힌 뒤 키 입력을 되찾는다
	}

	/** 모드를 떠날 때 부른다. 타이머와 BGM을 반드시 멈춘다. */
	public void stop() {
		SoundManager.stopBgm();
		timer.stop();
		isStarted = false;
		isPaused = false;
		input.clear();
	}

	/** 승패 확인하고 다시 그리기 */
	public void refresh() {
		if (isStarted && game.checkResult().isDecided())
			finish();
		repaint();
		for (Runnable listener : changeListeners)
			listener.run();
	}

	private void finish() {
		isStarted = false;
		SoundManager.stopBgm();
		timer.stop();
		input.clear();
	}

	/** 승부가 난 뒤에만 새 판을 시작한다. 캐릭터 외형 선택은 그대로 둔다. */
	public void restart() {
		if (game.isOver())
			start();
	}

	// ---------------- 그리기 ----------------

	private int cellSize() {
		return Math.max(1, Math.min(getWidth() / GameBoard.WIDTH, getHeight() / GameBoard.HEIGHT));
	}

	/** 보드 위쪽 화면 좌표 (정보판이 높이를 맞출 때 씀) */
	public int getBoardTop() {
		return (getHeight() - GameBoard.HEIGHT * cellSize()) / 2;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		int cell = cellSize();
		int boardW = GameBoard.WIDTH * cell;
		int boardH = GameBoard.HEIGHT * cell;
		int left = (getWidth() - boardW) / 2;
		int top = getBoardTop();

		g.setColor(Theme.BOARD);
		g.fillRect(left, top, boardW, boardH);

		drawGoalLine(g, left, top, boardW, cell);
		drawStack(g, left, top, cell);
		drawFallingPiece(g, left, top, cell);
		drawDodger(g, left, top, cell);

		g.setColor(Theme.BORDER);
		g.drawRect(left - 1, top - 1, boardW + 1, boardH + 1);

		drawStateOverlay(g, left, top, boardW, boardH);
	}

	// 보드 y(바닥 0)를 화면 y로
	private static int screenRow(int top, double boardY, int cell) {
		return top + (int) Math.round((GameBoard.HEIGHT - boardY - 1) * cell);
	}

	// 골 라인 (여기 닿으면 고양이 승)
	private void drawGoalLine(Graphics g, int left, int top, int boardW, int cell) {
		int goalTop = screenRow(top, GameRules.GOAL_ROW, cell);
		g.setColor(Theme.GOAL_FILL);
		g.fillRect(left, goalTop, boardW, cell);
		g.setColor(Theme.GOAL_LINE);
		g.drawLine(left, goalTop + cell - 1, left + boardW - 1, goalTop + cell - 1);
	}

	private void drawStack(Graphics g, int left, int top, int cell) {
		GameBoard board = game.getBoard();
		for (int y = 0; y < GameBoard.HEIGHT; ++y) {
			for (int x = 0; x < GameBoard.WIDTH; ++x) {
				int shape = board.shapeAt(x, y);
				if (shape != Shape.NONE)
					Theme.drawSquare(g, left + x * cell, screenRow(top, y, cell), cell, cell, shape);
			}
		}
	}

	private void drawFallingPiece(Graphics g, int left, int top, int cell) {
		TetrisPlayer tetris = game.getTetris();
		if (!tetris.hasPiece())
			return;
		Shape piece = tetris.getPiece();
		for (int i = 0; i < Shape.CELLS; ++i) {
			int x = tetris.getX() + piece.x(i);
			int y = tetris.getY() - piece.y(i);
			Theme.drawSquare(g, left + x * cell, screenRow(top, y, cell), cell, cell, piece.getShape());
		}
	}

	private void drawDodger(Graphics g, int left, int top, int cell) {
		// Skin and accessories share the interpolated position, never the collision cells.
		Dodger dodger = game.getDodger();
		dodgerSprite.draw(g, dodger, left + (int) Math.round(dodger.getDrawX() * cell),
				screenRow(top, dodger.getDrawY(), cell), cell, cell);
	}

	private void drawStateOverlay(Graphics g, int left, int top, int boardW, int boardH) {
		GameRules.Result result = game.getResult();
		if (result.isDecided()) {
			drawOverlay(g, left, top, boardW, boardH, result.getTitle(), result.getMessage(),
					KeySettings.getKeyName(KeyAction.DODGE_RESTART) + ": 다시 시작");
		} else if (isPaused) {
			drawOverlay(g, left, top, boardW, boardH, "일시정지", null,
					KeySettings.getKeyName(KeyAction.DODGE_PAUSE) + ": 계속하기");
		}
	}

	private void drawOverlay(Graphics g, int left, int top, int boardW, int boardH, String title, String message,
			String hint) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		int boxH = message != null ? 130 : 100;
		int boxY = top + boardH / 2 - boxH / 2;
		g2.setColor(Theme.OVERLAY);
		g2.fillRect(left, boxY, boardW, boxH);

		int y = boxY + 46;
		drawCentered(g2, title, Theme.font(Font.BOLD, 30f), Color.WHITE, left, boardW, y);
		if (message != null) {
			y += 34;
			drawCentered(g2, message, Theme.font(Font.PLAIN, 15f), Theme.TEXT, left, boardW, y);
		}
		y += 30;
		drawCentered(g2, hint, Theme.font(Font.PLAIN, 14f), Theme.SUBTEXT, left, boardW, y);
	}

	private static void drawCentered(Graphics2D g, String text, Font font, Color color, int left, int width,
			int baseline) {
		g.setFont(font);
		g.setColor(color);
		FontMetrics fm = g.getFontMetrics();
		g.drawString(text, left + (width - fm.stringWidth(text)) / 2, baseline);
	}
}
