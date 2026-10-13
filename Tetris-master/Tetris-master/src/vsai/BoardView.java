package vsai;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * 한쪽 판을 그린다: 이름, 판, 떨어지는 블록과 그림자, 오른쪽에 다음 블록과 점수.
 * 결과나 일시정지 문구가 있으면 판 위에 크게 띄운다.
 */
public class BoardView extends JPanel {

	static final Color BACKGROUND = new Color(32, 34, 44);
	private static final Color BOARD = new Color(18, 19, 26);
	private static final Color GRID = new Color(36, 38, 50);
	private static final Color BORDER = new Color(90, 94, 115);
	private static final Color CARD = new Color(48, 51, 66);
	private static final Color TEXT = new Color(235, 236, 242);
	private static final Color SUBTEXT = new Color(160, 164, 185);
	private static final String FONT_NAME = "Malgun Gothic";
	private static final int SIDE_WIDTH = 150;
	private static final int TITLE_HEIGHT = 40;

	private final PlayerBoard side;
	private final Color accent;
	private String overlayTitle;
	private String overlaySub;

	public BoardView(PlayerBoard side, Color accent) {
		this.side = side;
		this.accent = accent;
		setBackground(BACKGROUND);
	}

	/** 판 위에 띄울 문구. title이 null이면 지운다. */
	public void setOverlay(String title, String sub) {
		overlayTitle = title;
		overlaySub = sub;
	}

	static Font font(int style, float size) {
		return new Font(FONT_NAME, style, 12).deriveFont(size);
	}

	@Override
	protected void paintComponent(Graphics g0) {
		super.paintComponent(g0);
		Graphics2D g = (Graphics2D) g0;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int cell = Math.max(1, Math.min((getWidth() - SIDE_WIDTH - 30) / Board.WIDTH,
				(getHeight() - TITLE_HEIGHT - 20) / Board.HEIGHT));
		int boardW = cell * Board.WIDTH;
		int boardH = cell * Board.HEIGHT;
		int left = (getWidth() - boardW - SIDE_WIDTH - 16) / 2;
		int top = TITLE_HEIGHT + (getHeight() - TITLE_HEIGHT - boardH) / 2;

		// 이름
		g.setFont(font(Font.BOLD, 20f));
		g.setColor(accent);
		g.drawString(side.getName(), left, top - 12);

		// 판과 옅은 격자
		g.setColor(BOARD);
		g.fillRect(left, top, boardW, boardH);
		g.setColor(GRID);
		for (int x = 1; x < Board.WIDTH; ++x)
			g.drawLine(left + x * cell, top, left + x * cell, top + boardH - 1);
		for (int y = 1; y < Board.HEIGHT; ++y)
			g.drawLine(left, top + y * cell, left + boardW - 1, top + y * cell);

		Board board = side.getBoard();
		for (int y = 0; y < Board.HEIGHT; ++y)
			for (int x = 0; x < Board.WIDTH; ++x)
				if (board.get(x, y) != null)
					drawCell(g, left + x * cell, top + y * cell, cell, board.get(x, y).color());

		if (!side.isGameOver() && side.getCurrent() != null) {
			Tetromino t = side.getCurrent();
			int ghost = side.ghostY();
			for (int[] c : t.cells(side.getRotation())) {
				int gx = side.getX() + c[0];
				int gy = ghost + c[1];
				if (gy >= 0) {
					g.setColor(new Color(t.color().getRed(), t.color().getGreen(), t.color().getBlue(), 60));
					g.fillRect(left + gx * cell + 1, top + gy * cell + 1, cell - 2, cell - 2);
				}
			}
			for (int[] c : t.cells(side.getRotation())) {
				int px = side.getX() + c[0];
				int py = side.getY() + c[1];
				if (py >= 0)
					drawCell(g, left + px * cell, top + py * cell, cell, t.color());
			}
		}

		g.setColor(BORDER);
		g.drawRect(left - 1, top - 1, boardW + 1, boardH + 1);

		drawSide(g, left + boardW + 16, top, cell);

		if (overlayTitle != null)
			drawOverlay(g, left, top, boardW, boardH);
	}

	private void drawSide(Graphics2D g, int x, int y, int cell) {
		// 다음 블록
		int nextH = 120;
		g.setColor(CARD);
		g.fillRoundRect(x, y, SIDE_WIDTH, nextH, 14, 14);
		g.setColor(SUBTEXT);
		g.setFont(font(Font.BOLD, 13f));
		g.drawString("다음 블록", x + 14, y + 24);
		Tetromino next = side.getNext();
		if (next != null) {
			int small = Math.min(22, cell);
			int minX = 9, maxX = -9, minY = 9, maxY = -9;
			for (int[] c : next.cells(0)) {
				minX = Math.min(minX, c[0]);
				maxX = Math.max(maxX, c[0]);
				minY = Math.min(minY, c[1]);
				maxY = Math.max(maxY, c[1]);
			}
			int w = (maxX - minX + 1) * small;
			int h = (maxY - minY + 1) * small;
			int ox = x + (SIDE_WIDTH - w) / 2;
			int oy = y + 34 + (nextH - 34 - h) / 2;
			for (int[] c : next.cells(0))
				drawCell(g, ox + (c[0] - minX) * small, oy + (c[1] - minY) * small, small, next.color());
		}

		// 점수, 줄, 레벨
		int sy = y + nextH + 14;
		g.setColor(CARD);
		g.fillRoundRect(x, sy, SIDE_WIDTH, 190, 14, 14);
		drawStat(g, x + 14, sy + 30, "점수", String.valueOf(side.getScore()));
		drawStat(g, x + 14, sy + 90, "지운 줄", String.valueOf(side.getLines()));
		drawStat(g, x + 14, sy + 150, "레벨", String.valueOf(side.level()));
	}

	private void drawStat(Graphics2D g, int x, int y, String label, String value) {
		g.setColor(SUBTEXT);
		g.setFont(font(Font.PLAIN, 13f));
		g.drawString(label, x, y);
		g.setColor(TEXT);
		g.setFont(font(Font.BOLD, 24f));
		g.drawString(value, x, y + 28);
	}

	private void drawOverlay(Graphics2D g, int left, int top, int boardW, int boardH) {
		int boxH = overlaySub != null ? 110 : 80;
		int boxY = top + boardH / 2 - boxH / 2;
		g.setColor(new Color(0, 0, 0, 185));
		g.fillRect(left, boxY, boardW, boxH);
		g.setColor(Color.WHITE);
		g.setFont(font(Font.BOLD, 28f));
		drawCentered(g, overlayTitle, left, boardW, boxY + 46);
		if (overlaySub != null) {
			g.setColor(SUBTEXT);
			g.setFont(font(Font.PLAIN, 14f));
			drawCentered(g, overlaySub, left, boardW, boxY + 80);
		}
	}

	private static void drawCentered(Graphics g, String text, int left, int width, int baseline) {
		FontMetrics fm = g.getFontMetrics();
		g.drawString(text, left + (width - fm.stringWidth(text)) / 2, baseline);
	}

	private static void drawCell(Graphics g, int x, int y, int size, Color color) {
		g.setColor(color);
		g.fillRect(x + 1, y + 1, size - 2, size - 2);
		g.setColor(color.brighter());
		g.drawLine(x + 1, y + 1, x + size - 2, y + 1);
		g.drawLine(x + 1, y + 1, x + 1, y + size - 2);
		g.setColor(color.darker());
		g.drawLine(x + 1, y + size - 2, x + size - 2, y + size - 2);
		g.drawLine(x + size - 2, y + 1, x + size - 2, y + size - 2);
	}
}
