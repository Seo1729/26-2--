package origin;

import java.awt.*;
import java.awt.event.*;

import common.Bgm;
import common.KeyAction;
import common.KeySettings;
import common.Sound;
import common.SoundManager;

import javax.swing.*;

// 메인화면
public class MainMenuPanel extends JPanel implements ActionListener
{
	private static final String MENU_FONT = "Dialog";

	GameFrame frame;
	JLabel logo;
	JLabel bestLabel; // 최고 점수 표시
	// 버튼은 글자가 아니라 객체로 구분한다 (전에는 버튼 한글 문구를 비교해서 문구를 바꾸면 동작이 끊겼다)
	JButton normalButton, galagaButton, poopButton, threeDButton, controlsButton, exitButton, settingsButton,
			vsCpuButton, characterButton, rankingButton;

	public MainMenuPanel(GameFrame f)
	{
		frame = f;

		// 메인화면에서는 차분한 메뉴 음악을 튼다
		SoundManager.startBgm(Bgm.MENU);

		setLayout(null);
		setBackground(new Color(30, 160, 255));

		// 게임에 들어가기 전에 일반 모드 최고 점수를 보여 준다
		bestLabel = new JLabel(bestScoreText(), SwingConstants.CENTER);
		bestLabel.setFont(new Font(MENU_FONT, Font.BOLD, 16));
		bestLabel.setForeground(Color.WHITE);
		bestLabel.setBounds(0, 18, 360, 30);
		add(bestLabel);

		// 로고
		logo = new JLabel(ImageSource.IMG_LOGO);
		logo.setBounds(0, 60, 360, 180);
		add(logo);

		normalButton = makeButton("일반 모드");
		galagaButton = makeButton("갤러그 협동");
		poopButton = makeButton("똥피하기");
		threeDButton = makeButton("3D 테트리스");
		controlsButton = makeButton("조작법");
		exitButton = makeButton("종료");
		settingsButton = makeButton("설정");
		vsCpuButton = makeButton("컴퓨터와 대전");
		characterButton = makeButton("캐릭터 관리");
		rankingButton = makeButton("랭킹");

		// 버튼이 10개라서 간격을 좁혀 창 안에 모두 들어가게 맞춘다
		JButton[] menuButtons = { normalButton, galagaButton, poopButton, threeDButton, vsCpuButton, characterButton,
				rankingButton, controlsButton, settingsButton, exitButton };
		for (int i = 0; i < menuButtons.length; i++)
			menuButtons[i].setBounds(90, 246 + i * 33, 180, 29);
	}

	// 같은 모양의 메뉴 버튼을 만들어 붙인다
	private JButton makeButton(String text)
	{
		JButton button = new JButton(text);
		button.setFont(new Font(MENU_FONT, Font.BOLD, 16));
		button.setBackground(Color.WHITE);
		button.setFocusPainted(false);
		button.addActionListener(this);
		add(button);
		return button;
	}

	public void actionPerformed(ActionEvent e)
	{
		SoundManager.play(Sound.CLICK);
		Object source = e.getSource();

		if (source == normalButton)
			chooseLevelAndStart();
		else if (source == galagaButton)
			frame.startGame(GameMode.GALAGA);
		else if (source == poopButton)
			frame.startPoop();
		else if (source == threeDButton)
			frame.start3D();
		else if (source == controlsButton)
			showControls();
		else if (source == vsCpuButton)
			frame.startVsCpu();
		else if (source == characterButton)
			poop.CharacterCustomizationPanel.show(this);
		else if (source == rankingButton)
			JOptionPane.showMessageDialog(this, RankingManager.toText(), "랭킹 (일반 모드)", JOptionPane.INFORMATION_MESSAGE);
		else if (source == settingsButton)
			frame.openSettings();
		else if (source == exitButton)
			System.exit(0);
	}

	// 시작할 레벨(스테이지)을 고르게 하고, 창을 닫으면 메인화면에 그대로 남는다
	private void chooseLevelAndStart()
	{
		String[] levels = { "레벨 1", "레벨 2", "레벨 3", "레벨 4", "레벨 5" };
		int choice = JOptionPane.showOptionDialog(this, "시작할 레벨을 고르세요.\n레벨이 높을수록 블록이 빨리 떨어지고 점수를 더 많이 받습니다.",
				"레벨 선택", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, levels, levels[0]);
		if (choice >= 0)
			frame.startGame(GameMode.NORMAL, choice + 1);
	}

	// 모드별 조작 키를 보여 준다 (설정에서 바꾼 키가 그대로 나온다)
	private void showControls()
	{
		String msg = "";
		msg = msg + "[테트리스]\n";
		// 테트리스와 전투기 키는 설정에서 바꿀 수 있으므로 지금 지정된 키를 보여 준다
		msg = msg + KeySettings.getKeyName(KeyAction.TETRIS_LEFT) + " / " + KeySettings.getKeyName(KeyAction.TETRIS_RIGHT) + " : 좌우 이동\n";
		msg = msg + KeySettings.getKeyName(KeyAction.TETRIS_ROTATE) + " : 회전\n";
		msg = msg + KeySettings.getKeyName(KeyAction.TETRIS_DOWN) + " : 한 칸 내리기\n";
		msg = msg + KeySettings.getKeyName(KeyAction.TETRIS_DROP) + " : 바로 떨어뜨리기\n";
		msg = msg + KeySettings.getKeyName(KeyAction.TETRIS_HOLD) + " : 블록 홀드(보관)\n";
		msg = msg + KeySettings.getKeyName(KeyAction.TETRIS_ITEM_SELF) + " : 아이템 내게 쓰기   "
				+ KeySettings.getKeyName(KeyAction.TETRIS_ITEM_ENEMY) + " : 아이템 적에게 쓰기   "
				+ KeySettings.getKeyName(KeyAction.TETRIS_ITEM_DELETE) + " : 아이템 지우기\n";
		msg = msg + "ESC : 설정 (모든 모드에서 게임을 멈추고 설정 화면 열기)\n\n";
		msg = msg + "[전투기] (갤러그 협동 모드)\n";
		msg = msg + KeySettings.getKeyName(KeyAction.GALAGA_LEFT) + " / " + KeySettings.getKeyName(KeyAction.GALAGA_RIGHT) + " : 좌우 이동\n";
		msg = msg + KeySettings.getKeyName(KeyAction.GALAGA_SHOOT) + " (누르고 있기) : 연사\n\n";
		msg = msg + "[똥피하기 모드]\n";
		msg = msg + "P1 테트리스 : " + KeySettings.getKeyName(KeyAction.DODGE_BLOCK_LEFT) + " / "
				+ KeySettings.getKeyName(KeyAction.DODGE_BLOCK_RIGHT) + " 좌우 이동, "
				+ KeySettings.getKeyName(KeyAction.DODGE_BLOCK_ROTATE) + " 회전, "
				+ KeySettings.getKeyName(KeyAction.DODGE_BLOCK_DOWN) + " 내리기\n";
		msg = msg + "P2 피하는 사람 : " + KeySettings.getKeyName(KeyAction.DODGE_CAT_LEFT) + " / "
				+ KeySettings.getKeyName(KeyAction.DODGE_CAT_RIGHT) + " 이동, "
				+ KeySettings.getKeyName(KeyAction.DODGE_CAT_JUMP) + " 점프, "
				+ KeySettings.getKeyName(KeyAction.DODGE_CAT_JUMP) + " 두 번 3칸 점프(최대 3회, 20초마다 1회 충전)\n";
		msg = msg + KeySettings.getKeyName(KeyAction.DODGE_PAUSE) + " : 일시정지   "
				+ KeySettings.getKeyName(KeyAction.DODGE_RESTART) + " : 다시 시작\n\n";
		msg = msg + "[3D 테트리스]\n";
		msg = msg + KeySettings.getKeyName(KeyAction.TETRIS3D_UP) + " / " + KeySettings.getKeyName(KeyAction.TETRIS3D_LEFT) + " / "
				+ KeySettings.getKeyName(KeyAction.TETRIS3D_DOWN) + " / " + KeySettings.getKeyName(KeyAction.TETRIS3D_RIGHT)
				+ " : 블록 이동\n";
		msg = msg + KeySettings.getKeyName(KeyAction.TETRIS3D_ROTATE_X) + " / " + KeySettings.getKeyName(KeyAction.TETRIS3D_ROTATE_Y) + " / "
				+ KeySettings.getKeyName(KeyAction.TETRIS3D_ROTATE_Z) + " : X / Y / Z축 회전\n";
		msg = msg + KeySettings.getKeyName(KeyAction.TETRIS3D_DROP) + " : 바로 떨어뜨리기   "
				+ KeySettings.getKeyName(KeyAction.TETRIS3D_RESTART) + " : 다시 시작\n";
		msg = msg + "마우스 : 시점 조작";
		JOptionPane.showMessageDialog(this, msg, "조작법", JOptionPane.INFORMATION_MESSAGE);
	}

	// 메인화면에 보여 줄 최고 점수 글자 (랭킹 1위)
	String bestScoreText()
	{
		java.util.ArrayList<RankingManager.Entry> list = RankingManager.getEntries();
		if (list.isEmpty())
			return "최고 점수: 아직 없음";
		return "최고 점수: " + list.get(0).getScore() + "점 (" + list.get(0).getName() + ")";
	}
}
