/**
 * 일반 테트리스에서 쓰는 7가지 블록(I, J, L, O, S, T, Z)의 모양 데이터.
 * (원래 GameFrame.java 안에 있던 클래스를 파일로 분리했다)
 */
class Blocks
{
	int[][] block;
	int start_x;
	int start_y;
	int blockNum;

	Blocks()
	{
		// blockNum = (int)(Math.random() * 7 + 1);
		// block = getBlock();
	}

	public int getBlockNum()
	{
		return blockNum;
	}

	public int getStart_x()
	{
		return start_x;
	}

	public int getStart_y()
	{
		return start_y;
	}

	public int[][] getBlock()
	{
		switch (blockNum)
		{
		case 1: // block - I
			block = new int[][] { { 1, 1, 1, 1 } };
			break;
		case 2: // block - J
			block = new int[][] { { 2, 2, 2 }, { 0, 0, 2 } };
			break;
		case 3: // block - L
			block = new int[][] { { 3, 3, 3 }, { 3, 0, 0 } };
			break;
		case 4: // block - O
			block = new int[][] { { 4, 4 }, { 4, 4 } };
			break;
		case 5: // block - S
			block = new int[][] { { 0, 5, 5 }, { 5, 5, 0 } };
			break;
		case 6: // block - T
			block = new int[][] { { 0, 6, 0 }, { 6, 6, 6 } };
			break;
		case 7: // block - Z
			block = new int[][] { { 7, 7, 0 }, { 0, 7, 7 } };
			break;
		}

		start_x = (12 / 2) - (block[0].length / 2);
		start_y = 0;
		return block;
	}
}
