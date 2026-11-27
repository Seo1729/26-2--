package origin;

import java.util.ArrayList;

/**
 * 2D 테트리스 보드의 데이터와 규칙 (이동, 충돌, 회전, 고정, 줄 삭제).
 *
 * 전에는 이 내용이 화면·스레드·점수·아이템과 함께 GamePanel 한 클래스에 섞여 있었다(Large Class, SRP 위반).
 * 이 클래스는 Swing 부품, 소리, 스레드를 전혀 모르고 int 배열만 다룬다.
 * 그래서 화면 없이도 규칙만 따로 확인할 수 있다.
 *
 * 배열 두 개를 쓴다.
 * - array : 떨어지는 중인 블록과 벽(테두리)
 * - field : 바닥에 고정된 블록, 아이템 블록, 회색 쓰레기 블록
 * 두 배열 모두 보이는 판(20줄 × 10칸)에 왼쪽·오른쪽 벽 1칸씩과 바닥 1줄을 더한 21 × 12 크기다.
 *
 * 테스트(HoldCheck, IntegrationGameplayCheck)가 블록 위치를 직접 정해 보기 때문에 필드는 package-private으로 둔다.
 */
final class TetrisBoard implements HpSystem.BottomClearer
{
	static final int ROWS = 20; // 보이는 줄 수
	static final int COLS = 10; // 보이는 칸 수

	// 이 열부터는 I 블록을 세울 수 없다 (전부터 있던 회전 예외 규칙)
	private static final int I_BLOCK_ROTATE_LIMIT = 9;
	// 아이템 블록이 생길 확률의 분모 (1/20)
	private static final int ITEM_CHANCE = 20;
	// 줄을 한 번 지울 때 새로 생길 수 있는 아이템 블록 최대 개수
	private static final int MAX_NEW_ITEMS = 2;

	int[][] array = new int[ROWS + 1][COLS + 2];
	int[][] field = new int[ROWS + 1][COLS + 2];

	int[][] block; // 지금 떨어지는 블록 모양
	int x; // 블록 왼쪽 위의 가로 위치 (배열 기준)
	int y; // 블록 왼쪽 위의 세로 위치 (배열 기준)
	int blockNum; // 지금 블록 번호 (1~7)
	int blockWSize; // 블록의 너비
	int blockHSize; // 블록의 높이

	/**
	 * array 테두리에 벽 값을 다시 채운다. 벽은 출력과 충돌 판정에만 쓰는 표시라 실제 블록 데이터에는 영향이 없다.
	 * 안쪽에 남은 벽 값(아이템 bomb 등으로 생길 수 있음)은 먼저 지운다.
	 */
	void resetWalls()
	{
		for (int row = 0; row < array.length - 1; row++)
			for (int col = 1; col < array[0].length - 1; col++)
				if (array[row][col] == BoardCell.WALL)
					array[row][col] = BoardCell.EMPTY;

		for (int row = 0; row < array.length; row++)
		{
			array[row][0] = BoardCell.WALL;
			array[row][array[0].length - 1] = BoardCell.WALL;
		}

		for (int col = 0; col < array[0].length; col++)
			array[array.length - 1][col] = BoardCell.WALL;
	}

	/** 새 블록을 맨 위 가운데에 놓는다 (쌓인 블록과 겹치는지는 확인하지 않는다) */
	void spawn(BlockType type)
	{
		block = type.newShape();
		blockNum = type.getNumber();
		x = type.getStartX();
		y = 0;
		blockWSize = block[0].length;
		blockHSize = block.length;

		for (int row = 0; row < blockHSize; row++)
			for (int col = x, w = 0; col < blockWSize + x; col++, w++)
				if (block[row][w] > 0)
					array[row][col] = block[row][w];
	}

	/** 방금 나온 블록이 쌓인 블록과 겹친 칸 수 (0보다 크면 나올 자리가 막힌 것) */
	int countSpawnOverlap()
	{
		int count = 0;
		for (int row = y; row < y + blockHSize; row++)
			for (int col = x; col < x + blockWSize; col++)
				if (array[row][col] > 0 && field[row][col] > 0)
					count++;
		return count;
	}

	/** 떨어지던 블록만 지운다. 고정된 블록과 벽은 그대로 둔다 (홀드할 때 사용) */
	void clearFallingBlock()
	{
		for (int row = 0; row < array.length - 1; row++)
			for (int col = 1; col < array[0].length - 1; col++)
				array[row][col] = BoardCell.EMPTY;
	}

	/******************** 이동 ********************/

	/**
	 * array 전체(블록 + 벽)를 (dRow, dCol)만큼 민 복사본을 만든다.
	 * 전에는 move_left, move_right, move_down, move_drop이 이 복사 반복문을 각자 따로 갖고 있었다(Duplicated Code).
	 */
	private int[][] shiftedArray(int dRow, int dCol)
	{
		int[][] copy = new int[array.length][array[0].length];
		for (int row = 0; row < array.length; row++)
			for (int col = 0; col < array[0].length; col++)
			{
				int toRow = row + dRow;
				int toCol = col + dCol;
				if (toRow >= 0 && toRow < array.length && toCol >= 0 && toCol < array[0].length)
					copy[toRow][toCol] = array[row][col];
			}
		return copy;
	}

	/**
	 * 민 복사본이 field와 겹치는 칸 수를 주어진 범위(행 rowFrom 이상 rowTo 미만, 열 colFrom 이상 colTo 미만)에서 센다.
	 * 검사 범위는 방향마다 전과 똑같이 넘겨받는다.
	 */
	private int countShiftOverlap(int[][] shifted, int rowFrom, int rowTo, int colFrom, int colTo)
	{
		int count = 0;
		for (int row = rowFrom; row < rowTo; row++)
			for (int col = colFrom; col < colTo; col++)
				if (shifted[row][col] > 0 && field[row][col] > 0)
					count++;
		return count;
	}

	// 민 복사본을 array에 다시 옮겨 담는다
	private void copyBack(int[][] shifted)
	{
		for (int row = 0; row < array.length; row++)
			for (int col = 0; col < array[0].length; col++)
				array[row][col] = shifted[row][col];
	}

	/** 왼쪽으로 한 칸. 왼쪽 벽을 넘거나 블록과 겹치면 움직이지 않는다. 움직였으면 true */
	boolean moveLeft()
	{
		int[][] shifted = shiftedArray(0, -1);
		int count = countShiftOverlap(shifted, y, y + blockHSize, x - 1, x + blockWSize);
		if (count == 0 && x > 1)
		{
			x--;
			copyBack(shifted);
			return true;
		}
		return false;
	}

	/** 오른쪽으로 한 칸. 오른쪽 벽을 넘거나 블록과 겹치면 움직이지 않는다. 움직였으면 true */
	boolean moveRight()
	{
		int[][] shifted = shiftedArray(0, 1);
		int count = countShiftOverlap(shifted, y, y + blockHSize, x, x + blockWSize + 1);
		if (count == 0 && x < array[0].length - blockWSize - 1)
		{
			x++;
			copyBack(shifted);
			return true;
		}
		return false;
	}

	/**
	 * 한 칸 아래로 내릴 수 있으면 내리고 true. 바닥이나 블록에 닿았으면 그대로 두고 false.
	 * false가 나오면 부르는 쪽(GamePanel)이 블록을 고정하고 다음 블록을 꺼낸다.
	 */
	boolean moveDown()
	{
		int[][] shifted = shiftedArray(1, 0);
		int count = countShiftOverlap(shifted, y, y + blockHSize + 1, x, x + blockWSize);
		if (count == 0 && y < (array.length - 1) - blockHSize)
		{
			y++;
			copyBack(shifted);
			return true;
		}
		return false;
	}

	/**
	 * 블록을 시계 반대 방향으로 돌린다. 판 밖으로 나가거나 쌓인 블록과 겹치면 돌리지 않는다. 돌렸으면 true.
	 * I 블록은 오른쪽 끝(9열 이상)에서 세울 수 없다.
	 */
	boolean rotate()
	{
		// 돌린 모양이 배열 밖으로 나가면 배열을 읽기 전에 멈춘다
		if (x < 1 || y < 0 || x + blockHSize > array[0].length - 1
				|| y + blockWSize > array.length - 1)
			return false;
		int[][] turn = new int[blockWSize][blockHSize];
		for (int i = 0; i < blockWSize; i++)
			for (int j = 0; j < blockHSize; j++)
				turn[i][j] = block[j][(blockWSize - 1) - i];

		if (blockNum == BlockType.I.getNumber() && x >= I_BLOCK_ROTATE_LIMIT)
			return false;
		for (int row = y, i = 0; i < blockWSize; row++, i++)
			for (int col = x, j = 0; j < blockHSize; col++, j++)
				if (turn[i][j] > 0 && field[row][col] > 0)
					return false;

		int temp = blockHSize;
		blockHSize = blockWSize;
		blockWSize = temp;
		block = turn;
		drawTurn();
		return true;
	}

	/**
	 * 돌리기 전 모양이 있던 자리(가로·세로가 바뀌기 전 크기)를 지우고 돌린 모양을 그린다.
	 * rotate가 먼저 오른쪽 벽을 넘는지 확인하므로 여기서 다시 밀어 넣을 일은 없다.
	 */
	private void drawTurn()
	{
		for (int row = y, h = 0; h < blockWSize; row++, h++)
			for (int col = x, w = 0; w < blockHSize; col++, w++)
				array[row][col] = BoardCell.EMPTY;

		for (int row = y, h = 0; h < blockHSize; row++, h++)
			for (int col = x, w = 0; w < blockWSize; col++, w++)
				if (block[h][w] > 0)
					array[row][col] = block[h][w];
	}

	/******************** 고정과 줄 삭제 ********************/

	/** 떨어지던 블록을 고정 블록 값(번호 + 90)으로 field에 옮기고 array를 비운다 */
	void fixFallingBlock()
	{
		for (int row = 0; row < array.length - 1; row++)
			for (int col = 1; col < array[0].length - 1; col++)
			{
				if (array[row][col] != BoardCell.EMPTY)
					field[row][col] = array[row][col] + BoardCell.FIXED_OFFSET;
				array[row][col] = BoardCell.EMPTY;
			}
	}

	/** 한 줄의 보이는 10칸이 모두 차 있는가 */
	boolean isRowFull(int row)
	{
		int count = 0;
		for (int col = 1; col < field[0].length - 1; col++)
			if (field[row][col] != BoardCell.EMPTY)
				count++;
		return count == COLS;
	}

	/** num번 줄을 지우고 그 위의 줄들을 한 칸씩 내린다 */
	void deleteRow(int num)
	{
		int[][] copy = new int[field.length][field[0].length];

		for (int row = 0; row < field.length - 1; row++)
			for (int col = 0; col < field[0].length; col++)
				if (row < num)
					copy[row + 1][col] = field[row][col];
				else
					copy[row + 1][col] = field[row + 1][col];

		for (int row = 0; row < field.length; row++)
			for (int col = 0; col < field[0].length; col++)
				field[row][col] = copy[row][col];
	}

	/** HP 구제 때 맨 밑 줄을 지운다 (HpSystem이 부른다) */
	@Override
	public void clearBottomLine()
	{
		deleteRow(field.length - 2);
	}

	/**
	 * 줄을 지운 뒤 고정 블록(값 90 초과) 칸마다 지운 줄 수만큼 1/20 확률로 아이템 블록을 만든다.
	 * 한 번에 최대 2개까지 만든다. 무작위 값을 뽑는 순서는 전과 같다.
	 */
	void scatterItems(int lines)
	{
		int made = 0;
		for (int i = 0; i < field.length; i++)
			for (int j = 1; j < field[0].length - 1; j++)
				if (field[i][j] > BoardCell.FIXED_OFFSET)
				{
					made = rollItems(i, j, lines, made);
					if (made == MAX_NEW_ITEMS)
						return;
				}
	}

	// 한 칸에 대해 lines번 주사위를 굴려 아이템 블록을 만든다. 지금까지 만든 개수를 돌려준다 (최대에 닿으면 바로 멈춤)
	private int rollItems(int row, int col, int lines, int made)
	{
		int count = made;
		for (int k = 1; k <= lines; k++)
		{
			int chance = (int) (Math.random() * ITEM_CHANCE);
			if (chance == 1)
			{
				field[row][col] = (int) (Math.random() * BoardCell.ITEM_KINDS) + BoardCell.ITEM_BASE;
				count++;
			}
			if (count == MAX_NEW_ITEMS)
				return count;
		}
		return count;
	}

	/******************** 아이템이 쓰는 보드 조작 ********************/

	/** 보드 전체(벽 포함)를 비운다 (bomb) */
	void clearAll()
	{
		for (int row = 0; row < field.length; row++)
			for (int col = 0; col < field[0].length; col++)
			{
				array[row][col] = BoardCell.EMPTY;
				field[row][col] = BoardCell.EMPTY;
			}
	}

	/**
	 * 쌓인 블록을 lines줄 올리고 맨 아래 lines줄을 회색 블록으로 채운다. holeCol 열만 비운다.
	 * 회색으로 채우는 열은 firstCol부터 10열까지다 (1줄짜리는 1열부터, 3줄짜리는 0열부터 채우던 기존 동작을 그대로 지킨다).
	 */
	void raiseGarbage(int lines, int firstCol, int holeCol)
	{
		int[][] copy = new int[field.length][field[0].length];
		for (int row = lines; row < field.length - 1; row++)
			for (int col = 0; col < field[0].length; col++)
				copy[row - lines][col] = field[row][col];

		for (int row = 0; row < field.length; row++)
			for (int col = 0; col < field[0].length; col++)
				field[row][col] = copy[row][col];

		for (int row = field.length - 1 - lines; row < field.length - 1; row++)
			for (int col = firstCol; col < field[0].length - 1; col++)
				if (col != holeCol)
					field[row][col] = BoardCell.GARBAGE;
	}

	/**
	 * 블록이 있는 가장 윗줄부터 맨 아래까지 줄마다 칸 순서를 무작위로 섞는다 (zigzag).
	 * 0번 줄에만 블록이 있으면 섞지 않는 기존 동작을 그대로 지킨다.
	 */
	void shuffleRows()
	{
		int start = firstFilledRow();
		if (start <= 0)
			return;
		for (int i = start; i < field.length; i++)
		{
			ArrayList<Integer> cells = new ArrayList<>();
			for (int j = 1; j < field[0].length - 1; j++)
				cells.add(field[i][j]);

			for (int j = 1; j < field[0].length - 1; j++)
			{
				int index = (int) (Math.random() * cells.size());
				field[i][j] = cells.get(index);
				cells.remove(index);
			}
		}
	}

	// 블록이 있는 가장 윗줄 (0번 줄은 건너뛰고 1번 줄부터 찾는다. 없으면 0)
	private int firstFilledRow()
	{
		for (int i = 1; i < field.length; i++)
			for (int j = 1; j < field[0].length - 1; j++)
				if (field[i][j] > 0)
					return i;
		return 0;
	}

	/******************** 갤러그가 쓰는 보드 조작 ********************/

	/** 화면 기준 (row, col) 칸이 바닥이거나 블록으로 막혀 있는가 */
	boolean isBlocked(int row, int col)
	{
		if (row >= ROWS)
			return true;
		return field[row][col + 1] > 0;
	}

	/** 화면 기준 (row, col)이 비어 있으면 회색 블록을 놓고 true */
	boolean placeGarbage(int row, int col)
	{
		if (field[row][col + 1] == BoardCell.EMPTY && array[row][col + 1] == BoardCell.EMPTY)
		{
			field[row][col + 1] = BoardCell.GARBAGE;
			return true;
		}
		return false;
	}
}
