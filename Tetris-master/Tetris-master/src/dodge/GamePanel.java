package dodge;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * 게임 화면. 타이머(게임 루프)를 돌리고 보드와 현재 조각을 그린다.
 */
public class GamePanel extends JPanel implements ActionListener {

	/** 게임 루프 한 프레임 길이(ms). 입력과 P2 움직임은 매 프레임 처리한다. */
	private static final int FRAME_MS = 20;
	/** 테트리스 블록 자동 낙하 간격(ms) */
	private static final int FALL_MS = 400;
	/** P2 점프/중력 1칸 이동 간격(ms) */
	private static final int DODGER_STEP_MS = 80;

	private final GameBoard board = new GameBoard();
	private final TetrisPlayer tetris = new TetrisPlayer(board);
	private final Dodger dodger = new Dodger(board, tetris);
	private final InputHandler input;
	private final Timer timer;
	private final JLabel statusbar;
	private boolean isStarted = false;
	private boolean isPaused = false;
	private int fallElapsed = 0;
	private int dodgerElapsed = 0;
	/** 게임 시작 후 흐른 시간(ms). 일시정지 중에는 늘지 않는다. */
	private int playElapsed = 0;
	private GameRules.Result result = GameRules.Result.NONE;

	public GamePanel(JLabel statusbar) {
		setFocusable(true);
		timer = new Timer(FRAME_MS, this);
		this.statusbar = statusbar;
		tetris.setDodger(dodger);
		input = new InputHandler(this, tetris, dodger);
		addKeyListener(input);
	}

	public void start() {
		if (isPaused)
			return;

		requestFocusInWindow();
		isStarted = true;
		fallElapsed = 0;
		dodgerElapsed = 0;
		playElapsed = 0;
		result = GameRules.Result.NONE;
		board.clear();
		dodger.reset();
		tetris.start();
		input.clear();
		timer.start();
		refresh();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		input.update();

		playElapsed += FRAME_MS;
		fallElapsed += FRAME_MS;
		if (fallElapsed >= FALL_MS) {
			fallElapsed = 0;
			tetris.tick();
			dodger.resolveOverlap();
		}

		dodgerElapsed += FRAME_MS;
		if (dodgerElapsed >= DODGER_STEP_MS) {
			dodgerElapsed = 0;
			dodger.physicsStep();
		}

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

	/** 모드를 빠져나갈 때(메뉴로 돌아가기 등) 타이머를 멈춘다 */
	public void stop() {
		timer.stop();
		isStarted = false;
		isPaused = false;
		input.clear();
	}

	/** 승패를 판정하고 상태 표시줄을 갱신한 뒤 다시 그린다 */
	public void refresh() {
		if (isStarted && result == GameRules.Result.NONE) {
			result = GameRules.judge(tetris, dodger, playElapsed);
			if (result != GameRules.Result.NONE) {
				isStarted = false;
				timer.stop();
				input.clear();
			}
		}

		String info = " 줄 " + tetris.getLinesRemoved() + "/" + GameRules.TARGET_LINES + "  |  남은 시간 "
				+ GameRules.remainingSeconds(playElapsed) + "초";
		if (result != GameRules.Result.NONE)
			statusbar.setText(" " + result.getMessage() + "  (R: 다시 시작)");
		else if (isPaused)
			statusbar.setText(info + "  |  일시정지");
		else
			statusbar.setText(info);
		repaint();
	}

	/** 게임이 끝난 뒤 R키로 다시 시작 */
	public void restart() {
		if (result != GameRules.Result.NONE)
			start();
	}

	public GameRules.Result getResult() {
		return result;
	}

	/** 게임이 진행 중이고 일시정지가 아닐 때 true */
	public boolean isRunning() {
		return isStarted && !isPaused;
	}

	public boolean isStarted() {
		return isStarted;
	}

	public boolean isPaused() {
		return isPaused;
	}

	private int squareWidth() {
		return (int) getSize().getWidth() / GameBoard.WIDTH;
	}

	private int squareHeight() {
		return (int) getSize().getHeight() / GameBoard.HEIGHT;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Dimension size = getSize();
		int boardTop = (int) size.getHeight() - GameBoard.HEIGHT * squareHeight();

		// 골 라인: 이 줄에 닿으면 똥피하기 승리
		int goalTop = boardTop + (GameBoard.HEIGHT - GameRules.GOAL_ROW - 1) * squareHeight();
		g.setColor(new Color(255, 230, 150));
		g.fillRect(0, goalTop, GameBoard.WIDTH * squareWidth(), squareHeight());
		g.setColor(new Color(220, 160, 0));
		g.drawLine(0, goalTop + squareHeight() - 1, GameBoard.WIDTH * squareWidth(), goalTop + squareHeight() - 1);

		for (int i = 0; i < GameBoard.HEIGHT; ++i) {
			for (int j = 0; j < GameBoard.WIDTH; ++j) {
				Tetrominoes shape = board.shapeAt(j, GameBoard.HEIGHT - i - 1);
				if (shape != Tetrominoes.NoShape)
					drawSquare(g, j * squareWidth(), boardTop + i * squareHeight(), shape);
			}
		}

		if (tetris.hasPiece()) {
			Shape piece = tetris.getPiece();
			for (int i = 0; i < 4; ++i) {
				int x = tetris.getX() + piece.x(i);
				int y = tetris.getY() - piece.y(i);
				drawSquare(g, x * squareWidth(), boardTop + (GameBoard.HEIGHT - y - 1) * squareHeight(),
						piece.getShape());
			}
		}

		drawDodger(g, dodger.getX() * squareWidth(),
				boardTop + (GameBoard.HEIGHT - dodger.getY() - 1) * squareHeight());
		drawResult(g);
	}

	/** 승패가 나면 화면 가운데에 결과를 띄운다 */
	private void drawResult(Graphics g) {
		if (result == GameRules.Result.NONE)
			return;
		String title = result.isTetrisWin() ? "테트리스 승리!" : "똥피하기 승리!";
		g.setColor(new Color(0, 0, 0, 170));
		int boxH = 60;
		int boxY = getHeight() / 2 - boxH / 2;
		g.fillRect(0, boxY, getWidth(), boxH);
		g.setColor(Color.WHITE);
		g.setFont(getFont().deriveFont(Font.BOLD, 18f));
		FontMetrics fm = g.getFontMetrics();
		g.drawString(title, (getWidth() - fm.stringWidth(title)) / 2, boxY + 26);
		g.setFont(getFont().deriveFont(12f));
		fm = g.getFontMetrics();
		String sub = "R: 다시 시작";
		g.drawString(sub, (getWidth() - fm.stringWidth(sub)) / 2, boxY + 47);
	}

	private void drawDodger(Graphics g, int x, int y) {
		int w = squareWidth();
		int h = squareHeight();
		g.setColor(dodger.isCrushed() ? Color.GRAY : new Color(255, 140, 0));
		g.fillOval(x + 1, y + 1, w - 2, h - 2);
		g.setColor(Color.BLACK);
		g.drawOval(x + 1, y + 1, w - 2, h - 2);
	}

	private static final Color[] COLORS = { new Color(0, 0, 0), new Color(204, 102, 102), new Color(102, 204, 102),
			new Color(102, 102, 204), new Color(204, 204, 102), new Color(204, 102, 204), new Color(102, 204, 204),
			new Color(218, 170, 0) };

	private void drawSquare(Graphics g, int x, int y, Tetrominoes shape) {
		Color color = COLORS[shape.ordinal()];

		g.setColor(color);
		g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);

		g.setColor(color.brighter());
		g.drawLine(x, y + squareHeight() - 1, x, y);
		g.drawLine(x, y, x + squareWidth() - 1, y);

		g.setColor(color.darker());
		g.drawLine(x + 1, y + squareHeight() - 1, x + squareWidth() - 1, y + squareHeight() - 1);
		g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1, x + squareWidth() - 1, y + 1);
	}
}
