package origin;

/** 줄 내리기: 맨 아래 lines줄을 지운다 (위쪽 줄부터 차례로) */
final class LineDownEffect implements ItemEffect
{
	private final int lines;

	LineDownEffect(int lines)
	{
		this.lines = lines;
	}

	@Override
	public void apply(GamePanel user, GamePanel target)
	{
		for (int i = lines; i >= 1; i--)
			target.getBoard().deleteRow(TetrisBoard.ROWS - i);
		target.drawTetris();
	}
}
