package vscpu.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.JPanel;

import vscpu.game.Board;
import vscpu.game.BoardState;
import vscpu.game.Tetromino;

/**
 * 한쪽 판을 그린다: 이름, 판, 떨어지는 블록과 그림자, 오른쪽에 다음 블록과 점수.
 * 결과나 일시정지 문구가 있으면 판 위에 크게 띄운다.
 * 판은 읽기 전용 BoardState로만 받아서 그리기만 한다.
 */
public class BoardView extends JPanel {

	public static final Color BACKGROUND = new Color(32, 34, 44);
	public static final Color TEXT = new Color(235, 236, 242);
	private static final Color BOARD = new Color(18, 19, 26);
	private static final Color GRID = new Color(36, 38, 50);
	private static final Color BORDER = new Color(90, 94, 115);
	private static final Color CARD = new Color(48, 51, 66);
	private static final Color SUBTEXT = new Color(160, 164, 185);
	private static final Color OVERLAY = new Color(0, 0, 0, 185);
	/** 그림자(떨어질 자리) 투명도 */
	private static final int GHOST_ALPHA = 60;

	/** 블록 종류별 색 (블록 모양은 game.Tetromino, 색은 화면 쪽에서 정한다) */
	private static final Map<Tetromino, Color> PIECE_COLORS = new EnumMap<>(Map.of(
			Tetromino.I, new Color(80, 200, 230),
			Tetromino.O, new Color(235, 205, 70),
			Tetromino.T, new Color(170, 100, 210),
			Tetromino.S, new Color(110, 200, 100),
			Tetromino.Z, new Color(220, 90, 90),
			Tetromino.J, new Color(80, 120, 220),
			Tetromino.L, new Color(235, 150, 60)));

	private static final String FONT_NAME = "Malgun Gothic";
	private static final int BASE_FONT_SIZE = 12;

	// 배치 (픽셀)
	private static final int SIDE_WIDTH = 150;
	private static final int TITLE_HEIGHT = 40;
	private static final int SIDE_GAP = 16;
	private static final int H_MARGIN = 30;
	private static final int V_MARGIN = 20;
	private static final int NAME_OFFSET = 12;
	private static final int CARD_RADIUS = 14;
	private static final int CARD_PADDING = 14;
	private static final int NEXT_CARD_HEIGHT = 120;
	private static final int NEXT_LABEL_Y = 24;
	private static final int NEXT_PIECE_TOP = 34;
	private static final int NEXT_CELL_MAX = 22;
	private static final int STATS_CARD_HEIGHT = 190;
	private static final int STAT_FIRST_Y = 30;
	private static final int STAT_SPACING = 60;
	private static final int STAT_VALUE_OFFSET = 28;
	private static final int OVERLAY_HEIGHT = 80;
	private static final int OVERLAY_HEIGHT_WITH_SUB = 110;
	private static final int OVERLAY_TITLE_Y = 46;
	private static final int OVERLAY_SUB_Y = 80;

	/** 그릴 판. 화면 부품(JPanel)은 직렬화할 수 있는 타입이지만 판은 저장할 필요가 없어 transient로 둔다 */
	private final transient BoardState side;
	private final Color accent;
	private String overlayTitle;
	private String overlaySub;

	public BoardView(BoardState side, Color accent) {
		this.side = side;
		this.accent = accent;
		setBackground(BACKGROUND);
	}

	/** 판 위에 띄울 문구. title이 null이면 지운다. */
	public void setOverlay(String title, String sub) {
		overlayTitle = title;
		overlaySub = sub;
	}

	public static Font font(int style, float size) {
		return new Font(FONT_NAME, style, BASE_FONT_SIZE).deriveFont(size);
	}

	@Override
	protected void paintComponent(Graphics g0) {
		super.paintComponent(g0);
		Graphics2D g = (Graphics2D) g0;
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int cell = Math.max(1, Math.min((getWidth() - SIDE_WIDTH - H_MARGIN) / Board.WIDTH,
				(getHeight() - TITLE_HEIGHT - V_MARGIN) / Board.HEIGHT));
		int boardW = cell * Board.WIDTH;
		int boardH = cell * Board.HEIGHT;
		int left = (getWidth() - boardW - SIDE_WIDTH - SIDE_GAP) / 2;
		int top = TITLE_HEIGHT + (getHeight() - TITLE_HEIGHT - boardH) / 2;

		g.setFont(font(Font.BOLD, 20f));
		g.setColor(accent);
		g.drawString(side.getName(), left, top - NAME_OFFSET);

		drawBoard(g, left, top, cell);
		if (!side.isGameOver() && side.getCurrent() != null)
			drawFallingPiece(g, left, top, cell);

		g.setColor(BORDER);
		g.drawRect(left - 1, top - 1, boardW + 1, boardH + 1);

		drawSide(g, left + boardW + SIDE_GAP, top, cell);

		if (overlayTitle != null)
			drawOverlay(g, left, top, boardW, boardH);
	}

	/** 판 배경, 옅은 격자, 쌓인 블록 */
	private void drawBoard(Graphics2D g, int left, int top, int cell) {
		int boardW = cell * Board.WIDTH;
		int boardH = cell * Board.HEIGHT;
		g.setColor(BOARD);
		g.fillRect(left, top, boardW, boardH);
		g.setColor(GRID);
		for (int x = 1; x < Board.WIDTH; ++x)
			g.drawLine(left + x * cell, top, left + x * cell, top + boardH - 1);
		for (int y = 1; y < Board.HEIGHT; ++y)
			g.drawLine(left, top + y * cell, left + boardW - 1, top + y * cell);

		for (int y = 0; y < Board.HEIGHT; ++y)
			for (int x = 0; x < Board.WIDTH; ++x) {
				Tetromino t = side.cellAt(x, y);
				if (t != null)
					drawCell(g, left + x * cell, top + y * cell, cell, colorOf(t));
			}
	}

	/** 떨어지는 블록과 그 그림자 */
	private void drawFallingPiece(Graphics2D g, int left, int top, int cell) {
		Tetromino t = side.getCurrent();
		Color color = colorOf(t);
		Color ghost = new Color(color.getRed(), color.getGreen(), color.getBlue(), GHOST_ALPHA);
		int[][] cells = t.cells(side.getRotation());
		int ghostY = side.ghostY();
		g.setColor(ghost);
		for (int[] c : cells) {
			int gy = ghostY + c[1];
			if (gy >= 0)
				g.fillRect(left + (side.getX() + c[0]) * cell + 1, top + gy * cell + 1, cell - 2, cell - 2);
		}
		for (int[] c : cells) {
			int py = side.getY() + c[1];
			if (py >= 0)
				drawCell(g, left + (side.getX() + c[0]) * cell, top + py * cell, cell, color);
		}
	}

	private void drawSide(Graphics2D g, int x, int y, int cell) {
		drawNextCard(g, x, y, cell);

		int sy = y + NEXT_CARD_HEIGHT + CARD_PADDING;
		g.setColor(CARD);
		g.fillRoundRect(x, sy, SIDE_WIDTH, STATS_CARD_HEIGHT, CARD_RADIUS, CARD_RADIUS);
		String[][] stats = {
				{ "점수", String.valueOf(side.getScore()) },
				{ "지운 줄", String.valueOf(side.getLines()) },
				{ "레벨", String.valueOf(side.level()) } };
		for (int i = 0; i < stats.length; ++i)
			drawStat(g, x + CARD_PADDING, sy + STAT_FIRST_Y + i * STAT_SPACING, stats[i][0], stats[i][1]);
	}

	/** "다음 블록" 카드: 블록을 카드 가운데에 작게 그린다 */
	private void drawNextCard(Graphics2D g, int x, int y, int cell) {
		g.setColor(CARD);
		g.fillRoundRect(x, y, SIDE_WIDTH, NEXT_CARD_HEIGHT, CARD_RADIUS, CARD_RADIUS);
		g.setColor(SUBTEXT);
		g.setFont(font(Font.BOLD, 13f));
		g.drawString("다음 블록", x + CARD_PADDING, y + NEXT_LABEL_Y);
		Tetromino next = side.getNext();
		if (next == null)
			return;
		int small = Math.min(NEXT_CELL_MAX, cell);
		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
		for (int[] c : next.cells(0)) {
			minX = Math.min(minX, c[0]);
			maxX = Math.max(maxX, c[0]);
			minY = Math.min(minY, c[1]);
			maxY = Math.max(maxY, c[1]);
		}
		int w = (maxX - minX + 1) * small;
		int h = (maxY - minY + 1) * small;
		int ox = x + (SIDE_WIDTH - w) / 2;
		int oy = y + NEXT_PIECE_TOP + (NEXT_CARD_HEIGHT - NEXT_PIECE_TOP - h) / 2;
		for (int[] c : next.cells(0))
			drawCell(g, ox + (c[0] - minX) * small, oy + (c[1] - minY) * small, small, colorOf(next));
	}

	private void drawStat(Graphics2D g, int x, int y, String label, String value) {
		g.setColor(SUBTEXT);
		g.setFont(font(Font.PLAIN, 13f));
		g.drawString(label, x, y);
		g.setColor(TEXT);
		g.setFont(font(Font.BOLD, 24f));
		g.drawString(value, x, y + STAT_VALUE_OFFSET);
	}

	private void drawOverlay(Graphics2D g, int left, int top, int boardW, int boardH) {
		int boxH = overlaySub != null ? OVERLAY_HEIGHT_WITH_SUB : OVERLAY_HEIGHT;
		int boxY = top + boardH / 2 - boxH / 2;
		g.setColor(OVERLAY);
		g.fillRect(left, boxY, boardW, boxH);
		g.setColor(Color.WHITE);
		g.setFont(font(Font.BOLD, 28f));
		drawCentered(g, overlayTitle, left, boardW, boxY + OVERLAY_TITLE_Y);
		if (overlaySub != null) {
			g.setColor(SUBTEXT);
			g.setFont(font(Font.PLAIN, 14f));
			drawCentered(g, overlaySub, left, boardW, boxY + OVERLAY_SUB_Y);
		}
	}

	private static Color colorOf(Tetromino t) {
		return PIECE_COLORS.get(t);
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
