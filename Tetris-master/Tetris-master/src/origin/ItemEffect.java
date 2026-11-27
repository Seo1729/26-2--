package origin;

/**
 * 아이템 하나가 하는 일 (Strategy 패턴의 전략 인터페이스).
 *
 * 인터페이스는 "이런 메서드를 가진다"는 약속만 적은 것이다. 아이템마다 이 인터페이스를 구현한 클래스가 따로 있고,
 * GamePanel은 어떤 아이템인지 묻지 않고 apply만 부른다. 새 아이템을 넣을 때 GamePanel을 고칠 필요가 없다(OCP).
 */
interface ItemEffect
{
	/**
	 * @param user   아이템을 쓴 판
	 * @param target 효과를 받는 판 (지금은 상대가 없어 항상 user 자신이다)
	 */
	void apply(GamePanel user, GamePanel target);
}
