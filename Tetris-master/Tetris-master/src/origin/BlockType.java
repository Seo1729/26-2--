package origin;

import javax.swing.ImageIcon;

/**
 * 2D 테트리스 블록 7종.
 *
 * enum은 "정해진 몇 개의 값"만 가질 수 있는 특별한 클래스다. 여기서는 I, J, L, O, S, T, Z
 * 7개의 객체만 존재하고, 각 객체가 자기 보드 번호(1~7)와 모양 배열을 함께 가진다.
 * 전에는 "번호 → 모양", "번호 → 미리보기 아이콘", "번호 → 칸 색"이 Blocks, nextBlock,
 * holdBlock, drawTetris에 switch로 따로 흩어져 있었다(Switch Statements). 이제 이 표 한 곳만 보면 된다.
 */
enum BlockType
{
	I(1, new int[][] { { 1, 1, 1, 1 } }, ImageSource.BLOCK_I, ImageSource.BLOCK_RED),
	J(2, new int[][] { { 2, 2, 2 }, { 0, 0, 2 } }, ImageSource.BLOCK_J, ImageSource.BLOCK_LIME),
	L(3, new int[][] { { 3, 3, 3 }, { 3, 0, 0 } }, ImageSource.BLOCK_L, ImageSource.BLOCK_ORANGE),
	O(4, new int[][] { { 4, 4 }, { 4, 4 } }, ImageSource.BLOCK_O, ImageSource.BLOCK_PURPLE),
	S(5, new int[][] { { 0, 5, 5 }, { 5, 5, 0 } }, ImageSource.BLOCK_S, ImageSource.BLOCK_CYAN),
	T(6, new int[][] { { 0, 6, 0 }, { 6, 6, 6 } }, ImageSource.BLOCK_T, ImageSource.BLOCK_BLUE),
	Z(7, new int[][] { { 7, 7, 0 }, { 0, 7, 7 } }, ImageSource.BLOCK_Z, ImageSource.BLOCK_GREEN);

	// 블록이 처음 나오는 가로 위치를 정할 때 쓰는 보드 배열의 가로 칸 수 (벽 포함)
	private static final int BOARD_WIDTH = 12;

	private final int number;
	private final int[][] shape;
	private final ImageIcon previewIcon; // 다음 블록·홀드 칸 그림
	private final ImageIcon cellIcon; // 보드 칸 색 (I : red, J : lime, L : orange, O : purple, S : cyan, T : blue, Z : green)

	BlockType(int number, int[][] shape, ImageIcon previewIcon, ImageIcon cellIcon)
	{
		this.number = number;
		this.shape = shape;
		this.previewIcon = previewIcon;
		this.cellIcon = cellIcon;
	}

	/** 보드 배열에 들어가는 번호 (1~7) */
	int getNumber()
	{
		return number;
	}

	/** 처음 나올 때의 모양. 회전하면 배열이 바뀌므로 부를 때마다 새 배열을 만들어 준다 */
	int[][] newShape()
	{
		int[][] copy = new int[shape.length][];
		for (int i = 0; i < shape.length; i++)
			copy[i] = shape[i].clone();
		return copy;
	}

	/** 처음 나올 때의 가로 위치 (가운데 정렬) */
	int getStartX()
	{
		return (BOARD_WIDTH / 2) - (shape[0].length / 2);
	}

	/** 다음 블록·홀드 칸에 보여 줄 아이콘 */
	ImageIcon getPreviewIcon()
	{
		return previewIcon;
	}

	/** 보드 칸에 그릴 색 블록 아이콘 */
	ImageIcon getCellIcon()
	{
		return cellIcon;
	}

	/** 보드 번호로 블록 종류를 찾는다. 1~7이 아니면 null */
	static BlockType fromNumber(int number)
	{
		for (BlockType type : values())
			if (type.number == number)
				return type;
		return null;
	}

	/** 7종 중 하나를 무작위로 고른다 (전과 같은 Math.random 식) */
	static int randomNumber()
	{
		return (int) (Math.random() * 7 + 1);
	}
}
