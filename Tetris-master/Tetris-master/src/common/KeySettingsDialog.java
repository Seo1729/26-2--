package common;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.KeyboardFocusManager;
import java.awt.KeyEventDispatcher;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

/**
 * 키 설정 창. 모드별 탭에 동작과 현재 키가 나열되고,
 * 키 버튼을 누른 뒤 새 키를 누르면 그 동작의 키가 바뀌고 저장된다.
 */
public class KeySettingsDialog
{
	/** 키를 기다리는 동안 화면에 보여 줄 안내 글자 */
	private static final String WAITING_TEXT = "키를 누르세요";

	/** 창 본체 */
	private JDialog dialog;

	/** 동작별 키 버튼. KeyAction.values()와 같은 순서이다. */
	private JButton[] keyButtons = new JButton[KeyAction.values().length];

	/** 결과 안내 글자(키를 바꿨는지, 못 바꿨는지) */
	private JLabel message;

	/** 지금 새 키를 기다리는 동작. 기다리는 중이 아니면 null이다. */
	private KeyAction waiting = null;

	/** 새 키를 기다리는 동안만 등록해 두는 키 입력 가로채기 */
	private KeyEventDispatcher dispatcher = null;

	/**
	 * 키 설정 창을 만든다. 화면에 보이는 것은 show가 맡는다.
	 *
	 * @param owner 이 창의 주인이 될 창
	 */
	private KeySettingsDialog(Window owner)
	{
		dialog = new JDialog(owner, "키 설정", JDialog.ModalityType.APPLICATION_MODAL);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setResizable(false);

		// 창이 닫히면 키 기다리기도 끝낸다(안 그러면 키 입력을 계속 가로챈다)
		dialog.addWindowListener(new WindowAdapter()
		{
			public void windowClosed(WindowEvent e)
			{
				stopWaiting();
			}
		});

		dialog.add(makeTabs(), BorderLayout.CENTER);
		dialog.add(makeBottom(), BorderLayout.SOUTH);
		dialog.getContentPane().setPreferredSize(new Dimension(380, 520));
		dialog.pack();
		dialog.setLocationRelativeTo(owner);
	}

	/**
	 * 키 설정 창을 띄운다. 창을 닫을 때까지 이 메서드는 돌아오지 않는다.
	 *
	 * @param owner 이 창의 주인이 될 창
	 */
	public static void show(Window owner)
	{
		new KeySettingsDialog(owner).dialog.setVisible(true);
	}

	/**
	 * 모드별 탭을 만든다. 동작 목록에서 모드 이름이 처음 나온 순서대로 탭이 놓인다.
	 *
	 * @return 탭 묶음
	 */
	private JTabbedPane makeTabs()
	{
		JTabbedPane tabs = new JTabbedPane();
		tabs.setFocusable(false);

		// 모드 이름을 중복 없이 모은다
		ArrayList<String> groups = new ArrayList<String>();
		for (KeyAction action : KeyAction.values())
		{
			if (!groups.contains(action.getGroup()))
				groups.add(action.getGroup());
		}

		// 모드마다 탭을 하나씩 만들고 그 모드의 동작을 한 줄씩 채운다
		for (String group : groups)
		{
			JPanel rows = new JPanel(new GridLayout(0, 2, 10, 10));
			rows.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
			for (KeyAction action : KeyAction.values())
			{
				if (action.getGroup().equals(group))
					addRow(rows, action);
			}

			// 줄 높이가 늘어나지 않게 위쪽에 붙여 놓는다
			JPanel tab = new JPanel(new BorderLayout());
			tab.add(rows, BorderLayout.NORTH);
			tabs.addTab(group, tab);
		}
		return tabs;
	}

	/**
	 * 동작 이름과 키 버튼 한 줄을 만든다.
	 *
	 * @param rows 줄을 붙일 판
	 * @param action 이 줄이 맡는 동작
	 */
	private void addRow(JPanel rows, final KeyAction action)
	{
		JLabel name = new JLabel(action.getLabel());
		name.setFont(new Font("Dialog", Font.BOLD, 14));
		rows.add(name);

		JButton button = new JButton(KeySettings.getKeyName(action));
		button.setFocusable(false);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				startWaiting(action);
			}
		});
		keyButtons[action.ordinal()] = button;
		rows.add(button);
	}

	/**
	 * 창 아래쪽(안내 글자, 기본값 버튼, 닫기 버튼)을 만든다.
	 *
	 * @return 아래쪽 판
	 */
	private JPanel makeBottom()
	{
		message = new JLabel("바꿀 동작의 키 버튼을 누른 뒤 새 키를 누르세요. (ESC: 취소)", SwingConstants.CENTER);

		JButton resetButton = new JButton("기본값으로");
		resetButton.setFocusable(false);
		resetButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				stopWaiting();
				KeySettings.resetToDefaults();
				showAllKeys();
				message.setText("모든 키를 기본값으로 되돌렸습니다.");
			}
		});

		JButton closeButton = new JButton("닫기");
		closeButton.setFocusable(false);
		closeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dialog.dispose();
			}
		});

		JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
		buttons.add(resetButton);
		buttons.add(closeButton);

		JPanel bottom = new JPanel(new BorderLayout());
		bottom.add(message, BorderLayout.NORTH);
		bottom.add(buttons, BorderLayout.CENTER);
		return bottom;
	}

	/**
	 * 모든 키 버튼의 글자를 지금 지정된 키 이름으로 고친다.
	 */
	private void showAllKeys()
	{
		for (KeyAction action : KeyAction.values())
			keyButtons[action.ordinal()].setText(KeySettings.getKeyName(action));
	}

	/**
	 * 동작의 새 키를 기다리기 시작한다. 다음에 누르는 키 하나가 그 동작의 키가 된다.
	 *
	 * @param action 키를 바꿀 동작
	 */
	private void startWaiting(KeyAction action)
	{
		// 다른 동작을 기다리던 중이었다면 먼저 끝낸다
		stopWaiting();

		waiting = action;
		keyButtons[action.ordinal()].setText(WAITING_TEXT);
		message.setText(action.getLabel() + ": 새 키를 누르세요. (ESC: 취소)");

		// 기다리는 동안 눌리는 키는 모두 여기서 받고 다른 곳으로는 보내지 않는다
		dispatcher = new KeyEventDispatcher()
		{
			public boolean dispatchKeyEvent(KeyEvent e)
			{
				if (e.getID() == KeyEvent.KEY_PRESSED)
					receiveKey(e.getKeyCode());
				return true;
			}
		};
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
	}

	/**
	 * 기다리던 동작에 누른 키를 지정한다.
	 *
	 * @param keyCode 사용자가 누른 키 코드
	 */
	private void receiveKey(int keyCode)
	{
		KeyAction action = waiting;
		stopWaiting();

		// ESC는 키를 바꾸지 않고 취소하는 용도로 쓴다
		if (keyCode == KeyEvent.VK_ESCAPE)
		{
			message.setText("취소했습니다.");
			return;
		}

		if (KeySettings.setKey(action, keyCode))
			message.setText(action.getLabel() + " 키를 " + KeyEvent.getKeyText(keyCode) + "(으)로 바꿨습니다.");
		else
			message.setText(KeyEvent.getKeyText(keyCode) + " 키는 같은 화면에서 이미 쓰고 있어 바꿀 수 없습니다.");
	}

	/**
	 * 새 키 기다리기를 끝내고 키 버튼 글자를 원래대로 되돌린다. 기다리는 중이 아니면 아무것도 하지 않는다.
	 */
	private void stopWaiting()
	{
		if (waiting == null)
			return;

		KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
		dispatcher = null;
		waiting = null;
		showAllKeys();
	}
}
