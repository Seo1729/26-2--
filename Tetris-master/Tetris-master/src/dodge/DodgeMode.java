package dodge;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 테트리스 X 똥 피하기 모드의 화면 하나.
 * JPanel이라 단독 창(DodgeModeMain)에도, 나중에 다른 모드와 합친 메인 메뉴(CardLayout 등)에도 그대로 붙일 수 있다.
 * 테트리스 조각(Shape / Tetrominoes)은 학교 tetris2026 코드에서 가져와 이 패키지에 함께 둔다.
 */
public class DodgeMode extends JPanel {

	private final GamePanel gamePanel;

	public DodgeMode() {
		super(new BorderLayout());
		JLabel statusbar = new JLabel(" 0");
		gamePanel = new GamePanel(statusbar);
		add(gamePanel, BorderLayout.CENTER);
		add(statusbar, BorderLayout.SOUTH);
	}

	/** 모드 시작 (화면에 붙인 뒤 호출) */
	public void start() {
		gamePanel.start();
	}

	/** 모드 종료 (다른 모드로 넘어갈 때 호출) */
	public void stop() {
		gamePanel.stop();
	}
}
