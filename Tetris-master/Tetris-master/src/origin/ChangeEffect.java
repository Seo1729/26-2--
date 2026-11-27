package origin;

/** change: 두 판의 보드(블록, 위치 포함)를 맞바꾼다. 지금은 상대가 자기 자신이라 바뀌는 것이 없다 */
final class ChangeEffect implements ItemEffect
{
	@Override
	public void apply(GamePanel user, GamePanel target)
	{
		user.swapBoard(target);
	}
}
