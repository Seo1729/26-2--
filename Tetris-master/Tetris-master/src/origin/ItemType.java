package origin;

import javax.swing.ImageIcon;

/**
 * 2D 일반 모드의 아이템 10종.
 *
 * 전에는 아이템 종류를 세 가지 방법으로 따로 구분했다: 보드 칸 값(80~89), 보관 칸 JLabel의 이름 문자열("0"~"9"),
 * 쿨타임 그림의 아이콘 객체 비교. 그리고 종류마다 하는 일을 switch로 골랐다(Switch Statements, OCP 위반).
 * 이제 종류는 이 enum 하나로 구분하고, 하는 일은 getEffect()가 돌려주는 ItemEffect 객체가 맡는다(Strategy 패턴).
 * 상수 순서가 곧 아이템 번호(0~9)이고, 보드 칸 값은 80 + 번호다.
 */
enum ItemType
{
	BLACKOUT(ImageSource.ITEM_BLACKOUT, ImageSource.BLOCK_BLACKOUT), // 0: 잠깐 화면을 가림
	FAST(ImageSource.ITEM_FAST, ImageSource.BLOCK_FAST), // 1: 빨라짐
	ONE_LINE_UP(ImageSource.ITEM_LINEUP_1, ImageSource.BLOCK_LINEUP_1), // 2: 회색 줄 1개 올라옴
	THREE_LINE_UP(ImageSource.ITEM_LINEUP_3, ImageSource.BLOCK_LINEUP_3), // 3: 회색 줄 3개 올라옴
	ZIGZAG(ImageSource.ITEM_ZIGZAG, ImageSource.BLOCK_ZIGZAG), // 4: 줄마다 칸 섞기
	BOMB(ImageSource.ITEM_BOMB, ImageSource.BLOCK_BOMB), // 5: 판 전체 지우기
	CHANGE(ImageSource.ITEM_CHANGE, ImageSource.BLOCK_CHANGE), // 6: 두 판 맞바꾸기
	ONE_LINE_DOWN(ImageSource.ITEM_LINEDOWN_1, ImageSource.BLOCK_LINEDOWN_1), // 7: 맨 아래 1줄 지우기
	THREE_LINE_DOWN(ImageSource.ITEM_LINEDOWN_3, ImageSource.BLOCK_LINEDOWN_3), // 8: 맨 아래 3줄 지우기
	SLOW(ImageSource.ITEM_SLOW, ImageSource.BLOCK_SLOW); // 9: 느려짐

	// 아이템마다 하는 일 (Strategy). 상태가 없는 객체라 하나씩만 만들어 함께 쓴다
	private static final ItemEffect[] EFFECTS = { new BlackoutEffect(), new SpeedEffect(-SpeedEffect.STEP, FAST),
		new LineUpEffect(1, 1), new LineUpEffect(3, 0), new ZigzagEffect(), new BombEffect(), new ChangeEffect(),
		new LineDownEffect(1), new LineDownEffect(3), new SpeedEffect(SpeedEffect.STEP, SLOW) };

	private final ImageIcon itemIcon; // 보관 칸에 보여 줄 아이콘
	private final ImageIcon blockIcon; // 보드 위 아이템 블록 칸 아이콘

	ItemType(ImageIcon itemIcon, ImageIcon blockIcon)
	{
		this.itemIcon = itemIcon;
		this.blockIcon = blockIcon;
	}

	/** 보드 칸 값(80~89)으로 아이템 종류를 찾는다. 아이템 칸이 아니면 null */
	static ItemType fromCell(int value)
	{
		if (!BoardCell.isItem(value))
			return null;
		return values()[value - BoardCell.ITEM_BASE];
	}

	/** 이 아이템이 하는 일 */
	ItemEffect getEffect()
	{
		return EFFECTS[ordinal()];
	}

	/** 보관 칸에 보여 줄 아이콘 */
	ImageIcon getItemIcon()
	{
		return itemIcon;
	}

	/** 보드 위 아이템 블록 칸에 그릴 아이콘 */
	ImageIcon getBlockIcon()
	{
		return blockIcon;
	}
}
