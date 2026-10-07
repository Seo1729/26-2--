import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

/** Main view, information sidebar and window-wide game controls. */
public class Game3DPanel extends JPanel
{
	private final Game3D game;
	private final Set<Integer> pressedKeys = new HashSet<Integer>();
	private final JLabel status = new JLabel();
	private final Board3DRenderer renderer = new Board3DRenderer();
	private final TopViewPanel topView = new TopViewPanel();
	private final JLabel nextBlock = new JLabel();
	private final NextBlockPanel nextPreview = new NextBlockPanel();
	private final JLabel score = new JLabel("0");
	private final JLabel clearedLayers = new JLabel("0");
	private final JLabel gameState = new JLabel("READY");
	private final JLabel restartHint = new JLabel("R : 게임 오버 후 재시작");
	private static final Color BACKGROUND = new Color(13, 18, 29);
	private static final Color CARD = new Color(19, 25, 38);
	private static final Color TEXT = new Color(220, 230, 245);

	public Game3DPanel(Game3D game)
	{
		this.game = java.util.Objects.requireNonNull(game, "game");
		setLayout(new BorderLayout(16, 12));
		setBackground(BACKGROUND);
		setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
		JLabel title = new JLabel("3D TETRIS");
		title.setForeground(TEXT);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
		add(title, BorderLayout.NORTH);
		JPanel center = new JPanel(new BorderLayout(0, 8));
		center.setOpaque(false);
		center.add(renderer, BorderLayout.CENTER);
		status.setForeground(TEXT);
		center.add(status, BorderLayout.SOUTH);
		add(center, BorderLayout.CENTER);
		add(buildSidebar(), BorderLayout.EAST);
		bindMovement(KeyEvent.VK_W, "W", 0, -1);
		bindMovement(KeyEvent.VK_S, "S", 0, 1);
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

	private JScrollPane buildSidebar()
	{
		JPanel sidebar = new JPanel();
		sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
		sidebar.setBackground(BACKGROUND);
		topView.setAlignmentX(LEFT_ALIGNMENT);
		topView.setPreferredSize(new Dimension(280, 280));
		topView.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
		topView.setMinimumSize(new Dimension(280, 280));
		sidebar.add(topView);
		sidebar.add(Box.createVerticalStrut(10));
		JPanel next = card("NEXT BLOCK");
		JPanel nextContent = new JPanel(new BorderLayout(12, 0));
		nextContent.setOpaque(false);
		nextContent.add(styledValue(nextBlock), BorderLayout.WEST);
		nextContent.add(nextPreview, BorderLayout.CENTER);
		next.add(nextContent);
		sidebar.add(next);
		sidebar.add(Box.createVerticalStrut(10));
		JPanel stats = card("SCORE");
		JPanel values = new JPanel(new GridLayout(1, 2, 12, 0)); values.setOpaque(false);
		values.add(styledValue(score));
		JPanel layers = new JPanel(); layers.setOpaque(false);
		layers.setLayout(new BoxLayout(layers, BoxLayout.Y_AXIS));
		JLabel layerTitle = new JLabel("삭제한 층 수"); layerTitle.setForeground(TEXT);
		layers.add(layerTitle);
		layers.add(styledValue(clearedLayers));
		values.add(layers); stats.add(values);
		JLabel scoringRule = new JLabel("삭제 층당 " + Game3D.POINTS_PER_LAYER + "점");
		scoringRule.setForeground(new Color(145, 166, 193));
		stats.add(scoringRule);
		sidebar.add(stats);
		sidebar.add(Box.createVerticalStrut(10));
		JPanel state = card("GAME STATUS");
		state.add(styledValue(gameState));
		restartHint.setForeground(TEXT);
		state.add(restartHint);
		sidebar.add(state);
		sidebar.add(Box.createVerticalStrut(10));
		JPanel controls = card("조작법");
		for (String text : new String[] {"WASD : 평면 이동", "Space : Hard Drop",
				"J / K / L : X / Y / Z축 회전", "Mouse Drag : 카메라 회전",
				"Mouse Wheel : Zoom", "R : 게임 오버 후 재시작"})
		{
			JLabel label = new JLabel(text); label.setForeground(TEXT);
			controls.add(label);
		}
		sidebar.add(controls);
		sidebar.add(Box.createVerticalGlue());
		JScrollPane scroll = new JScrollPane(sidebar, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setFocusable(false);
		scroll.getVerticalScrollBar().setFocusable(false);
		scroll.setPreferredSize(new Dimension(320, 800));
		scroll.getViewport().setBackground(BACKGROUND);
		scroll.getVerticalScrollBar().setUnitIncrement(20);
		return scroll;
	}

	private static JPanel card(String title)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(CARD);
		panel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
		panel.setAlignmentX(LEFT_ALIGNMENT);
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
		JLabel heading = new JLabel(title);
		heading.setForeground(new Color(145, 166, 193));
		heading.setFont(heading.getFont().deriveFont(Font.BOLD, 12f));
		panel.add(heading);
		panel.add(Box.createVerticalStrut(6));
		return panel;
	}

	private static JLabel styledValue(JLabel label)
	{
		label.setForeground(TEXT);
		label.setFont(label.getFont().deriveFont(Font.BOLD, 24f));
		return label;
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
		}
		// Modifiers may change between pressing and releasing a game key.
		int[] masks = {InputEvent.SHIFT_DOWN_MASK, InputEvent.CTRL_DOWN_MASK,
				InputEvent.ALT_DOWN_MASK, InputEvent.META_DOWN_MASK, InputEvent.ALT_GRAPH_DOWN_MASK};
		for (int combination = 0; combination < (1 << masks.length); combination++)
		{
			int modifiers = 0;
			for (int i = 0; i < masks.length; i++)
				if ((combination & (1 << i)) != 0) modifiers |= masks[i];
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
		synchronized (game)
		{
			Game3DScene scene = game.getScene();
			renderer.setScene(scene);
			topView.setScene(scene);
			Block3D.Type nextType = game.getNextBlockType();
			nextBlock.setText(nextType.name());
			nextBlock.setForeground(BlockColors.colorFor(nextType));
			nextPreview.setType(nextType);
			clearedLayers.setText(Long.toString(game.getClearedLayerCount()));
			score.setText(Long.toString(game.getScore()));
			Game3D.State state = game.getState();
			gameState.setText(state == Game3D.State.GAME_OVER ? "GAME OVER" : state.name());
			gameState.setForeground(state == Game3D.State.GAME_OVER ? new Color(245, 98, 121) : TEXT);
			restartHint.setText(state == Game3D.State.GAME_OVER ? "R 키를 눌러 다시 시작" : " ");
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
	}


	@Override
	public void removeNotify()
	{
		resetPressedKeys();
		super.removeNotify();
	}
}
