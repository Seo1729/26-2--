package origin;

/**
 * 줄 올리기: 쌓인 블록을 lines줄 올리고 맨 아래를 회색 줄로 채운다. 무작위 한 칸만 비운다.
 * firstCol은 회색으로 채우기 시작하는 열이다. 1줄짜리는 1열, 3줄짜리는 0열(벽 자리)부터 채우던 기존 동작을 그대로 지킨다.
 */
final class LineUpEffect implements ItemEffect
{
	private final int lines;
	private final int firstCol;

	LineUpEffect(int lines, int firstCol)
	{
		this.lines = lines;
		this.firstCol = firstCol;
	}

	@Override
	public void apply(GamePanel user, GamePanel target)
	{
		int holeCol = (int) (Math.random() * TetrisBoard.COLS + 1);
		target.getBoard().raiseGarbage(lines, firstCol, holeCol);
		target.drawTetris();
	}
}
