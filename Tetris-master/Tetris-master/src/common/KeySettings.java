package common;

import java.awt.event.KeyEvent;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * 동작별로 지정된 키를 보관하고 저장하는 클래스.
 * Settings와 같은 저장소를 쓰므로 프로그램을 껐다 켜도 지정한 키가 유지된다.
 * 게임 화면은 키 코드를 직접 적지 않고 getKey로 현재 키를 물어서 사용한다.
 */
public class KeySettings
{
	/** 저장소 항목 이름 앞에 붙이는 말 */
	private static final String KEY_PREFIX = "key_";

	/** 설정을 저장하는 저장소 */
	private static Preferences store = Preferences.userRoot().node(Settings.NODE_NAME);

	/** 동작별 현재 키 코드. KeyAction.values()와 같은 순서이다. */
	private static int[] keys = loadKeys();

	/** 객체를 만들 필요가 없는 도구 클래스이므로 생성자를 막는다. */
	private KeySettings()
	{
	}

	/**
	 * 저장소에서 키를 읽어 온다. 저장된 값이 없는 동작은 기본 키를 쓴다.
	 *
	 * @return 동작별 키 코드
	 */
	private static int[] loadKeys()
	{
		KeyAction[] actions = KeyAction.values();
		int[] loaded = new int[actions.length];
		for (int i = 0; i < actions.length; i++)
			loaded[i] = store.getInt(KEY_PREFIX + actions[i].name(), actions[i].getDefaultKey());
		return loaded;
	}

	/**
	 * 동작에 지정된 현재 키를 알려 준다.
	 *
	 * @param action 알고 싶은 동작
	 * @return 키 코드(KeyEvent.VK_ 값)
	 */
	public static synchronized int getKey(KeyAction action)
	{
		return keys[action.ordinal()];
	}

	/**
	 * 동작에 지정된 현재 키의 이름을 알려 준다.
	 *
	 * @param action 알고 싶은 동작
	 * @return 화면에 보여 줄 키 이름(예: "Space", "A")
	 */
	public static String getKeyName(KeyAction action)
	{
		return KeyEvent.getKeyText(getKey(action));
	}

	/**
	 * 동작의 키를 바꾸고 저장한다. ESC는 설정 화면을 여는 키이고,
	 * 같은 화면의 다른 동작이 이미 쓰는 키는 겹치므로 바꾸지 않는다.
	 *
	 * @param action 키를 바꿀 동작
	 * @param keyCode 새 키 코드
	 * @return 바꿨으면 true, 쓸 수 없는 키라 바꾸지 않았으면 false
	 */
	public static synchronized boolean setKey(KeyAction action, int keyCode)
	{
		// 설정 화면을 여는 키는 다른 동작에 줄 수 없다
		if (keyCode == KeyEvent.VK_ESCAPE)
			return false;

		// 같은 화면의 다른 동작이 이미 쓰는 키인지 확인한다
		KeyAction[] actions = KeyAction.values();
		for (int i = 0; i < actions.length; i++)
		{
			if (actions[i] != action && actions[i].getScreen().equals(action.getScreen()) && keys[i] == keyCode)
				return false;
		}

		keys[action.ordinal()] = keyCode;
		store.putInt(KEY_PREFIX + action.name(), keyCode);
		flush();
		return true;
	}

	/**
	 * 모든 동작의 키를 기본 키로 되돌리고 저장한다.
	 */
	public static synchronized void resetToDefaults()
	{
		KeyAction[] actions = KeyAction.values();
		for (int i = 0; i < actions.length; i++)
		{
			keys[i] = actions[i].getDefaultKey();

			// 저장된 값을 지워서 다음 실행에서도 기본 키가 쓰이게 한다
			store.remove(KEY_PREFIX + actions[i].name());
		}
		flush();
	}

	/**
	 * 바꾼 값을 저장소에 바로 기록한다. 저장소를 쓸 수 없는 환경에서는 이번 실행 동안만 값이 유지된다.
	 */
	private static void flush()
	{
		try
		{
			store.flush();
		}
		catch (BackingStoreException e)
		{
			// 저장소에 쓰지 못해도 게임은 계속되어야 하므로 이번 실행의 값만 쓰고 넘어간다
		}
	}
}
