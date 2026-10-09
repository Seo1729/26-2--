import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

/**
 * 일반 테트리스(원본) 게임 화면.
 * 화면 구성, 블록 이동과 회전, 줄 삭제, 아이템, 점수, 블록 낙하 스레드를 담당한다.
 */
class GamePanel extends JPanel implements Runnable
{
	// ======================== 화면 구성 요소 ========================

	/** 블록이 그려지는 테트리스 영역(20줄 x 10칸) */
	JPanel tetrisArea = new JPanel();

	/** 테트리스 영역의 칸 하나하나를 그리는 라벨 */
	JLabel[][] cellLabel = new JLabel[20][10];

	/** 다음 블록 미리보기 */
	JLabel nextBlockLabel = new JLabel();

	/** 현재 점수 */
	JLabel scoreLabel = new JLabel();

	/** 최고 점수 */
	JLabel highScoreLabel = new JLabel();

	/** 경과 시간 */
	JLabel timeLabel = new JLabel();

	/** 가지고 있는 아이템을 보여 주는 칸(테트리스 영역 아래) */
	JPanel itemPanel = new JPanel();

	/** 사용 중인 아이템의 쿨타임을 보여 주는 칸(테트리스 영역 위) */
	JPanel usingItemPanel = new JPanel();

	/** 카운트다운, 게임 결과, 블랙아웃 때 테트리스 영역을 덮는 검은 패널 */
	JPanel coverPanel = new JPanel();

	/** 덮개 패널 위에 쓰는 글자(3, 2, 1, START!!, FAIL 등) */
	JLabel messageLabel = new JLabel();

	// ======================== 보드 데이터 ========================

	/** 지금 떨어지고 있는 블록과 벽이 들어 있는 배열(21줄 x 12칸) */
	int[][] blockArray = new int[BoardValue.ROWS][BoardValue.COLS];

	/** 바닥에 고정된 블록, 아이템 블록, 방해 블록이 들어 있는 배열(21줄 x 12칸) */
	int[][] fieldArray = new int[BoardValue.ROWS][BoardValue.COLS];

	/** 지금 떨어지고 있는 블록의 모양 */
	int[][] currentBlock;

	/** 지금 떨어지고 있는 블록의 종류(1~7: I, J, L, O, S, T, Z) */
	int blockType;

	/** 다음에 나올 블록의 종류 */
	int nextBlockType = (int) (Math.random() * 7 + 1);

	/** 블록의 왼쪽 위 칸의 열 위치 */
	int blockX;

	/** 블록의 왼쪽 위 칸의 행 위치 */
	int blockY;

	/** 블록의 가로 칸 수 */
	int blockWidth;

	/** 블록의 세로 칸 수 */
	int blockHeight;

	// ======================== 아이템 ========================

	/** 가지고 있는 아이템 아이콘 목록(최대 7개). 맨 앞의 것부터 쓴다. */
	Vector<JLabel> itemList = new Vector<JLabel>();

	/** 사용 중인 아이템의 쿨타임 아이콘 목록 */
	Vector<CoolTimeLabel> coolTimeList = new Vector<CoolTimeLabel>();

	/** 아이템 효과를 실행해 주는 객체 */
	ItemEffect itemEffect = new ItemEffect(this);

	/** 아이템 효과를 받을 게임 화면. 1인 모드에서는 자기 자신이다. */
	GamePanel targetPanel;

	// ======================== 게임 진행 ========================

	/** 블록을 일정 간격으로 떨어뜨리는 스레드. 블록이 새로 나올 때마다 새로 만든다. */
	Thread dropThread;

	/** 경과 시간을 재는 스레드 */
	TimeThread timeThread;

	/** 블록이 한 칸 떨어지는 간격(밀리초). 작을수록 빠르다. */
	int gameSpeed = 1000;

	/** 게임이 진행 중인지 여부. 게임오버가 되면 false가 된다. */
	boolean gameRunning = true;

	/** 시작 카운트다운이 끝났는지 여부. 끝나기 전에는 키 입력을 받지 않는다. */
	boolean started = false;

	/**
	 * 게임 화면을 만들고 첫 블록을 내보낸 뒤 3초 카운트다운을 시작한다.
	 */
	GamePanel()
	{
		setLayout(null);

		makeComponents();
		makeTetrisArea();
		makeBackground();

		resetWalls();
		addNewBlock();

		// 3, 2, 1, START!! 를 1초 간격으로 보여 준 뒤 게임을 시작한다
		Thread countdownThread = new Thread()
		{
			public void run()
			{
				int count = 3;

				while (count > -1)
				{
					if (count > 0)
					{
						messageLabel.setText(Integer.toString(count));
					}
					else
					{
						messageLabel.setText("START!!");
					}

					try
					{
						sleep(1000);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					coverPanel.repaint();
					count--;
				}

				// 덮개를 치우고 테트리스 영역을 보여 준다
				tetrisArea.setVisible(true);
				coverPanel.setVisible(false);

				// 기다리고 있던 시간 스레드와 낙하 스레드를 깨운다
				synchronized (timeThread)
				{
					timeThread.notify();
				}
				synchronized (dropThread)
				{
					dropThread.notify();
				}
				started = true;
			}
		};
		countdownThread.start();
	}

	// ======================== 화면 만들기 ========================

	/**
	 * 배경(하늘색, 카카오 튜브 그림, 아래쪽 땅 그림)을 만든다.
	 */
	public void makeBackground()
	{
		setOpaque(false);
		setBackground(new Color(30, 160, 255));

		JLabel tube = new JLabel(ImageSource.kakao_tube);
		tube.setBounds(240, 360, ImageSource.kakao_tube.getIconWidth(), ImageSource.kakao_tube.getIconHeight());
		add(tube);

		// 땅 그림은 화면 맨 아래에 붙인다
		JLabel ground = new JLabel(ImageSource.bg_ground);
		int groundHeight = ImageSource.bg_ground.getIconHeight();
		ground.setBounds(0, 640 - groundHeight, ImageSource.bg_ground.getIconWidth(), groundHeight);
		add(ground);
	}

	/**
	 * 테트리스 영역, 덮개, 정보 칸(다음 블록, 점수, 최고 점수, 시간), 아이템 칸을 만들고 시간 스레드를 시작한다.
	 */
	public void makeComponents()
	{
		LineBorder whiteBorder = new LineBorder(Color.WHITE);

		// 테트리스 영역은 카운트다운이 끝날 때까지 덮개로 가려 둔다
		tetrisArea = new JPanel();
		tetrisArea.setBounds(10, 60, 240, 480);
		tetrisArea.setBackground(Color.BLACK);
		coverPanel.setBounds(10, 60, 240, 480);
		coverPanel.setBackground(Color.BLACK);
		add(coverPanel);
		add(tetrisArea);
		tetrisArea.setVisible(false);

		// 다음 블록 미리보기
		nextBlockLabel = new JLabel(ImageSource.block_L);
		nextBlockLabel.setFont(new Font("verdana", Font.PLAIN, 12));
		nextBlockLabel.setForeground(Color.WHITE);
		nextBlockLabel.setBackground(Color.BLACK);
		nextBlockLabel.setOpaque(true);
		nextBlockLabel.setBorder(whiteBorder);
		nextBlockLabel.setBounds(255, 60, 90, 90);
		add(nextBlockLabel);

		// 점수, 최고 점수, 시간 칸은 모양이 같아서 같은 메서드로 만든다
		scoreLabel = makeInfoLabel("0", SwingConstants.RIGHT, 20, 160, whiteBorder);
		highScoreLabel = makeInfoLabel("999999", SwingConstants.RIGHT, 20, 200, whiteBorder);
		timeLabel = makeInfoLabel("time", SwingConstants.CENTER, 12, 240, whiteBorder);

		// 시간 스레드는 카운트다운이 끝날 때까지 기다리다가 시작한다
		timeThread = new TimeThread(this);
		timeThread.start();

		// 사용 중인 아이템 칸(위)과 가지고 있는 아이템 칸(아래)
		usingItemPanel = new JPanel(null);
		usingItemPanel.setBackground(Color.BLACK);
		usingItemPanel.setBorder(whiteBorder);
		usingItemPanel.setBounds(10, 25, 240, 30);
		add(usingItemPanel);

		itemPanel = new JPanel(null);
		itemPanel.setBackground(Color.BLACK);
		itemPanel.setBorder(whiteBorder);
		itemPanel.setBounds(10, 545, 240, 30);
		add(itemPanel);

		// 덮개 위의 글자는 가운데 정렬된 큰 흰 글씨로 쓴다
		coverPanel.setLayout(null);
		coverPanel.add(messageLabel);
		messageLabel.setBounds(20, 50, 200, 50);
		messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
		messageLabel.setFont(new Font("Verdana", Font.BOLD, 30));
		messageLabel.setOpaque(true);
		messageLabel.setBackground(new Color(255, 255, 255, 0));
		messageLabel.setForeground(Color.WHITE);
		messageLabel.setBorder(null);
	}

	/**
	 * 오른쪽 정보 칸(검은 바탕, 흰 글씨, 흰 테두리, 90x30) 하나를 만들어 화면에 붙인다.
	 *
	 * @param text      처음에 보여 줄 글자
	 * @param align     글자 정렬(SwingConstants.RIGHT 등)
	 * @param fontSize  글자 크기
	 * @param y         칸의 y좌표
	 * @param border    테두리
	 * @return 만들어진 정보 칸
	 */
	private JLabel makeInfoLabel(String text, int align, int fontSize, int y, LineBorder border)
	{
		JLabel label = new JLabel(text, align);
		label.setFont(new Font("verdana", Font.PLAIN, fontSize));
		label.setForeground(Color.WHITE);
		label.setBackground(Color.BLACK);
		label.setOpaque(true);
		label.setBorder(border);
		label.setBounds(255, y, 90, 30);
		add(label);
		return label;
	}

	/**
	 * 테트리스 영역을 20줄 x 10칸의 라벨로 채운다.
	 */
	public void makeTetrisArea()
	{
		tetrisArea.setLayout(new GridLayout(20, 10));

		// 칸마다 검은 바탕에 아주 어두운 회색 테두리를 둔다
		for (int i = 0; i < 20; i++)
		{
			for (int j = 0; j < 10; j++)
			{
				cellLabel[i][j] = new JLabel();
				tetrisArea.add(cellLabel[i][j]);
				cellLabel[i][j].setBackground(Color.BLACK);
				cellLabel[i][j].setOpaque(true);
				cellLabel[i][j].setBorder(new LineBorder(new Color(15, 15, 15)));
			}
		}
	}

	// ======================== 블록 만들기 ========================

	/**
	 * 블록 배열의 양옆과 바닥에 벽을 다시 그린다.
	 * 블록을 움직일 때 배열 전체를 한 칸씩 옮기기 때문에 벽도 같이 밀리므로, 움직인 뒤에 불러서 바로잡는다.
	 */
	public void resetWalls()
	{
		// 안쪽에 밀려 들어온 벽 값을 지운다
		for (int row = 0; row < blockArray.length - 1; row++)
		{
			for (int col = 1; col < blockArray[0].length - 1; col++)
			{
				if (blockArray[row][col] == BoardValue.WALL)
				{
					blockArray[row][col] = BoardValue.EMPTY;
				}
			}
		}

		// 양옆 벽
		for (int row = 0; row < blockArray.length; row++)
		{
			blockArray[row][0] = BoardValue.WALL;
			blockArray[row][blockArray[0].length - 1] = BoardValue.WALL;
		}

		// 바닥 벽
		for (int col = 0; col < blockArray[0].length; col++)
		{
			blockArray[blockArray.length - 1][col] = BoardValue.WALL;
		}
	}

	/**
	 * 새 블록을 맨 위에 내보내고 낙하 스레드를 새로 시작한다.
	 * 새 블록이 나올 자리가 이미 막혀 있으면 게임오버다.
	 */
	public void addNewBlock()
	{
		Blocks newBlock = new Blocks();
		pickNextBlock(newBlock);

		currentBlock = newBlock.getBlock();
		blockType = newBlock.getBlockNum();
		blockX = newBlock.getStart_x();
		blockY = newBlock.getStart_y();
		blockWidth = currentBlock[0].length;
		blockHeight = currentBlock.length;

		// 블록 모양을 블록 배열의 시작 위치에 옮겨 적는다
		for (int row = 0, h = blockY; row < blockHeight - blockY; row++, h++)
		{
			for (int col = blockX, w = 0; col < blockWidth + blockX; col++, w++)
			{
				if (currentBlock[h][w] > 0)
				{
					blockArray[row][col] = currentBlock[h][w];
				}
			}
		}

		// 새 블록이 고정 블록과 겹치는 칸을 센다
		int overlap = 0;
		for (int row = blockY; row < blockY + blockHeight; row++)
		{
			for (int col = blockX; col < blockX + blockWidth; col++)
			{
				if (blockArray[row][col] > 0 && fieldArray[row][col] > 0)
				{
					overlap++;
				}
			}
		}

		if (overlap > 0)
		{
			// 갤러그 협동 모드는 창을 바로 닫지 않고 두 사람의 패배 결과를 보여 준다
			if (GameFrame.gameMode == 3)
			{
				endGame("GAME OVER");
				return;
			}

			// 일반 모드는 원본 그대로 게임을 멈추고 프로그램을 끝낸다
			gameRunning = false;
			dropThread.interrupt();
			timeThread.interrupt();
			messageLabel.setText("FAIL");
			messageLabel.setVisible(true);
			coverPanel.setVisible(true);
			tetrisArea.setVisible(false);

			System.exit(0);
		}
		else
		{
			// 새 블록을 그리고 낙하 스레드를 시작한다
			drawBoard();
			resetWalls();
			dropThread = new Thread(this);
			dropThread.start();
		}
	}

	/**
	 * 미리 뽑아 둔 블록을 이번 블록으로 넘겨주고, 다음 블록을 새로 뽑아 미리보기에 보여 준다.
	 *
	 * @param newBlock 이번에 나올 블록 정보를 채울 객체
	 */
	public void pickNextBlock(Blocks newBlock)
	{
		newBlock.blockNum = nextBlockType;
		nextBlockType = (int) (Math.random() * 7 + 1);

		// 블록 종류마다 미리보기 그림이 다르다
		switch (nextBlockType)
		{
		case 1:
			nextBlockLabel.setIcon(ImageSource.block_I);
			break;
		case 2:
			nextBlockLabel.setIcon(ImageSource.block_J);
			break;
		case 3:
			nextBlockLabel.setIcon(ImageSource.block_L);
			break;
		case 4:
			nextBlockLabel.setIcon(ImageSource.block_O);
			break;
		case 5:
			nextBlockLabel.setIcon(ImageSource.block_S);
			break;
		case 6:
			nextBlockLabel.setIcon(ImageSource.block_T);
			break;
		case 7:
			nextBlockLabel.setIcon(ImageSource.block_Z);
			break;
		}
	}

	// ======================== 그리기 ========================

	/**
	 * 블록 배열과 고정 블록 배열을 보고 테트리스 영역의 모든 칸 그림을 다시 정한다.
	 */
	public void drawBoard()
	{
		for (int row = 0; row < cellLabel.length; row++)
		{
			for (int col = 0; col < cellLabel[0].length; col++)
			{
				// 배열은 왼쪽 벽 때문에 화면보다 한 칸 오른쪽에 있다
				int moving = blockArray[row][col + 1];
				int fixed = fieldArray[row][col + 1];

				if (moving > 0 && moving < 8)
				{
					// 떨어지고 있는 블록(1~7)
					cellLabel[row][col].setIcon(getBlockColorIcon(moving));
				}
				else if (fixed >= BoardValue.ITEM_START && fixed < BoardValue.FIXED_START)
				{
					// 아이템 블록(80~89)
					cellLabel[row][col].setIcon(getItemBlockIcon(fixed - BoardValue.ITEM_START));
				}
				else if (fixed >= BoardValue.FIXED_START && fixed < BoardValue.GARBAGE)
				{
					// 고정된 블록(91~97)
					cellLabel[row][col].setIcon(getBlockColorIcon(fixed - BoardValue.FIXED_START));
				}
				else if (fixed >= BoardValue.GARBAGE)
				{
					// 회색 방해 블록(100)
					cellLabel[row][col].setIcon(ImageSource.block_gray);
				}
				else
				{
					cellLabel[row][col].setIcon(null);
				}
			}
		}
	}

	/**
	 * 블록 종류에 맞는 색 칸 그림을 돌려준다.
	 * I: 빨강, J: 라임, L: 주황, O: 보라, S: 하늘, T: 파랑, Z: 초록
	 *
	 * @param type 블록 종류(1~7)
	 * @return 색 칸 그림. 종류가 잘못되면 null
	 */
	private ImageIcon getBlockColorIcon(int type)
	{
		switch (type)
		{
		case 1:
			return ImageSource.block_red;
		case 2:
			return ImageSource.block_lime;
		case 3:
			return ImageSource.block_orange;
		case 4:
			return ImageSource.block_puple;
		case 5:
			return ImageSource.block_cyan;
		case 6:
			return ImageSource.block_blue;
		case 7:
			return ImageSource.block_green;
		}
		return null;
	}

	/**
	 * 보드 위에 놓인 아이템 블록의 그림을 돌려준다.
	 *
	 * @param itemNumber 아이템 번호(0~9)
	 * @return 아이템 블록 그림. 번호가 잘못되면 null
	 */
	private ImageIcon getItemBlockIcon(int itemNumber)
	{
		switch (itemNumber)
		{
		case 0:
			return ImageSource.block_blackout;
		case 1:
			return ImageSource.block_fast;
		case 2:
			return ImageSource.block_lineup_1;
		case 3:
			return ImageSource.block_lineup_3;
		case 4:
			return ImageSource.block_zigzag;
		case 5:
			return ImageSource.block_bomb;
		case 6:
			return ImageSource.block_change;
		case 7:
			return ImageSource.block_linedown_1;
		case 8:
			return ImageSource.block_linedown_3;
		case 9:
			return ImageSource.block_slow;
		}
		return null;
	}

	/**
	 * 아이템 칸에 넣을 아이템 아이콘을 만든다. 아이템 번호는 라벨 이름에 적어 둔다.
	 *
	 * @param itemNumber 아이템 번호(0~9)
	 * @return 아이템 아이콘 라벨
	 */
	private JLabel makeItemLabel(int itemNumber)
	{
		JLabel label = new JLabel();
		label.setName(Integer.toString(itemNumber));

		// 아이템 번호마다 그림이 다르다
		switch (itemNumber)
		{
		case 0:
			label.setIcon(ImageSource.item_blackout);
			break;
		case 1:
			label.setIcon(ImageSource.item_fast);
			break;
		case 2:
			label.setIcon(ImageSource.item_lineup_1);
			break;
		case 3:
			label.setIcon(ImageSource.item_lineup_3);
			break;
		case 4:
			label.setIcon(ImageSource.item_zigzag);
			break;
		case 5:
			label.setIcon(ImageSource.item_bomb);
			break;
		case 6:
			label.setIcon(ImageSource.item_change);
			break;
		case 7:
			label.setIcon(ImageSource.item_linedown_1);
			break;
		case 8:
			label.setIcon(ImageSource.item_linedown_3);
			break;
		case 9:
			label.setIcon(ImageSource.item_slow);
			break;
		}
		return label;
	}

	/**
	 * 회전한 블록을 블록 배열에 다시 적는다. 오른쪽 벽을 넘으면 왼쪽으로 한 칸 민다.
	 */
	public void drawTurnedBlock()
	{
		// 회전 전 모양(가로세로가 바뀐 크기)이 있던 자리를 지운다
		for (int row = blockY, h = 0; h < blockWidth; row++, h++)
		{
			for (int col = blockX, w = 0; w < blockHeight; col++, w++)
			{
				blockArray[row][col] = BoardValue.EMPTY;
			}
		}

		// 회전한 모양을 적는다
		for (int row = blockY, h = 0; h < blockHeight; row++, h++)
		{
			for (int col = blockX, w = 0; w < blockWidth; col++, w++)
			{
				if (currentBlock[h][w] > 0)
				{
					blockArray[row][col] = currentBlock[h][w];
				}
			}
		}

		// 오른쪽 벽 밖으로 나간 부분이 잘리지 않게 왼쪽으로 민다
		if (blockX + blockWidth > blockArray[0].length - 1)
		{
			moveLeft();
		}
	}

	// ======================== 블록 고정과 줄 삭제 ========================

	/**
	 * 바닥에 닿은 블록을 고정 블록 배열로 옮기고(90 + 블록 종류), 꽉 찬 줄이 있는지 검사한다.
	 */
	public void fixBlock()
	{
		for (int row = 0; row < blockArray.length - 1; row++)
		{
			for (int col = 1; col < blockArray[0].length - 1; col++)
			{
				if (blockArray[row][col] != BoardValue.EMPTY)
				{
					fieldArray[row][col] = blockArray[row][col] + BoardValue.FIXED_START;
				}
				blockArray[row][col] = BoardValue.EMPTY;
			}
		}
		checkFullLines();
	}

	/**
	 * 방금 고정된 블록이 걸친 줄들 중에서 꽉 찬 줄을 지운다.
	 * 지운 줄에 아이템 블록이 있었으면 아이템을 얻고, 지운 줄 수만큼 점수를 더한다.
	 */
	public void checkFullLines()
	{
		int deleteCount = 0;

		for (int i = blockY; i < blockY + currentBlock.length; i++)
		{
			// 이 줄에 블록이 몇 칸 있는지 센다
			int count = 0;
			for (int j = 1; j < fieldArray[0].length - 1; j++)
			{
				if (fieldArray[i][j] != BoardValue.EMPTY)
				{
					count++;
				}
			}

			// 10칸이 다 차 있으면 지운다
			if (count == 10)
			{
				// 지울 줄에 있던 아이템 블록은 아이템 칸으로 옮긴다(최대 7개)
				for (int j = 1; j < fieldArray[0].length; j++)
				{
					int value = fieldArray[i][j];
					if (value >= BoardValue.ITEM_START && value < BoardValue.FIXED_START && itemList.size() < 7)
					{
						itemList.add(makeItemLabel(value - BoardValue.ITEM_START));
						arrangeItemPanel();
					}
				}

				deleteLine(i);
				deleteCount++;
			}
		}

		if (deleteCount > 0)
		{
			addRandomItems(deleteCount);
			addScore(deleteCount);
		}
	}

	/**
	 * 지정한 줄을 지우고 그 위의 줄들을 한 칸씩 내린다.
	 *
	 * @param lineNumber 지울 줄 번호
	 */
	public void deleteLine(int lineNumber)
	{
		int[][] copy = new int[fieldArray.length][fieldArray[0].length];

		// 지울 줄보다 위는 한 칸 내리고, 아래는 그대로 둔다
		for (int row = 0; row < fieldArray.length - 1; row++)
		{
			for (int col = 0; col < fieldArray[0].length; col++)
			{
				if (row < lineNumber)
				{
					copy[row + 1][col] = fieldArray[row][col];
				}
				else
				{
					copy[row + 1][col] = fieldArray[row + 1][col];
				}
			}
		}

		copyArray(fieldArray, copy);
	}

	/**
	 * 줄을 지울 때마다 고정 블록 몇 개를 무작위로 아이템 블록으로 바꾼다(한 번에 최대 2개).
	 *
	 * @param lineCount 지운 줄 수. 많이 지울수록 아이템이 생길 기회가 많다.
	 */
	public void addRandomItems(int lineCount)
	{
		int itemCount = 0;

		for (int i = 0; i < fieldArray.length; i++)
		{
			for (int j = 1; j < fieldArray[0].length - 1; j++)
			{
				// 고정 블록(90보다 큰 값)만 아이템이 될 수 있다
				if (fieldArray[i][j] > BoardValue.FIXED_START)
				{
					for (int k = 1; k <= lineCount; k++)
					{
						// 20분의 1 확률로 아이템 블록(80~89)으로 바꾼다
						int chance = (int) (Math.random() * 20);
						if (chance == 1)
						{
							fieldArray[i][j] = (int) (Math.random() * 10) + BoardValue.ITEM_START;
							itemCount++;
						}
						if (itemCount == 2)
						{
							return;
						}
					}
				}
			}
		}
	}

	/**
	 * 지운 줄 수에 따라 점수를 더한다. 블록이 빨리 떨어질수록 점수가 커진다.
	 *
	 * @param lineCount 한 번에 지운 줄 수(1~4)
	 */
	public void addScore(int lineCount)
	{
		int score = Integer.parseInt(scoreLabel.getText());

		// 한 번에 많이 지울수록 점수가 크다
		if (lineCount == 4)
		{
			score = score + (120 * 1000 / gameSpeed);
		}
		else if (lineCount == 3)
		{
			score = score + (70 * 1000 / gameSpeed);
		}
		else if (lineCount == 2)
		{
			score = score + (30 * 1000 / gameSpeed);
		}
		else
		{
			score = score + (10 * 1000 / gameSpeed);
		}

		scoreLabel.setText(Integer.toString(score));
	}

	// ======================== 아이템 사용 ========================

	/**
	 * 아이템 칸 맨 앞의 아이템을 대상 화면에 사용하고, 그 아이템을 목록에서 뺀다.
	 */
	public void useItem()
	{
		// 아이템이 없거나 대상이 정해지지 않았으면 아무것도 하지 않는다
		if (itemList.isEmpty() || targetPanel == null)
		{
			return;
		}

		// 라벨 이름에 적어 둔 아이템 번호로 효과를 고른다
		String itemNumber = itemList.get(0).getName();
		switch (itemNumber)
		{
		case "0":
			itemEffect.blackout(targetPanel);
			break;
		case "1":
			itemEffect.fast(targetPanel);
			break;
		case "2":
			itemEffect.oneLineUp(targetPanel);
			break;
		case "3":
			itemEffect.threeLineUp(targetPanel);
			break;
		case "4":
			itemEffect.zigzag(targetPanel);
			break;
		case "5":
			itemEffect.bomb(targetPanel);
			break;
		case "6":
			itemEffect.change(targetPanel);
			break;
		case "7":
			itemEffect.oneLineDown(targetPanel);
			break;
		case "8":
			itemEffect.threeLineDown(targetPanel);
			break;
		case "9":
			itemEffect.slow(targetPanel);
			break;
		}
		removeFirstItem();
	}

	/**
	 * 상대에게 아이템을 쓴다. 1인 모드에서는 상대가 없으므로 useItem()과 같다.
	 */
	public void attackItem()
	{
		useItem();
	}

	/**
	 * 아이템 칸 맨 앞의 아이템을 버린다.
	 */
	public void removeFirstItem()
	{
		if (itemList.size() > 0)
		{
			itemList.remove(0);
			arrangeItemPanel();
		}
	}

	/**
	 * 아이템 칸을 비우고 아이템 목록을 왼쪽부터 30픽셀 간격으로 다시 놓는다.
	 */
	private void arrangeItemPanel()
	{
		itemPanel.removeAll();
		itemPanel.repaint();
		for (int i = 0; i < itemList.size(); i++)
		{
			itemPanel.add(itemList.get(i));
			itemList.get(i).setBounds(i * 30, 0, 30, 30);
		}
	}

	// ======================== 블록 움직이기 ========================

	/**
	 * 블록을 왼쪽으로 한 칸 옮긴다. 벽이나 고정 블록에 막히면 그대로 둔다.
	 */
	public void moveLeft()
	{
		// 배열 전체를 왼쪽으로 한 칸 민 복사본을 만든다
		int[][] copy = new int[blockArray.length][blockArray[0].length];
		for (int row = 0; row < blockArray.length; row++)
		{
			for (int col = 1; col < blockArray[0].length; col++)
			{
				copy[row][col - 1] = blockArray[row][col];
			}
		}

		// 옮긴 자리에 고정 블록이 있는지 센다
		int count = 0;
		for (int row = blockY; row < blockY + blockHeight; row++)
		{
			for (int col = blockX - 1; col < blockX + blockWidth; col++)
			{
				if (copy[row][col] > 0 && fieldArray[row][col] > 0)
				{
					count++;
				}
			}
		}

		// 막히지 않았고 왼쪽 벽을 넘지 않을 때만 옮긴다
		if (count == 0 && blockX > 1)
		{
			blockX--;
			copyArray(blockArray, copy);
		}
	}

	/**
	 * 블록을 오른쪽으로 한 칸 옮긴다. 벽이나 고정 블록에 막히면 그대로 둔다.
	 */
	public void moveRight()
	{
		// 배열 전체를 오른쪽으로 한 칸 민 복사본을 만든다
		int[][] copy = new int[blockArray.length][blockArray[0].length];
		for (int row = 0; row < blockArray.length; row++)
		{
			for (int col = 0; col < blockArray[0].length - 1; col++)
			{
				copy[row][col + 1] = blockArray[row][col];
			}
		}

		// 옮긴 자리에 고정 블록이 있는지 센다
		int count = 0;
		for (int row = blockY; row < blockY + blockHeight; row++)
		{
			for (int col = blockX; col < blockX + blockWidth + 1; col++)
			{
				if (copy[row][col] > 0 && fieldArray[row][col] > 0)
				{
					count++;
				}
			}
		}

		// 막히지 않았고 오른쪽 벽을 넘지 않을 때만 옮긴다
		if (count == 0 && blockX < blockArray[0].length - blockWidth - 1)
		{
			blockX++;
			copyArray(blockArray, copy);
		}
	}

	/**
	 * 블록을 아래로 한 칸 내린다. 더 내려갈 수 없으면 고정하고 새 블록을 내보낸다.
	 */
	public void moveDown()
	{
		// 배열 전체를 아래로 한 칸 민 복사본을 만든다
		int[][] copy = new int[blockArray.length][blockArray[0].length];
		for (int row = 0; row < blockArray.length - 1; row++)
		{
			for (int col = 0; col < blockArray[0].length; col++)
			{
				copy[row + 1][col] = blockArray[row][col];
			}
		}

		// 내린 자리에 고정 블록이 있는지 센다
		int count = 0;
		for (int row = blockY; row < blockY + blockHeight + 1; row++)
		{
			for (int col = blockX; col < blockX + blockWidth; col++)
			{
				if (copy[row][col] > 0 && fieldArray[row][col] > 0)
				{
					count++;
				}
			}
		}

		if (count > 0)
		{
			// 고정 블록에 닿았다
			dropThread.interrupt();
			fixBlock();
			addNewBlock();
		}
		else if (blockY < (blockArray.length - 1) - blockHeight)
		{
			// 아직 내려갈 수 있다
			blockY++;
			copyArray(blockArray, copy);
		}
		else
		{
			// 바닥에 닿았다
			dropThread.interrupt();
			fixBlock();
			addNewBlock();
		}
	}

	/**
	 * 블록을 바닥(또는 고정 블록 위)까지 한 번에 떨어뜨린다.
	 */
	public void hardDrop()
	{
		// 더 내려갈 수 없을 때까지 한 칸씩 내린다
		while (true)
		{
			int[][] copy = new int[blockArray.length][blockArray[0].length];
			for (int row = 0; row < blockArray.length - 1; row++)
			{
				for (int col = 0; col < blockArray[0].length; col++)
				{
					copy[row + 1][col] = blockArray[row][col];
				}
			}

			int count = 0;
			for (int row = blockY; row < blockY + blockHeight + 1; row++)
			{
				for (int col = blockX; col < blockX + blockWidth + 1; col++)
				{
					if (copy[row][col] > 0 && fieldArray[row][col] > 0)
					{
						count++;
					}
				}
			}

			if (count > 0)
			{
				// 고정 블록에 닿았다
				dropThread.interrupt();
				fixBlock();
				addNewBlock();
				break;
			}
			else if (blockY < (blockArray.length - 1) - blockHeight)
			{
				blockY++;
				copyArray(blockArray, copy);
			}
			else
			{
				// 바닥에 닿았다
				dropThread.interrupt();
				fixBlock();
				addNewBlock();
				break;
			}
		}
	}

	/**
	 * 블록을 시계 방향으로 90도 돌린다. 돌린 자리가 막혀 있으면 돌리지 않는다.
	 */
	public void rotate()
	{
		// 돌린 블록이 보드 밖으로 나가면 돌리지 않는다
		if (blockX < 1 || blockY < 0 || blockX + blockHeight > blockArray[0].length - 1
				|| blockY + blockWidth > blockArray.length - 1)
		{
			return;
		}

		// 가로세로를 바꾼 새 배열에 돌린 모양을 만든다
		int[][] turned = new int[blockWidth][blockHeight];
		for (int i = 0; i < blockWidth; i++)
		{
			for (int j = 0; j < blockHeight; j++)
			{
				turned[i][j] = currentBlock[j][(blockWidth - 1) - i];
			}
		}

		// I 블록은 오른쪽 끝에서 돌리지 않는다
		int count = 0;
		if (blockType == 1 && blockX >= 9)
		{
			count++;
		}

		// 돌린 모양이 고정 블록과 겹치는지 센다
		if (count == 0)
		{
			for (int row = blockY, i = 0; i < blockWidth; row++, i++)
			{
				for (int col = blockX, j = 0; j < blockHeight; col++, j++)
				{
					if (turned[i][j] > 0 && fieldArray[row][col] > 0)
					{
						count++;
					}
				}
			}
		}

		// 겹치지 않으면 크기와 모양을 바꾸고 다시 그린다
		if (count == 0)
		{
			int temp = blockHeight;
			blockHeight = blockWidth;
			blockWidth = temp;
			currentBlock = turned;
			drawTurnedBlock();
		}
	}

	/**
	 * 복사본 배열의 값을 원래 배열에 그대로 옮겨 적는다.
	 *
	 * @param original 값을 받을 배열
	 * @param copy     옮길 값이 들어 있는 배열
	 */
	public void copyArray(int[][] original, int[][] copy)
	{
		for (int row = 0; row < original.length; row++)
		{
			for (int col = 0; col < original[0].length; col++)
			{
				original[row][col] = copy[row][col];
			}
		}
	}

	// ======================== 낙하 스레드 ========================

	/**
	 * 블록을 gameSpeed 간격으로 한 칸씩 떨어뜨린다.
	 * 첫 블록은 카운트다운이 끝날 때까지 기다렸다가 시작한다.
	 */
	public void run()
	{
		if (!started)
		{
			// 카운트다운 스레드가 깨워 줄 때까지 기다린다
			synchronized (dropThread)
			{
				try
				{
					dropThread.wait();
					while (gameRunning)
					{
						try
						{
							Thread.sleep(gameSpeed);
						}
						catch (InterruptedException e)
						{
							return;
						}
						moveDown();
						drawBoard();
					}
				}
				catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
		else
		{
			while (gameRunning)
			{
				try
				{
					Thread.sleep(gameSpeed);
				}
				catch (InterruptedException e)
				{
					return;
				}
				moveDown();
				drawBoard();
			}
		}
	}

	// ======================== 갤러그 협동 모드에서 쓰는 메서드 ========================

	/**
	 * 갤러그 협동 모드에서 외계인이 착지할 수 있는지 판단할 때 쓴다.
	 * 지정한 칸에 고정된 블록이 있거나 그 칸이 바닥 아래인지 알려 준다.
	 *
	 * @param row 화면 칸 기준 행(0이 맨 위)
	 * @param col 화면 칸 기준 열(0이 맨 왼쪽)
	 * @return 고정 블록이 있거나 바닥 아래면 true
	 */
	public boolean isBlocked(int row, int col)
	{
		// 맨 아래 줄보다 아래는 바닥이므로 막힌 것으로 본다
		if (row >= cellLabel.length)
		{
			return true;
		}
		// 배열은 왼쪽 벽 때문에 화면 칸보다 열이 한 칸 밀려 있다
		return fieldArray[row][col + 1] > 0;
	}

	/**
	 * 갤러그 협동 모드에서 착지한 외계인을 회색 방해 블록으로 바꿀 때 쓴다.
	 * 지정한 칸이 비어 있을 때만 방해 블록을 놓고 화면을 다시 그린다.
	 *
	 * @param row 화면 칸 기준 행(0이 맨 위)
	 * @param col 화면 칸 기준 열(0이 맨 왼쪽)
	 */
	public void placeGarbage(int row, int col)
	{
		// 고정 블록이나 떨어지는 중인 블록과 겹치면 놓지 않는다(블록이 덮어써지는 것을 막기 위해)
		if (fieldArray[row][col + 1] == BoardValue.EMPTY && blockArray[row][col + 1] == BoardValue.EMPTY)
		{
			fieldArray[row][col + 1] = BoardValue.GARBAGE;
			drawBoard();
		}
	}

	/**
	 * 갤러그 협동 모드에서 게임을 끝내고 결과 메시지를 보여 준다.
	 * 블록 낙하와 시간 표시를 멈추고, 테트리스 영역을 가린 검은 패널에 메시지를 띄운다.
	 *
	 * @param message 화면에 띄울 결과 문구(예: "CLEAR", "GAME OVER")
	 */
	public void endGame(String message)
	{
		// 블록 낙하 스레드와 시간 스레드를 멈춘다
		gameRunning = false;
		dropThread.interrupt();
		timeThread.interrupt();

		// 테트리스 영역을 가리고 시작 카운트다운에 쓰던 검은 패널에 결과를 띄운다
		messageLabel.setText(message);
		messageLabel.setVisible(true);
		coverPanel.setVisible(true);
		tetrisArea.setVisible(false);
	}

	/**
	 * 갤러그 협동 모드에서 갤러그 쪽도 멈춰야 하는지 판단할 때 쓴다.
	 *
	 * @return 게임이 아직 진행 중이면 true, 끝났으면 false
	 */
	public boolean isRunning()
	{
		return gameRunning;
	}
}
