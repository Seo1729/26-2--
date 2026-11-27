package origin;

/**
 * 점수와 최고 점수를 int로 보관하고 점수 공식을 계산한다.
 * 전에는 점수가 JLabel 글자로만 있어서 계산할 때마다 Integer.parseInt(score.getText())로 읽어야 했다(Primitive Obsession).
 * 이제 화면 글자는 표시만 하고, 값은 이 클래스가 들고 있다.
 */
final class ScoreKeeper
{
	// 한 번에 지운 줄 수(1~4)별 기본 점수. 실제 점수는 기본 점수 × 1000 / 낙하 간격(ms)이라 빠를수록 많이 받는다
	private static final int ONE_LINE = 10;
	private static final int TWO_LINES = 30;
	private static final int THREE_LINES = 70;
	private static final int FOUR_LINES = 120;

	private int score;
	private int best;

	/** @param best 저장된 최고 점수 (랭킹 1위) */
	ScoreKeeper(int best)
	{
		this.best = best;
	}

	int getScore()
	{
		return score;
	}

	int getBest()
	{
		return best;
	}

	/**
	 * 지운 줄 수만큼 점수를 더한다.
	 * @return 최고 점수를 새로 넘었으면 true
	 */
	boolean addLines(int lines, int gameSpeed)
	{
		score += baseScore(lines) * 1000 / gameSpeed;
		if (score > best)
		{
			best = score;
			return true;
		}
		return false;
	}

	private static int baseScore(int lines)
	{
		switch (lines)
		{
		case 4:
			return FOUR_LINES;
		case 3:
			return THREE_LINES;
		case 2:
			return TWO_LINES;
		default:
			return ONE_LINE;
		}
	}
}
