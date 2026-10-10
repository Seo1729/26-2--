package dodge;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;

import common.Bgm;
import common.SoundManager;

// 똥피하기 게임 화면
// 보드, 테트리스(P1), 키 입력, 승패 판정 다 여기서 함
public class GamePanel extends JPanel implements ActionListener {

	// 보드 (가로 10, 세로 22) y는 바닥이 0
	int[] cells = new int[10 * 22];

	// P1 테트리스
	Shape curPiece = new Shape();
	Shape nextPiece = new Shape();
	int curX = 0;
	int curY = 0;
	boolean isFallingFinished = false;
	boolean isGameOver = false;
	int numLinesRemoved = 0;

	// P2 고양이
	Dodger dodger = new Dodger(this);
	DodgerSprite dodgerSprite = new DodgerSprite();

	InfoPanel info; // 오른쪽 정보판
	Timer timer;
	boolean isStarted = false;
	boolean isPaused = false;
	int fallElapsed = 0;
	int dodgerElapsed = 0;
	int playElapsed = 0;
	// 결과 0 아직, 1 압사, 2 10줄, 3 꼭대기, 4 시간끝, 5 블록꽉참
	int result = 0;
	int tetrisWins = 0;
	int dodgerWins = 0;

	// 키 (눌려있는 키, 반복 시간)
	HashSet<Integer> held = new HashSet<Integer>();
	HashMap<Integer, Long> nextRepeat = new HashMap<Integer, Long>();

	int debugCount = 0;

	static Color[] colors = { new Color(0, 0, 0), new Color(204, 102, 102), new Color(102, 204, 102),
			new Color(102, 102, 204), new Color(204, 204, 102), new Color(204, 102, 204), new Color(102, 204, 204),
			new Color(218, 170, 0) };

	public GamePanel() {
		setFocusable(true);
		setBackground(new Color(32, 34, 44));
		timer = new Timer(20, this);

		for (int i = 0; i < 10 * 22; i++)
			cells[i] = 0;

		// 포커스 잃으면 키 다 뗀걸로
		addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				held.clear();
				nextRepeat.clear();
			}
		});

		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int code = e.getKeyCode();
				// 이미 눌려있으면 무시 (OS 자동반복)
				if (!held.add(code))
					return;

				if (code == KeyEvent.VK_P) {
					togglePause();
					return;
				}
				if (code == KeyEvent.VK_R) {
					restart();
					return;
				}
				if (!(isStarted && !isPaused))
					return;

				act(code);
				if (code == KeyEvent.VK_A || code == KeyEvent.VK_D || code == KeyEvent.VK_S
						|| code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT)
					nextRepeat.put(code, System.currentTimeMillis() + 170);
				repaint();
			}

			public void keyReleased(KeyEvent e) {
				held.remove(e.getKeyCode());
				nextRepeat.remove(e.getKeyCode());
			}
		});
	}

	public void start() {
		if (isPaused)
			return;

		requestFocusInWindow();
		SoundManager.startBgm(Bgm.GAME);
		isStarted = true;
		fallElapsed = 0;
		dodgerElapsed = 0;
		playElapsed = 0;
		result = 0;
		for (int i = 0; i < 10 * 22; i++)
			cells[i] = 0;
		dodger.reset();

		// 테트리스 시작
		isFallingFinished = false;
		isGameOver = false;
		numLinesRemoved = 0;
		nextPiece.setRandomShape();
		newPiece();

		held.clear();
		nextRepeat.clear();
		timer.start();
		refresh();
	}

	public void actionPerformed(ActionEvent e) {
		// 꾹 누르고 있는 키 반복
		if (isStarted && !isPaused) {
			long now = System.currentTimeMillis();
			for (Map.Entry<Integer, Long> entry : nextRepeat.entrySet()) {
				if (now >= entry.getValue()) {
					int code = entry.getKey();
					act(code);
					if (code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT)
						entry.setValue(now + 90);
					else
						entry.setValue(now + 50);
				}
			}
		}

		playElapsed += 20;
		fallElapsed += 20;
		if (fallElapsed >= 400) {
			fallElapsed = 0;
			// 테트리스 한칸 내리기
			if (isGameOver == false) {
				if (isFallingFinished) {
					isFallingFinished = false;
					newPiece();
				} else {
					oneLineDown();
				}
			}
			dodger.resolveOverlap();
		}

		dodgerElapsed += 20;
		if (dodgerElapsed >= 80) {
			dodgerElapsed = 0;
			dodger.physicsStep();
		}
		dodger.updateDraw(20);

		refresh();
	}

	void act(int code) {
		switch (code) {
		case KeyEvent.VK_A:
			tryMove(curPiece, curX - 1, curY);
			break;
		case KeyEvent.VK_D:
			tryMove(curPiece, curX + 1, curY);
			break;
		case KeyEvent.VK_W:
			tryMove(curPiece.rotateLeft(), curX, curY);
			break;
		case KeyEvent.VK_S:
			oneLineDown();
			break;
		case KeyEvent.VK_LEFT:
			dodger.moveLeft();
			break;
		case KeyEvent.VK_RIGHT:
			dodger.moveRight();
			break;
		case KeyEvent.VK_UP:
			dodger.jump();
			break;
		}
	}

	public void togglePause() {
		if (!isStarted)
			return;

		isPaused = !isPaused;
		if (isPaused)
			timer.stop();
		else
			timer.start();
		held.clear();
		nextRepeat.clear();
		refresh();
	}

	public void stop() {
		SoundManager.stopBgm();
		timer.stop();
		isStarted = false;
		isPaused = false;
		held.clear();
		nextRepeat.clear();
	}

	// 승패 확인하고 다시 그리기
	public void refresh() {
		if (isStarted && result == 0) {
			if (dodger.crushed)
				result = 1;
			else if (numLinesRemoved >= 10)
				result = 2;
			else if (dodger.y >= 22 - 4)
				result = 3;
			else if (isGameOver)
				result = 5;
			else if (playElapsed >= 120000)
				result = 4;
			else
				result = 0;

			if (result != 0) {
				isStarted = false;
				SoundManager.stopBgm();
				timer.stop();
				held.clear();
				nextRepeat.clear();
				if (result == 1 || result == 2)
					++tetrisWins;
				else
					++dodgerWins;
			}
		}
		repaint();
		if (info != null)
			info.repaint();
	}

	public void restart() {
		if (result != 0)
			start();
	}

	// ---------------- 테트리스 ----------------

	public void oneLineDown() {
		if (isGameOver || curPiece.getShape() == 0)
			return;
		// 내려가는 자리에 고양이 있으면 깔림
		if (canPlace(curPiece, curX, curY - 1) && covers(curPiece, curX, curY - 1)) {
			dodger.crush();
			return;
		}
		if (!tryMove(curPiece, curX, curY - 1)) {
			// 바닥에 닿음
			place(curPiece, curX, curY);
			int lines = removeFullLines();
			if (lines > 0) {
				numLinesRemoved += lines;
				isFallingFinished = true;
				curPiece.setShape(0);
			} else {
				newPiece();
			}
		}
	}

	void newPiece() {
		curPiece.setShape(nextPiece.getShape());
		nextPiece.setRandomShape();
		curX = 10 / 2 + 1;
		curY = 22 - 1 + curPiece.minY();

		if (!canPlace(curPiece, curX, curY)) {
			curPiece.setShape(0);
			isGameOver = true;
		}
	}

	boolean tryMove(Shape newPiece, int newX, int newY) {
		if (isGameOver || curPiece.getShape() == 0)
			return false;
		// 옆으로 밀거나 돌려서 고양이 덮는건 안됨
		if (!canPlace(newPiece, newX, newY) || covers(newPiece, newX, newY))
			return false;
		curPiece = newPiece;
		curX = newX;
		curY = newY;
		return true;
	}

	boolean covers(Shape piece, int posX, int posY) {
		for (int i = 0; i < 4; ++i) {
			if (posX + piece.x(i) == dodger.x && posY - piece.y(i) == dodger.y)
				return true;
		}
		return false;
	}

	// 떨어지는 블록이 (x, y)에 있나
	public boolean occupies(int x, int y) {
		if (curPiece.getShape() == 0)
			return false;
		for (int i = 0; i < 4; ++i) {
			if (curX + curPiece.x(i) == x && curY - curPiece.y(i) == y)
				return true;
		}
		return false;
	}

	public void rotateRight() {
		tryMove(curPiece.rotateRight(), curX, curY);
	}

	// 하드드롭은 안넣기로 함
//	public void dropDown() {
//		int newY = curY;
//		while (newY > 0) {
//			if (!tryMove(curPiece, curX, newY - 1))
//				break;
//			--newY;
//		}
//	}

	// ---------------- 보드 ----------------

	public boolean isFree(int x, int y) {
		if (x >= 0 && x < 10 && y >= 0 && y < 22) {
			if (cells[(y * 10) + x] == 0)
				return true;
		}
		return false;
	}

	boolean canPlace(Shape piece, int posX, int posY) {
		for (int i = 0; i < 4; ++i) {
			if (!isFree(posX + piece.x(i), posY - piece.y(i)))
				return false;
		}
		return true;
	}

	void place(Shape piece, int posX, int posY) {
		for (int i = 0; i < 4; ++i) {
			int x = posX + piece.x(i);
			int y = posY - piece.y(i);
			cells[(y * 10) + x] = piece.getShape();
		}
	}

	int removeFullLines() {
		int numFullLines = 0;

		for (int i = 22 - 1; i >= 0; --i) {
			boolean lineIsFull = true;

			for (int j = 0; j < 10; ++j) {
				if (cells[(i * 10) + j] == 0) {
					lineIsFull = false;
					break;
				}
			}

			if (lineIsFull) {
				++numFullLines;
				for (int k = i; k < 22 - 1; ++k) {
					for (int j = 0; j < 10; ++j)
						cells[(k * 10) + j] = cells[((k + 1) * 10) + j];
				}
				for (int j = 0; j < 10; ++j)
					cells[((22 - 1) * 10) + j] = 0;
			}
		}
		return numFullLines;
	}

	// ---------------- 그리기 ----------------

	public int getBoardTop() {
		int cell = Math.max(1, Math.min(getWidth() / 10, getHeight() / 22));
		return (getHeight() - 22 * cell) / 2;
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		int cell = Math.max(1, Math.min(getWidth() / 10, getHeight() / 22));
		int left = (getWidth() - 10 * cell) / 2;
		int top = (getHeight() - 22 * cell) / 2;
		int boardW = 10 * cell;
		int boardH = 22 * cell;

		g.setColor(new Color(18, 19, 26));
		g.fillRect(left, top, boardW, boardH);

		// 골 라인 (여기 닿으면 고양이 승)
		int goalTop = top + (22 - 18 - 1) * cell;
		g.setColor(new Color(255, 220, 120, 60));
		g.fillRect(left, goalTop, boardW, cell);
		g.setColor(new Color(230, 170, 30));
		g.drawLine(left, goalTop + cell - 1, left + boardW - 1, goalTop + cell - 1);

		for (int i = 0; i < 22; ++i) {
			for (int j = 0; j < 10; ++j) {
				int shape = cells[((22 - i - 1) * 10) + j];
				if (shape != 0)
					drawSquare(g, left + j * cell, top + i * cell, cell, cell, shape);
			}
		}

		if (curPiece.getShape() != 0) {
			for (int i = 0; i < 4; ++i) {
				int x = curX + curPiece.x(i);
				int y = curY - curPiece.y(i);
				drawSquare(g, left + x * cell, top + (22 - y - 1) * cell, cell, cell, curPiece.getShape());
			}
		}

		dodgerSprite.draw(g, dodger, left + (int) Math.round(dodger.drawX * cell),
				top + (int) Math.round((22 - dodger.drawY - 1) * cell), cell, cell);

		g.setColor(new Color(90, 94, 115));
		g.drawRect(left - 1, top - 1, boardW + 1, boardH + 1);

		if (result != 0) {
			String title;
			if (result == 1 || result == 2)
				title = "테트리스 승리!";
			else
				title = "똥피하기 승리!";
			String msg = "";
			if (result == 1)
				msg = "압사! 테트리스 승리";
			else if (result == 2)
				msg = "목표 10줄 달성! 테트리스 승리";
			else if (result == 3)
				msg = "꼭대기 도달! 똥피하기 승리";
			else if (result == 4)
				msg = "시간 종료까지 생존! 똥피하기 승리";
			else if (result == 5)
				msg = "블록이 꼭대기까지 쌓임! 똥피하기 승리";
			drawOverlay(g, left, top, boardW, boardH, title, msg, "R: 다시 시작");
		} else if (isPaused) {
			drawOverlay(g, left, top, boardW, boardH, "일시정지", null, "P: 계속하기");
		}
	}

	void drawOverlay(Graphics g, int left, int top, int boardW, int boardH, String title, String message,
			String hint) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		int boxH = 100;
		if (message != null)
			boxH = 130;
		int boxY = top + boardH / 2 - boxH / 2;
		g2.setColor(new Color(0, 0, 0, 180));
		g2.fillRect(left, boxY, boardW, boxH);

		int y = boxY + 46;
		g2.setColor(Color.WHITE);
		g2.setFont(new Font("Malgun Gothic", Font.BOLD, 12).deriveFont(30f));
		FontMetrics fm = g2.getFontMetrics();
		g2.drawString(title, left + (boardW - fm.stringWidth(title)) / 2, y);
		if (message != null) {
			y += 34;
			g2.setColor(new Color(235, 236, 242));
			g2.setFont(new Font("Malgun Gothic", Font.PLAIN, 12).deriveFont(15f));
			fm = g2.getFontMetrics();
			g2.drawString(message, left + (boardW - fm.stringWidth(message)) / 2, y);
		}
		y += 30;
		g2.setColor(new Color(160, 164, 185));
		g2.setFont(new Font("Malgun Gothic", Font.PLAIN, 12).deriveFont(14f));
		fm = g2.getFontMetrics();
		g2.drawString(hint, left + (boardW - fm.stringWidth(hint)) / 2, y);
	}

	// 블록 한칸 그리기 (InfoPanel에도 똑같은거 있음)
	static void drawSquare(Graphics g, int x, int y, int w, int h, int shape) {
		Color color = colors[shape];

		g.setColor(color);
		g.fillRect(x + 1, y + 1, w - 2, h - 2);

		g.setColor(color.brighter());
		g.drawLine(x, y + h - 1, x, y);
		g.drawLine(x, y, x + w - 1, y);

		g.setColor(color.darker());
		g.drawLine(x + 1, y + h - 1, x + w - 1, y + h - 1);
		g.drawLine(x + w - 1, y + h - 1, x + w - 1, y + 1);
	}
}
