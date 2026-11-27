package threed;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import common.KeyAction;
import common.KeySettings;

// 3D 테트리스 키 입력: 설정된 키를 화면 부품의 InputMap/ActionMap에 등록하고 눌린 키를 관리한다.
// 게임은 Game3DControl(조작 전용)로만 다룬다. 화면 그리기와 정보창은 모른다.
final class Game3DKeyBindings
{
	private final JComponent target;      // 키를 등록할 화면 부품
	private final Game3DControl game;
	private final Runnable afterKey;      // 키 하나를 처리한 뒤 할 일 (화면 갱신)
	private final Set<Integer> pressedKeys = new HashSet<>();

	Game3DKeyBindings(JComponent target, Game3DControl game, Runnable afterKey)
	{
		this.target = target;
		this.game = game;
		this.afterKey = afterKey;
	}

	// 지금 설정된 키로 모든 키를 (다시) 등록한다. openSettings는 ESC를 눌렀을 때 할 일
	void bindAll(Runnable openSettings)
	{
		// 이전에 등록한 키를 모두 지워서 설정에서 바꾼 키가 바로 적용되게 한다
		target.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).clear();
		target.getActionMap().clear();
		bindMovement(KeyAction.TETRIS3D_UP, 0, -1);
		bindMovement(KeyAction.TETRIS3D_DOWN, 0, 1);
		bindMovement(KeyAction.TETRIS3D_LEFT, -1, 0);
		bindMovement(KeyAction.TETRIS3D_RIGHT, 1, 0);
		// 회전 J K L
		bindRotation(KeyAction.TETRIS3D_ROTATE_X, Block3D.Axis.X);
		bindRotation(KeyAction.TETRIS3D_ROTATE_Y, Block3D.Axis.Y);
		bindRotation(KeyAction.TETRIS3D_ROTATE_Z, Block3D.Axis.Z);
		// 하드드롭
		bind(KeyAction.TETRIS3D_DROP, new Runnable()
		{
			@Override
			public void run()
			{
				game.hardDrop();
			}
		}, Game3D.State.RUNNING);
		// ESC: 설정 화면 (게임 중에만)
		bind(KeyEvent.VK_ESCAPE, "Escape", openSettings, Game3D.State.RUNNING);
		// 재시작 (게임 오버일 때만)
		bind(KeyAction.TETRIS3D_RESTART, new Runnable()
		{
			@Override
			public void run()
			{
				resetPressedKeys();
				game.restart();
			}
		}, Game3D.State.GAME_OVER);
	}

	void resetPressedKeys()
	{
		pressedKeys.clear();
	}

	// 이동 키
	private void bindMovement(KeyAction action, final int dx, final int dy)
	{
		bind(action, new Runnable()
		{
			@Override
			public void run()
			{
				game.move(dx, dy);
			}
		}, Game3D.State.RUNNING);
	}

	// 회전 키
	private void bindRotation(KeyAction action, final Block3D.Axis axis)
	{
		bind(action, new Runnable()
		{
			@Override
			public void run()
			{
				game.rotate(axis);
			}
		}, Game3D.State.RUNNING);
	}

	// 설정에 지정된 키를 등록한다 (requiredState일 때만 동작)
	private void bind(KeyAction keyAction, Runnable action, Game3D.State requiredState)
	{
		bind(KeySettings.getKey(keyAction), keyAction.name(), action, requiredState);
	}

	// 키 누를때, 뗄때 등록 (shift 같은거 눌러도 되게)
	private void bind(final int keyCode, String name, final Runnable action, final Game3D.State requiredState)
	{
		String pressAction = "press" + name;
		String releaseAction = "release" + name;
		for (int modifiers : new int[] { 0, InputEvent.SHIFT_DOWN_MASK })
		{
			target.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, modifiers, false), pressAction);
		}
		// 뗄 때는 어떤 보조키 조합이든 받아야 눌린 상태가 남지 않는다
		int[] masks = {InputEvent.SHIFT_DOWN_MASK, InputEvent.CTRL_DOWN_MASK,
				InputEvent.ALT_DOWN_MASK, InputEvent.META_DOWN_MASK, InputEvent.ALT_GRAPH_DOWN_MASK};
		for (int combination = 0; combination < (1 << masks.length); combination++)
			target.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
					KeyStroke.getKeyStroke(keyCode, modifiersOf(masks, combination), true), releaseAction);
		target.getActionMap().put(pressAction, new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				if (game.getState() != requiredState)
				{
					resetPressedKeys();
					return;
				}
				// 꾹 누르고 있으면 계속 들어와서 무시
				if (pressedKeys.add(keyCode))
					action.run();
				afterKey.run();
			}
		});
		target.getActionMap().put(releaseAction, new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent event)
			{
				pressedKeys.remove(keyCode);
			}
		});
	}

	// combination의 비트가 켜진 보조키만 모은 값
	private static int modifiersOf(int[] masks, int combination)
	{
		int modifiers = 0;
		for (int i = 0; i < masks.length; i++)
			if ((combination & (1 << i)) != 0) modifiers |= masks[i];
		return modifiers;
	}
}
