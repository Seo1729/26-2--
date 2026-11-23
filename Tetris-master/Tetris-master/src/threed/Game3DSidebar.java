package threed;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import common.KeyAction;
import common.KeySettings;

// 3D 화면 오른쪽 정보창 (위에서 본 화면, 다음 블록, 점수, 게임 상태, 조작법)
// 정보창을 만들고 값을 채우는 일만 한다. 값은 Game3DStatus(조회 전용)로 받는다.
public class Game3DSidebar extends JPanel
{
	private final TopViewPanel topView = new TopViewPanel();
	private final JLabel nextBlock = new JLabel();
	private final NextBlockPanel nextPreview = new NextBlockPanel();
	private final JLabel score = new JLabel("0");
	private final JLabel clearedLayers = new JLabel("0");
	private final JLabel gameState = new JLabel("READY");
	private final JLabel restartHint = new JLabel(" ");
	private final ArrayList<JLabel> controlLabels = new ArrayList<>(); // 조작법 안내 글들

	public Game3DSidebar()
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(Theme3D.PANEL_BACKGROUND);
		topView.setAlignmentX(LEFT_ALIGNMENT);
		topView.setPreferredSize(new Dimension(280, 280));
		topView.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
		topView.setMinimumSize(new Dimension(280, 280));
		add(topView);
		add(Box.createVerticalStrut(10));
		add(buildNextCard());
		add(Box.createVerticalStrut(10));
		add(buildScoreCard());
		add(Box.createVerticalStrut(10));
		add(buildStateCard());
		add(Box.createVerticalStrut(10));
		add(buildControlsCard());
		add(Box.createVerticalGlue());
	}

	private JPanel buildNextCard()
	{
		JPanel next = card("NEXT BLOCK");
		JPanel nextContent = new JPanel(new BorderLayout(12, 0));
		nextContent.setOpaque(false);
		nextContent.add(styledValue(nextBlock), BorderLayout.WEST);
		nextContent.add(nextPreview, BorderLayout.CENTER);
		next.add(nextContent);
		return next;
	}

	private JPanel buildScoreCard()
	{
		JPanel stats = card("SCORE");
		JPanel values = new JPanel(new GridLayout(1, 2, 12, 0));
		values.setOpaque(false);
		values.add(styledValue(score));
		JPanel layers = new JPanel();
		layers.setOpaque(false);
		layers.setLayout(new BoxLayout(layers, BoxLayout.Y_AXIS));
		JLabel layerTitle = new JLabel("삭제한 층 수");
		layerTitle.setForeground(Theme3D.TEXT);
		layers.add(layerTitle);
		layers.add(styledValue(clearedLayers));
		values.add(layers);
		stats.add(values);
		JLabel scoringRule = new JLabel("삭제 층당 " + Game3D.POINTS_PER_LAYER + "점");
		scoringRule.setForeground(Theme3D.MUTED_TEXT);
		stats.add(scoringRule);
		return stats;
	}

	private JPanel buildStateCard()
	{
		JPanel state = card("GAME STATUS");
		state.add(styledValue(gameState));
		restartHint.setForeground(Theme3D.TEXT);
		state.add(restartHint);
		return state;
	}

	private JPanel buildControlsCard()
	{
		JPanel controls = card("조작법");
		String[] texts = controlTexts();
		for (int i = 0; i < texts.length; i++)
		{
			JLabel label = new JLabel(texts[i]);
			label.setForeground(Theme3D.TEXT);
			controlLabels.add(label);
			controls.add(label);
		}
		return controls;
	}

	// 게임 상태를 정보창에 옮겨 적는다 (scene은 3D 화면과 같은 것을 받아 위에서 본 화면에 쓴다)
	void show(Game3DStatus status, Game3DScene scene)
	{
		topView.setScene(scene);
		Block3D.Type nextType = status.getNextBlockType();
		nextBlock.setText(nextType.name());
		nextBlock.setForeground(BlockColors.colorFor(nextType));
		nextPreview.setType(nextType);
		clearedLayers.setText(Long.toString(status.getClearedLayerCount()));
		score.setText(Long.toString(status.getScore()));
		boolean over = status.getState() == Game3D.State.GAME_OVER;
		gameState.setText(over ? "GAME OVER" : status.getState().name());
		gameState.setForeground(over ? Theme3D.DANGER_TEXT : Theme3D.TEXT);
		restartHint.setText(over ? KeySettings.getKeyName(KeyAction.TETRIS3D_RESTART) + " 키를 눌러 다시 시작" : " ");
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

	private static JPanel card(String title)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBackground(Theme3D.VIEW_BACKGROUND);
		panel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
		panel.setAlignmentX(LEFT_ALIGNMENT);
		panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
		JLabel heading = new JLabel(title);
		heading.setForeground(Theme3D.MUTED_TEXT);
		heading.setFont(heading.getFont().deriveFont(Font.BOLD, 12f));
		panel.add(heading);
		panel.add(Box.createVerticalStrut(6));
		return panel;
	}

	private static JLabel styledValue(JLabel label)
	{
		label.setForeground(Theme3D.TEXT);
		label.setFont(label.getFont().deriveFont(Font.BOLD, 24f));
		return label;
	}
}
