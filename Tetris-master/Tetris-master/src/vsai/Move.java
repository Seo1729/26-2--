package vsai;

/**
 * AI가 고른 둘 자리.
 *
 * @param rotation 시계 방향으로 돌릴 횟수 (0~3)
 * @param column   그 회전 상태에서 블록의 가장 왼쪽 칸이 놓일 열 (0~9)
 */
public record Move(int rotation, int column) {
}
