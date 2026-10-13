import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * 일반 모드 랭킹(상위 10개)을 저장하고 불러오는 클래스.
 * 기록은 java.util.prefs.Preferences에 저장되므로 프로그램을 껐다 켜도 남고, 프로젝트 폴더에 파일이 생기지 않는다.
 */
class RankingManager
{
	/** 랭킹에 남기는 최대 기록 수 */
	static final int MAX_ENTRIES = 10;

	/** 이름의 최대 글자 수 */
	private static final int MAX_NAME_LENGTH = 10;

	/** 저장소 항목 이름 앞에 붙이는 말 */
	private static final String KEY_PREFIX = "rank_";

	/** 기록 하나 안에서 이름, 점수, 레벨을 나누는 문자 */
	private static final String SEPARATOR = "\t";

	/** 랭킹을 저장하는 저장소 */
	private static Preferences store = Preferences.userRoot().node("jbnu_tetris_ranking");

	/** 객체를 만들 필요가 없는 도구 클래스이므로 생성자를 막는다. */
	private RankingManager()
	{
	}

	/**
	 * 랭킹 기록 하나(이름, 점수, 도달 레벨).
	 */
	static class Entry
	{
		/** 이름 */
		private String name;

		/** 점수 */
		private int score;

		/** 게임이 끝났을 때의 레벨 */
		private int level;

		/**
		 * 기록을 만든다.
		 *
		 * @param name 이름
		 * @param score 점수
		 * @param level 도달 레벨
		 */
		Entry(String name, int score, int level)
		{
			this.name = name;
			this.score = score;
			this.level = level;
		}

		/**
		 * 이름을 알려 준다.
		 *
		 * @return 이름
		 */
		String getName()
		{
			return name;
		}

		/**
		 * 점수를 알려 준다.
		 *
		 * @return 점수
		 */
		int getScore()
		{
			return score;
		}

		/**
		 * 도달 레벨을 알려 준다.
		 *
		 * @return 레벨
		 */
		int getLevel()
		{
			return level;
		}
	}

	/**
	 * 저장된 랭킹을 높은 점수 순서로 읽어 온다.
	 *
	 * @return 기록 목록(최대 10개)
	 */
	static synchronized ArrayList<Entry> getEntries()
	{
		ArrayList<Entry> list = new ArrayList<Entry>();
		for (int i = 1; i <= MAX_ENTRIES; i++)
		{
			String value = store.get(KEY_PREFIX + i, null);

			// 비어 있는 순위부터는 기록이 없다
			if (value == null)
				break;

			// 형식이 깨진 기록은 건너뛴다
			String[] parts = value.split(SEPARATOR);
			if (parts.length != 3)
				continue;
			try
			{
				list.add(new Entry(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
			}
			catch (NumberFormatException e)
			{
				// 숫자가 아닌 기록은 무시하고 나머지 기록만 보여 준다
			}
		}
		return list;
	}

	/**
	 * 저장된 최고 점수를 알려 준다.
	 *
	 * @return 최고 점수(기록이 없으면 0)
	 */
	static int getBestScore()
	{
		ArrayList<Entry> list = getEntries();
		if (list.isEmpty())
			return 0;
		return list.get(0).getScore();
	}

	/**
	 * 이 점수가 들어갈 순위를 알려 준다.
	 *
	 * @param score 점수
	 * @return 1 ~ 10위, 10위 안에 못 들면 0
	 */
	static int rankOf(int score)
	{
		ArrayList<Entry> list = getEntries();

		// 같은 점수면 먼저 세운 기록이 앞이므로, 기존 점수보다 높아야 그 자리를 차지한다
		int rank = 1;
		for (int i = 0; i < list.size(); i++)
		{
			if (list.get(i).getScore() >= score)
				rank++;
		}
		if (rank > MAX_ENTRIES)
			return 0;
		return rank;
	}

	/**
	 * 기록을 랭킹에 넣고 저장한다. 10위 밖으로 밀린 기록은 지운다.
	 *
	 * @param name 이름(비어 있으면 "익명")
	 * @param score 점수
	 * @param level 도달 레벨
	 */
	static synchronized void add(String name, int score, int level)
	{
		// 저장 형식이 깨지지 않게 구분 문자를 빼고 길이를 줄인다
		String clean = name == null ? "" : name.replace(SEPARATOR, " ").trim();
		if (clean.isEmpty())
			clean = "익명";
		if (clean.length() > MAX_NAME_LENGTH)
			clean = clean.substring(0, MAX_NAME_LENGTH);

		ArrayList<Entry> list = getEntries();
		int rank = rankOf(score);
		if (rank == 0)
			return;
		list.add(rank - 1, new Entry(clean, score, level));

		// 위에서부터 10개만 다시 저장한다
		for (int i = 0; i < MAX_ENTRIES; i++)
		{
			if (i < list.size())
			{
				Entry e = list.get(i);
				store.put(KEY_PREFIX + (i + 1), e.getName() + SEPARATOR + e.getScore() + SEPARATOR + e.getLevel());
			}
			else
			{
				store.remove(KEY_PREFIX + (i + 1));
			}
		}
		try
		{
			store.flush();
		}
		catch (BackingStoreException e)
		{
			// 저장소에 쓰지 못해도 게임은 계속되어야 하므로 이번 실행에서만 기록이 남는다
		}
	}

	/**
	 * 순위표를 화면에 보여 줄 글자로 만든다.
	 *
	 * @return 순위표 글자
	 */
	static String toText()
	{
		ArrayList<Entry> list = getEntries();
		if (list.isEmpty())
			return "아직 기록이 없습니다. 일반 모드를 플레이해 보세요!";

		String text = "순위   이름   점수   레벨\n";
		for (int i = 0; i < list.size(); i++)
		{
			Entry e = list.get(i);
			text = text + (i + 1) + "위   " + e.getName() + "   " + e.getScore() + "점   LV " + e.getLevel() + "\n";
		}
		return text;
	}
}
