package threed;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;

import common.Bgm;
import common.SoundManager;

// 3D 테트리스 창: 게임(Game3D)과 화면(Game3DPanel)을 만들어 연결하는 곳
public class Game3DFrame extends JFrame
{
	private static final int REFRESH_INTERVAL_MS = 50;

	// 게임 객체는 직렬화와 상관없어 transient (JFrame은 Serializable)
	private final transient Game3D game = new Game3D();
	private final Game3DPanel panel = new Game3DPanel(game);
	private final Timer statusTimer = new Timer(REFRESH_INTERVAL_MS, new ActionListener()
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			panel.refreshStatus(); // 0.05초마다 화면 갱신
		}
	});

	public Game3DFrame()
	{
		super("3D Tetris");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setContentPane(panel);
		pack();
		setMinimumSize(new Dimension(940, 600));
		setLocationRelativeTo(null);
		addWindowFocusListener(new WindowAdapter()
		{
			@Override
			public void windowLostFocus(WindowEvent event)
			{
				panel.resetPressedKeys();
			}
		});
		game.start();
		SoundManager.startBgm(Bgm.GAME);
		panel.refreshStatus();
		statusTimer.start();
	}

	// 창 닫을때 타이머 멈추기
	@Override
	public void dispose()
	{
		game.stop();
		statusTimer.stop();
		panel.resetPressedKeys();
		SoundManager.stopBgm();
		super.dispose();
	}
}
