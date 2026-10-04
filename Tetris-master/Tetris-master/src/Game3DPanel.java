import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/** 3D 렌더링 없이 조작 안내, 현재 좌표 및 창 단위 키 바인딩을 제공한다. */
public class Game3DPanel extends JPanel
{
	private final Game3D game;
	private final Set<Integer> pressedKeys = new HashSet<Integer>();
	private final JLabel status = new JLabel();

	public Game3DPanel(Game3D game)
	{
		this.game = java.util.Objects.requireNonNull(game, "game");
		setLayout(new BorderLayout(0, 12));
		setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		add(new JLabel("W: Y+1   S: Y-1   A: X-1   D: X+1   |   J: X축   K: Y축   L: Z축 (90°)"), BorderLayout.NORTH);
		add(status, BorderLayout.CENTER);
		add(new JLabel("Space: 즉시 낙하·고정   |   R: 게임 오버 후 재시작   |   자동 낙하: Z-1"), BorderLayout.SOUTH);
		bindMovement(KeyEvent.VK_W, "W", 0, 1);
		bindMovement(KeyEvent.VK_S, "S", 0, -1);
		bindMovement(KeyEvent.VK_A, "A", -1, 0);
		bindMovement(KeyEvent.VK_D, "D", 1, 0);
		bindKey(KeyEvent.VK_J, "J", () -> game.rotate(Block3D.Axis.X));
		bindKey(KeyEvent.VK_K, "K", () -> game.rotate(Block3D.Axis.Y));
		bindKey(KeyEvent.VK_L, "L", () -> game.rotate(Block3D.Axis.Z));
		bindKey(KeyEvent.VK_SPACE, "Space", () -> game.hardDrop());
		bindKey(KeyEvent.VK_R, "R", () -> {
			resetPressedKeys();
			game.restart();
		}, Game3D.State.GAME_OVER);
		refreshStatus();
	}

	private void bindMovement(final int keyCode, String name, final int dx, final int dy)
	{
		bindKey(keyCode, name, () -> game.move(dx, dy));
	}

	private void bindKey(final int keyCode, String name, final Runnable action)
	{
		bindKey(keyCode, name, action, Game3D.State.RUNNING);
	}

	private void bindKey(final int keyCode, String name, final Runnable action, final Game3D.State requiredState)
	{
		String pressAction = "press" + name;
		String releaseAction = "release" + name;
		// Shift를 누른 경우에도 같은 조작을 제공한다.
		for (int modifiers : new int[] { 0, InputEvent.SHIFT_DOWN_MASK })
		{
			getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, modifiers, false), pressAction);
			getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, modifiers, true), releaseAction);
		}
		getActionMap().put(pressAction, new AbstractAction()
		{
			public void actionPerformed(ActionEvent event)
			{
				if (game.getState() != requiredState)
				{
					resetPressedKeys();
					return;
				}
				// OS 자동 반복으로 들어오는 pressed 이벤트는 무시한다.
				if (pressedKeys.add(keyCode))
					action.run();
				refreshStatus();
			}
		});
		getActionMap().put(releaseAction, new AbstractAction()
		{
			public void actionPerformed(ActionEvent event)
			{
				pressedKeys.remove(keyCode);
			}
		});
	}

	/** 창 밖에서 키를 놓아 release 이벤트를 받지 못했을 때 눌림 상태를 초기화한다. */
	public void resetPressedKeys()
	{
		pressedKeys.clear();
	}

	public void refreshStatus()
	{
		if (!game.isRunning())
			resetPressedKeys();
		Block3D block = game.getActiveBlock();
		if (block == null)
			status.setText("블록 없음: 생성 위치를 확인하세요. 고정 블록: " + game.getLockedBlockCount());
		else
			status.setText(block.getType() + "  (X=" + block.getX() + ", Y=" + block.getY()
					+ ", Z=" + block.getZ() + ")  " + (game.isRunning() ? "낙하 중" : "정지")
					+ "  고정 블록: " + game.getLockedBlockCount());
	}

	@Override
	public void removeNotify()
	{
		resetPressedKeys();
		super.removeNotify();
	}
}
