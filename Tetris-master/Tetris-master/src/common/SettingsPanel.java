package common;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * 환경 설정 화면. 배경음악/효과음 음량, 음소거, 키 설정, 기본값 복원을 조절한다.
 * 바꾼 값은 Settings와 KeySettings를 통해 바로 적용되고 저장된다.
 */
public class SettingsPanel extends JPanel
{
	/** 돌아가기 버튼을 눌렀을 때 실행할 동작. 이 화면을 닫는 일은 화면을 띄운 쪽이 맡는다. */
	private ActionListener backAction;

	/** 배경음악 음량 슬라이더 */
	private JSlider bgmSlider;

	/** 효과음 음량 슬라이더 */
	private JSlider sfxSlider;

	/** 배경음악 음량 숫자 표시 */
	private JLabel bgmValue;

	/** 효과음 음량 숫자 표시 */
	private JLabel sfxValue;

	/** 음소거 체크박스 */
	private JCheckBox muteBox;

	/** 화면 값을 코드로 바꾸는 중인지 여부. true이면 컨트롤 이벤트가 설정을 다시 저장하지 않는다. */
	private boolean updating = false;

	/**
	 * 설정 화면을 만든다.
	 *
	 * @param back 돌아가기 버튼을 눌렀을 때 실행할 동작
	 */
	public SettingsPanel(ActionListener back)
	{
		backAction = back;

		setLayout(null);
		setBackground(new Color(30, 160, 255));

		// 화면 제목
		JLabel title = new JLabel("설정", SwingConstants.CENTER);
		title.setFont(new Font("Dialog", Font.BOLD, 28));
		title.setForeground(Color.WHITE);
		title.setBounds(0, 40, 360, 50);
		add(title);

		// 배경음악 음량
		bgmValue = makeLabel("배경음악 음량", 120);
		bgmSlider = makeSlider(150);
		bgmSlider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				bgmValue.setText(String.valueOf(bgmSlider.getValue()));
				// 끌고 있는 동안에는 저장하지 않고, 손을 뗐을 때 한 번만 반영한다
				if (!updating && !bgmSlider.getValueIsAdjusting())
					Settings.setBgmVolume(bgmSlider.getValue());
			}
		});
		add(bgmSlider);

		// 효과음 음량
		sfxValue = makeLabel("효과음 음량", 200);
		sfxSlider = makeSlider(230);
		sfxSlider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				sfxValue.setText(String.valueOf(sfxSlider.getValue()));
				if (!updating && !sfxSlider.getValueIsAdjusting())
				{
					Settings.setSfxVolume(sfxSlider.getValue());
					// 바뀐 음량을 바로 들어 볼 수 있게 효과음을 한 번 낸다
					SoundManager.play(Sound.CLICK);
				}
			}
		});
		add(sfxSlider);

		// 음소거
		muteBox = new JCheckBox("음소거");
		muteBox.setFont(new Font("Dialog", Font.BOLD, 16));
		muteBox.setForeground(Color.WHITE);
		muteBox.setOpaque(false);
		muteBox.setFocusPainted(false);
		muteBox.setBounds(40, 290, 280, 30);
		muteBox.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (!updating)
					Settings.setMuted(muteBox.isSelected());
			}
		});
		add(muteBox);

		// 키 설정 창 열기
		JButton keyButton = makeButton("키 설정", 340);
		keyButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				SoundManager.play(Sound.CLICK);
				KeySettingsDialog.show(SwingUtilities.getWindowAncestor(SettingsPanel.this));
			}
		});
		add(keyButton);

		// 기본값으로 되돌리기
		JButton resetButton = makeButton("기본값으로 되돌리기", 394);
		resetButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				Settings.resetToDefaults();
				KeySettings.resetToDefaults();
				showCurrentSettings();
				SoundManager.play(Sound.CLICK);
			}
		});
		add(resetButton);

		// 메인화면으로 돌아가기
		JButton backButton = makeButton("돌아가기", 448);
		backButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				SoundManager.play(Sound.CLICK);
				backAction.actionPerformed(e);
			}
		});
		add(backButton);

		showCurrentSettings();
	}

	/**
	 * 게임 도중에 설정 화면을 팝업 창으로 띄운다. 창을 닫을 때까지 이 메서드는 돌아오지 않고,
	 * 그동안 뒤의 게임 창은 조작할 수 없다. 돌아가기 버튼, ESC, 창 닫기 중 무엇으로든 닫을 수 있다.
	 *
	 * @param owner 팝업 창의 주인이 될 게임 창
	 */
	public static void showDialog(Window owner)
	{
		final JDialog dialog = new JDialog(owner, "설정", JDialog.ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setResizable(false);

		// 돌아가기 버튼을 누르면 팝업 창을 닫는다
		SettingsPanel panel = new SettingsPanel(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dialog.dispose();
			}
		});
		panel.setPreferredSize(new Dimension(360, 520));
		dialog.add(panel);

		// ESC를 다시 누르면 게임으로 돌아간다
		dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke("ESCAPE"), "closeSettings");
		dialog.getRootPane().getActionMap().put("closeSettings", new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				dialog.dispose();
			}
		});

		dialog.pack();
		dialog.setLocationRelativeTo(owner);
		dialog.setVisible(true);
	}

	/**
	 * 저장된 설정 값을 슬라이더와 체크박스에 보여 준다.
	 */
	private void showCurrentSettings()
	{
		// 값을 채우는 동안 발생하는 이벤트가 설정을 다시 저장하지 않게 막는다
		updating = true;
		bgmSlider.setValue(Settings.getBgmVolume());
		sfxSlider.setValue(Settings.getSfxVolume());
		bgmValue.setText(String.valueOf(Settings.getBgmVolume()));
		sfxValue.setText(String.valueOf(Settings.getSfxVolume()));
		muteBox.setSelected(Settings.isMuted());
		updating = false;
	}

	/**
	 * 항목 이름과 그 오른쪽 숫자 표시를 만들어 화면에 붙인다.
	 *
	 * @param name 항목 이름
	 * @param y 항목 이름을 놓을 세로 위치
	 * @return 오른쪽에 놓인 숫자 표시 라벨
	 */
	private JLabel makeLabel(String name, int y)
	{
		JLabel nameLabel = new JLabel(name);
		nameLabel.setFont(new Font("Dialog", Font.BOLD, 16));
		nameLabel.setForeground(Color.WHITE);
		nameLabel.setBounds(40, y, 200, 24);
		add(nameLabel);

		JLabel valueLabel = new JLabel("", SwingConstants.RIGHT);
		valueLabel.setFont(new Font("Dialog", Font.BOLD, 16));
		valueLabel.setForeground(Color.WHITE);
		valueLabel.setBounds(240, y, 80, 24);
		add(valueLabel);
		return valueLabel;
	}

	/**
	 * 0 ~ 100 범위의 음량 슬라이더를 만든다.
	 *
	 * @param y 슬라이더를 놓을 세로 위치
	 * @return 만들어진 슬라이더
	 */
	private JSlider makeSlider(int y)
	{
		JSlider slider = new JSlider(0, 100, 0);
		slider.setOpaque(false);
		slider.setBounds(40, y, 280, 30);
		return slider;
	}

	/**
	 * 설정 화면에서 쓰는 모양의 버튼을 만든다.
	 *
	 * @param text 버튼 글자
	 * @param y 버튼을 놓을 세로 위치
	 * @return 만들어진 버튼
	 */
	private JButton makeButton(String text, int y)
	{
		JButton button = new JButton(text);
		button.setFont(new Font("Dialog", Font.BOLD, 16));
		button.setBackground(Color.WHITE);
		button.setFocusPainted(false);
		button.setBounds(70, y, 220, 44);
		return button;
	}
}
