import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

// 메인화면
public class MainMenuPanel extends JPanel implements ActionListener
{
	GameFrame frame;
	JLabel logo;
	JButton btn1, btn2, btn3, btn4, btn5, btn6, volumeButton;
	int selectMode = 0;
	boolean isOpen = true;

	public MainMenuPanel(GameFrame f)
	{
		frame = f;

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
		btn6.setBounds(90, 568, 180, 28);
		btn6.addActionListener(this);
		add(btn6);

		volumeButton = new JButton("환경 설정");
		volumeButton.setFont(new Font("Dialog", Font.BOLD, 16));
		volumeButton.setBackground(Color.WHITE);
		volumeButton.setFocusPainted(false);
		volumeButton.setBounds(90, 520, 180, 44);
		volumeButton.addActionListener(event -> settings.EnvironmentSettingsPanel.show(this));
		add(volumeButton);

//		btn7 = new JButton("설정");
//		btn7.setBounds(90, 572, 180, 44);
//		add(btn7);
	}

	public void actionPerformed(ActionEvent e)
	{
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
			String msg = "ESC : 환경 설정\n\n";
			for (settings.GameSettings.Game game : settings.GameSettings.Game.values())
				msg += settings.KeySettingsPanel.help(game) + "\n";
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
