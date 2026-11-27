package vscpu.cpu;

import vscpu.game.Board;
import vscpu.game.Tetromino;

/**
 * CPU의 "머리". 새 블록이 나올 때마다 어디에 둘지 정한다.
 * 별도 스레드에서 불리므로 받은 판(복사본)만 보고 판단해야 한다.
 */
public interface CpuBrain {

	/**
	 * CPU가 고른 둘 자리.
	 *
	 * @param rotation 시계 방향으로 돌릴 횟수 (0~3)
	 * @param column   그 회전 상태에서 블록의 가장 왼쪽 칸이 놓일 열 (0~9)
	 */
	record Move(int rotation, int column) {
	}

	/**
	 * @param board   지금 판의 복사본 (마음대로 바꿔도 된다)
	 * @param current 지금 떨어지는 블록
	 * @param next    다음 블록 (미리 생각해 둘 수 있다)
	 * @return 둘 자리. 놓을 수 있는 곳이 없으면 null
	 */
	Move decide(Board board, Tetromino current, Tetromino next);
}
