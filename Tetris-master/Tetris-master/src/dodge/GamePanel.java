package kr.ac.jbnu.se.tetris.dodge;

import kr.ac.jbnu.se.tetris.Shape;
import kr.ac.jbnu.se.tetris.Tetrominoes;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * 게임 화면. 타이머(게임 루프)를 돌리고 보드와 현재 조각을 그린다.
 * 보드는 칸을 정사각형으로 유지하며 이 패널 가운데에 그린다.
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
	private final DodgerSprite dodgerSprite = new DodgerSprite();
	private final InputHandler input;
	private final Timer timer;
	/** 화면이 바뀔 때마다 부를 일 (정보판 다시 그리기) */
	private Runnable onRefresh;
	private boolean isStarted = false;
	private boolean isPaused = false;
	private int fallElapsed = 0;
	private int dodgerElapsed = 0;
	/** 게임 시작 후 흐른 시간(ms). 일시정지 중에는 늘지 않는다. */
	private int playElapsed = 0;
	private GameRules.Result result = GameRules.Result.NONE;
	/** 전적. R로 다시 시작해도 유지된다. */
	private int tetrisWins = 0;
	private int dodgerWins = 0;

	public GamePanel() {
		setFocusable(true);
		setBackground(Theme.BACKGROUND);
		timer = new Timer(FRAME_MS, this);
		tetris.setDodger(dodger);
		input = new InputHandler(this, tetris, dodger);
		addKeyListener(input);
	}

	public void setOnRefresh(Runnable onRefresh) {
		this.onRefresh = onRefresh;
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

	/** 승패를 판정하고 다시 그린다 */
	public void refresh() {
		if (isStarted && result == GameRules.Result.NONE) {
			result = GameRules.judge(tetris, dodger, playElapsed);
			if (result != GameRules.Result.NONE) {
				isStarted = false;
				timer.stop();
				input.clear();
				if (result.isTetrisWin())
					++tetrisWins;
				else
					++dodgerWins;
			}
		}
		repaint();
		if (onRefresh != null)
			onRefresh.run();
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

	public TetrisPlayer getTetris() {
		return tetris;
	}

	public Dodger getDodger() {
		return dodger;
	}

	public int getPlayElapsed() {
		return playElapsed;
	}

	public int getTetrisWins() {
		return tetrisWins;
	}

	public int getDodgerWins() {
		return dodgerWins;
	}

	/** 정사각형 한 칸 크기. 패널에 보드 전체가 들어가는 가장 큰 값. */
	private int cellSize() {
		return Math.max(1, Math.min(getWidth() / GameBoard.WIDTH, getHeight() / GameBoard.HEIGHT));
	}

	private int boardLeft() {
		return (getWidth() - GameBoard.WIDTH * cellSize()) / 2;
	}

	/** 보드 맨 위 y 좌표 (정보판이 높이를 맞출 때 쓴다) */
	public int getBoardTop() {
		return (getHeight() - GameBoard.HEIGHT * cellSize()) / 2;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		int cell = cellSize();
		int left = boardLeft();
		int top = getBoardTop();
		int boardW = GameBoard.WIDTH * cell;
		int boardH = GameBoard.HEIGHT * cell;

		g.setColor(Theme.BOARD);
		g.fillRect(left, top, boardW, boardH);

		// 골 라인: 이 줄에 닿으면 똥피하기 승리
		int goalTop = top + (GameBoard.HEIGHT - GameRules.GOAL_ROW - 1) * cell;
		g.setColor(Theme.GOAL_FILL);
		g.fillRect(left, goalTop, boardW, cell);
		g.setColor(Theme.GOAL_LINE);
		g.drawLine(left, goalTop + cell - 1, left + boardW - 1, goalTop + cell - 1);

		for (int i = 0; i < GameBoard.HEIGHT; ++i) {
			for (int j = 0; j < GameBoard.WIDTH; ++j) {
				Tetrominoes shape = board.shapeAt(j, GameBoard.HEIGHT - i - 1);
				if (shape != Tetrominoes.NoShape)
					Theme.drawSquare(g, left + j * cell, top + i * cell, cell, cell, shape);
			}
		}

		if (tetris.hasPiece()) {
			Shape piece = tetris.getPiece();
			for (int i = 0; i < 4; ++i) {
				int x = tetris.getX() + piece.x(i);
				int y = tetris.getY() - piece.y(i);
				Theme.drawSquare(g, left + x * cell, top + (GameBoard.HEIGHT - y - 1) * cell, cell, cell,
						piece.getShape());
			}
		}

		dodgerSprite.draw(g, dodger, left + dodger.getX() * cell,
				top + (GameBoard.HEIGHT - dodger.getY() - 1) * cell, cell, cell);

		g.setColor(Theme.BORDER);
		g.drawRect(left - 1, top - 1, boardW + 1, boardH + 1);

		if (result != GameRules.Result.NONE) {
			String title = result.isTetrisWin() ? "테트리스 승리!" : "똥피하기 승리!";
			drawOverlay(g, left, top, boardW, boardH, title, result.getMessage(), "R: 다시 시작");
		} else if (isPaused) {
			drawOverlay(g, left, top, boardW, boardH, "일시정지", null, "P: 계속하기");
		}
	}

	/** 보드 가운데에 반투명 띠를 깔고 큰 제목과 안내 문구를 띄운다 */
	private void drawOverlay(Graphics g, int left, int top, int boardW, int boardH, String title, String message,
			String hint) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		int boxH = message != null ? 130 : 100;
		int boxY = top + boardH / 2 - boxH / 2;
		g2.setColor(new Color(0, 0, 0, 180));
		g2.fillRect(left, boxY, boardW, boxH);

		int y = boxY + 46;
		g2.setColor(Color.WHITE);
		g2.setFont(Theme.font(Font.BOLD, 30f));
		drawCentered(g2, title, left, boardW, y);
		if (message != null) {
			y += 34;
			g2.setColor(Theme.TEXT);
			g2.setFont(Theme.font(Font.PLAIN, 15f));
			drawCentered(g2, message, left, boardW, y);
		}
		y += 30;
		g2.setColor(Theme.SUBTEXT);
		g2.setFont(Theme.font(Font.PLAIN, 14f));
		drawCentered(g2, hint, left, boardW, y);
	}

	private static void drawCentered(Graphics g, String text, int left, int width, int baseline) {
		FontMetrics fm = g.getFontMetrics();
		g.drawString(text, left + (width - fm.stringWidth(text)) / 2, baseline);
	}
}
