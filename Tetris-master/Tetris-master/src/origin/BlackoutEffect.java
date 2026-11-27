package origin;

/** blackout: 쓴 사람의 화면을 검게 가렸다가 서서히 걷어 낸다. 효과를 받는 판에는 쿨타임을 보여 준다 */
final class BlackoutEffect implements ItemEffect
{
	@Override
	public void apply(GamePanel user, GamePanel target)
	{
		user.startBlackout();
		target.showCooldown(ItemType.BLACKOUT);
	}
}
