import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * 일반 모드의 생명(HP).
 * 최대 HP는 3이다. 블록이 맨 위에 닿으면 HP가 1 줄고, 그때마다 맨 밑 4줄이 사라진다.
 * HP가 0이 되면 원래대로 게임 오버가 된다.
 *
 * GamePanel(GameFrame.java)에는 이 객체를 하나 만들고, addBlock()의 게임 오버 판정에서
 * hitTop()을 부르는 몇 줄만 넣는다. 나머지 규칙과 화면 표시는 모두 이 파일에 있다.
 */
class HpSystem
{
	static final int MAX_HP = 3;
	/** HP가 줄 때 맨 밑에서 지우는 줄 수 */
	static final int CLEAR_LINES = 4;
	/** HP를 쓰는 게임 모드 (1 = 일반 모드) */
	private static final int HP_MODE = 1;

	private static final Color HEART = new Color(255, 80, 90);
	private static final Color FLASH = new Color(160, 20, 30);

	private final JLabel label = new JLabel("", SwingConstants.CENTER);
	private int hp = MAX_HP;

	/** panel의 오른쪽 정보 칸(시간 아래)에 HP 표시를 붙인다. 일반 모드가 아니면 보이지 않는다. */
	HpSystem(GamePanel panel)
	{
		label.setFont(new Font("Dialog", Font.BOLD, 20));
		label.setForeground(HEART);
		label.setBackground(Color.BLACK);
		label.setOpaque(true);
		label.setBorder(panel.WhiteLineBorder);
		label.setBounds(255, 280, 90, 30);
		label.setVisible(isEnabled());
		panel.add(label);
		updateLabel();
	}

	/** 지금 게임 모드에서 HP를 쓰는지 */
	boolean isEnabled()
	{
		return GameFrame.gameMode == HP_MODE;
	}

	int getHp()
	{
		return hp;
	}

	/**
	 * 블록이 맨 위에 닿았을 때 부른다. HP를 1 줄이고 화면을 깜빡인다.
	 *
	 * @return 아직 HP가 남아서 계속할 수 있으면 true (부른 쪽이 맨 밑 4줄을 지운다), HP가 0이면 false
	 */
	boolean hitTop()
	{
		if (!isEnabled() || hp <= 0)
			return false;
		hp--;
		updateLabel();
		flash();
		return hp > 0;
	}

	private void updateLabel()
	{
		StringBuilder hearts = new StringBuilder();
		for (int i = 0; i < MAX_HP; i++)
			hearts.append(i < hp ? "♥" : "♡");
		String text = hearts.toString();
		SwingUtilities.invokeLater(() -> label.setText(text));
	}

	/** HP가 줄면 HP 칸을 빨갛게 세 번 깜빡인다 */
	private void flash()
	{
		SwingUtilities.invokeLater(() ->
		{
			int[] count = { 0 };
			Timer timer = new Timer(120, null);
			timer.addActionListener(e ->
			{
				label.setBackground(count[0] % 2 == 0 ? FLASH : Color.BLACK);
				if (++count[0] >= 6)
				{
					label.setBackground(Color.BLACK);
					timer.stop();
				}
			});
			timer.start();
		});
	}
}
