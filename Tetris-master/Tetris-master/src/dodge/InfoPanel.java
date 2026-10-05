package dodge;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * 보드 오른쪽 정보판. 위에서부터 남은 시간, 다음 블록, 점수판을 보여 준다.
 */
public class InfoPanel extends JPanel {

	/** 정보판 너비. 보드를 창 가운데 두려고 왼쪽에도 같은 너비의 빈 공간을 둔다. */
	public static final int PANEL_WIDTH = 280;

	private static final int MARGIN = 24;
	private static final int GAP = 18;
	private static final int TIME_CARD_H = 140;
	private static final int NEXT_CARD_H = 190;
	private static final int SCORE_CARD_H = 300;
	private static final int NEXT_CELL = 32;

	private final GamePanel game;

	public InfoPanel(GamePanel game) {
		this.game = game;
		setPreferredSize(new Dimension(PANEL_WIDTH, 0));
		setBackground(Theme.BACKGROUND);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		int x = MARGIN / 2;
		int w = getWidth() - MARGIN;
		// 카드 맨 위를 보드 맨 위와 맞춘다
		int y = game.getBoardTop();

		drawTimeCard(g2, x, y, w);
		y += TIME_CARD_H + GAP;
		drawNextCard(g2, x, y, w);
		y += NEXT_CARD_H + GAP;
		drawScoreCard(g2, x, y, w);
	}

	private void drawCard(Graphics2D g, int x, int y, int w, int h, String title) {
		g.setColor(Theme.CARD);
		g.fillRoundRect(x, y, w, h, 16, 16);
		g.setColor(Theme.SUBTEXT);
		g.setFont(Theme.font(Font.BOLD, 15f));
		g.drawString(title, x + 18, y + 30);
	}

	private void drawTimeCard(Graphics2D g, int x, int y, int w) {
		drawCard(g, x, y, w, TIME_CARD_H, "남은 시간");

		int remainingMs = Math.max(0, GameRules.TIME_LIMIT_MS - game.getPlayElapsed());
		int seconds = GameRules.remainingSeconds(game.getPlayElapsed());
		boolean hurry = seconds <= 10;
		Color color = hurry ? Theme.WARNING : Theme.TEXT;

		String text = String.format("%d:%02d", seconds / 60, seconds % 60);
		g.setColor(color);
		g.setFont(Theme.font(Font.BOLD, 46f));
		g.drawString(text, x + 18, y + 88);

		// 시간이 줄어드는 막대
		int barX = x + 18;
		int barY = y + 108;
		int barW = w - 36;
		int barH = 12;
		g.setColor(Theme.BOARD);
		g.fillRoundRect(barX, barY, barW, barH, barH, barH);
		int fill = (int) ((long) barW * remainingMs / GameRules.TIME_LIMIT_MS);
		g.setColor(hurry ? Theme.WARNING : Theme.DODGER);
		if (fill > 0)
			g.fillRoundRect(barX, barY, fill, barH, barH, barH);
	}

	private void drawNextCard(Graphics2D g, int x, int y, int w) {
		drawCard(g, x, y, w, NEXT_CARD_H, "다음 블록");

		Shape next = game.getTetris().getNextPiece();
		if (next.getShape() != Tetrominoes.NoShape) {
			int minX = next.x(0), maxX = next.x(0), minY = next.y(0), maxY = next.y(0);
			for (int i = 1; i < 4; ++i) {
				minX = Math.min(minX, next.x(i));
				maxX = Math.max(maxX, next.x(i));
				minY = Math.min(minY, next.y(i));
				maxY = Math.max(maxY, next.y(i));
			}
			int pieceW = (maxX - minX + 1) * NEXT_CELL;
			int pieceH = (maxY - minY + 1) * NEXT_CELL;
			int areaTop = y + 44;
			int areaH = NEXT_CARD_H - 44 - 14;
			int left = x + (w - pieceW) / 2;
			int top = areaTop + (areaH - pieceH) / 2;
			// 보드와 같은 방향으로 보이게: 조각의 y가 클수록 화면 아래
			for (int i = 0; i < 4; ++i) {
				Theme.drawSquare(g, left + (next.x(i) - minX) * NEXT_CELL, top + (next.y(i) - minY) * NEXT_CELL,
						NEXT_CELL, NEXT_CELL, next.getShape());
			}
		}
	}

	private void drawScoreCard(Graphics2D g, int x, int y, int w) {
		drawCard(g, x, y, w, SCORE_CARD_H, "점수판");

		int left = x + 18;
		int right = x + w - 18;

		g.setFont(Theme.font(Font.PLAIN, 13f));
		g.setColor(Theme.SUBTEXT);
		g.drawString("이번 판", left, y + 62);

		int lines = game.getTetris().getLinesRemoved();
		drawScoreRow(g, left, right, y + 94, "P1 테트리스", "지운 줄", lines, GameRules.TARGET_LINES, Theme.TETRIS);
		// 바닥이 0줄이라 화면에서 보는 높이와 맞게 그대로 쓴다
		int height = game.getDodger().getY();
		drawScoreRow(g, left, right, y + 150, "P2 고양이", "높이", height, GameRules.GOAL_ROW, Theme.DODGER);

		g.setColor(Theme.BORDER);
		g.drawLine(left, y + 186, right, y + 186);

		g.setFont(Theme.font(Font.PLAIN, 13f));
		g.setColor(Theme.SUBTEXT);
		g.drawString("전적", left, y + 212);

		// 전적: "테트리스 n승 : 고양이 m승"
		int baseline = y + 262;
		int mid = (left + right) / 2;
		g.setFont(Theme.font(Font.BOLD, 26f));
		g.setColor(Theme.TEXT);
		FontMetrics fm = g.getFontMetrics();
		g.drawString(":", mid - fm.stringWidth(":") / 2, baseline);
		drawWins(g, left, mid - 10, baseline, "테트리스", game.getTetrisWins(), Theme.TETRIS);
		drawWins(g, mid + 10, right, baseline, "고양이", game.getDodgerWins(), Theme.DODGER);
	}

	/** 이름, 값 "3 / 10", 그 아래 진행 막대 */
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

		int barY = baseline + 10;
		int barW = right - left;
		int barH = 8;
		g.setColor(Theme.BOARD);
		g.fillRoundRect(left, barY, barW, barH, barH, barH);
		int fill = (int) ((long) barW * Math.min(value, max) / max);
		g.setColor(color);
		if (fill > 0)
			g.fillRoundRect(left, barY, fill, barH, barH, barH);
	}

	/** 전적 한쪽: 큰 숫자 + "승", 가운데 쪽으로 붙인다 */
	private void drawWins(Graphics2D g, int from, int to, int baseline, String who, int wins, Color color) {
		g.setFont(Theme.font(Font.PLAIN, 13f));
		FontMetrics small = g.getFontMetrics();
		g.setFont(Theme.font(Font.BOLD, 30f));
		FontMetrics big = g.getFontMetrics();
		String num = String.valueOf(wins);
		String label = who + " ";
		String unit = "승";
		int total = small.stringWidth(label) + big.stringWidth(num) + small.stringWidth(unit);
		int x = from + (to - from - total) / 2;

		g.setFont(Theme.font(Font.PLAIN, 13f));
		g.setColor(Theme.SUBTEXT);
		g.drawString(label, x, baseline);
		x += small.stringWidth(label);
		g.setFont(Theme.font(Font.BOLD, 30f));
		g.setColor(color);
		g.drawString(num, x, baseline);
		x += big.stringWidth(num);
		g.setFont(Theme.font(Font.PLAIN, 13f));
		g.setColor(Theme.SUBTEXT);
		g.drawString(unit, x, baseline);
	}
}
