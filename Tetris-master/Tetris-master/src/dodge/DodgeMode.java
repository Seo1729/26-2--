package dodge;

import java.awt.*;

import javax.swing.*;

// 똥피하기 모드 화면 (왼쪽 빈칸 + 게임 + 오른쪽 정보판)
public class DodgeMode extends JPanel {

	GamePanel gamePanel;
	InfoPanel infoPanel;

	public DodgeMode() {
		super(new BorderLayout());
		setBackground(new Color(32, 34, 44));

		gamePanel = new GamePanel();
		infoPanel = new InfoPanel(gamePanel);
		gamePanel.info = infoPanel;

		// 게임판을 가운데 두려고 왼쪽에도 정보판만큼 빈칸
		JPanel leftSpace = new JPanel();
		leftSpace.setOpaque(false);
		leftSpace.setPreferredSize(new Dimension(280, 0));

		add(leftSpace, BorderLayout.WEST);
		add(gamePanel, BorderLayout.CENTER);
		add(infoPanel, BorderLayout.EAST);
	}

	public void start() {
		gamePanel.start();
	}

	public void stop() {
		gamePanel.stop();
	}
}
