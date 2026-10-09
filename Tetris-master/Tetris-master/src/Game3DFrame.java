import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;

// 3D 테트리스 창
public class Game3DFrame extends JFrame
{
	Game3D game = new Game3D();
	Game3DPanel panel = new Game3DPanel(game);
	Timer statusTimer = new Timer(50, new ActionListener()
	{
		public void actionPerformed(ActionEvent e)
		{
			panel.refreshStatus(); // 0.05초마다 화면 갱신
		}
	});

	public Game3DFrame()
	{
		super("3D Tetris");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setContentPane(panel);
		pack();
		setMinimumSize(new Dimension(940, 600));
		setLocationRelativeTo(null);
		addWindowFocusListener(new WindowAdapter()
		{
			public void windowLostFocus(WindowEvent event)
			{
				panel.resetPressedKeys();
			}
		});
		game.start();
		panel.refreshStatus();
		statusTimer.start();
	}

	// 창 닫을때 타이머 멈추기
	public void dispose()
	{
		game.stop();
		statusTimer.stop();
		panel.resetPressedKeys();
		super.dispose();
	}
}
