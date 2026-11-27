package origin;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import common.KeyAction;
import common.KeySettings;
import common.SettingsPanel;

/**
 * 일반·갤러그 모드의 테트리스 키 입력을 GamePanel 명령으로 바꿔 주는 클래스.
 *
 * 전에는 GameFrame.MyKey1 안의 익명 클래스가 GamePanel 내부 필드(x, y, block ...)를 직접 읽고 바꿨고,
 * ((GamePanel) gp) 형 변환을 37번 했다(Feature Envy, Inappropriate Intimacy).
 * 이제 이 클래스는 "어떤 키가 어떤 명령인가"만 알고, 움직였는지 확인하고 소리를 내는 일은 GamePanel이 한다.
 */
final class TetrisKeyHandler extends KeyAdapter
{
	private final GameFrame frame;
	private final GamePanel panel;

	TetrisKeyHandler(GameFrame frame, GamePanel panel)
	{
		this.frame = frame;
		this.panel = panel;
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		// ESC를 누르면 게임을 멈추고 설정 화면을 띄운다(카운트다운과 게임이 끝난 뒤에는 무시)
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
		{
			if (panel.acceptsInput())
				openSettings();
			return;
		}

		// 홀드와 낙하 스레드가 동시에 블록을 건드리지 않게 게임 패널을 잠그고 처리한다
		synchronized (panel)
		{
			if (panel.acceptsInput())
				runCommand(e.getKeyCode());
		}
	}

	// 설정 창을 띄우는 동안 게임을 멈춘다
	private void openSettings()
	{
		panel.paused = true;
		SettingsPanel.showDialog(frame);
		panel.paused = false;
		panel.refreshHoldTitle(); // 키를 바꿨을 수 있다
		frame.requestFocusInWindow(); // 설정 창이 닫힌 뒤 키 입력을 되찾는다
	}

	// 키 하나를 명령 하나로 바꾼다. 같은 키가 여러 동작에 걸려 있으면 위에 있는 동작이 먼저다
	private void runCommand(int code)
	{
		if (code == KeySettings.getKey(KeyAction.TETRIS_HOLD))
			panel.holdBlock();
		else if (code == KeySettings.getKey(KeyAction.TETRIS_DOWN))
			panel.userMoveDown();
		else if (code == KeySettings.getKey(KeyAction.TETRIS_LEFT))
			panel.userMoveLeft();
		else if (code == KeySettings.getKey(KeyAction.TETRIS_RIGHT))
			panel.userMoveRight();
		else if (code == KeySettings.getKey(KeyAction.TETRIS_ITEM_SELF))
			panel.useItemOnSelf();
		else if (code == KeySettings.getKey(KeyAction.TETRIS_ITEM_ENEMY))
			panel.useItem();
		else if (code == KeySettings.getKey(KeyAction.TETRIS_ITEM_DELETE))
			panel.deleteItem();
		else if (code == KeySettings.getKey(KeyAction.TETRIS_DROP))
			panel.userDrop();
		else if (code == KeySettings.getKey(KeyAction.TETRIS_ROTATE))
			panel.userRotate();
	}
}
