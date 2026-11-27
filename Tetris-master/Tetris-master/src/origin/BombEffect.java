package origin;

/** bomb: 판 전체를 비우고 새 블록을 꺼낸다 */
final class BombEffect implements ItemEffect
{
	@Override
	public void apply(GamePanel user, GamePanel target)
	{
		target.getBoard().clearAll();
		target.respawnBlock();
	}
}
