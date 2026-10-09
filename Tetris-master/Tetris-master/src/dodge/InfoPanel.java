package dodge;

import java.awt.*;

import javax.swing.*;

// 오른쪽 정보판 (남은시간, 다음블록, 점수판)
public class InfoPanel extends JPanel {

	GamePanel game;

	static Color[] colors = { new Color(0, 0, 0), new Color(204, 102, 102), new Color(102, 204, 102),
			new Color(102, 102, 204), new Color(204, 204, 102), new Color(204, 102, 204), new Color(102, 204, 204),
			new Color(218, 170, 0) };

	public InfoPanel(GamePanel game) {
		this.game = game;
		setPreferredSize(new Dimension(280, 0));
		setBackground(new Color(32, 34, 44));
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		int x = 24 / 2;
		int w = getWidth() - 24;
		int y = game.getBoardTop(); // 보드 위쪽이랑 맞춤

		// ---- 남은 시간 ----
		g2.setColor(new Color(48, 51, 66));
		g2.fillRoundRect(x, y, w, 140, 16, 16);
		g2.setColor(new Color(160, 164, 185));
		g2.setFont(new Font("Malgun Gothic", Font.BOLD, 12).deriveFont(15f));
		g2.drawString("남은 시간", x + 18, y + 30);

		int remainingMs = Math.max(0, 120000 - game.playElapsed);
		int seconds = Math.max(0, (120000 - game.playElapsed + 999) / 1000);
		boolean hurry = false;
		if (seconds <= 10)
			hurry = true;
		Color color;
		if (hurry)
			color = new Color(255, 90, 90);
		else
			color = new Color(235, 236, 242);

		String text = String.format("%d:%02d", seconds / 60, seconds % 60);
		g2.setColor(color);
		g2.setFont(new Font("Malgun Gothic", Font.BOLD, 12).deriveFont(46f));
		g2.drawString(text, x + 18, y + 88);

		int barX = x + 18;
		int barY = y + 108;
		int barW = w - 36;
		int barH = 12;
		g2.setColor(new Color(18, 19, 26));
		g2.fillRoundRect(barX, barY, barW, barH, barH, barH);
		int fill = (int) ((long) barW * remainingMs / 120000);
		if (hurry)
			g2.setColor(new Color(255, 90, 90));
		else
			g2.setColor(new Color(255, 170, 60));
		if (fill > 0)
			g2.fillRoundRect(barX, barY, fill, barH, barH, barH);

		y = y + 140 + 18;

		// ---- 다음 블록 ----
		g2.setColor(new Color(48, 51, 66));
		g2.fillRoundRect(x, y, w, 190, 16, 16);
		g2.setColor(new Color(160, 164, 185));
		g2.setFont(new Font("Malgun Gothic", Font.BOLD, 12).deriveFont(15f));
		g2.drawString("다음 블록", x + 18, y + 30);

		Shape next = game.nextPiece;
		if (next.getShape() != 0) {
			int minX = next.x(0), maxX = next.x(0), minY = next.y(0), maxY = next.y(0);
			for (int i = 1; i < 4; ++i) {
				minX = Math.min(minX, next.x(i));
				maxX = Math.max(maxX, next.x(i));
				minY = Math.min(minY, next.y(i));
				maxY = Math.max(maxY, next.y(i));
			}
			int pieceW = (maxX - minX + 1) * 32;
			int pieceH = (maxY - minY + 1) * 32;
			int areaTop = y + 44;
			int areaH = 190 - 44 - 14;
			int left = x + (w - pieceW) / 2;
			int top = areaTop + (areaH - pieceH) / 2;
			for (int i = 0; i < 4; ++i) {
				drawSquare(g2, left + (next.x(i) - minX) * 32, top + (next.y(i) - minY) * 32, 32, 32,
						next.getShape());
			}
		}

		y = y + 190 + 18;

		// ---- 점수판 ----
		g2.setColor(new Color(48, 51, 66));
		g2.fillRoundRect(x, y, w, 300, 16, 16);
		g2.setColor(new Color(160, 164, 185));
		g2.setFont(new Font("Malgun Gothic", Font.BOLD, 12).deriveFont(15f));
		g2.drawString("점수판", x + 18, y + 30);

		int left = x + 18;
		int right = x + w - 18;

		g2.setFont(new Font("Malgun Gothic", Font.PLAIN, 12).deriveFont(13f));
		g2.setColor(new Color(160, 164, 185));
		g2.drawString("이번 판", left, y + 62);

		int lines = game.numLinesRemoved;
		drawScoreRow(g2, left, right, y + 94, "P1 테트리스", "지운 줄", lines, 10, new Color(110, 170, 255));
		int height = game.dodger.y;
		drawScoreRow(g2, left, right, y + 150, "P2 고양이", "높이", height, 18, new Color(255, 170, 60));

		g2.setColor(new Color(90, 94, 115));
		g2.drawLine(left, y + 186, right, y + 186);

		g2.setFont(new Font("Malgun Gothic", Font.PLAIN, 12).deriveFont(13f));
		g2.setColor(new Color(160, 164, 185));
		g2.drawString("전적", left, y + 212);

		// 전적 "테트리스 n승 : 고양이 m승"
		int baseline = y + 262;
		int mid = (left + right) / 2;
		g2.setFont(new Font("Malgun Gothic", Font.BOLD, 12).deriveFont(26f));
		g2.setColor(new Color(235, 236, 242));
		FontMetrics fm = g2.getFontMetrics();
		g2.drawString(":", mid - fm.stringWidth(":") / 2, baseline);
		drawWins(g2, left, mid - 10, baseline, "테트리스", game.tetrisWins, new Color(110, 170, 255));
		drawWins(g2, mid + 10, right, baseline, "고양이", game.dodgerWins, new Color(255, 170, 60));
	}

	void drawScoreRow(Graphics2D g, int left, int right, int baseline, String who, String what, int value,
			int max, Color color) {
		g.setFont(new Font("Malgun Gothic", Font.BOLD, 12).deriveFont(15f));
		g.setColor(color);
		g.drawString(who, left, baseline);
		FontMetrics fm = g.getFontMetrics();
		g.setFont(new Font("Malgun Gothic", Font.PLAIN, 12).deriveFont(12f));
		g.setColor(new Color(160, 164, 185));
		g.drawString(what, left + fm.stringWidth(who) + 8, baseline);

		String text = value + " / " + max;
		g.setFont(new Font("Malgun Gothic", Font.BOLD, 12).deriveFont(17f));
		g.setColor(new Color(235, 236, 242));
		fm = g.getFontMetrics();
		g.drawString(text, right - fm.stringWidth(text), baseline);

		int barY = baseline + 10;
		int barW = right - left;
		int barH = 8;
		g.setColor(new Color(18, 19, 26));
		g.fillRoundRect(left, barY, barW, barH, barH, barH);
		int fill = (int) ((long) barW * Math.min(value, max) / max);
		g.setColor(color);
		if (fill > 0)
			g.fillRoundRect(left, barY, fill, barH, barH, barH);
	}

	void drawWins(Graphics2D g, int from, int to, int baseline, String who, int wins, Color color) {
		g.setFont(new Font("Malgun Gothic", Font.PLAIN, 12).deriveFont(13f));
		FontMetrics small = g.getFontMetrics();
		g.setFont(new Font("Malgun Gothic", Font.BOLD, 12).deriveFont(30f));
		FontMetrics big = g.getFontMetrics();
		String num = String.valueOf(wins);
		String label = who + " ";
		String unit = "승";
		int total = small.stringWidth(label) + big.stringWidth(num) + small.stringWidth(unit);
		int x = from + (to - from - total) / 2;

		g.setFont(new Font("Malgun Gothic", Font.PLAIN, 12).deriveFont(13f));
		g.setColor(new Color(160, 164, 185));
		g.drawString(label, x, baseline);
		x += small.stringWidth(label);
		g.setFont(new Font("Malgun Gothic", Font.BOLD, 12).deriveFont(30f));
		g.setColor(color);
		g.drawString(num, x, baseline);
		x += big.stringWidth(num);
		g.setFont(new Font("Malgun Gothic", Font.PLAIN, 12).deriveFont(13f));
		g.setColor(new Color(160, 164, 185));
		g.drawString(unit, x, baseline);
	}

	// GamePanel꺼 복사
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
