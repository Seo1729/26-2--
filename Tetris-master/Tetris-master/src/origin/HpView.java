package origin;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * 일반 모드 HP 표시 칸. 하트(♥ 남은 HP, ♡ 잃은 HP)를 보여 주고, HP가 줄면 칸을 빨갛게 깜빡인다.
 * 규칙은 모르고 HpSystem.Listener로 값만 받는다. 위치와 테두리는 붙이는 쪽(GamePanel)이 정한다.
 */
class HpView extends JLabel implements HpSystem.Listener
{
	private static final Color HEART = new Color(255, 80, 90);
	private static final Color FLASH = new Color(160, 20, 30);
	private static final Color BACKGROUND = Color.BLACK;
	private static final String FULL = "♥";
	private static final String EMPTY = "♡";
	/** 깜빡임 한 번(켜짐 또는 꺼짐)의 시간(ms) */
	private static final int FLASH_STEP_MS = 120;
	/** 켜짐·꺼짐을 합쳐 바꾸는 횟수 (6이면 세 번 깜빡임) */
	private static final int FLASH_STEPS = 6;

	/** hp를 보여 주는 칸을 만든다. HP를 안 쓰는 모드면 숨겨 둔다. */
	HpView(HpSystem hp)
	{
		super("", SwingConstants.CENTER);
		setFont(new Font("Dialog", Font.BOLD, 20));
		setForeground(HEART);
		setBackground(BACKGROUND);
		setOpaque(true);
		setVisible(hp.isEnabled());
		hp.setListener(this);
	}

	/** 게임 스레드에서 불려도 되도록 화면 변경은 화면 스레드로 넘긴다 */
	@Override
	public void hpChanged(int hp, int maxHp, boolean lost)
	{
		String text = hearts(hp, maxHp);
		SwingUtilities.invokeLater(() ->
		{
			setText(text);
			if (lost)
				flash();
		});
	}

	static String hearts(int hp, int maxHp)
	{
		StringBuilder hearts = new StringBuilder();
		for (int i = 0; i < maxHp; i++)
			hearts.append(i < hp ? FULL : EMPTY);
		return hearts.toString();
	}

	// 화면 스레드에서만 부른다
	private void flash()
	{
		int[] count = { 0 };
		Timer timer = new Timer(FLASH_STEP_MS, null);
		timer.addActionListener(e ->
		{
			setBackground(count[0] % 2 == 0 ? FLASH : BACKGROUND);
			if (++count[0] >= FLASH_STEPS)
			{
				setBackground(BACKGROUND);
				timer.stop();
			}
		});
		timer.start();
	}
}
