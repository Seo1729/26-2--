package dodge;

import java.awt.*;

import javax.swing.*;

// 똥피하기 모드 화면 (왼쪽 빈칸 + 게임 + 오른쪽 정보판)
public class DodgeMode extends JPanel {

	GamePanel gamePanel;
	InfoPanel infoPanel;

	public DodgeMode() {
		this(CharacterManager.getInstance());
	}

	public DodgeMode(CharacterManager characters) {
		super(new BorderLayout());
		setBackground(new Color(32, 34, 44));

		gamePanel = new GamePanel(characters);
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
		try { gamePanel.getCharacterManager().load(); }
		catch (java.io.IOException e) {
			System.err.println("캐릭터 외형을 불러오지 못함: " + e.getMessage());
		}
		gamePanel.start();
	}

	public void stop() {
		gamePanel.stop();
	}
}
