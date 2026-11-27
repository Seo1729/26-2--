package origin;

/**
 * fast / slow: 낙하 간격을 STEP(200ms)만큼 바꾸고 쿨타임을 보여 준다. 쿨타임이 끝나면 CooldownLabel이 되돌린다.
 * delta가 음수면 fast(빨라짐), 양수면 slow(느려짐)다.
 */
final class SpeedEffect implements ItemEffect
{
	static final int STEP = 200; // 아이템 하나가 바꾸는 낙하 간격(ms)

	private final int delta;
	private final ItemType type; // 쿨타임 그림에 보여 줄 아이템

	SpeedEffect(int delta, ItemType type)
	{
		this.delta = delta;
		this.type = type;
	}

	@Override
	public void apply(GamePanel user, GamePanel target)
	{
		// 이미 충분히 빠르면(간격 200ms 이하) fast는 아무것도 하지 않는다
		if (delta < 0 && target.gameSpeed <= STEP)
			return;
		target.gameSpeed += delta;
		target.showCooldown(type);
	}
}
