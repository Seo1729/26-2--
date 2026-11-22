package dodge;

/**
 * 정보판(InfoPanel)이 보여 주는 값만 읽기 전용으로 꺼내 주는 창구.
 * 정보판은 게임 안쪽 필드를 모르고, 게임은 이 값들만 약속하면 된다.
 */
public interface GameStatus {

	/** 이번 판에서 흐른 시간(ms) */
	int getElapsedMs();

	/** 다음에 나올 블록 */
	Shape getNextPiece();

	/** P1이 이번 판에 지운 줄 수 */
	int getLinesRemoved();

	/** P2 고양이의 현재 높이 (바닥 0) */
	int getDodgerHeight();

	/** P2 고양이가 지금 쓸 수 있는 3칸 점프 횟수 */
	int getSuperJumps();

	/** 다음 3칸 점프 충전까지 남은 시간(ms). 가득 차 있으면 0. */
	int getSuperJumpRechargeMs();

	int getTetrisWins();

	int getDodgerWins();
}
