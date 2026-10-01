import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * 갤러그 협동 모드에서 테트리스 영역 위에 겹쳐 그려지는 투명 레이어.
 * 전투기 등 갤러그 요소를 일정 주기로 갱신하고 그리는 역할을 맡는다.
 */
public class GalagaLayer extends JPanel
{
	/** 화면 갱신 주기(밀리초). 약 33프레임/초 */
	private static final int TICK = 30;

	/** 연사 간격(타이머 주기 횟수). 7 x 30ms = 약 0.2초마다 한 발 */
	private static final int FIRE_INTERVAL = 7;

	/** 하단에서 외계인을 격추하는 전투기 */
	private final Fighter fighter;

	/** 화면 상단에서 좌우로 움직이는 외계인 편대 */
	private final AlienFleet fleet;

	/** 현재 화면에 날아가고 있는 총알 목록 */
	private final ArrayList<Bullet> bullets = new ArrayList<Bullet>();

	/** 발사 키(W)를 누르고 있는지 여부. 누르고 있는 동안 연사한다. */
	private boolean fireHeld;

	/** 다음 발사까지 남은 타이머 주기 횟수. 0이면 바로 쏠 수 있다. */
	private int fireCooldown;

	/** 왼쪽 이동 키(A)를 누르고 있는지 여부 */
	private boolean leftHeld;

	/** 오른쪽 이동 키(D)를 누르고 있는지 여부 */
	private boolean rightHeld;

	/** 전투기 조작 키의 눌림 상태를 기록하는 키 리스너 */
	private final KeyListener keyListener;

	/**
	 * 지정한 위치와 크기로 투명 레이어를 만들고 갱신 타이머를 시작한다.
	 *
	 * @param x      레이어 왼쪽 위 x좌표(프레임 기준 픽셀)
	 * @param y      레이어 왼쪽 위 y좌표(프레임 기준 픽셀)
	 * @param width  레이어 너비(픽셀). 테트리스 영역 너비와 같게 준다.
	 * @param height 레이어 높이(픽셀). 테트리스 영역 높이와 같게 준다.
	 */
	public GalagaLayer(int x, int y, int width, int height)
	{
		// 아래의 테트리스 블록이 비쳐 보이도록 배경을 칠하지 않는다
		setOpaque(false);
		setBounds(x, y, width, height);
		fighter = new Fighter(width, height);
		fleet = new AlienFleet(width, height);

		// 키를 누르거나 뗄 때 상태만 기록한다. 실제 이동은 타이머에서 하므로
		// 테트리스 플레이어가 다른 키를 누르고 있어도 전투기 이동이 끊기지 않는다
		keyListener = new KeyAdapter()
		{
			public void keyPressed(KeyEvent e)
			{
				setHeld(e.getKeyCode(), true);
			}

			public void keyReleased(KeyEvent e)
			{
				setHeld(e.getKeyCode(), false);
			}
		};

		// 일정 주기마다 눌린 키에 따라 전투기를 움직이고 화면을 다시 그린다
		Timer timer = new Timer(TICK, new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				update();
				repaint();
			}
		});
		timer.start();
	}

	/**
	 * 전투기 조작 키 입력을 받을 키 리스너를 돌려준다. 프레임에 등록해서 사용한다.
	 *
	 * @return 전투기 조작용 키 리스너
	 */
	public KeyListener getKeyListener()
	{
		return keyListener;
	}

	/**
	 * 전투기 조작 키의 눌림 상태를 바꾼다. 전투기 키가 아니면 무시한다.
	 *
	 * @param keyCode 눌리거나 떼어진 키 코드
	 * @param held    눌렸으면 true, 떼어졌으면 false
	 */
	private void setHeld(int keyCode, boolean held)
	{
		// A는 왼쪽, D는 오른쪽 이동 키, W는 발사 키로 쓴다
		if (keyCode == KeyEvent.VK_A)
			leftHeld = held;
		else if (keyCode == KeyEvent.VK_D)
			rightHeld = held;
		else if (keyCode == KeyEvent.VK_W)
			fireHeld = held;
	}

	/**
	 * 한 주기 동안의 갤러그 상태를 갱신한다. 편대 이동, 전투기 이동, 연사, 총알 이동을 처리한다.
	 */
	private void update()
	{
		// 외계인 편대를 좌우로 흔든다
		fleet.update();

		// 누르고 있는 방향으로 전투기를 이동시킨다(둘 다 누르면 제자리)
		if (leftHeld)
			fighter.moveLeft();
		if (rightHeld)
			fighter.moveRight();

		// 발사 대기 시간을 한 주기만큼 줄인다
		if (fireCooldown > 0)
			fireCooldown--;

		// W를 누르고 있고 대기 시간이 끝났으면 한 발 쏘고, 다음 발사까지 간격을 다시 채운다
		if (fireHeld && fireCooldown == 0)
		{
			bullets.add(fighter.fire());
			fireCooldown = FIRE_INTERVAL;
		}

		// 총알을 위로 옮기고, 화면 밖으로 나갔거나 외계인을 맞힌 총알은 지운다
		// (삭제해도 인덱스가 꼬이지 않게 뒤에서부터 순회)
		for (int i = bullets.size() - 1; i >= 0; i--)
		{
			bullets.get(i).move();
			if (bullets.get(i).isOutOfArea() || fleet.hit(bullets.get(i)))
				bullets.remove(i);
		}
	}

	/**
	 * 레이어 위에 갤러그 요소를 그린다.
	 *
	 * @param g 그리기에 사용할 그래픽 객체
	 */
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		fleet.draw(g);
		fighter.draw(g);

		// 날아가고 있는 총알을 모두 그린다
		for (int i = 0; i < bullets.size(); i++)
			bullets.get(i).draw(g);
	}
}
