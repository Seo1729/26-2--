package galag;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import common.KeyAction;
import common.KeySettings;

/**
 * 전투기 키(왼쪽, 오른쪽, 발사)를 누르고 있는지 기억한다. 키를 누르면 true, 떼면 false가 된다.
 * 전에는 GalagaLayer 안의 익명 클래스였고, 그 필드 이름 keyListener가 부모 클래스(Component)의 필드를 가렸다.
 */
final class GalagaControls extends KeyAdapter
{
	boolean left = false;
	boolean right = false;
	boolean shoot = false;

	@Override
	public void keyPressed(KeyEvent e)
	{
		set(e.getKeyCode(), true);
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		set(e.getKeyCode(), false);
	}

	private void set(int code, boolean pressed)
	{
		if (code == KeySettings.getKey(KeyAction.GALAGA_LEFT))
			left = pressed;
		else if (code == KeySettings.getKey(KeyAction.GALAGA_RIGHT))
			right = pressed;
		else if (code == KeySettings.getKey(KeyAction.GALAGA_SHOOT))
			shoot = pressed;
	}

	/** 모든 키를 뗀 것으로 한다 (멈춤 중이나 창이 포커스를 잃었을 때) */
	void releaseAll()
	{
		left = false;
		right = false;
		shoot = false;
	}
}
