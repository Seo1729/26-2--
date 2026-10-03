package kr.ac.jbnu.se.tetris.dodge;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.JFrame;

/** 똥 피하기 모드만 단독으로 실행해 보는 진입점 */
public class DodgeModeMain {

	/** 원하는 창 크기. 처음 300x640보다 크게, 가로로 넓게. */
	private static final int WINDOW_WIDTH = 1100;
	private static final int WINDOW_HEIGHT = 1000;

	public static void main(String[] args) {
		JFrame frame = new JFrame("Tetris X 똥 피하기");
		DodgeMode mode = new DodgeMode();
		frame.add(mode);
		// 작업 표시줄을 뺀 화면 크기보다 크면 화면에 맞춘다
		Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		frame.setSize(Math.min(WINDOW_WIDTH, screen.width), Math.min(WINDOW_HEIGHT, screen.height));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		mode.start();
	}
}
