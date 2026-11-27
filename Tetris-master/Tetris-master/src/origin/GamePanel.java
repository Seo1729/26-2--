package origin;

import java.awt.Color;
import java.awt.Font;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;

import common.KeyAction;
import common.KeySettings;
import common.Sound;
import common.SoundManager;

import galag.GalagaTarget;

/**
 * 2D 일반·갤러그 모드의 게임 화면과 진행.
 *
 * 전에는 이 클래스 하나(약 1,500줄)가 보드 규칙, 칸 그리기, 점수, 아이템 10종, 쿨타임 그림, 랭킹 창까지 모두 맡았다
 * (Large Class, SRP 위반). 이제 일을 나눠 맡긴다.
 * - TetrisBoard : 보드 데이터와 이동·충돌·줄 삭제 규칙
 * - BoardView : 보드 칸 그리기
 * - ScoreKeeper : 점수 계산과 최고 점수
 * - ItemInventory, ItemType, ItemEffect : 아이템 보관과 효과
 * - CooldownLabel : 쿨타임 그림
 * 이 클래스는 그 부품들을 이어 붙여 "블록 등장 → 낙하 → 고정 → 줄 삭제 → 다음 블록" 흐름과 화면 배치를 맡는다.
 */
class GamePanel extends JPanel implements Runnable, GalagaTarget
{
	static final int MIN_SPEED = 100; // 레벨이 올라도 낙하 간격은 이보다 짧아지지 않는다(ms)

	private static final String INFO_FONT = "verdana"; // 오른쪽 정보 칸 글꼴
	private static final int COOLDOWN_SLOT = 30; // 쿨타임 그림 한 칸 너비(px)
	private static final int COUNTDOWN_FROM = 3; // 시작 전 카운트다운 숫자

	JPanel tetrisArea;
	JLabel nextBlock;
	JLabel holdSlot = new JLabel("HOLD (" + KeySettings.getKeyName(KeyAction.TETRIS_HOLD) + ")", SwingConstants.CENTER);
	transient HoldManager holdManager = new HoldManager();
	JLabel score;
	JLabel highestScore;
	JPanel item;
	JPanel itemUsing;
	JLabel time;
	JLabel levelLabel; // 지금 레벨 표시
	transient LevelManager levelManager; // 레벨(1~5)과 레벨별 속도
	private LineBorder whiteBorder; // 오른쪽 정보 칸과 아이템 칸의 흰 테두리

	transient Thread th; // 지금 블록을 내리는 낙하 스레드 (블록마다 새로 만든다)
	transient TimeThread timeTh;
	final transient StartGate startGate = new StartGate(); // 카운트다운이 끝나면 열리는 출발 신호
	int gameSpeed = 1000; // 낙하 간격(ms). 시간 스레드와 쿨타임 스레드도 바꾼다

	JLabel textLabel = new JLabel();
	JPanel blackPanel = new JPanel();
	boolean gaming = false; // 카운트다운이 끝나 게임이 진행 중이면 true
	GameMode mode; // 일반 또는 갤러그

	transient GamePanel itemTarget; // 아이템을 쓸 대상 판. 내게 쓰기를 누르면 자기 자신이 되고, 그 전에는 비어 있다(null)
	transient HpSystem hp; // 일반 모드 생명(HP) - 규칙과 표시는 HpSystem.java

	transient TetrisBoard board = new TetrisBoard();
	private transient BoardView boardView;
	transient ScoreKeeper scoreKeeper;
	private transient ItemInventory inventory;
	// 쿨타임 스레드와 화면 스레드가 함께 건드리므로 동기화되는 Vector를 그대로 쓴다
	private final Vector<CooldownLabel> cooldowns = new Vector<>();

	int preBlock = BlockType.randomNumber(); // 다음에 나올 블록 번호 (미리보기)
	boolean gameRun = true;
	volatile boolean paused = false; // 설정 화면을 보는 동안 true (낙하, 시간, 갤러그가 멈춤)

	// 모드와 시작 레벨(1~5)을 정해서 만든다
	GamePanel(GameMode mode, int startLevel)
	{
		this.mode = mode;
		setLayout(null);

		// 시작 레벨의 속도로 떨어지게 한다
		levelManager = new LevelManager(startLevel);
		gameSpeed = levelManager.getSpeed();

		makeComponent();
		hp = HpSystem.forMode(mode.getNumber());
		HpView hpView = new HpView(hp);
		hpView.setBorder(whiteBorder);
		hpView.setBounds(255, 280, 90, 30); // 시간 칸 아래
		add(hpView);
		boardView = new BoardView(tetrisArea);
		makeBackground();

		setArray(); // 데이터 배열 테두리 초기화

		addBlock(); // 블럭추가

		startCountdown();
	}

	/******************** 화면 구성 ********************/

	public void makeBackground()
	{
		// 배경은 단색(하늘색)만 쓴다. 투명으로 두면 창의 배경색이 그대로 보인다.
		setOpaque(false);
		setBackground(new Color(30, 160, 255));
	}

	// 보드, 오른쪽 정보 칸들, 아이템 칸, 카운트다운 글자를 만들어 붙인다
	public void makeComponent()
	{
		whiteBorder = new LineBorder(Color.WHITE);

		tetrisArea = new JPanel();
		tetrisArea.setBounds(10, 60, 240, 480);
		tetrisArea.setBackground(Color.BLACK);
		blackPanel.setBounds(10, 60, 240, 480);
		blackPanel.setBackground(Color.BLACK);
		add(blackPanel);
		add(tetrisArea);
		tetrisArea.setVisible(false);

		nextBlock = new JLabel(ImageSource.BLOCK_L);
		addInfoLabel(nextBlock, Font.PLAIN, 12, 60, 90);

		holdSlot.setVerticalTextPosition(SwingConstants.TOP);
		holdSlot.setHorizontalTextPosition(SwingConstants.CENTER);
		addInfoLabel(holdSlot, Font.PLAIN, 12, 320, 110);

		score = new JLabel("0", SwingConstants.RIGHT);
		addInfoLabel(score, Font.PLAIN, 20, 160, 30);

		scoreKeeper = new ScoreKeeper(RankingManager.getBestScore()); // 저장된 랭킹 1위 점수
		highestScore = new JLabel(Integer.toString(scoreKeeper.getBest()), SwingConstants.RIGHT);
		addInfoLabel(highestScore, Font.PLAIN, 20, 200, 30);

		time = new JLabel("time", SwingConstants.CENTER);
		addInfoLabel(time, Font.PLAIN, 12, 240, 30);

		levelLabel = new JLabel("LV " + levelManager.getLevel(), SwingConstants.CENTER);
		addInfoLabel(levelLabel, Font.BOLD, 16, 440, 30);
		timeTh = new TimeThread(this);
		timeTh.start();

		itemUsing = makeItemBox(25);
		item = makeItemBox(545);
		inventory = new ItemInventory(item);

		blackPanel.setLayout(null);
		blackPanel.add(textLabel);
		textLabel.setBounds(20, 50, 200, 50);
		textLabel.setHorizontalAlignment(SwingConstants.CENTER);
		textLabel.setFont(new Font("Verdana", Font.BOLD, 30));
		textLabel.setOpaque(true);
		textLabel.setBackground(new Color(255, 255, 255, 0));
		textLabel.setForeground(Color.WHITE);
		textLabel.setBorder(null);
	}

	// 오른쪽 정보 칸 하나(검은 바탕, 흰 글자, 흰 테두리)를 x = 255 자리에 붙인다
	private void addInfoLabel(JLabel label, int fontStyle, int fontSize, int top, int height)
	{
		label.setFont(new Font(INFO_FONT, fontStyle, fontSize));
		label.setForeground(Color.WHITE);
		label.setBackground(Color.BLACK);
		label.setOpaque(true);
		label.setBorder(whiteBorder);
		label.setBounds(255, top, 90, height);
		add(label);
	}

	// 보드 위·아래의 아이템 칸(검은 줄)을 만든다
	private JPanel makeItemBox(int top)
	{
		JPanel box = new JPanel(null);
		box.setBackground(Color.BLACK);
		box.setBorder(whiteBorder);
		box.setBounds(10, top, 240, 30);
		add(box);
		return box;
	}

	// 3, 2, 1, START!! 를 1초마다 보여 준 뒤 보드를 보이고 낙하·시간 스레드를 깨운다
	private void startCountdown()
	{
		Thread textTh = new Thread()
		{
			@Override
			public void run()
			{
				int textCount = COUNTDOWN_FROM;

				while (textCount > -1)
				{
					if (textCount > 0)
						textLabel.setText(Integer.toString(textCount));
					else
						textLabel.setText("START!!");
					try
					{
						sleep(1000);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					blackPanel.repaint();
					textCount--;
				}
				tetrisArea.setVisible(true);
				blackPanel.setVisible(false);

				startGate.open();
				gaming = true;
			}
		};
		textTh.start();
	}

	/******************** 보드 ********************/

	TetrisBoard getBoard()
	{
		return board;
	}

	// 보드 테두리(벽) 값을 다시 채운다
	public void setArray()
	{
		board.resetWalls();
	}

	// 보드를 화면에 그린다 (테스트는 이 메서드를 비워 화면 없이 돌린다)
	public void drawTetris()
	{
		boardView.render(board);
	}

	// 이동 뒤 보드 테두리를 다시 맞춘다 (테스트가 재정의한다)
	public void checkArray()
	{
		setArray();
	}

	/******************** 블록 등장과 고정 ********************/

	public synchronized void addBlock()
	{
		spawnBlock(BlockType.fromNumber(takeNextBlock()), true);
	}

	public synchronized void holdBlock()
	{
		if (!gameRun || !gaming || !holdManager.canHold()) return;
		int replacement = holdManager.hold(board.blockNum);
		holdSlot.setIcon(BlockType.fromNumber(holdManager.getHeldBlock()).getPreviewIcon());
		// Only clear the falling piece; fixed cells and board boundaries stay intact.
		board.clearFallingBlock();
		// 홀드 칸이 비어 있었으면 다음 블록을, 아니면 보관해 둔 블록을 꺼낸다
		int type = replacement;
		if (replacement == 0)
			type = takeNextBlock();
		// Keep the existing fall thread and its timing on a hold.
		spawnBlock(BlockType.fromNumber(type), false);
	}

	// 미리보기에 있던 블록 번호를 꺼내 주고, 새 다음 블록을 뽑아 미리보기 칸에 보여 준다
	int takeNextBlock()
	{
		int currentBlock = preBlock;
		preBlock = BlockType.randomNumber();
		nextBlock.setIcon(BlockType.fromNumber(preBlock).getPreviewIcon());
		return currentBlock;
	}

	// 새 블록을 맨 위에 놓는다. 나올 자리가 막혔으면 HP로 구제해 보고, 그래도 막히면 게임 오버
	private void spawnBlock(BlockType type, boolean startFallThread)
	{
		board.spawn(type);
		if (!reviveIfBlocked())
		{
			gameOver();
			return;
		}
		drawTetris();
		checkArray();
		if (startFallThread)
		{
			th = new Thread(this);
			th.start();
		}
	}

	/**
	 * 나올 자리가 막혔으면 HP가 남아 있는 동안 HP를 1 줄이고 맨 밑 줄들을 지운 뒤 다시 확인한다 (HpSystem.java).
	 * @return 블록이 나올 수 있으면 true
	 */
	private boolean reviveIfBlocked()
	{
		int count = board.countSpawnOverlap();
		while (count > 0 && hp.tryRevive(board))
			count = board.countSpawnOverlap();
		return count == 0;
	}

	// 새 블록이 나올 자리가 막혔을 때
	private void gameOver()
	{
		// 배경음악을 멈추고 게임오버 소리를 낸다
		SoundManager.stopBgm();
		SoundManager.play(Sound.GAME_OVER);

		// 갤러그 모드면 끄지 말고 GAME OVER
		if (mode == GameMode.GALAGA)
		{
			endGame("GAME OVER");
			return;
		}

		gameRun = false;
		if (th != null) th.interrupt();
		timeTh.interrupt();
		textLabel.setText("FAIL"); // 여기까지 오는 모드는 일반 모드(1)뿐이다
		textLabel.setVisible(true);
		blackPanel.setVisible(true);
		tetrisArea.setVisible(false);

		// 게임오버 소리가 끝까지 들리도록 1초 기다렸다가 끝낸다.
		// 위에서 th.interrupt()가 이 스레드를 가리켰다면 중단 표시가 남아 sleep이 바로 끝나므로 먼저 지운다.
		Thread.interrupted();
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		// 랭킹 창은 화면 쓰레드에서 띄워야 하므로 넘겨 두고, 이름을 받은 뒤 끝낸다
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				recordRanking();
				System.exit(0);
			}
		});
	}

	// 바닥에 닿은 블록을 고정하고 꽉 찬 줄을 지운다
	public synchronized void fixBlock()
	{
		SoundManager.play(Sound.LOCK);
		board.fixFallingBlock();
		lineCheck();
		holdManager.onBlockLocked();
	}

	// 방금 고정한 블록이 걸친 줄 중 꽉 찬 줄을 위에서부터 지운다
	public void lineCheck()
	{
		int deleteCount = 0;
		for (int i = board.y; i < board.y + board.block.length; i++)
		{
			if (board.isRowFull(i))
			{
				collectItems(i);
				board.deleteRow(i);
				deleteCount++;
			}
		}
		if (deleteCount > 0)
			onLinesCleared(deleteCount);
	}

	// 지울 줄에 있는 아이템 블록을 보관함에 넣는다 (보관함이 차면 더 넣지 않는다)
	private void collectItems(int row)
	{
		for (int j = 1; j < board.field[0].length; j++)
		{
			int value = board.field[row][j];
			if (BoardCell.isItem(value) && !inventory.isFull())
			{
				inventory.add(ItemType.fromCell(value));
				SoundManager.play(Sound.ITEM);
			}
		}
	}

	// 줄을 지운 뒤: 아이템 블록 만들기, 점수, 레벨
	private void onLinesCleared(int lines)
	{
		SoundManager.play(Sound.LINE_CLEAR);
		board.scatterItems(lines);
		addScore(lines);

		// 지운 줄 수로 레벨을 올리고, 오르면 레벨 속도 차이만큼만 빠르게 한다(아이템 효과는 그대로 유지)
		int beforeSpeed = levelManager.getSpeed();
		if (levelManager.addLines(lines))
		{
			gameSpeed = Math.max(MIN_SPEED, gameSpeed - (beforeSpeed - levelManager.getSpeed()));
			levelLabel.setText("LV " + levelManager.getLevel());
			SoundManager.play(Sound.ITEM);
		}
	}

	public void addScore(int lines)
	{
		boolean newBest = scoreKeeper.addLines(lines, gameSpeed);
		score.setText(Integer.toString(scoreKeeper.getScore()));

		// 지금 점수가 최고 기록을 넘으면 최고 점수 칸도 같이 올린다
		if (newBest)
			highestScore.setText(Integer.toString(scoreKeeper.getBest()));
	}

	// 일반 모드가 끝났을 때 점수가 10위 안이면 이름을 받아 랭킹에 저장하고 순위표를 보여 준다
	void recordRanking()
	{
		int finalScore = scoreKeeper.getScore();
		int rank = RankingManager.rankOf(finalScore);
		if (rank == 0)
		{
			JOptionPane.showMessageDialog(this, "점수: " + finalScore + "점\n아쉽게도 10위 안에 들지 못했습니다.",
					"게임 오버", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		String name = JOptionPane.showInputDialog(this, "점수 " + finalScore + "점으로 " + rank + "위입니다!\n이름을 입력하세요.",
				"랭킹 등록", JOptionPane.PLAIN_MESSAGE);
		RankingManager.add(name, finalScore, levelManager.getLevel());
		JOptionPane.showMessageDialog(this, RankingManager.toText(), "랭킹", JOptionPane.INFORMATION_MESSAGE);
	}

	/******************** 키 입력 명령 (TetrisKeyHandler가 부른다) ********************/

	// 카운트다운이 끝났고 게임이 끝나지 않았으면 키를 받는다
	boolean acceptsInput()
	{
		return gameRun && gaming;
	}

	// 설정에서 홀드 키를 바꿨을 수 있으므로 홀드 칸 제목을 다시 쓴다
	void refreshHoldTitle()
	{
		holdSlot.setText("HOLD (" + KeySettings.getKeyName(KeyAction.TETRIS_HOLD) + ")");
	}

	// 한 칸 내리기. 실제로 한 칸 내려갔을 때만 이동음을 낸다(바닥에 닿아 고정되면 고정음이 난다)
	void userMoveDown()
	{
		int oldY = board.y;
		moveDown();
		if (board.y > oldY)
			SoundManager.play(Sound.MOVE);
		afterUserMove();
	}

	// 왼쪽 이동. 벽이나 블록에 막혀 안 움직였으면 소리를 내지 않는다
	void userMoveLeft()
	{
		int oldX = board.x;
		moveLeft();
		if (board.x != oldX)
			SoundManager.play(Sound.MOVE);
		afterUserMove();
	}

	// 오른쪽 이동
	void userMoveRight()
	{
		int oldX = board.x;
		moveRight();
		if (board.x != oldX)
			SoundManager.play(Sound.MOVE);
		afterUserMove();
	}

	// 바로 떨어뜨리기
	void userDrop()
	{
		moveDrop();
		afterUserMove();
	}

	// 회전. 회전하면 블록 모양 배열이 새로 바뀌므로, 바뀌었을 때만 회전음을 낸다
	void userRotate()
	{
		int[][] oldBlock = board.block;
		moveTurn();
		if (board.block != oldBlock)
			SoundManager.play(Sound.ROTATE);
		afterUserMove();
	}

	// 키로 움직인 뒤 테두리를 맞추고 다시 그린다
	private void afterUserMove()
	{
		checkArray();
		drawTetris();
	}

	// 아이템 내게 쓰기: 대상을 자기 자신으로 정하고 쓴다
	void useItemOnSelf()
	{
		itemTarget = this;
		useItem();
	}

	/******************** 블록 이동 ********************/

	public void moveLeft()
	{
		board.moveLeft();
	}

	public void moveRight()
	{
		board.moveRight();
	}

	// 한 칸 내린다. 바닥이나 블록에 닿았으면 고정하고 다음 블록을 꺼낸다
	public void moveDown()
	{
		if (!board.moveDown())
			lockAndNext();
	}

	// 바닥에 닿을 때까지 내린 뒤 고정하고 다음 블록을 꺼낸다
	public void moveDrop()
	{
		while (board.moveDown())
		{
			// 한 칸씩 끝까지 내린다
		}
		lockAndNext();
	}

	public void moveTurn()
	{
		board.rotate();
	}

	// 지금 블록의 낙하 스레드를 멈추고, 블록을 고정한 뒤 다음 블록을 꺼낸다
	private void lockAndNext()
	{
		th.interrupt();
		fixBlock();
		addBlock();
	}

	/******************** 낙하 스레드 ********************/

	@Override
	public void run()
	{
		// 첫 블록의 스레드는 카운트다운이 끝날 때까지 기다린다
		if (!gaming)
		{
			try
			{
				startGate.await();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
				return;
			}
		}
		fallLoop();
	}

	// 게임이 끝날 때까지 낙하 간격마다 한 칸씩 내린다 (interrupt되면 멈춘다)
	private void fallLoop()
	{
		while (gameRun)
		{
			try
			{
				Thread.sleep(gameSpeed);
			}
			catch (InterruptedException e)
			{
				return;
			}
			// 설정 화면을 보는 동안에는 블록을 내리지 않는다
			if (paused)
				continue;
			fallStep();
		}
	}

	private synchronized void fallStep()
	{
		// An interrupted previous piece's thread must not move its replacement.
		if (gameRun && Thread.currentThread() == th
				&& !Thread.currentThread().isInterrupted())
		{
			moveDown();
			drawTetris();
		}
	}

	/******************** 아이템 ********************/

	// 맨 앞 아이템을 대상 판(itemTarget)에 쓴다. 대상이 아직 없으면 아무것도 하지 않는다 (적에게 쓰기 키도 이 메서드)
	public void useItem()
	{
		if (inventory.isEmpty() || itemTarget == null) return;
		inventory.first().getEffect().apply(this, itemTarget);
		SoundManager.play(Sound.ITEM);
		deleteItem();
	}

	// 맨 앞 아이템을 버린다
	public void deleteItem()
	{
		inventory.removeFirst();
	}

	// 낙하 간격(ms)을 바꾼다 (쿨타임이 끝난 속도 아이템이 되돌릴 때 사용)
	void changeSpeed(int delta)
	{
		gameSpeed += delta;
	}

	// 판을 비운 뒤(bomb) 지금 블록의 낙하를 멈추고 새 블록을 꺼낸다
	void respawnBlock()
	{
		th.interrupt();
		addBlock();
	}

	// 두 판의 보드를 맞바꾸고 둘 다 다시 그린다 (change)
	void swapBoard(GamePanel other)
	{
		TetrisBoard temp = board;
		board = other.board;
		other.board = temp;

		drawTetris();
		other.drawTetris();
	}

	// 보드를 검게 가렸다가 약 10초에 걸쳐 서서히 걷어 낸다 (blackout)
	void startBlackout()
	{
		Thread blackTh = new Thread()
		{
			@Override
			public void run()
			{
				int alpha = 255;

				blackPanel.setVisible(true);
				blackPanel.remove(textLabel);

				while (alpha > 0)
				{
					blackPanel.setBackground(new Color(0, 0, 0, alpha));
					alpha -= 25;
					try
					{
						sleep(1000);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}
				blackPanel.setVisible(false);
			}
		};
		blackTh.start();
	}

	// 보드 위 칸에 쿨타임 그림을 하나 더 붙인다
	void showCooldown(ItemType type)
	{
		itemUsing.removeAll();
		cooldowns.add(new CooldownLabel(this, type));
		layoutCooldowns();
	}

	// 쿨타임이 끝난 그림을 치운다. 속도 아이템이었으면 바꿨던 속도를 되돌린다
	void finishCooldown(CooldownLabel label)
	{
		if (label.getType() == ItemType.FAST)
			changeSpeed(SpeedEffect.STEP);
		else if (label.getType() == ItemType.SLOW)
			changeSpeed(-SpeedEffect.STEP);

		itemUsing.removeAll();
		cooldowns.remove(label);
		layoutCooldowns();
	}

	private void layoutCooldowns()
	{
		for (int i = 0; i < cooldowns.size(); i++)
		{
			itemUsing.add(cooldowns.get(i));
			cooldowns.get(i).setLocation(i * COOLDOWN_SLOT, 0);
		}
		itemUsing.repaint();
	}

	/******************** 갤러그용 (GalagaTarget 구현) ********************/

	@Override
	public boolean isPaused()
	{
		return paused;
	}

	// 외계인이 내려앉을수 있나 확인 (row, col은 화면 기준)
	@Override
	public boolean isBlocked(int row, int col)
	{
		return board.isBlocked(row, col);
	}

	// 외계인 착지한 자리에 회색블록
	@Override
	public void placeGarbage(int row, int col)
	{
		if (board.placeGarbage(row, col))
			drawTetris();
	}

	// 갤러그 끝났을때 (CLEAR, GAME OVER)
	@Override
	public void endGame(String message)
	{
		SoundManager.stopBgm();
		gameRun = false;
		th.interrupt();
		timeTh.interrupt();

		textLabel.setText(message);
		textLabel.setVisible(true);
		blackPanel.setVisible(true);
		tetrisArea.setVisible(false);
	}

	@Override
	public boolean isRunning()
	{
		return gameRun;
	}
}
