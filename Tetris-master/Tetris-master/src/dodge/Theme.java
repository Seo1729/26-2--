package kr.ac.jbnu.se.tetris.dodge;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import kr.ac.jbnu.se.tetris.Tetrominoes;

/**
 * 똥피하기 모드 화면의 색과 글꼴, 블록 한 칸 그리기를 한곳에 모아 둔다.
 */
public final class Theme {

	/** 창 전체 배경 */
	public static final Color BACKGROUND = new Color(32, 34, 44);
	/** 보드 안쪽 배경 */
	public static final Color BOARD = new Color(18, 19, 26);
	public static final Color BORDER = new Color(90, 94, 115);
	/** 오른쪽 정보 카드 배경 */
	public static final Color CARD = new Color(48, 51, 66);
	public static final Color TEXT = new Color(235, 236, 242);
	public static final Color SUBTEXT = new Color(160, 164, 185);
	public static final Color TETRIS = new Color(110, 170, 255);
	public static final Color DODGER = new Color(255, 170, 60);
	public static final Color WARNING = new Color(255, 90, 90);
	public static final Color GOAL_FILL = new Color(255, 220, 120, 60);
	public static final Color GOAL_LINE = new Color(230, 170, 30);

	private static final String FONT_NAME = "Malgun Gothic";

	private static final Color[] BLOCK_COLORS = { new Color(0, 0, 0), new Color(204, 102, 102),
			new Color(102, 204, 102), new Color(102, 102, 204), new Color(204, 204, 102), new Color(204, 102, 204),
			new Color(102, 204, 204), new Color(218, 170, 0) };

	private Theme() {
	}

	/** 한글이 잘 보이는 글꼴 (맑은 고딕, 없으면 자바 기본 글꼴) */
	public static Font font(int style, float size) {
		return new Font(FONT_NAME, style, 12).deriveFont(size);
	}

	/** 블록 한 칸을 (x, y)부터 w x h 크기로 그린다 */
	public static void drawSquare(Graphics g, int x, int y, int w, int h, Tetrominoes shape) {
		Color color = BLOCK_COLORS[shape.ordinal()];

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
