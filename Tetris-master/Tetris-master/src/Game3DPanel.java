import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
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
import javax.swing.SwingUtilities;

import common.KeyAction;
import common.KeySettings;
import common.SettingsPanel;

// 3D 화면 + 오른쪽 정보 + 키 설정
public class Game3DPanel extends JPanel
{
	Game3D game;
	Set<Integer> pressedKeys = new HashSet<Integer>();
	JLabel status = new JLabel();
	Board3DRenderer renderer = new Board3DRenderer();
	TopViewPanel topView = new TopViewPanel();
	JLabel nextBlock = new JLabel();
	NextBlockPanel nextPreview = new NextBlockPanel();
	JLabel score = new JLabel("0");
	JLabel clearedLayers = new JLabel("0");
	JLabel gameState = new JLabel("READY");
	JLabel restartHint = new JLabel(" ");
	ArrayList<JLabel> controlLabels = new ArrayList<JLabel>(); // 조작법 안내 글들
	int keyCount = 0; // 키 몇번 눌렀나 세보려고

	public Game3DPanel(Game3D game)
	{
		if (game == null)
			throw new NullPointerException("game");
		this.game = game;
		setLayout(new BorderLayout(16, 12));
		setBackground(new Color(13, 18, 29));
		setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
		JLabel title = new JLabel("3D TETRIS");
		title.setForeground(new Color(220, 230, 245));
		title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
		add(title, BorderLayout.NORTH);
		JPanel center = new JPanel(new BorderLayout(0, 8));
		center.setOpaque(false);
		center.add(renderer, BorderLayout.CENTER);
		status.setForeground(new Color(220, 230, 245));
		center.add(status, BorderLayout.SOUTH);
		add(center, BorderLayout.CENTER);
		add(buildSidebar(), BorderLayout.EAST);
		bindAllKeys();
	}

	// 지금 설정된 키로 모든 키를 (다시) 등록한다
	void bindAllKeys()
	{
		// 이전에 등록한 키를 모두 지워서 설정에서 바꾼 키가 바로 적용되게 한다
		getInputMap(WHEN_IN_FOCUSED_WINDOW).clear();
		getActionMap().clear();
		bindMovement(KeyAction.TETRIS3D_UP, 0, -1);
		bindMovement(KeyAction.TETRIS3D_DOWN, 0, 1);
		bindMovement(KeyAction.TETRIS3D_LEFT, -1, 0);
		bindMovement(KeyAction.TETRIS3D_RIGHT, 1, 0);
		// 회전 J K L
		bindKey(KeyAction.TETRIS3D_ROTATE_X, new Runnable()
		{
			public void run()
			{
				game.rotate(Block3D.Axis.X);
			}
		});
		bindKey(KeyAction.TETRIS3D_ROTATE_Y, new Runnable()
		{
			public void run()
			{
				game.rotate(Block3D.Axis.Y);
			}
		});
		bindKey(KeyAction.TETRIS3D_ROTATE_Z, new Runnable()
		{
			public void run()
			{
				game.rotate(Block3D.Axis.Z);
			}
		});
		// 하드드롭
		bindKey(KeyAction.TETRIS3D_DROP, new Runnable()
		{
			public void run()
			{
				game.hardDrop();
			}
		});
		// ESC: 게임을 멈추고 설정 화면을 띄운 뒤, 닫으면 이어서 진행
		bindKey(KeyEvent.VK_ESCAPE, "Escape", new Runnable()
		{
			public void run()
			{
				game.stop();
				SettingsPanel.showDialog(SwingUtilities.getWindowAncestor(Game3DPanel.this));

				// 설정에서 키를 바꿨을 수 있으니 키 등록과 조작법 안내를 새로 고친다
				bindAllKeys();
				refreshControlTexts();
				resetPressedKeys();
				game.start();
			}
		});
		// 재시작
		bindKey(KeyAction.TETRIS3D_RESTART, new Runnable()
		{
			public void run()
			{
				resetPressedKeys();
				game.restart();
			}
		}, Game3D.State.GAME_OVER);
		refreshStatus();
	}

	// 오른쪽 정보창
	JScrollPane buildSidebar()
	{
		JPanel sidebar = new JPanel();
		sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
		sidebar.setBackground(new Color(13, 18, 29));
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
		JLabel layerTitle = new JLabel("삭제한 층 수"); layerTitle.setForeground(new Color(220, 230, 245));
		layers.add(layerTitle);
		layers.add(styledValue(clearedLayers));
		values.add(layers); stats.add(values);
		JLabel scoringRule = new JLabel("삭제 층당 " + 100 + "점");
		scoringRule.setForeground(new Color(145, 166, 193));
		stats.add(scoringRule);
		sidebar.add(stats);
		sidebar.add(Box.createVerticalStrut(10));
		JPanel state = card("GAME STATUS");
		state.add(styledValue(gameState));
		restartHint.setForeground(new Color(220, 230, 245));
		state.add(restartHint);
		sidebar.add(state);
		sidebar.add(Box.createVerticalStrut(10));
		JPanel controls = card("조작법");
		String[] texts = controlTexts();
		for (int i = 0; i < texts.length; i++)
		{
			JLabel label = new JLabel(texts[i]); label.setForeground(new Color(220, 230, 245));
			controlLabels.add(label);
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
		scroll.getViewport().setBackground(new Color(13, 18, 29));
		scroll.getVerticalScrollBar().setUnitIncrement(20);
		return scroll;
	}

	// 지금 설정된 키로 조작법 안내 글을 만든다
	static String[] controlTexts()
	{
		return new String[] {
				KeySettings.getKeyName(KeyAction.TETRIS3D_UP) + " / " + KeySettings.getKeyName(KeyAction.TETRIS3D_LEFT)
						+ " / " + KeySettings.getKeyName(KeyAction.TETRIS3D_DOWN) + " / "
						+ KeySettings.getKeyName(KeyAction.TETRIS3D_RIGHT) + " : 평면 이동",
				KeySettings.getKeyName(KeyAction.TETRIS3D_DROP) + " : Hard Drop",
				KeySettings.getKeyName(KeyAction.TETRIS3D_ROTATE_X) + " / " + KeySettings.getKeyName(KeyAction.TETRIS3D_ROTATE_Y)
						+ " / " + KeySettings.getKeyName(KeyAction.TETRIS3D_ROTATE_Z) + " : X / Y / Z축 회전",
				"Mouse Drag : 카메라 회전",
				"Mouse Wheel : Zoom",
				KeySettings.getKeyName(KeyAction.TETRIS3D_RESTART) + " : 게임 오버 후 재시작",
				"ESC : 설정" };
	}

	// 조작법 안내 글을 지금 설정된 키로 다시 쓴다
	void refreshControlTexts()
	{
		String[] texts = controlTexts();
		for (int i = 0; i < texts.length; i++)
			controlLabels.get(i).setText(texts[i]);
	}

	static JPanel card(String title)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(new Color(19, 25, 38));
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

	static JLabel styledValue(JLabel label)
	{
		label.setForeground(new Color(220, 230, 245));
		label.setFont(label.getFont().deriveFont(Font.BOLD, 24f));
		return label;
	}

	// 이동 키
	void bindMovement(KeyAction action, final int dx, final int dy)
	{
		bindKey(action, new Runnable()
		{
			public void run()
			{
				game.move(dx, dy);
			}
		});
	}

	// 설정에 지정된 키를 등록한다 (게임 중에만 동작)
	void bindKey(KeyAction keyAction, Runnable action)
	{
		bindKey(keyAction, action, Game3D.State.RUNNING);
	}

	// 설정에 지정된 키를 등록한다 (requiredState일 때만 동작)
	void bindKey(KeyAction keyAction, Runnable action, Game3D.State requiredState)
	{
		bindKey(KeySettings.getKey(keyAction), keyAction.name(), action, requiredState);
	}

	void bindKey(final int keyCode, String name, final Runnable action)
	{
		bindKey(keyCode, name, action, Game3D.State.RUNNING);
	}

	// 키 누를때, 뗄때 등록 (shift 같은거 눌러도 되게)
	void bindKey(final int keyCode, String name, final Runnable action, final Game3D.State requiredState)
	{
		String pressAction = "press" + name;
		String releaseAction = "release" + name;
		for (int modifiers : new int[] { 0, InputEvent.SHIFT_DOWN_MASK })
		{
			getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, modifiers, false), pressAction);
		}
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
				// 꾹 누르고 있으면 계속 들어와서 무시
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
			gameState.setForeground(state == Game3D.State.GAME_OVER ? new Color(245, 98, 121) : new Color(220, 230, 245));
			restartHint.setText(state == Game3D.State.GAME_OVER ? KeySettings.getKeyName(KeyAction.TETRIS3D_RESTART) + " 키를 눌러 다시 시작" : " ");
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


	public void removeNotify()
	{
		resetPressedKeys();
		super.removeNotify();
	}
}
