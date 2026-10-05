package dodge;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

/**
 * 테트리스 X 똥 피하기 모드의 화면 하나.
 * JPanel이라 단독 창(DodgeModeMain)에도, 나중에 다른 모드와 합친 메인 메뉴(CardLayout 등)에도 그대로 붙일 수 있다.
 * 테트리스 조각(Shape / Tetrominoes)은 학교 tetris2026 코드에서 가져와 이 패키지에 함께 둔다.
 *
 * 배치: [왼쪽 빈 공간] [보드] [오른쪽 정보판]. 왼쪽과 오른쪽 너비가 같아서 보드가 창 가운데에 온다.
 */
public class DodgeMode extends JPanel {

	private final GamePanel gamePanel;

	public DodgeMode() {
		super(new BorderLayout());
		setBackground(Theme.BACKGROUND);

		gamePanel = new GamePanel();
		InfoPanel infoPanel = new InfoPanel(gamePanel);
		gamePanel.setOnRefresh(infoPanel::repaint);

		JPanel leftSpace = new JPanel();
		leftSpace.setOpaque(false);
		leftSpace.setPreferredSize(new Dimension(InfoPanel.PANEL_WIDTH, 0));

		add(leftSpace, BorderLayout.WEST);
		add(gamePanel, BorderLayout.CENTER);
		add(infoPanel, BorderLayout.EAST);
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
