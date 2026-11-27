package origin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import common.Bgm;
import common.Settings;
import common.SettingsPanel;
import common.SoundManager;

import galag.GalagaLayer;
import threed.Game3DFrame;

/**
 * 프로그램의 메인 창. 메인화면, 설정 화면, 각 게임 모드 화면을 바꿔 끼우는 일만 한다.
 * 키 입력 처리는 TetrisKeyHandler, 게임 진행은 GamePanel이 맡는다.
 */
public class GameFrame extends JFrame
{
	private static final int WIDTH = 360; // 메인화면·2D 테트리스 창 크기
	private static final int HEIGHT = 640;

	GamePanel fgp;

	MainMenuPanel mainMenu;
	SettingsPanel settingsPanel; // 환경 설정 화면
	dodge.DodgeMode dodgeMode; // 똥피하기
	vsai.VsAiMode vsAiMode; // 사람 vs AI

	GameFrame()
	{
		setTitle("Tetris");
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		setLayout(null);
		setSize(WIDTH, HEIGHT);
		setResizable(false);

		Container c = this.getContentPane();

		c.setBackground(new Color(30, 160, 255));

		// 메인화면
		mainMenu = new MainMenuPanel(this);
		mainMenu.setBounds(0, 0, WIDTH, HEIGHT);
		add(mainMenu);

		setLocationRelativeTo(getParent());

		setVisible(true);
	}

	// 게임 시작 (일반 또는 갤러그)
	public void startGame(GameMode mode)
	{
		startGame(mode, 1);
	}

	// 시작 레벨(1~5)을 정해서 게임 시작 (일반 모드에서 레벨을 고를 때 쓴다)
	public void startGame(GameMode mode, int level)
	{
		remove(mainMenu);
		SoundManager.startBgm(Bgm.GAME);

		fgp = new GamePanel(mode, level);
		fgp.setBounds(0, 0, WIDTH, HEIGHT);
		add(fgp);
		setFocusable(true);
		addKeyListener(new TetrisKeyHandler(this, fgp));

		// 갤러그: 보드 위에 전투기·외계인 층을 얹는다
		if (mode == GameMode.GALAGA)
		{
			GalagaLayer galagaLayer = new GalagaLayer(10, 60, 240, 480, fgp, fgp);
			getLayeredPane().add(galagaLayer, JLayeredPane.PALETTE_LAYER);
			addKeyListener(galagaLayer.getKeyListener());
		}

		revalidate();
		repaint();
		requestFocusInWindow(); // 이거 안하면 키 안먹음
	}

	public void startDodge()
	{
		remove(mainMenu);
		SoundManager.startBgm(Bgm.GAME);

		setLayout(new BorderLayout());
		setTitle("Tetris X 똥 피하기");
		fitToScreen(1100, 1000);

		dodgeMode = new dodge.DodgeMode();
		add(dodgeMode, BorderLayout.CENTER);
		revalidate();
		repaint();
		dodgeMode.start();
	}

	// 사람 vs AI 모드 시작
	public void startVsAi()
	{
		remove(mainMenu);
		SoundManager.startBgm(Bgm.GAME);

		setLayout(new BorderLayout());
		setTitle("Tetris - 컴퓨터와 대전");
		fitToScreen(1100, 800);

		vsAiMode = new vsai.VsAiMode();
		add(vsAiMode, BorderLayout.CENTER);
		revalidate();
		repaint();
		vsAiMode.start();
	}

	// 창 크기를 정하되 화면보다 크면 화면 크기로 줄이고 가운데에 놓는다
	private void fitToScreen(int width, int height)
	{
		Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		setSize(Math.min(width, screen.width), Math.min(height, screen.height));
		setLocationRelativeTo(null);
	}

	// 메인화면에서 환경 설정 화면으로 바꾼다
	public void openSettings()
	{
		remove(mainMenu);

		// 돌아가기 버튼을 누르면 메인화면으로 되돌아간다
		settingsPanel = new SettingsPanel(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				closeSettings();
			}
		});
		settingsPanel.setBounds(0, 0, WIDTH, HEIGHT);
		add(settingsPanel);

		revalidate();
		repaint();
	}

	// 환경 설정 화면에서 메인화면으로 되돌아간다
	public void closeSettings()
	{
		remove(settingsPanel);
		add(mainMenu);

		revalidate();
		repaint();
	}

	public void start3D()
	{
		// 3D는 창이 따로 있음
		Game3DFrame frame3d = new Game3DFrame();
		frame3d.setVisible(true);

		dispose();
	}

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				// 저장된 음량과 음소거 설정을 소리에 적용한 뒤 시작한다
				Settings.load();
				ImageSource.preload();
				new GameFrame();
			}
		});
	}
}
