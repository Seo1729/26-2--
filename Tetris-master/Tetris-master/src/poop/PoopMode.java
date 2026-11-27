package poop;

import java.awt.*;
import java.io.IOException;

import javax.swing.*;

// 똥피하기 모드 화면 (왼쪽 빈칸 + 게임 + 오른쪽 정보판)
public class PoopMode extends JPanel {

	private final GamePanel gamePanel;

	public PoopMode() {
		this(CharacterManager.getInstance());
	}

	public PoopMode(CharacterManager characters) {
		super(new BorderLayout());
		setBackground(Theme.BACKGROUND);

		gamePanel = new GamePanel(characters);
		InfoPanel infoPanel = new InfoPanel(gamePanel.getStatus(), gamePanel::getBoardTop);
		gamePanel.addChangeListener(infoPanel::repaint);

		// 게임판을 가운데 두려고 왼쪽에도 정보판만큼 빈칸
		JPanel leftSpace = new JPanel();
		leftSpace.setOpaque(false);
		leftSpace.setPreferredSize(new Dimension(InfoPanel.WIDTH, 0));

		add(leftSpace, BorderLayout.WEST);
		add(gamePanel, BorderLayout.CENTER);
		add(infoPanel, BorderLayout.EAST);
	}

	public void start() {
		try {
			gamePanel.getCharacterManager().load();
		} catch (IOException e) {
			System.err.println("캐릭터 외형을 불러오지 못함: " + e.getMessage());
		}
		gamePanel.start();
	}

	public void stop() {
		gamePanel.stop();
	}
}
