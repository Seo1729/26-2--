import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.Timer;

/** 기존 2D GameFrame과 분리된 3D 키 입력 실행 창. Swing EDT에서 생성한다. */
public class Game3DFrame extends JFrame
{
	private final Game3D game = new Game3D();
	private final Game3DPanel panel = new Game3DPanel(game);
	private final Timer statusTimer = new Timer(50, event -> panel.refreshStatus());

	public Game3DFrame()
	{
		super("3D Tetris");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setContentPane(panel);
		pack();
		setMinimumSize(new java.awt.Dimension(640, 600));
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
		panel.refreshStatus();
		statusTimer.start();
	}

	@Override
	public void dispose()
	{
		game.stop();
		statusTimer.stop();
		panel.resetPressedKeys();
		super.dispose();
	}
}
