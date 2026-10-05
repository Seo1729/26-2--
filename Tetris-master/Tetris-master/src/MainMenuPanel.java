import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * 게임을 실행하면 처음 보이는 메인화면.
 * 로고와 메뉴 버튼(일반 모드, 갤러그 협동, 똥피하기, 조작법, 종료)을 보여 주고, 누른 버튼에 맞는 동작을 한다.
 */
public class MainMenuPanel extends JPanel implements ActionListener
{
	/** 메뉴 버튼의 가로 크기(픽셀) */
	private static final int BUTTON_WIDTH = 180;

	/** 메뉴 버튼의 세로 크기(픽셀) */
	private static final int BUTTON_HEIGHT = 44;

	/** 첫 번째 버튼의 y좌표(픽셀). 로고 바로 아래에 둔다. */
	private static final int FIRST_BUTTON_Y = 290;

	/** 버튼 사이의 세로 간격(위쪽 끝 기준, 픽셀) */
	private static final int BUTTON_GAP = 56;

	/** 조작법 버튼을 누르면 보여 줄 키 안내 문구 */
	private static final String CONTROLS_TEXT =
			"[테트리스]\n"
			+ "← / → : 좌우 이동\n"
			+ "↑ : 회전\n"
			+ "↓ : 한 칸 내리기\n"
			+ "Space : 바로 떨어뜨리기\n"
			+ "1 : 아이템 내게 쓰기   2 : 아이템 적에게 쓰기   3 : 아이템 지우기\n\n"
			+ "[전투기] (갤러그 협동 모드)\n"
			+ "A / D : 좌우 이동\n"
			+ "W (누르고 있기) : 연사\n\n"
			+ "[똥피하기 모드]\n"
			+ "P1 테트리스 : A / D 좌우 이동, W 회전, S 내리기\n"
			+ "P2 피하는 사람 : ← / → 이동, ↑ 점프\n"
			+ "P : 일시정지   R : 끝난 뒤 다시 시작";

	/** 메뉴에서 고른 게임을 시작해 줄 게임 창 */
	private final GameFrame frame;

	/** 일반 테트리스를 시작하는 버튼 */
	private final JButton normalButton;

	/** 갤러그 협동 모드를 시작하는 버튼 */
	private final JButton galagaButton;

	/** 똥피하기 모드를 시작하는 버튼 */
	private final JButton dodgeButton;

	/** 조작 키 안내를 보여 주는 버튼 */
	private final JButton controlsButton;

	/** 프로그램을 끝내는 버튼 */
	private final JButton exitButton;

	/**
	 * 로고와 메뉴 버튼을 배치한 메인화면을 만든다.
	 *
	 * @param frame 버튼을 눌렀을 때 게임을 시작할 게임 창
	 */
	public MainMenuPanel(GameFrame frame)
	{
		this.frame = frame;

		// 버튼을 좌표로 직접 배치하고, 게임 화면과 같은 하늘색 배경을 쓴다
		setLayout(null);
		setBackground(new Color(30, 160, 255));

		// 화면 위쪽에 기존 Tetris 로고 이미지를 가로 전체 너비로 놓는다
		JLabel logo = new JLabel(ImageSource.img_logo);
		logo.setBounds(0, 60, ImageSource.img_logo.getIconWidth(), ImageSource.img_logo.getIconHeight());
		add(logo);

		// 로고 아래에 메뉴 버튼을 위에서부터 순서대로 놓는다
		normalButton = makeButton("일반 모드", 0);
		galagaButton = makeButton("갤러그 협동", 1);
		dodgeButton = makeButton("똥피하기", 2);
		controlsButton = makeButton("조작법", 3);
		exitButton = makeButton("종료", 4);
	}

	/**
	 * 메뉴 버튼 하나를 만들어 가운데 정렬로 배치하고, 이 패널이 클릭을 받도록 등록한다.
	 *
	 * @param text  버튼에 쓸 글자
	 * @param order 위에서부터 몇 번째 버튼인지(0부터)
	 * @return 배치가 끝난 버튼
	 */
	private JButton makeButton(String text, int order)
	{
		JButton button = new JButton(text);

		// 한글이 깨지지 않도록 기본 논리 글꼴(Dialog)을 쓴다
		button.setFont(new Font("Dialog", Font.BOLD, 16));
		button.setBackground(Color.WHITE);
		button.setFocusPainted(false);

		// 화면 가운데에 놓고, 순서만큼 아래로 내린다
		button.setBounds((360 - BUTTON_WIDTH) / 2, FIRST_BUTTON_Y + order * BUTTON_GAP, BUTTON_WIDTH, BUTTON_HEIGHT);
		button.addActionListener(this);
		add(button);
		return button;
	}

	/**
	 * 메뉴 버튼이 눌렸을 때 그 버튼에 맞는 동작을 한다.
	 *
	 * @param e 눌린 버튼 정보가 담긴 이벤트
	 */
	public void actionPerformed(ActionEvent e)
	{
		// 게임 모드 버튼은 해당 모드로 게임을 시작한다(1: 일반, 3: 갤러그 협동)
		if (e.getSource() == normalButton)
			frame.startGame(1);
		else if (e.getSource() == galagaButton)
			frame.startGame(3);
		// 똥피하기는 화면 크기가 달라서 게임 창이 별도 메서드로 화면을 바꾼다
		else if (e.getSource() == dodgeButton)
			frame.startDodge();
		// 조작법은 메인화면 위에 안내 창으로 보여 준다
		else if (e.getSource() == controlsButton)
			JOptionPane.showMessageDialog(this, CONTROLS_TEXT, "조작법", JOptionPane.INFORMATION_MESSAGE);
		// 종료는 프로그램을 끝낸다
		else if (e.getSource() == exitButton)
			System.exit(0);
	}
}
