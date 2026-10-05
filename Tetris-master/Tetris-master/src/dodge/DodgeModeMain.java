package dodge;

import javax.swing.JFrame;

/** 똥 피하기 모드만 단독으로 실행해 보는 진입점 */
public class DodgeModeMain {

	public static void main(String[] args) {
		JFrame frame = new JFrame("Tetris X 똥 피하기");
		DodgeMode mode = new DodgeMode();
		frame.add(mode);
		frame.setSize(300, 640);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		mode.start();
	}
}
