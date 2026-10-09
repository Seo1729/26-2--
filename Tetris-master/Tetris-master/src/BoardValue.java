/**
 * 일반 테트리스 보드 배열에 들어가는 숫자들의 의미를 모아 둔 클래스.
 * 원본 코드는 9, 80, 90, 100 같은 숫자를 그대로 썼는데, 읽기 쉽도록 이름을 붙였다.
 */
public class BoardValue
{
	/** 빈 칸 */
	public static final int EMPTY = 0;

	/** 보드 양옆과 바닥의 벽 */
	public static final int WALL = 9;

	/** 아이템 블록의 시작 값. 80 + 아이템 번호(0~9)로 저장한다. */
	public static final int ITEM_START = 80;

	/** 바닥에 고정된 블록의 시작 값. 90 + 블록 번호(1~7)로 저장한다. */
	public static final int FIXED_START = 90;

	/** 회색 방해 블록(줄 올리기 아이템, 갤러그 외계인 착지) */
	public static final int GARBAGE = 100;

	/** 보드 배열의 행 수(화면 20줄 + 바닥 벽 1줄) */
	public static final int ROWS = 21;

	/** 보드 배열의 열 수(화면 10칸 + 양옆 벽 2칸) */
	public static final int COLS = 12;
}
