import java.awt.Color;
import java.util.Vector;

import javax.swing.ImageIcon;

/**
 * 일반 테트리스의 아이템 10종의 효과를 실행하는 클래스.
 * 원본에서는 GamePanel 안의 내부 클래스(UseItem)였는데, 따로 파일로 분리했다.
 * 아이템을 쓴 쪽(owner)과 효과를 받는 쪽(target)을 구분한다. 1인 모드에서는 둘이 같다.
 */
public class ItemEffect
{
	/** 아이템을 사용한 게임 화면. 블랙아웃처럼 자기 화면에 효과를 줄 때 쓴다. */
	private GamePanel owner;

	/**
	 * 아이템 효과 실행기를 만든다.
	 *
	 * @param owner 아이템을 사용하는 게임 화면
	 */
	public ItemEffect(GamePanel owner)
	{
		this.owner = owner;
	}

	/**
	 * 폭탄: 상대 보드의 모든 블록을 지우고 새 블록부터 다시 시작하게 한다.
	 *
	 * @param target 효과를 받는 게임 화면
	 */
	public void bomb(GamePanel target)
	{
		// 떨어지는 블록 배열과 고정 블록 배열을 전부 비운다
		for (int row = 0; row < BoardValue.ROWS; row++)
		{
			for (int col = 0; col < BoardValue.COLS; col++)
			{
				target.blockArray[row][col] = BoardValue.EMPTY;
				target.fieldArray[row][col] = BoardValue.EMPTY;
			}
		}

		// 지금 떨어지던 블록의 스레드를 멈추고 새 블록을 내보낸다
		target.dropThread.interrupt();
		target.addNewBlock();
	}

	/**
	 * 체인지: 내 보드와 상대 보드를 통째로 맞바꾼다.
	 *
	 * @param target 효과를 받는 게임 화면
	 */
	public void change(GamePanel target)
	{
		// 배열 세 개(떨어지는 블록, 고정 블록, 블록 모양)를 서로 바꾼다
		int[][] temp = owner.blockArray;
		owner.blockArray = target.blockArray;
		target.blockArray = temp;

		temp = owner.fieldArray;
		owner.fieldArray = target.fieldArray;
		target.fieldArray = temp;

		temp = owner.currentBlock;
		owner.currentBlock = target.currentBlock;
		target.currentBlock = temp;

		// 블록 위치와 크기, 종류도 바꾼다
		int tempValue = owner.blockX;
		owner.blockX = target.blockX;
		target.blockX = tempValue;

		tempValue = owner.blockY;
		owner.blockY = target.blockY;
		target.blockY = tempValue;

		tempValue = owner.blockHeight;
		owner.blockHeight = target.blockHeight;
		target.blockHeight = tempValue;

		tempValue = owner.blockWidth;
		owner.blockWidth = target.blockWidth;
		target.blockWidth = tempValue;

		tempValue = owner.blockType;
		owner.blockType = target.blockType;
		target.blockType = tempValue;

		// 바뀐 보드를 양쪽 모두 다시 그린다
		owner.drawBoard();
		target.drawBoard();
	}

	/**
	 * 한 줄 내리기: 상대 보드의 맨 아래 한 줄을 지운다.
	 *
	 * @param target 효과를 받는 게임 화면
	 */
	public void oneLineDown(GamePanel target)
	{
		// 바닥 벽 바로 위 줄이 맨 아래 줄이다
		target.deleteLine(BoardValue.ROWS - 2);
		target.drawBoard();
	}

	/**
	 * 세 줄 내리기: 상대 보드의 맨 아래 세 줄을 지운다.
	 *
	 * @param target 효과를 받는 게임 화면
	 */
	public void threeLineDown(GamePanel target)
	{
		// 아래에서 세 번째 줄부터 한 줄씩 지운다
		for (int i = 3; i >= 1; i--)
		{
			target.deleteLine((BoardValue.ROWS - 1) - i);
		}
		target.drawBoard();
	}

	/**
	 * 슬로우: 상대 블록이 떨어지는 간격을 0.2초 늘린다. 쿨타임이 끝나면 원래대로 돌아온다.
	 *
	 * @param target 효과를 받는 게임 화면
	 */
	public void slow(GamePanel target)
	{
		target.gameSpeed = target.gameSpeed + 200;
		showCoolTime(target, ImageSource.item_slow);
	}

	/**
	 * 패스트: 상대 블록이 떨어지는 간격을 0.2초 줄인다. 이미 너무 빠르면 쓰지 않는다.
	 *
	 * @param target 효과를 받는 게임 화면
	 */
	public void fast(GamePanel target)
	{
		// 간격이 0 이하가 되면 게임이 멈추므로 막는다
		if (target.gameSpeed <= 200)
		{
			return;
		}
		target.gameSpeed = target.gameSpeed - 200;
		showCoolTime(target, ImageSource.item_fast);
	}

	/**
	 * 블랙아웃: 내 화면을 검게 가렸다가 10초 동안 조금씩 밝아지게 한다.
	 *
	 * @param target 효과를 받는 게임 화면(쿨타임 표시만 한다)
	 */
	public void blackout(GamePanel target)
	{
		// 화면이 서서히 밝아지는 동안 게임이 멈추지 않도록 별도 스레드에서 처리한다
		Thread blackThread = new Thread()
		{
			public void run()
			{
				int alpha = 255;

				owner.coverPanel.setVisible(true);
				owner.coverPanel.remove(owner.messageLabel);

				// 1초마다 투명도를 25씩 낮춘다
				while (alpha > 0)
				{
					owner.coverPanel.setBackground(new Color(0, 0, 0, alpha));
					alpha = alpha - 25;
					try
					{
						sleep(1000);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
				owner.coverPanel.setVisible(false);
			}
		};
		blackThread.start();
		showCoolTime(target, ImageSource.item_blackout);
	}

	/**
	 * 지그재그: 블록이 있는 가장 위 줄부터 아래까지, 각 줄의 칸 순서를 무작위로 섞는다.
	 *
	 * @param target 효과를 받는 게임 화면
	 */
	public void zigzag(GamePanel target)
	{
		int[][] field = target.fieldArray;
		int topRow = 0;

		// 블록이 처음 나오는 줄(가장 위 줄)을 찾는다
		for (int i = 0; i < field.length; i++)
		{
			for (int j = 1; j < field[0].length - 1; j++)
			{
				if (field[i][j] > 0)
				{
					topRow = i;
					break;
				}
			}
			if (topRow > 0)
			{
				break;
			}
		}

		// 찾은 줄부터 아래 줄까지 한 줄씩 칸을 섞는다
		if (topRow > 0)
		{
			for (int i = topRow; i < field.length; i++)
			{
				// 한 줄의 값을 목록에 담아 두었다가 무작위로 하나씩 꺼내 다시 채운다
				Vector<Integer> values = new Vector<Integer>();
				for (int j = 1; j < field[0].length - 1; j++)
				{
					values.add(field[i][j]);
				}

				for (int j = 1; j < field[0].length - 1; j++)
				{
					int index = (int) (Math.random() * values.size());
					field[i][j] = values.get(index);
					values.remove(index);
				}
			}
		}

		target.drawBoard();
	}

	/**
	 * 한 줄 올리기: 상대 보드를 한 칸 올리고, 맨 아래에 구멍 하나 뚫린 회색 줄을 넣는다.
	 *
	 * @param target 효과를 받는 게임 화면
	 */
	public void oneLineUp(GamePanel target)
	{
		int[][] field = target.fieldArray;
		int[][] copy = new int[field.length][field[0].length];

		// 모든 줄을 한 줄씩 위로 올린다
		for (int row = 1; row < field.length - 1; row++)
		{
			for (int col = 0; col < field[0].length; col++)
			{
				copy[row - 1][col] = field[row][col];
			}
		}
		target.copyArray(field, copy);

		// 맨 아래 줄을 회색 블록으로 채우되, 무작위로 한 칸은 비워 둔다
		int hole = (int) (Math.random() * 10 + 1);
		for (int col = 1; col < field[0].length - 1; col++)
		{
			if (col != hole)
			{
				field[field.length - 2][col] = BoardValue.GARBAGE;
			}
		}

		target.drawBoard();
	}

	/**
	 * 세 줄 올리기: 상대 보드를 세 칸 올리고, 맨 아래 세 줄을 구멍 하나 뚫린 회색 줄로 채운다.
	 *
	 * @param target 효과를 받는 게임 화면
	 */
	public void threeLineUp(GamePanel target)
	{
		int[][] field = target.fieldArray;
		int[][] copy = new int[field.length][field[0].length];

		// 모든 줄을 세 줄씩 위로 올린다
		for (int row = 3; row < field.length - 1; row++)
		{
			for (int col = 0; col < field[0].length; col++)
			{
				copy[row - 3][col] = field[row][col];
			}
		}
		target.copyArray(field, copy);

		// 맨 아래 세 줄을 회색 블록으로 채우되, 무작위로 한 열은 비워 둔다
		int hole = (int) (Math.random() * 10 + 1);
		for (int row = field.length - 4; row < field.length - 1; row++)
		{
			for (int col = 0; col < field[0].length - 1; col++)
			{
				if (col != hole)
				{
					field[row][col] = BoardValue.GARBAGE;
				}
			}
		}

		target.drawBoard();
	}

	/**
	 * 시간이 걸리는 아이템(슬로우, 패스트, 블랙아웃)의 쿨타임 아이콘을 상대 화면 위쪽에 추가한다.
	 *
	 * @param target 쿨타임을 표시할 게임 화면
	 * @param icon   표시할 아이템 아이콘
	 */
	private void showCoolTime(GamePanel target, ImageIcon icon)
	{
		target.usingItemPanel.removeAll();
		target.coolTimeList.add(new CoolTimeLabel(icon, target));

		// 쿨타임 아이콘들을 왼쪽부터 30픽셀 간격으로 다시 놓는다
		for (int i = 0; i < target.coolTimeList.size(); i++)
		{
			target.usingItemPanel.add(target.coolTimeList.get(i));
			target.coolTimeList.get(i).setLocation(i * 30, 0);
		}
		target.usingItemPanel.repaint();
	}
}
