package poop;

/**
 * 게임 로직에서 일어난 일을 바깥(효과음 등)에 알리는 통로.
 * 로직 클래스는 SoundManager를 직접 부르지 않고 이 인터페이스만 안다.
 * 필요한 것만 골라 구현할 수 있게 전부 빈 기본 메서드로 둔다.
 */
public interface GameEvents {

	/** 아무것도 하지 않는 기본값 (테스트용) */
	GameEvents NONE = new GameEvents() {
	};

	/** P1 블록이 실제로 옆으로 움직임 */
	default void onPieceMoved() {
	}

	/** P1 블록이 실제로 돌아감 */
	default void onPieceRotated() {
	}

	/** P1 블록이 바닥에 고정됨 */
	default void onPieceLocked() {
	}

	/** 줄이 지워짐 */
	default void onLinesCleared(int lines) {
	}

	/** P2 고양이가 점프함 */
	default void onJump() {
	}

	/** P2 고양이가 3칸 점프를 씀 (따로 정하지 않으면 보통 점프와 같게 처리) */
	default void onSuperJump() {
		onJump();
	}

	/** P2 고양이가 블록에 깔림 */
	default void onCrushed() {
	}

	/** 승부가 남 */
	default void onGameEnd(GameRules.Result result) {
	}
}
