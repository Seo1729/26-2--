package vsai;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/** 사람 vs AI 모드만 단독으로 실행해 보는 진입점 */
public class VsAiMain {

	private static final int WINDOW_WIDTH = 1100;
	private static final int WINDOW_HEIGHT = 800;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("Tetris - 사람 vs AI");
			VsAiMode mode = new VsAiMode();
			frame.add(mode);
			// 작업 표시줄을 뺀 화면 크기보다 크면 화면에 맞춘다
			Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
			frame.setSize(Math.min(WINDOW_WIDTH, screen.width), Math.min(WINDOW_HEIGHT, screen.height));
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
			mode.start();
		});
	}
}
