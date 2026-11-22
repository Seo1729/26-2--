package dodge;

import java.awt.*;
import java.util.function.IntSupplier;

import javax.swing.*;

/**
 * 오른쪽 정보판 (남은 시간, 다음 블록, 점수판).
 * 게임 내부는 모르고 읽기 전용 GameStatus만 받는다.
 */
public class InfoPanel extends JPanel {

	/** 정보판 폭. DodgeMode가 왼쪽 빈칸도 같은 폭으로 둬서 보드를 가운데 맞춘다. */
	public static final int WIDTH = 280;

	private static final int MARGIN = 12;
	private static final int PADDING = 18;
	private static final int CARD_GAP = 18;
	private static final int CARD_RADIUS = 16;
	private static final int TIME_CARD_H = 140;
	private static final int NEXT_CARD_H = 190;
	private static final int SCORE_CARD_H = 300;
	private static final int PREVIEW_CELL = 32;
	/** 남은 시간이 이 초 이하면 빨간색으로 재촉한다 */
	private static final int HURRY_SECONDS = 10;

	private final GameStatus status;
	private final IntSupplier boardTop;

	/**
	 * @param status   보여 줄 게임 상태
	 * @param boardTop 게임 보드 위쪽 y (카드 높이를 보드에 맞추려고)
	 */
	public InfoPanel(GameStatus status, IntSupplier boardTop) {
		this.status = status;
		this.boardTop = boardTop;
		setPreferredSize(new Dimension(WIDTH, 0));
		setBackground(Theme.BACKGROUND);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		int x = MARGIN;
		int w = getWidth() - MARGIN * 2;
		int y = boardTop.getAsInt();

		drawTimeCard(g2, x, y, w);
		y += TIME_CARD_H + CARD_GAP;
		drawNextCard(g2, x, y, w);
		y += NEXT_CARD_H + CARD_GAP;
		drawScoreCard(g2, x, y, w);
	}

	// 카드 배경과 제목
	private void drawCard(Graphics2D g, int x, int y, int w, int h, String title) {
		g.setColor(Theme.CARD);
		g.fillRoundRect(x, y, w, h, CARD_RADIUS, CARD_RADIUS);
		g.setColor(Theme.SUBTEXT);
		g.setFont(Theme.font(Font.BOLD, 15f));
		g.drawString(title, x + PADDING, y + 30);
	}

	private void drawTimeCard(Graphics2D g, int x, int y, int w) {
		drawCard(g, x, y, w, TIME_CARD_H, "남은 시간");

		int elapsed = status.getElapsedMs();
		int remainingMs = GameRules.remainingMs(elapsed);
		int seconds = GameRules.remainingSeconds(elapsed);
		boolean hurry = seconds <= HURRY_SECONDS;

		String text = String.format("%d:%02d", seconds / 60, seconds % 60);
		g.setColor(hurry ? Theme.WARNING : Theme.TEXT);
		g.setFont(Theme.font(Font.BOLD, 46f));
		g.drawString(text, x + PADDING, y + 88);

		drawBar(g, x + PADDING, y + 108, w - PADDING * 2, 12, remainingMs, GameRules.TIME_LIMIT_MS,
				hurry ? Theme.WARNING : Theme.DODGER);
	}

	private void drawNextCard(Graphics2D g, int x, int y, int w) {
		drawCard(g, x, y, w, NEXT_CARD_H, "다음 블록");

		Shape next = status.getNextPiece();
		if (next.isEmpty())
			return;
		int pieceW = (next.maxX() - next.minX() + 1) * PREVIEW_CELL;
		int pieceH = (next.maxY() - next.minY() + 1) * PREVIEW_CELL;
		int areaTop = y + 44;
		int areaH = NEXT_CARD_H - 44 - 14;
		int left = x + (w - pieceW) / 2;
		int top = areaTop + (areaH - pieceH) / 2;
		for (int i = 0; i < Shape.CELLS; ++i) {
			Theme.drawSquare(g, left + (next.x(i) - next.minX()) * PREVIEW_CELL,
					top + (next.y(i) - next.minY()) * PREVIEW_CELL, PREVIEW_CELL, PREVIEW_CELL, next.getShape());
		}
	}

	private void drawScoreCard(Graphics2D g, int x, int y, int w) {
		drawCard(g, x, y, w, SCORE_CARD_H, "점수판");

		int left = x + PADDING;
		int right = x + w - PADDING;

		drawLabel(g, "이번 판", left, y + 62);
		drawScoreRow(g, left, right, y + 94, "P1 테트리스", "지운 줄", status.getLinesRemoved(),
				GameRules.TARGET_LINES, Theme.TETRIS);
		drawScoreRow(g, left, right, y + 150, "P2 고양이", "높이", status.getDodgerHeight(), GameRules.GOAL_ROW,
				Theme.DODGER);

		g.setColor(Theme.BORDER);
		g.drawLine(left, y + 186, right, y + 186);

		drawLabel(g, "전적", left, y + 212);

		// 전적 "테트리스 n승 : 고양이 m승"
		int baseline = y + 262;
		int mid = (left + right) / 2;
		g.setFont(Theme.font(Font.BOLD, 26f));
		g.setColor(Theme.TEXT);
		FontMetrics fm = g.getFontMetrics();
		g.drawString(":", mid - fm.stringWidth(":") / 2, baseline);
		drawWins(g, left, mid - 10, baseline, "테트리스", status.getTetrisWins(), Theme.TETRIS);
		drawWins(g, mid + 10, right, baseline, "고양이", status.getDodgerWins(), Theme.DODGER);
	}

	private static void drawLabel(Graphics2D g, String text, int x, int baseline) {
		g.setFont(Theme.font(Font.PLAIN, 13f));
		g.setColor(Theme.SUBTEXT);
		g.drawString(text, x, baseline);
	}

	// 둥근 진행 막대 (value / max 만큼 채움)
	private static void drawBar(Graphics2D g, int x, int y, int w, int h, int value, int max, Color color) {
		g.setColor(Theme.BOARD);
		g.fillRoundRect(x, y, w, h, h, h);
		int fill = (int) ((long) w * Math.max(0, Math.min(value, max)) / max);
		g.setColor(color);
		if (fill > 0)
			g.fillRoundRect(x, y, fill, h, h, h);
	}

	private void drawScoreRow(Graphics2D g, int left, int right, int baseline, String who, String what, int value,
			int max, Color color) {
		g.setFont(Theme.font(Font.BOLD, 15f));
		g.setColor(color);
		g.drawString(who, left, baseline);
		FontMetrics fm = g.getFontMetrics();
		g.setFont(Theme.font(Font.PLAIN, 12f));
		g.setColor(Theme.SUBTEXT);
		g.drawString(what, left + fm.stringWidth(who) + 8, baseline);

		String text = value + " / " + max;
		g.setFont(Theme.font(Font.BOLD, 17f));
		g.setColor(Theme.TEXT);
		fm = g.getFontMetrics();
		g.drawString(text, right - fm.stringWidth(text), baseline);

		drawBar(g, left, baseline + 10, right - left, 8, value, max, color);
	}

	private void drawWins(Graphics2D g, int from, int to, int baseline, String who, int wins, Color color) {
		Font smallFont = Theme.font(Font.PLAIN, 13f);
		Font bigFont = Theme.font(Font.BOLD, 30f);
		FontMetrics small = g.getFontMetrics(smallFont);
		FontMetrics big = g.getFontMetrics(bigFont);
		String num = String.valueOf(wins);
		String label = who + " ";
		String unit = "승";
		int total = small.stringWidth(label) + big.stringWidth(num) + small.stringWidth(unit);
		int x = from + (to - from - total) / 2;

		g.setFont(smallFont);
		g.setColor(Theme.SUBTEXT);
		g.drawString(label, x, baseline);
		x += small.stringWidth(label);
		g.setFont(bigFont);
		g.setColor(color);
		g.drawString(num, x, baseline);
		x += big.stringWidth(num);
		g.setFont(smallFont);
		g.setColor(Theme.SUBTEXT);
		g.drawString(unit, x, baseline);
	}
}
