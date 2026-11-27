package galag;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * 갤러그 협동 모드 화면 (테트리스 영역 위에 투명하게 겹쳐서 그림).
 * 30ms마다 GalagaGame을 한 단계 진행하고 그 상태를 그린다. 규칙은 GalagaGame, 키 상태는 GalagaControls가 맡는다.
 */
public class GalagaLayer extends JPanel
{
	private static final int TICK = 30; // 한 단계 간격(ms)
	private static final Color ALIEN_COLOR = new Color(255, 105, 180);

	private final transient GalagaTarget board;
	private final transient GalagaGame game;
	private final transient GalagaControls controls = new GalagaControls();
	private final Timer timer;

	/**
	 * @param target    갤러그가 보드에 요구하는 기능 (GamePanel이 구현)
	 * @param boardView 보드 화면 부품. 이 부품이 들어 있는 창이 포커스를 잃으면 눌린 키를 모두 뗀 것으로 한다
	 */
	public GalagaLayer(int x, int y, int width, int height, GalagaTarget target, Component boardView)
	{
		setOpaque(false);
		setBounds(x, y, width, height);
		board = target;
		game = new GalagaGame(target);

		timer = new Timer(TICK, new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				tick();
			}
		});
		timer.start();
		Window window = SwingUtilities.getWindowAncestor(boardView);
		if (window != null)
			window.addWindowFocusListener(new WindowAdapter()
			{
				@Override
				public void windowLostFocus(WindowEvent event)
				{
					controls.releaseAll();
				}
			});
	}

	/** 전투기 키 입력을 받을 리스너 (GameFrame이 창에 붙인다) */
	public KeyListener getKeyListener()
	{
		return controls;
	}

	// 타이머가 30ms마다 부른다
	private void tick()
	{
		// 테트리스 끝나면 같이 멈춤
		if (!board.isRunning())
		{
			timer.stop();
			setVisible(false);
			return;
		}
		// 설정 화면을 보는 동안에는 멈추고, 그 사이 뗀 키가 눌린 채로 남지 않게 지운다
		if (board.isPaused())
		{
			controls.releaseAll();
			return;
		}
		game.update(controls);
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		// 외계인
		for (Alien alien : game.getAliens())
		{
			if (alien.alive)
			{
				g.setColor(ALIEN_COLOR);
				g.fillOval(alien.x, alien.y, Alien.WIDTH, Alien.HEIGHT);
				g.setColor(Color.BLACK);
				g.fillRect(alien.x + 5, alien.y + 5, 3, 3);
				g.fillRect(alien.x + 12, alien.y + 5, 3, 3);
			}
		}

		// 전투기
		int fx = game.getFighterX();
		int fy = game.getFighterY();
		g.setColor(Color.CYAN);
		g.fillPolygon(new int[] { fx + GalagaGame.FIGHTER_WIDTH / 2, fx + GalagaGame.FIGHTER_WIDTH, fx },
				new int[] { fy, fy + GalagaGame.FIGHTER_HEIGHT, fy + GalagaGame.FIGHTER_HEIGHT }, 3);

		// 총알
		g.setColor(Color.YELLOW);
		for (Bullet bullet : game.getBullets())
			g.fillRect(bullet.x, bullet.y, Bullet.WIDTH, Bullet.HEIGHT);

		g.setColor(Color.WHITE);
		g.setFont(new Font("Verdana", Font.PLAIN, 10));
		g.drawString("WAVE " + game.getWave() + "/" + GalagaGame.LAST_WAVE, 4, 12);
	}
}
