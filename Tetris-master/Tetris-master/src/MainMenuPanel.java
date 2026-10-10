import java.awt.*;
import java.awt.event.*;

import common.Bgm;
import common.Sound;
import common.SoundManager;

import javax.swing.*;

// 메인화면
public class MainMenuPanel extends JPanel implements ActionListener
{
	GameFrame frame;
	JLabel logo;
	JButton btn1, btn2, btn3, btn4, btn5, btn6;
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
		btn1.setBounds(90, 260, 180, 44);
		btn1.addActionListener(this);
		add(btn1);

		btn2 = new JButton("갤러그 협동");
		btn2.setFont(new Font("Dialog", Font.BOLD, 16));
		btn2.setBackground(Color.WHITE);
		btn2.setFocusPainted(false);
		btn2.setBounds(90, 312, 180, 44);
		btn2.addActionListener(this);
		add(btn2);

		btn3 = new JButton("똥피하기");
		btn3.setFont(new Font("Dialog", Font.BOLD, 16));
		btn3.setBackground(Color.WHITE);
		btn3.setFocusPainted(false);
		btn3.setBounds(90, 364, 180, 44);
		btn3.addActionListener(this);
		add(btn3);

		btn4 = new JButton("3D 테트리스");
		btn4.setFont(new Font("Dialog", Font.BOLD, 16));
		btn4.setBackground(Color.WHITE);
		btn4.setFocusPainted(false);
		btn4.setBounds(90, 416, 180, 44);
		btn4.addActionListener(this);
		add(btn4);

		btn5 = new JButton("조작법");
		btn5.setFont(new Font("Dialog", Font.BOLD, 16));
		btn5.setBackground(Color.WHITE);
		btn5.setFocusPainted(false);
		btn5.setBounds(90, 468, 180, 44);
		btn5.addActionListener(this);
		add(btn5);

		btn6 = new JButton("종료");
		btn6.setFont(new Font("Dialog", Font.BOLD, 16));
		btn6.setBackground(Color.WHITE);
		btn6.setFocusPainted(false);
		btn6.setBounds(90, 520, 180, 44);
		btn6.addActionListener(this);
		add(btn6);

//		btn7 = new JButton("설정");
//		btn7.setBounds(90, 572, 180, 44);
//		add(btn7);
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
			msg = msg + "← / → : 좌우 이동\n";
			msg = msg + "↑ : 회전\n";
			msg = msg + "↓ : 한 칸 내리기\n";
			msg = msg + "Space : 바로 떨어뜨리기\n";
			msg = msg + "1 : 아이템 내게 쓰기   2 : 아이템 적에게 쓰기   3 : 아이템 지우기\n\n";
			msg = msg + "[전투기] (갤러그 협동 모드)\n";
			msg = msg + "A / D : 좌우 이동\n";
			msg = msg + "W (누르고 있기) : 연사\n\n";
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
