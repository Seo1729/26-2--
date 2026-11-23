package threed;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import common.SettingsPanel;

// 3D 화면 전체: 가운데 3D 화면 + 오른쪽 정보창(Game3DSidebar) + 키 입력(Game3DKeyBindings)을 조립한다.
// 키 입력은 Game3DControl(조작), 화면 갱신은 Game3DStatus(조회)로 같은 게임을 나눠서 본다. (ISP)
public class Game3DPanel extends JPanel
{
	private final transient Game3DControl control;
	private final transient Game3DStatus status;
	private final transient Game3DKeyBindings keyBindings;
	private final JLabel statusLine = new JLabel();
	private final Board3DRenderer renderer = new Board3DRenderer();
	private final Game3DSidebar sidebar = new Game3DSidebar();

	public Game3DPanel(Game3D game)
	{
		if (game == null)
			throw new NullPointerException("game");
		this.control = game;
		this.status = game;
		this.keyBindings = new Game3DKeyBindings(this, control, new Runnable()
		{
			@Override
			public void run()
			{
				refreshStatus();
			}
		});
		setLayout(new BorderLayout(16, 12));
		setBackground(Theme3D.PANEL_BACKGROUND);
		setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
		JLabel title = new JLabel("3D TETRIS");
		title.setForeground(Theme3D.TEXT);
		title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
		add(title, BorderLayout.NORTH);
		JPanel center = new JPanel(new BorderLayout(0, 8));
		center.setOpaque(false);
		center.add(renderer, BorderLayout.CENTER);
		statusLine.setForeground(Theme3D.TEXT);
		center.add(statusLine, BorderLayout.SOUTH);
		add(center, BorderLayout.CENTER);
		add(wrapInScrollPane(sidebar), BorderLayout.EAST);
		bindAllKeys();
	}

	// 지금 설정된 키로 모든 키를 (다시) 등록한다
	void bindAllKeys()
	{
		keyBindings.bindAll(new Runnable()
		{
			@Override
			public void run()
			{
				openSettings();
			}
		});
		refreshStatus();
	}

	// ESC: 게임을 멈추고 설정 화면을 띄운 뒤, 닫으면 이어서 진행
	private void openSettings()
	{
		control.stop();
		SettingsPanel.showDialog(SwingUtilities.getWindowAncestor(this));

		// 설정에서 키를 바꿨을 수 있으니 키 등록과 조작법 안내를 새로 고친다
		bindAllKeys();
		sidebar.refreshControlTexts();
		resetPressedKeys();
		control.start();
	}

	// 오른쪽 정보창은 세로로 길어질 수 있어 스크롤 안에 넣는다
	private static JScrollPane wrapInScrollPane(JPanel content)
	{
		JScrollPane scroll = new JScrollPane(content, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setFocusable(false);
		scroll.getVerticalScrollBar().setFocusable(false);
		scroll.setPreferredSize(new Dimension(320, 800));
		scroll.getViewport().setBackground(Theme3D.PANEL_BACKGROUND);
		scroll.getVerticalScrollBar().setUnitIncrement(20);
		return scroll;
	}

	public void resetPressedKeys()
	{
		keyBindings.resetPressedKeys();
	}

	// 게임 상태를 3D 화면, 정보창, 아래 상태 줄에 반영한다
	public void refreshStatus()
	{
		synchronized (status)
		{
			Game3DScene scene = status.getScene();
			renderer.setScene(scene);
			sidebar.show(status, scene);
			if (!status.isRunning())
				resetPressedKeys();
			statusLine.setText(statusLineText());
		}
	}

	private String statusLineText()
	{
		Block3D block = status.getActiveBlock();
		if (block == null)
			return "블록 없음: 생성 위치를 확인하세요. 고정 블록: " + status.getLockedBlockCount();
		return block.getType() + "  (X=" + block.getX() + ", Y=" + block.getY()
				+ ", Z=" + block.getZ() + ")  " + (status.isRunning() ? "낙하 중" : "정지")
				+ "  고정 블록: " + status.getLockedBlockCount();
	}

	@Override
	public void removeNotify()
	{
		resetPressedKeys();
		super.removeNotify();
	}
}
