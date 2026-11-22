/**
 * 일반 모드의 생명(HP) 규칙. 화면은 모르고(HpView가 그림), 게임판도 모른다(줄 지우기는 콜백으로 받음).
 *
 * 최대 HP는 3이다. 새 블록이 나올 자리가 막히면(블록이 맨 위에 닿으면) HP가 1 줄고,
 * 그때마다 맨 밑 4줄을 지운 뒤 계속한다. HP가 0이 되면 원래대로 게임 오버가 된다.
 */
class HpSystem
{
	static final int MAX_HP = 3;
	/** HP가 줄 때 맨 밑에서 지우는 줄 수 */
	static final int CLEAR_LINES = 4;
	/** HP를 쓰는 게임 모드 번호 (GameFrame.gameMode 1 = 일반 모드) */
	static final int NORMAL_MODE = 1;

	/** 맨 밑 한 줄을 지우는 방법. 게임판(GamePanel)이 넘겨준다. */
	interface BottomClearer
	{
		void clearBottomLine();
	}

	/** HP가 바뀔 때 알림을 받는 쪽 (화면, 소리 등) */
	interface Listener
	{
		/**
		 * @param hp    지금 HP
		 * @param maxHp 최대 HP
		 * @param lost  HP가 줄어서 바뀐 것이면 true (처음 연결할 때는 false)
		 */
		void hpChanged(int hp, int maxHp, boolean lost);
	}

	// 생성자 없이 만든 객체(테스트의 allocateInstance)는 enabled=false라서 "HP 꺼짐"으로 동작한다
	private boolean enabled;
	private volatile int hp;
	private Listener listener;

	HpSystem(boolean enabled)
	{
		this.enabled = enabled;
		this.hp = MAX_HP;
	}

	/** 게임 모드에 맞는 HP를 만든다. 일반 모드에서만 켜진다. */
	static HpSystem forMode(int gameMode)
	{
		return new HpSystem(gameMode == NORMAL_MODE);
	}

	/** 지금 게임 모드에서 HP를 쓰는지 */
	boolean isEnabled()
	{
		return enabled;
	}

	int getHp()
	{
		return hp;
	}

	/** 알림 받을 곳을 정하고 지금 HP를 바로 한 번 알려 준다 */
	void setListener(Listener listener)
	{
		this.listener = listener;
		notifyChanged(false);
	}

	/**
	 * 블록이 맨 위에 닿았을 때 부른다. HP가 남아 있으면 1 줄이고 맨 밑 CLEAR_LINES줄을 지운다.
	 *
	 * @param board 맨 밑 줄을 지울 게임판
	 * @return 지우고 계속할 수 있으면 true, HP를 안 쓰는 모드이거나 HP가 0이 되면 false (게임 오버)
	 */
	boolean tryRevive(BottomClearer board)
	{
		if (!enabled || hp <= 0)
			return false;
		hp--;
		notifyChanged(true);
		if (hp <= 0)
			return false;
		for (int i = 0; i < CLEAR_LINES; i++)
			board.clearBottomLine();
		return true;
	}

	private void notifyChanged(boolean lost)
	{
		if (listener != null)
			listener.hpChanged(hp, MAX_HP, lost);
	}
}
