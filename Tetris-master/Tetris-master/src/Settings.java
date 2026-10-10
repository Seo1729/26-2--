import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * 환경 설정(효과음 음량, 배경음악 음량, 음소거)을 보관하고 저장하는 클래스.
 * 값은 java.util.Preferences에 저장되므로 프로그램을 껐다 켜도 유지되고,
 * 프로젝트 폴더에는 설정 파일이 생기지 않는다(Windows에서는 사용자 레지스트리에 저장된다).
 * 값을 바꾸면 SoundManager에도 바로 반영된다.
 */
public class Settings
{
	/** 효과음 음량의 기본값(0 ~ 100) */
	public static final int DEFAULT_SFX_VOLUME = 70;

	/** 배경음악 음량의 기본값(0 ~ 100) */
	public static final int DEFAULT_BGM_VOLUME = 40;

	/** 저장소에서 이 프로젝트의 설정을 모아 두는 이름 */
	private static final String NODE_NAME = "jbnu_tetris";

	/** 저장소 항목 이름: 효과음 음량 */
	private static final String KEY_SFX_VOLUME = "sfxVolume";

	/** 저장소 항목 이름: 배경음악 음량 */
	private static final String KEY_BGM_VOLUME = "bgmVolume";

	/** 저장소 항목 이름: 음소거 */
	private static final String KEY_MUTED = "muted";

	/** 설정을 저장하는 저장소 */
	private static Preferences store = Preferences.userRoot().node(NODE_NAME);

	/** 효과음 음량(0 ~ 100) */
	private static int sfxVolume = store.getInt(KEY_SFX_VOLUME, DEFAULT_SFX_VOLUME);

	/** 배경음악 음량(0 ~ 100) */
	private static int bgmVolume = store.getInt(KEY_BGM_VOLUME, DEFAULT_BGM_VOLUME);

	/** 음소거 여부 */
	private static boolean muted = store.getBoolean(KEY_MUTED, false);

	/** 객체를 만들 필요가 없는 도구 클래스이므로 생성자를 막는다. */
	private Settings()
	{
	}

	/**
	 * 저장된 설정을 SoundManager에 적용한다. 프로그램을 시작할 때 한 번 부른다.
	 */
	public static synchronized void load()
	{
		applyToSound();
	}

	/**
	 * 효과음 음량을 바꾸고 저장한다.
	 *
	 * @param percent 새 음량. 0(무음) ~ 100(최대) 밖의 값은 범위 안으로 잘라 낸다.
	 */
	public static synchronized void setSfxVolume(int percent)
	{
		sfxVolume = clampPercent(percent);
		store.putInt(KEY_SFX_VOLUME, sfxVolume);
		applyToSound();
		flush();
	}

	/**
	 * 배경음악 음량을 바꾸고 저장한다.
	 *
	 * @param percent 새 음량. 0(무음) ~ 100(최대) 밖의 값은 범위 안으로 잘라 낸다.
	 */
	public static synchronized void setBgmVolume(int percent)
	{
		bgmVolume = clampPercent(percent);
		store.putInt(KEY_BGM_VOLUME, bgmVolume);
		applyToSound();
		flush();
	}

	/**
	 * 음소거를 켜거나 끄고 저장한다.
	 *
	 * @param mute true이면 음소거, false이면 해제
	 */
	public static synchronized void setMuted(boolean mute)
	{
		muted = mute;
		store.putBoolean(KEY_MUTED, muted);
		applyToSound();
		flush();
	}

	/**
	 * 모든 설정을 기본값으로 되돌리고 저장한다.
	 */
	public static synchronized void resetToDefaults()
	{
		sfxVolume = DEFAULT_SFX_VOLUME;
		bgmVolume = DEFAULT_BGM_VOLUME;
		muted = false;

		// 저장된 값을 지워서 다음 실행에서도 기본값이 쓰이게 한다
		store.remove(KEY_SFX_VOLUME);
		store.remove(KEY_BGM_VOLUME);
		store.remove(KEY_MUTED);

		applyToSound();
		flush();
	}

	/**
	 * 현재 효과음 음량을 알려 준다.
	 *
	 * @return 효과음 음량(0 ~ 100)
	 */
	public static synchronized int getSfxVolume()
	{
		return sfxVolume;
	}

	/**
	 * 현재 배경음악 음량을 알려 준다.
	 *
	 * @return 배경음악 음량(0 ~ 100)
	 */
	public static synchronized int getBgmVolume()
	{
		return bgmVolume;
	}

	/**
	 * 지금 음소거인지 알려 준다.
	 *
	 * @return 음소거면 true
	 */
	public static synchronized boolean isMuted()
	{
		return muted;
	}

	/**
	 * 음량 값을 0 ~ 100 범위로 잘라 낸다.
	 *
	 * @param percent 잘라 낼 음량
	 * @return 범위 안으로 맞춘 음량
	 */
	private static int clampPercent(int percent)
	{
		return Math.max(0, Math.min(100, percent));
	}

	/**
	 * 지금 설정 값을 SoundManager에 넘긴다.
	 */
	private static void applyToSound()
	{
		SoundManager.setSfxVolume(sfxVolume / 100f);
		SoundManager.setBgmVolume(bgmVolume / 100f);
		SoundManager.setMuted(muted);
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
