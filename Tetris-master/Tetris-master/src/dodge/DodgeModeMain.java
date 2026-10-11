package dodge;

import java.awt.*;

import javax.swing.*;

// 똥피하기만 따로 실행해보는 용도
public class DodgeModeMain {

	public static void main(String[] args) {
		JFrame frame = new JFrame("Tetris X 똥 피하기");
		DodgeMode mode = new DodgeMode();
		frame.add(mode);
		Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		frame.setSize(Math.min(1100, screen.width), Math.min(1000, screen.height));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		mode.start();
	}
}
