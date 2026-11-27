package origin;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

/**
 * 보드 20줄 × 10칸을 JLabel 격자로 그리는 화면 담당.
 * 보드 값(TetrisBoard)을 읽어 칸마다 알맞은 아이콘을 붙이기만 하고, 규칙은 모른다.
 * 전에는 GamePanel.drawTetris 안에 switch 3개·case 24개로 들어 있었다(Long Method, Switch Statements).
 */
final class BoardView
{
	private final JLabel[][] cells = new JLabel[TetrisBoard.ROWS][TetrisBoard.COLS];

	/** area 패널에 칸 200개를 격자로 붙인다 */
	BoardView(JPanel area)
	{
		area.setLayout(new GridLayout(TetrisBoard.ROWS, TetrisBoard.COLS));
		LineBorder gridLine = new LineBorder(new Color(15, 15, 15)); // 칸 사이의 아주 어두운 선
		for (int i = 0; i < TetrisBoard.ROWS; i++)
		{
			for (int j = 0; j < TetrisBoard.COLS; j++)
			{
				cells[i][j] = new JLabel();
				area.add(cells[i][j]);
				cells[i][j].setBackground(Color.BLACK);
				cells[i][j].setOpaque(true);
				cells[i][j].setBorder(gridLine);
			}
		}
	}

	/** 보드의 지금 상태를 칸마다 그린다 (배열은 왼쪽 벽 1칸이 있어 열 번호가 1 크다) */
	void render(TetrisBoard board)
	{
		for (int row = 0; row < TetrisBoard.ROWS; row++)
			for (int col = 0; col < TetrisBoard.COLS; col++)
			{
				JLabel cell = cells[row][col];
				cell.setIcon(iconFor(cell, board.array[row][col + 1], board.field[row][col + 1]));
			}
	}

	/**
	 * 한 칸에 그릴 아이콘. 떨어지는 블록이 있으면 그 색이 먼저이고, 없으면 field 값(아이템 → 고정 → 회색 → 빈 칸) 순서로 정한다.
	 * 고정 블록 값 90, 98, 99는 나올 수 없는 값이라 원래 그림을 그대로 둔다.
	 */
	private ImageIcon iconFor(JLabel cell, int moving, int fixed)
	{
		if (BoardCell.isFalling(moving))
			return BlockType.fromNumber(moving).getCellIcon();
		if (BoardCell.isItem(fixed))
			return ItemType.fromCell(fixed).getBlockIcon();
		if (BoardCell.isFixed(fixed))
		{
			BlockType type = BlockType.fromNumber(fixed - BoardCell.FIXED_OFFSET);
			if (type == null)
				return (ImageIcon) cell.getIcon();
			return type.getCellIcon();
		}
		if (fixed >= BoardCell.GARBAGE)
			return ImageSource.BLOCK_GRAY;
		return null;
	}
}
