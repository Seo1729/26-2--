package threed;

import java.util.ArrayList;
import java.util.List;

// 게임 상태(보드 + 떨어지는 블록 + 고스트)를 화면용 장면(Game3DScene)으로 바꾼다.
// 게임 규칙(Game3D)과 화면에 넘길 데이터 만들기를 나눠서 각자 한 가지 일만 하게 했다. (SRP)
final class Game3DSceneBuilder
{
	private Game3DSceneBuilder() { }

	// 순서: 보드의 고정 칸(x → y → z 순) → 떨어지는 블록 큐브 / 고스트는 따로
	static Game3DScene build(Board3D board, Block3D activeBlock, Block3D ghost)
	{
		List<Game3DScene.Cell> cells = new ArrayList<>();
		addFixedCells(cells, board);
		if (activeBlock != null)
			addBlockCells(cells, activeBlock, true);
		List<Game3DScene.Cell> ghostCells = new ArrayList<>();
		if (ghost != null)
			addBlockCells(ghostCells, ghost, false);
		return new Game3DScene(cells, ghostCells);
	}

	// 보드에 고정된 칸을 모두 담는다
	private static void addFixedCells(List<Game3DScene.Cell> cells, Board3D board)
	{
		for (int x = 0; x < Board3D.SIZE_X; x++)
			for (int y = 0; y < Board3D.SIZE_Y; y++)
				for (int z = 0; z < Board3D.SIZE_Z; z++)
					if (board.isFilled(x, y, z))
						cells.add(new Game3DScene.Cell(x, y, z, board.getType(x, y, z), false));
	}

	// 블록 하나의 큐브 4개를 보드 좌표로 담는다
	private static void addBlockCells(List<Game3DScene.Cell> cells, Block3D block, boolean active)
	{
		for (Block3D.Cube cube : block.getAbsoluteCubes())
			cells.add(new Game3DScene.Cell(cube.getX(), cube.getY(), cube.getZ(), block.getType(), active));
	}
}
