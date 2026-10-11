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
	GameFrame frame;
	JLabel logo;
	JButton btn1, btn2, btn3, btn4, btn5, btn6, btn7;
	int selectMode = 0;
	boolean isOpen = true;

	public MainMenuPanel(GameFrame f)
	{
		frame = f;

		// 메인화면에서는 차분한 메뉴 음악을 튼다
		SoundManager.startBgm(Bgm.MENU);

		setLayout(null);
		setBackground(new Color(30, 160, 255));

		// 로고
		logo = new JLabel(ImageSource.img_logo);
		logo.setBounds(0, 60, 360, 180);
		add(logo);

		btn1 = new JButton("일반 모드");
		btn1.setFont(new Font("Dialog", Font.BOLD, 16));
		btn1.setBackground(Color.WHITE);
		btn1.setFocusPainted(false);
		btn1.setBounds(90, 250, 180, 40);
		btn1.addActionListener(this);
		add(btn1);

		btn2 = new JButton("갤러그 협동");
		btn2.setFont(new Font("Dialog", Font.BOLD, 16));
		btn2.setBackground(Color.WHITE);
		btn2.setFocusPainted(false);
		btn2.setBounds(90, 296, 180, 40);
		btn2.addActionListener(this);
		add(btn2);

		btn3 = new JButton("똥피하기");
		btn3.setFont(new Font("Dialog", Font.BOLD, 16));
		btn3.setBackground(Color.WHITE);
		btn3.setFocusPainted(false);
		btn3.setBounds(90, 342, 180, 40);
		btn3.addActionListener(this);
		add(btn3);

		btn4 = new JButton("3D 테트리스");
		btn4.setFont(new Font("Dialog", Font.BOLD, 16));
		btn4.setBackground(Color.WHITE);
		btn4.setFocusPainted(false);
		btn4.setBounds(90, 388, 180, 40);
		btn4.addActionListener(this);
		add(btn4);

		btn5 = new JButton("조작법");
		btn5.setFont(new Font("Dialog", Font.BOLD, 16));
		btn5.setBackground(Color.WHITE);
		btn5.setFocusPainted(false);
		btn5.setBounds(90, 434, 180, 40);
		btn5.addActionListener(this);
		add(btn5);

		btn6 = new JButton("종료");
		btn6.setFont(new Font("Dialog", Font.BOLD, 16));
		btn6.setBackground(Color.WHITE);
		btn6.setFocusPainted(false);
		btn6.setBounds(90, 526, 180, 40);
		btn6.addActionListener(this);
		add(btn6);

		btn7 = new JButton("설정");
		btn7.setFont(new Font("Dialog", Font.BOLD, 16));
		btn7.setBackground(Color.WHITE);
		btn7.setFocusPainted(false);
		btn7.setBounds(90, 480, 180, 40);
		btn7.addActionListener(this);
		add(btn7);
	}

	public void actionPerformed(ActionEvent e)
	{
		SoundManager.play(Sound.CLICK);
		String cmd = e.getActionCommand();

		if (cmd.equals("일반 모드"))
		{
			selectMode = 1;
			frame.startGame(1);
		}
		else if (cmd.equals("갤러그 협동"))
		{
			selectMode = 3;
			frame.startGame(3);
		}
		else if (cmd.equals("똥피하기"))
		{
			frame.startDodge();
		}
		if (cmd.equals("3D 테트리스"))
		{
			frame.start3D();
		}
		if (cmd.equals("조작법"))
		{
			String msg = "";
			msg = msg + "[테트리스]\n";
			// 테트리스와 전투기 키는 설정에서 바꿀 수 있으므로 지금 지정된 키를 보여 준다
			msg = msg + KeySettings.getKeyName(KeyAction.TETRIS_LEFT) + " / " + KeySettings.getKeyName(KeyAction.TETRIS_RIGHT) + " : 좌우 이동\n";
			msg = msg + KeySettings.getKeyName(KeyAction.TETRIS_ROTATE) + " : 회전\n";
			msg = msg + KeySettings.getKeyName(KeyAction.TETRIS_DOWN) + " : 한 칸 내리기\n";
			msg = msg + KeySettings.getKeyName(KeyAction.TETRIS_DROP) + " : 바로 떨어뜨리기\n";
			msg = msg + KeySettings.getKeyName(KeyAction.TETRIS_ITEM_SELF) + " : 아이템 내게 쓰기   "
					+ KeySettings.getKeyName(KeyAction.TETRIS_ITEM_ENEMY) + " : 아이템 적에게 쓰기   "
					+ KeySettings.getKeyName(KeyAction.TETRIS_ITEM_DELETE) + " : 아이템 지우기\n";
			msg = msg + "ESC : 설정 (모든 모드에서 게임을 멈추고 설정 화면 열기)\n\n";
			msg = msg + "[전투기] (갤러그 협동 모드)\n";
			msg = msg + KeySettings.getKeyName(KeyAction.GALAGA_LEFT) + " / " + KeySettings.getKeyName(KeyAction.GALAGA_RIGHT) + " : 좌우 이동\n";
			msg = msg + KeySettings.getKeyName(KeyAction.GALAGA_SHOOT) + " (누르고 있기) : 연사\n\n";
			msg = msg + "[똥피하기 모드]\n";
			msg = msg + "P1 테트리스 : A / D 좌우 이동, W 회전, S 내리기\n";
			msg = msg + "P2 피하는 사람 : ← / → 이동, ↑ 점프\n";
			msg = msg + "P : 일시정지   R : 끝난 뒤 다시 시작\n\n";
			msg = msg + "[3D 테트리스]\n";
			msg = msg + "W / A / S / D : 블록 이동\n";
			msg = msg + "J / K / L : X / Y / Z축 회전\n";
			msg = msg + "Space : 바로 떨어뜨리기   R : 다시 시작\n";
			msg = msg + "마우스 : 시점 조작";
			JOptionPane.showMessageDialog(this, msg, "조작법", JOptionPane.INFORMATION_MESSAGE);
		}
		else if (cmd.equals("설정"))
		{
			frame.openSettings();
		}
		else if (cmd.equals("종료"))
		{
			isOpen = false;
			System.exit(0);
		}
	}

	// 버튼 누를때 소리 나게 하기 (나중에)
//	public void playClickSound()
//	{
//	}
}
