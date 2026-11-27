package origin;

/**
 * 2D 보드 배열(array, field)의 칸 값이 무엇을 뜻하는지 이름으로 모아 둔 클래스.
 * 전에는 9, 80, 90, 100 같은 숫자가 코드 곳곳에 그대로 적혀 있어서(Magic Number)
 * 숫자만 보고는 뜻을 알 수 없었다. 값 자체는 전과 똑같다.
 *
 * 칸 값 정리
 * - 0      : 빈 칸
 * - 1 ~ 7  : 떨어지는 중인 블록 (BlockType 번호)
 * - 9      : 벽 (array 테두리에만 있음)
 * - 80~89  : 아이템 블록 (80 + 아이템 번호)
 * - 91~97  : 고정된 블록 (블록 번호 + 90)
 * - 100 이상: 회색 쓰레기 블록
 */
final class BoardCell
{
	static final int EMPTY = 0;
	static final int WALL = 9;
	static final int ITEM_BASE = 80;
	static final int ITEM_KINDS = 10;
	static final int FIXED_OFFSET = 90;
	static final int GARBAGE = 100;

	// 객체를 만들 일이 없는 상수 모음이므로 생성자를 막는다
	private BoardCell()
	{
	}

	/** 떨어지는 중인 블록 칸인가 (1~7) */
	static boolean isFalling(int value)
	{
		return BlockType.fromNumber(value) != null;
	}

	/** 아이템 블록 칸인가 (80~89) */
	static boolean isItem(int value)
	{
		return value >= ITEM_BASE && value < ITEM_BASE + ITEM_KINDS;
	}

	/** 고정 블록 칸인가 (90~99) */
	static boolean isFixed(int value)
	{
		return value >= FIXED_OFFSET && value < GARBAGE;
	}
}
