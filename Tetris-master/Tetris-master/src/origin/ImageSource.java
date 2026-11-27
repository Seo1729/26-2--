package origin;

import javax.swing.ImageIcon;

/**
 * 2D 화면에서 쓰는 이미지 아이콘 모음.
 *
 * 전에는 static 필드 46개를 new ImageSource() 생성자가 채웠다. 그래서 생성자를 부르기 전에는 모두 null이었고,
 * 아무 코드나 필드를 바꿀 수 있었다(Global Data, SpotBugs ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD 46건).
 * 이제 클래스를 처음 쓸 때 한 번 읽어 두는 static final 상수다. 어디서도 쓰지 않던 이미지 10개는 뺐다(Dead Code).
 */
public final class ImageSource
{
	// 메인화면 로고 (360x180 자리)
	static final ImageIcon IMG_LOGO = load("logo.png");

	// 다음 블록·홀드 칸 미리보기 (BlockType.getPreviewIcon)
	static final ImageIcon BLOCK_I = load("block/block_I.png");
	static final ImageIcon BLOCK_J = load("block/block_J.png");
	static final ImageIcon BLOCK_L = load("block/block_L.png");
	static final ImageIcon BLOCK_O = load("block/block_O.png");
	static final ImageIcon BLOCK_S = load("block/block_S.png");
	static final ImageIcon BLOCK_T = load("block/block_T.png");
	static final ImageIcon BLOCK_Z = load("block/block_Z.png");

	// 보드 칸 색 (BlockType.getCellIcon, 회색 쓰레기 블록)
	static final ImageIcon BLOCK_BLUE = load("block/blue.png");
	static final ImageIcon BLOCK_CYAN = load("block/cyan.png");
	static final ImageIcon BLOCK_GRAY = load("block/gray.png");
	static final ImageIcon BLOCK_GREEN = load("block/green.png");
	static final ImageIcon BLOCK_LIME = load("block/lime.png");
	static final ImageIcon BLOCK_ORANGE = load("block/orange.png");
	static final ImageIcon BLOCK_PURPLE = load("block/puple.png");
	static final ImageIcon BLOCK_RED = load("block/red.png");

	// 보드 위 아이템 블록 (ItemType.getBlockIcon)
	static final ImageIcon BLOCK_BLACKOUT = load("block/a_blackout.png");
	static final ImageIcon BLOCK_FAST = load("block/a_fast.png");
	static final ImageIcon BLOCK_LINEUP_1 = load("block/a_lineup_1.png");
	static final ImageIcon BLOCK_LINEUP_3 = load("block/a_lineup_3.png");
	static final ImageIcon BLOCK_ZIGZAG = load("block/a_zigzag.png");
	static final ImageIcon BLOCK_BOMB = load("block/p_bomb.png");
	static final ImageIcon BLOCK_CHANGE = load("block/p_change.png");
	static final ImageIcon BLOCK_LINEDOWN_1 = load("block/p_linedown_1.png");
	static final ImageIcon BLOCK_LINEDOWN_3 = load("block/p_linedown_3.png");
	static final ImageIcon BLOCK_SLOW = load("block/p_slow.png");

	// 보관 칸 아이템 (ItemType.getItemIcon)
	static final ImageIcon ITEM_BLACKOUT = load("item/a_blackout.png");
	static final ImageIcon ITEM_FAST = load("item/a_fast.png");
	static final ImageIcon ITEM_LINEUP_1 = load("item/a_lineup_1.png");
	static final ImageIcon ITEM_LINEUP_3 = load("item/a_lineup_3.png");
	static final ImageIcon ITEM_ZIGZAG = load("item/a_zigzag.png");
	static final ImageIcon ITEM_BOMB = load("item/p_bomb.png");
	static final ImageIcon ITEM_CHANGE = load("item/p_change.png");
	static final ImageIcon ITEM_LINEDOWN_1 = load("item/p_linedown_1.png");
	static final ImageIcon ITEM_LINEDOWN_3 = load("item/p_linedown_3.png");
	static final ImageIcon ITEM_SLOW = load("item/p_slow.png");

	// 객체를 만들 일이 없는 상수 모음이므로 생성자를 막는다
	private ImageSource()
	{
	}

	/**
	 * 이미지를 미리 읽어 둔다. 상수는 클래스를 처음 쓸 때 읽히므로, 게임 화면을 열기 전에 불러 두면
	 * 첫 화면에서 기다리지 않는다. (전에 new ImageSource()를 부르던 자리)
	 */
	public static void preload()
	{
		// 이 메서드를 부르는 것만으로 위의 static final 상수가 모두 읽힌다
	}

	// images 폴더(클래스패스 맨 위)에서 아이콘을 읽는다
	private static ImageIcon load(String path)
	{
		return new ImageIcon(ImageSource.class.getClassLoader().getResource(path));
	}
}
