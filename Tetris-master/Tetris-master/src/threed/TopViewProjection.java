package threed;

// 장면(Game3DScene)을 위에서 내려다본 3x3 칸으로 줄인 결과
// 칸마다 고정 블록은 가장 높은 것, 떨어지는 블록도 가장 높은 것, 고스트는 가장 낮은 것을 남긴다.
// 그리기(TopViewPanel)와 계산(이 클래스)을 나눠서 각자 한 가지 일만 하게 했다. (SRP)
final class TopViewProjection
{
	private final Game3DScene.Cell[][] fixed = new Game3DScene.Cell[Board3D.SIZE_X][Board3D.SIZE_Y];
	private final Game3DScene.Cell[][] active = new Game3DScene.Cell[Board3D.SIZE_X][Board3D.SIZE_Y];
	private final Game3DScene.Cell[][] ghost = new Game3DScene.Cell[Board3D.SIZE_X][Board3D.SIZE_Y];

	TopViewProjection(Game3DScene scene)
	{
		for (Game3DScene.Cell cell : scene.ghostCells)
			if (isInsideColumn(cell))
				keepLowest(ghost, cell);
		for (Game3DScene.Cell cell : scene.cells)
			if (isInsideColumn(cell))
				keepHighest(cell.active ? active : fixed, cell);
	}

	Game3DScene.Cell fixedAt(int x, int y) { return fixed[x][y]; }
	Game3DScene.Cell activeAt(int x, int y) { return active[x][y]; }
	Game3DScene.Cell ghostAt(int x, int y) { return ghost[x][y]; }

	private static boolean isInsideColumn(Game3DScene.Cell cell)
	{
		return cell.x >= 0 && cell.x < Board3D.SIZE_X && cell.y >= 0 && cell.y < Board3D.SIZE_Y;
	}

	private static void keepHighest(Game3DScene.Cell[][] target, Game3DScene.Cell cell)
	{
		Game3DScene.Cell previous = target[cell.x][cell.y];
		if (previous == null || cell.z > previous.z)
			target[cell.x][cell.y] = cell;
	}

	private static void keepLowest(Game3DScene.Cell[][] target, Game3DScene.Cell cell)
	{
		Game3DScene.Cell previous = target[cell.x][cell.y];
		if (previous == null || cell.z < previous.z)
			target[cell.x][cell.y] = cell;
	}
}
