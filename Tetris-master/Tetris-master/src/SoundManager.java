import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 * 효과음과 배경음악을 코드로 합성해서 재생하는 클래스.
 * 사각파, 삼각파, 사인파, 잡음을 이어 붙여 소리를 만들기 때문에 음원 파일이 필요 없다.
 * 어느 모드에서든 SoundManager.play(Sound.MOVE)처럼 한 줄로 부를 수 있다.
 * 소리 장치가 없거나 열 수 없는 컴퓨터에서는 한 번 실패한 뒤로 조용히 소리 없이 동작한다.
 */
public class SoundManager
{
	/** 샘플링 주파수(초당 샘플 수). 낮은 값이라 소리가 8비트 게임기처럼 들린다. */
	private static final float SAMPLE_RATE = 22050f;

	/** 소리 형식: 16비트, 모노, 부호 있음, 리틀 엔디언 */
	private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

	/** 파형 종류: 사각파(거친 소리) */
	private static final int SQUARE = 0;

	/** 파형 종류: 폭이 좁은 사각파(코맹맹이 소리, 멜로디용) */
	private static final int PULSE = 1;

	/** 파형 종류: 삼각파(부드러운 소리, 베이스용) */
	private static final int TRIANGLE = 2;

	/** 파형 종류: 사인파(맑고 둥근 소리) */
	private static final int SINE = 3;

	/** 파형 종류: 잡음(폭발음용) */
	private static final int NOISE = 4;

	/** 게임 BGM에서 8분음표 하나의 길이(밀리초) */
	private static final int GAME_EIGHTH_MS = 200;

	/** 메뉴 BGM에서 아르페지오 한 음의 길이(밀리초) */
	private static final int MENU_STEP_MS = 260;

	/** 게임 BGM 멜로디의 음높이(MIDI 번호, 69 = 라4, 0 = 쉼표) */
	private static final int[] GAME_MELODY = {
		76, 71, 72, 74, 72, 71,
		69, 69, 72, 76, 74, 72,
		71, 72, 74, 76,
		72, 69, 69, 0,
		74, 77, 81, 79, 77,
		76, 72, 76, 74, 72,
		71, 71, 72, 74, 76,
		72, 69, 69, 0
	};

	/** 게임 BGM 멜로디 각 음의 길이(8분음표 개수). 모두 더하면 64가 된다. */
	private static final int[] GAME_MELODY_LENGTH = {
		2, 1, 1, 2, 1, 1,
		2, 1, 1, 2, 1, 1,
		3, 1, 2, 2,
		2, 2, 2, 2,
		3, 1, 2, 1, 1,
		3, 1, 2, 1, 1,
		2, 1, 1, 2, 2,
		2, 2, 2, 2
	};

	/** 게임 BGM의 마디(8분음표 8개)마다 깔리는 베이스의 으뜸음(MIDI 번호) */
	private static final int[] GAME_BASS_ROOT = { 45, 45, 40, 45, 50, 48, 40, 45 };

	/** 메뉴 BGM의 코드(화음) 4개의 으뜸음(MIDI 번호): 도, 라, 파, 솔 */
	private static final int[] MENU_ROOT = { 60, 57, 53, 55 };

	/** 메뉴 BGM의 코드 4개에서 으뜸음과 3음 사이의 반음 수(장3화음 4, 단3화음 3) */
	private static final int[] MENU_THIRD = { 4, 3, 4, 4 };

	/** 메뉴 BGM의 아르페지오 순서(0: 으뜸음, 1: 3음, 2: 5음, 3: 한 옥타브 위) */
	private static final int[] MENU_PATTERN = { 0, 1, 2, 3, 2, 1, 2, 1 };

	/** 효과음 종류마다 한 번만 만들어 두고 다시 쓰는 재생 장치 */
	private static final Clip[] sfxClips = new Clip[Sound.values().length];

	/** 지금 재생 중인 배경음악 장치. 재생 중이 아니면 null */
	private static Clip bgmClip;

	/** 틀기로 한 배경음악. 음소거로 멈췄다가 다시 이어서 틀 때 쓴다. 없으면 null */
	private static Bgm currentBgm;

	/** 효과음 음량(0.0 ~ 1.0) */
	private static float sfxVolume = 0.7f;

	/** 배경음악 음량(0.0 ~ 1.0) */
	private static float bgmVolume = 0.4f;

	/** 음소거 여부. true이면 효과음과 배경음악이 모두 나지 않는다. */
	private static boolean muted = false;

	/** 소리 장치를 열지 못한 적이 있는지. true이면 이후에는 소리를 시도하지 않는다. */
	private static boolean failed = false;

	/** 객체를 만들 필요가 없는 도구 클래스이므로 생성자를 막는다. */
	private SoundManager()
	{
	}

	// ======================== 재생 ========================

	/**
	 * 효과음을 한 번 재생한다. 같은 효과음이 아직 울리는 중이면 처음부터 다시 울린다.
	 * 음소거이거나 소리 장치를 쓸 수 없으면 아무것도 하지 않는다.
	 *
	 * @param sound 재생할 효과음
	 */
	public static synchronized void play(Sound sound)
	{
		// 소리를 낼 수 없거나 낼 필요가 없으면 바로 끝낸다
		if (failed || muted || sfxVolume <= 0)
		{
			return;
		}

		Clip clip = getSfxClip(sound);
		if (clip == null)
		{
			return;
		}

		// 음량을 맞추고 처음부터 재생한다
		applyVolume(clip, sfxVolume);
		clip.stop();
		clip.setFramePosition(0);
		clip.start();
	}

	/**
	 * 배경음악을 처음부터 반복 재생한다. 이미 같은 음악이 나오는 중이면 아무것도 하지 않고,
	 * 다른 음악이 나오는 중이면 그것을 멈추고 새로 튼다.
	 *
	 * @param track 재생할 배경음악
	 */
	public static synchronized void startBgm(Bgm track)
	{
		// 같은 음악이 이미 나오고 있으면 끊기지 않게 그대로 둔다
		if (track == currentBgm && bgmClip != null)
		{
			return;
		}

		stopBgm();
		currentBgm = track;

		// 음소거 중이면 소리는 내지 않고, 해제될 때 이어서 틀도록 기억만 해 둔다
		if (failed || muted)
		{
			return;
		}
		playCurrentBgm();
	}

	/**
	 * 배경음악을 멈추고 다시 이어서 틀지 않도록 잊는다.
	 */
	public static synchronized void stopBgm()
	{
		currentBgm = null;
		closeBgmClip();
	}

	// ======================== 설정 ========================

	/**
	 * 효과음 음량을 바꾼다.
	 *
	 * @param volume 새 음량. 0.0(무음) ~ 1.0(최대) 밖의 값은 범위 안으로 잘라 낸다.
	 */
	public static synchronized void setSfxVolume(float volume)
	{
		sfxVolume = clampVolume(volume);
	}

	/**
	 * 배경음악 음량을 바꾼다. 재생 중이면 바로 반영된다.
	 *
	 * @param volume 새 음량. 0.0(무음) ~ 1.0(최대) 밖의 값은 범위 안으로 잘라 낸다.
	 */
	public static synchronized void setBgmVolume(float volume)
	{
		bgmVolume = clampVolume(volume);
		if (bgmClip != null)
		{
			applyVolume(bgmClip, bgmVolume);
		}
	}

	/**
	 * 음소거를 켜거나 끈다. 켜면 배경음악이 멈추고, 끄면 틀기로 했던 배경음악이 처음부터 다시 나온다.
	 *
	 * @param mute true이면 음소거, false이면 해제
	 */
	public static synchronized void setMuted(boolean mute)
	{
		muted = mute;

		if (muted)
		{
			// 배경음악 장치만 닫고, 무슨 음악이었는지는 기억해 둔다
			closeBgmClip();
		}
		else if (currentBgm != null && bgmClip == null && !failed)
		{
			playCurrentBgm();
		}
	}

	// ======================== 내부 동작 ========================

	/**
	 * 음량 값을 0.0 ~ 1.0 범위로 잘라 낸다.
	 *
	 * @param volume 잘라 낼 음량
	 * @return 범위 안으로 맞춘 음량
	 */
	private static float clampVolume(float volume)
	{
		return Math.max(0f, Math.min(1f, volume));
	}

	/**
	 * 효과음 재생 장치를 돌려준다. 처음 쓰는 효과음이면 소리를 합성해서 장치를 만들어 둔다.
	 *
	 * @param sound 재생할 효과음
	 * @return 재생 장치. 소리 장치를 열지 못하면 null
	 */
	private static Clip getSfxClip(Sound sound)
	{
		int index = sound.ordinal();

		// 이미 만들어 둔 장치가 있으면 그대로 쓴다
		if (sfxClips[index] == null)
		{
			sfxClips[index] = openClip(makeSfx(sound));
		}
		return sfxClips[index];
	}

	/**
	 * 지금 기억하고 있는 배경음악을 합성해서 반복 재생한다.
	 */
	private static void playCurrentBgm()
	{
		bgmClip = openClip(makeBgm(currentBgm));
		if (bgmClip == null)
		{
			return;
		}

		applyVolume(bgmClip, bgmVolume);
		bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
	}

	/**
	 * 배경음악 장치를 멈추고 닫는다. 재생 중이 아니면 아무것도 하지 않는다.
	 */
	private static void closeBgmClip()
	{
		if (bgmClip != null)
		{
			bgmClip.stop();
			bgmClip.close();
			bgmClip = null;
		}
	}

	/**
	 * 합성한 소리 데이터로 재생 장치를 연다.
	 *
	 * @param data 16비트 모노 소리 데이터
	 * @return 열린 재생 장치. 소리 장치가 없거나 열 수 없으면 null
	 */
	private static Clip openClip(byte[] data)
	{
		try
		{
			Clip clip = AudioSystem.getClip();
			clip.open(FORMAT, data, 0, data.length);
			return clip;
		}
		catch (Exception e)
		{
			// 소리 장치가 없는 환경(서버, 소리 장치 사용 중 등)에서도 게임은 계속되어야 하므로
			// 실패를 기억해 두고 이후에는 시도하지 않는다
			failed = true;
			return null;
		}
	}

	/**
	 * 재생 장치의 음량을 맞춘다. 음량 조절을 지원하지 않는 장치면 아무것도 하지 않는다.
	 *
	 * @param clip   음량을 맞출 재생 장치
	 * @param volume 0.0 ~ 1.0 사이의 음량
	 */
	private static void applyVolume(Clip clip, float volume)
	{
		if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
		{
			return;
		}

		// 사람이 느끼는 크기에 맞게 음량을 데시벨로 바꾸고, 장치가 허용하는 범위로 자른다
		FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
		float decibel = (float) (20.0 * Math.log10(Math.max(volume, 0.0001f)));
		gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), decibel)));
	}

	// ======================== 소리 합성 ========================

	/**
	 * 효과음 하나를 합성한다.
	 *
	 * @param sound 만들 효과음
	 * @return 16비트 모노 소리 데이터
	 */
	static byte[] makeSfx(Sound sound)
	{
		Wave wave;

		switch (sound)
		{
		case CLICK:
			wave = new Wave(60);
			wave.tone(0, 30, 700, 700, SQUARE, 0.3f, 0.5f);
			break;
		case MOVE:
			wave = new Wave(50);
			wave.tone(0, 30, 300, 260, PULSE, 0.25f, 0.6f);
			break;
		case ROTATE:
			// 낮은 음에서 높은 음으로 두 번 짧게 울린다
			wave = new Wave(100);
			wave.tone(0, 45, 520, 520, SQUARE, 0.25f, 0.3f);
			wave.tone(45, 45, 780, 780, SQUARE, 0.25f, 0.3f);
			break;
		case LOCK:
			// 낮은 음이 급히 내려가는 쿵 소리에 짧은 잡음을 섞는다
			wave = new Wave(150);
			wave.tone(0, 110, 150, 60, SINE, 0.35f, 1.0f);
			wave.tone(0, 30, 0, 0, NOISE, 0.12f, 1.0f);
			break;
		case LINE_CLEAR:
			// 도, 미, 솔, 높은 도로 올라가는 네 음
			wave = new Wave(350);
			wave.tone(0, 80, 523, 523, PULSE, 0.3f, 0.4f);
			wave.tone(80, 80, 659, 659, PULSE, 0.3f, 0.4f);
			wave.tone(160, 80, 784, 784, PULSE, 0.3f, 0.4f);
			wave.tone(240, 110, 1047, 1047, PULSE, 0.3f, 0.6f);
			break;
		case ITEM:
			wave = new Wave(160);
			wave.tone(0, 150, 700, 1500, SQUARE, 0.25f, 0.3f);
			break;
		case SHOOT:
			// 높은 음이 빠르게 내려가는 레이저 소리
			wave = new Wave(110);
			wave.tone(0, 100, 1400, 300, PULSE, 0.25f, 0.8f);
			break;
		case EXPLODE:
			wave = new Wave(230);
			wave.tone(0, 220, 0, 0, NOISE, 0.25f, 1.0f);
			wave.tone(0, 220, 200, 60, SINE, 0.25f, 1.0f);
			break;
		case GAME_OVER:
			// 솔, 미, 도, 낮은 솔로 내려가는 네 음
			wave = new Wave(950);
			wave.tone(0, 190, 392, 392, SQUARE, 0.25f, 0.3f);
			wave.tone(200, 190, 330, 330, SQUARE, 0.25f, 0.3f);
			wave.tone(400, 190, 262, 262, SQUARE, 0.25f, 0.3f);
			wave.tone(600, 340, 196, 196, SQUARE, 0.25f, 1.0f);
			break;
		default:
			// WIN: 도 도 도 미 솔로 이어지는 팡파르
			wave = new Wave(820);
			wave.tone(0, 120, 523, 523, PULSE, 0.3f, 0.2f);
			wave.tone(130, 120, 523, 523, PULSE, 0.3f, 0.2f);
			wave.tone(260, 120, 523, 523, PULSE, 0.3f, 0.2f);
			wave.tone(390, 150, 659, 659, PULSE, 0.3f, 0.2f);
			wave.tone(550, 260, 784, 784, PULSE, 0.3f, 0.6f);
			break;
		}
		return wave.toBytes();
	}

	/**
	 * 배경음악 하나를 합성한다.
	 *
	 * @param track 만들 배경음악
	 * @return 16비트 모노 소리 데이터. 처음과 끝이 이어지도록 한 바퀴 길이로 만든다.
	 */
	static byte[] makeBgm(Bgm track)
	{
		if (track == Bgm.MENU)
		{
			return makeMenuBgm();
		}
		return makeGameBgm();
	}

	/**
	 * 게임 BGM을 합성한다. 멜로디(폭이 좁은 사각파)와 베이스(삼각파) 두 파트로 이루어진다.
	 *
	 * @return 16비트 모노 소리 데이터(약 12.8초)
	 */
	private static byte[] makeGameBgm()
	{
		// 멜로디 길이를 모두 더해 전체 길이를 정한다
		int totalEighths = 0;
		for (int i = 0; i < GAME_MELODY_LENGTH.length; i++)
		{
			totalEighths = totalEighths + GAME_MELODY_LENGTH[i];
		}
		Wave wave = new Wave(totalEighths * GAME_EIGHTH_MS);

		// 멜로디: 음마다 정해진 길이만큼 울리고, 0은 쉼표로 건너뛴다
		int time = 0;
		for (int i = 0; i < GAME_MELODY.length; i++)
		{
			int length = GAME_MELODY_LENGTH[i] * GAME_EIGHTH_MS;
			if (GAME_MELODY[i] > 0)
			{
				double hz = midiToHz(GAME_MELODY[i]);
				wave.tone(time, length, hz, hz, PULSE, 0.2f, 0.3f);
			}
			time = time + length;
		}

		// 베이스: 마디마다 으뜸음과 5도 위 음을 번갈아 4분음표로 깐다
		for (int measure = 0; measure < GAME_BASS_ROOT.length; measure++)
		{
			for (int beat = 0; beat < 4; beat++)
			{
				int note = GAME_BASS_ROOT[measure];
				if (beat % 2 == 1)
				{
					note = note + 7;
				}
				int start = (measure * 8 + beat * 2) * GAME_EIGHTH_MS;
				double hz = midiToHz(note);
				wave.tone(start, 2 * GAME_EIGHTH_MS, hz, hz, TRIANGLE, 0.25f, 0.5f);
			}
		}
		return wave.toBytes();
	}

	/**
	 * 메뉴 BGM을 합성한다. 코드 4개를 차례로 아르페지오(화음을 한 음씩)로 연주한다.
	 *
	 * @return 16비트 모노 소리 데이터(약 8.3초)
	 */
	private static byte[] makeMenuBgm()
	{
		int stepsPerChord = MENU_PATTERN.length;
		Wave wave = new Wave(MENU_ROOT.length * stepsPerChord * MENU_STEP_MS);

		for (int chord = 0; chord < MENU_ROOT.length; chord++)
		{
			// 으뜸음, 3음, 5음, 한 옥타브 위의 반음 간격
			int[] intervals = { 0, MENU_THIRD[chord], 7, 12 };

			// 코드 하나 동안 아르페지오를 연주한다
			for (int step = 0; step < stepsPerChord; step++)
			{
				int note = MENU_ROOT[chord] + intervals[MENU_PATTERN[step]];
				int start = (chord * stepsPerChord + step) * MENU_STEP_MS;
				double hz = midiToHz(note);
				wave.tone(start, MENU_STEP_MS, hz, hz, TRIANGLE, 0.3f, 0.6f);
			}

			// 코드 하나 길이 동안 한 옥타브 아래의 으뜸음을 길게 깐다
			double bassHz = midiToHz(MENU_ROOT[chord] - 12);
			wave.tone(chord * stepsPerChord * MENU_STEP_MS, stepsPerChord * MENU_STEP_MS, bassHz, bassHz, SINE, 0.3f, 0.3f);
		}
		return wave.toBytes();
	}

	/**
	 * MIDI 음 번호를 주파수(Hz)로 바꾼다. 69번(라4)이 440Hz이고, 번호가 12 오를 때마다 주파수가 2배가 된다.
	 *
	 * @param midi MIDI 음 번호
	 * @return 주파수(Hz)
	 */
	private static double midiToHz(int midi)
	{
		return 440.0 * Math.pow(2.0, (midi - 69) / 12.0);
	}

	/**
	 * 소리 파형을 모아 두는 버퍼. 음을 시간 위치에 맞춰 겹쳐 쌓고, 마지막에 16비트 소리 데이터로 바꾼다.
	 */
	private static class Wave
	{
		/** 샘플 값(-1.0 ~ 1.0). 여러 음을 더하므로 범위를 넘을 수 있어 마지막에 잘라 낸다. */
		private final float[] data;

		/**
		 * 정해진 길이의 빈 버퍼를 만든다.
		 *
		 * @param durationMs 전체 길이(밀리초)
		 */
		Wave(int durationMs)
		{
			data = new float[(int) (SAMPLE_RATE * durationMs / 1000)];
		}

		/**
		 * 음 하나를 만들어 버퍼의 정해진 시간 위치에 더한다.
		 * 시작과 끝에 짧은 증감을 주어 '틱' 하는 잡음이 나지 않게 한다.
		 *
		 * @param startMs  음이 시작하는 시간(밀리초)
		 * @param lengthMs 음의 길이(밀리초)
		 * @param startHz  시작 주파수(Hz)
		 * @param endHz    끝 주파수(Hz). 시작과 다르면 음이 미끄러진다.
		 * @param shape    파형 종류(SQUARE, PULSE, TRIANGLE, SINE, NOISE)
		 * @param amp      크기(0.0 ~ 1.0)
		 * @param fade     끝으로 갈수록 작아지는 정도(0.0: 일정, 1.0: 끝에서 0)
		 */
		void tone(int startMs, int lengthMs, double startHz, double endHz, int shape, float amp, float fade)
		{
			int start = (int) (SAMPLE_RATE * startMs / 1000);
			int count = (int) (SAMPLE_RATE * lengthMs / 1000);
			int attack = (int) (SAMPLE_RATE * 0.003);
			int release = Math.min(count / 4, (int) (SAMPLE_RATE * 0.02));
			double phase = 0;
			Random noise = new Random(7);

			for (int i = 0; i < count && start + i < data.length; i++)
			{
				// 주파수를 시작값에서 끝값까지 서서히 바꾸며 위상(한 주기 안의 위치, 0~1)을 쌓는다
				double hz = startHz + (endHz - startHz) * i / count;
				phase = phase + hz / SAMPLE_RATE;
				phase = phase - Math.floor(phase);

				// 파형 종류에 따라 -1.0 ~ 1.0 값을 만든다
				float value;
				if (shape == SQUARE)
				{
					value = phase < 0.5 ? 1f : -1f;
				}
				else if (shape == PULSE)
				{
					value = phase < 0.25 ? 1f : -1f;
				}
				else if (shape == TRIANGLE)
				{
					value = (float) (4.0 * Math.abs(phase - 0.5) - 1.0);
				}
				else if (shape == SINE)
				{
					value = (float) Math.sin(2.0 * Math.PI * phase);
				}
				else
				{
					value = noise.nextFloat() * 2f - 1f;
				}

				// 서서히 작아지는 정도, 시작과 끝의 짧은 증감을 곱해 크기를 정한다
				float envelope = 1f - fade * i / count;
				if (i < attack)
				{
					envelope = envelope * i / attack;
				}
				if (count - i < release)
				{
					envelope = envelope * (count - i) / release;
				}
				data[start + i] = data[start + i] + value * amp * envelope;
			}
		}

		/**
		 * 버퍼를 16비트 모노 리틀 엔디언 소리 데이터로 바꾼다. 범위를 넘는 값은 잘라 낸다.
		 *
		 * @return 소리 데이터
		 */
		byte[] toBytes()
		{
			byte[] bytes = new byte[data.length * 2];

			for (int i = 0; i < data.length; i++)
			{
				float clipped = Math.max(-1f, Math.min(1f, data[i]));
				int sample = (int) (clipped * 32767);
				bytes[i * 2] = (byte) (sample & 0xFF);
				bytes[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
			}
			return bytes;
		}
	}

	// ======================== 소리 시험 ========================

	/**
	 * 소리를 귀로 확인하는 시험용 실행 진입점. 효과음을 선언 순서대로(0.7초 간격) 한 번씩 들려주고,
	 * 이어서 메뉴 BGM 6초, 게임 BGM 8초를 들려준 뒤 끝낸다.
	 *
	 * @param args 사용하지 않는다.
	 * @throws InterruptedException 기다리는 중에 중단된 경우
	 */
	public static void main(String[] args) throws InterruptedException
	{
		// 효과음을 선언 순서대로 하나씩 들려준다
		Sound[] sounds = Sound.values();
		for (int i = 0; i < sounds.length; i++)
		{
			play(sounds[i]);
			Thread.sleep(700);
		}

		// 배경음악 두 곡을 차례로 들려준다
		startBgm(Bgm.MENU);
		Thread.sleep(6000);
		startBgm(Bgm.GAME);
		Thread.sleep(8000);

		stopBgm();
		System.exit(0);
	}
}
