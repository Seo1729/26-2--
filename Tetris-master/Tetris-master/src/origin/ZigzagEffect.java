package origin;

/** zigzag: 블록이 있는 줄마다 칸 순서를 무작위로 섞는다 */
final class ZigzagEffect implements ItemEffect
{
	@Override
	public void apply(GamePanel user, GamePanel target)
	{
		target.getBoard().shuffleRows();
		target.drawTetris();
	}
}
