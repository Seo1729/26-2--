import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * 프로그램의 시작점이자 메인 창. 메인화면을 보여 주고, 고른 모드에 맞는 게임 화면으로 바꾼다.
 */
public class GameFrame extends JFrame
{
	/** 일반 모드와 갤러그 협동 모드에서 쓰는 테트리스 게임 화면 */
	GamePanel fgp;

	/** 지금 게임 모드(1: 일반, 3: 갤러그 협동). 게임 화면이 모드에 따라 다르게 동작할 때 쓴다. */
	static int gameMode = 1;

	/** 똥피하기 모드의 창 가로 크기(픽셀). dodge.DodgeModeMain과 같은 값이다. */
	private static final int DODGE_WIDTH = 1100;

	/** 똥피하기 모드의 창 세로 크기(픽셀). dodge.DodgeModeMain과 같은 값이다. */
	private static final int DODGE_HEIGHT = 1000;

	/** 실행하면 처음 보이는 메인화면. 게임을 시작하면 창에서 떼어 낸다. */
	private final MainMenuPanel mainMenu;

	GameFrame()
	{
		setTitle("Tetris");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setLayout(null);
		setSize(360, 640);
		setResizable(false);

		Container c = this.getContentPane();

		c.setBackground(new Color(30, 160, 255));

		// 게임은 메뉴에서 모드를 고른 뒤 만들고, 처음에는 메인화면만 보여 준다
		mainMenu = new MainMenuPanel(this);
		mainMenu.setBounds(0, 0, 360, 640);
		add(mainMenu);

		setLocationRelativeTo(getParent());

		setVisible(true);
	}

	/**
	 * 메인화면에서 고른 모드로 게임을 시작한다. 메인화면을 떼어 내고 같은 창에 게임 화면을 붙인다.
	 *
	 * @param mode 시작할 게임 모드(1: 일반, 3: 갤러그 협동)
	 */
	public void startGame(int mode)
	{
		// 게임 패널과 아이템 처리가 이 값으로 모드를 구분하므로 먼저 정해 둔다
		gameMode = mode;

		// 메인화면을 창에서 떼어 내고 그 자리에 게임 화면을 만든다
		remove(mainMenu);

		switch (gameMode)
		{
			case 1:
			case 3:
				fgp = new GamePanel();
				fgp.setBounds(0, 0, 360, 640);
				add(fgp);
				addTetrisKeyListener(fgp);
				break;
		}

		// 갤러그 협동 모드면 테트리스 영역(10, 60, 240x480) 위에 갤러그 레이어를 겹쳐 올리고
		// 전투기 조작 키도 프레임에서 함께 받도록 등록한다
		if (gameMode == 3)
		{
			GalagaLayer galagaLayer = new GalagaLayer(10, 60, 240, 480, fgp);
			getLayeredPane().add(galagaLayer, JLayeredPane.PALETTE_LAYER);
			addKeyListener(galagaLayer.getKeyListener());
		}

		// 바뀐 화면을 다시 그리고, 버튼에 가 있던 키 입력을 게임 창으로 돌려놓는다
		revalidate();
		repaint();
		requestFocusInWindow();
	}

	/**
	 * 메인화면에서 똥피하기 모드를 시작한다. 메인화면을 떼어 내고, 창을 이 모드에 맞는 크기로 키운 뒤
	 * 같은 창에 똥피하기 화면을 붙인다.
	 */
	public void startDodge()
	{
		// 메인화면을 창에서 떼어 낸다
		remove(mainMenu);

		// 똥피하기 화면은 창 전체를 채우도록 배치 방식을 바꾼다(기존 좌표 배치는 게임 모드에서만 쓴다)
		setLayout(new BorderLayout());
		setTitle("Tetris X 똥 피하기");

		// 작업 표시줄을 뺀 화면 크기보다 크면 화면에 맞춰 창 크기를 정하고, 화면 가운데로 옮긴다
		Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		setSize(Math.min(DODGE_WIDTH, screen.width), Math.min(DODGE_HEIGHT, screen.height));
		setLocationRelativeTo(null);

		// 똥피하기 화면을 붙이고, 화면에 붙은 뒤에 시작해야 키 입력 포커스를 받을 수 있다
		dodge.DodgeMode dodgeMode = new dodge.DodgeMode();
		add(dodgeMode, BorderLayout.CENTER);
		revalidate();
		repaint();
		dodgeMode.start();
	}

	/**
	 * 메인화면에서 3D 테트리스를 시작한다. 3D 테트리스는 자체 창을 쓰므로, 그 창을 먼저 띄운 뒤
	 * 이 메인 창은 닫는다. 3D 창을 닫으면 프로그램이 끝난다.
	 */
	public void start3D()
	{
		// 3D 창을 먼저 띄워야 열린 창이 없어지는 순간이 생기지 않는다
		new Game3DFrame().setVisible(true);

		// 메인 창은 더 쓰지 않으므로 닫는다
		dispose();
	}

	/**
	 * 일반 테트리스 조작 키를 창에 등록한다. 카운트다운이 끝나고 게임이 진행 중일 때만 키를 받는다.
	 * 키 배치는 FirstPlayerKeySetting에 있다(↓, ←, →, 1, 2, 3, Space, ↑).
	 *
	 * @param gp 키 입력을 받을 테트리스 게임 화면
	 */
	public void addTetrisKeyListener(GamePanel gp)
	{
		this.setFocusable(true);
		this.addKeyListener(new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				// 카운트다운 중이거나 게임이 끝났으면 무시한다
				if (!gp.gameRunning || !gp.started)
				{
					return;
				}

				int key = e.getKeyCode();

				// 블록을 움직인 키는 벽을 바로잡고 화면을 다시 그린다
				if (key == FirstPlayerKeySetting.FKeyType[0])
				{
					gp.moveDown();
					gp.resetWalls();
					gp.drawBoard();
				}
				else if (key == FirstPlayerKeySetting.FKeyType[1])
				{
					gp.moveLeft();
					gp.resetWalls();
					gp.drawBoard();
				}
				else if (key == FirstPlayerKeySetting.FKeyType[2])
				{
					gp.moveRight();
					gp.resetWalls();
					gp.drawBoard();
				}
				else if (key == FirstPlayerKeySetting.FKeyType[3])
				{
					// 1인 모드에서는 내게 쓰기만 있으므로 대상을 자기 화면으로 정한다
					gp.targetPanel = fgp;
					gp.useItem();
				}
				else if (key == FirstPlayerKeySetting.FKeyType[4])
				{
					gp.attackItem();
				}
				else if (key == FirstPlayerKeySetting.FKeyType[5])
				{
					gp.removeFirstItem();
				}
				else if (key == FirstPlayerKeySetting.FKeyType[6])
				{
					gp.hardDrop();
					gp.resetWalls();
					gp.drawBoard();
				}
				else if (key == FirstPlayerKeySetting.FKeyType[7])
				{
					gp.rotate();
					gp.resetWalls();
					gp.drawBoard();
				}
			}
		});
	}

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(() -> {
			new ImageSource();
			new GameFrame();
		});
	}
}
