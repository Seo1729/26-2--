package common;

import java.awt.event.KeyEvent;

/**
 * 키로 조작하는 동작의 목록. 모드별로 묶여 있으며 각 동작마다 기본 키가 정해져 있다.
 * 실제로 지금 어떤 키가 지정돼 있는지는 KeySettings가 관리한다.
 */
public enum KeyAction
{
	/** 일반 모드: 한 칸 내리기 */
	TETRIS_DOWN(Names.NORMAL_TAB, Names.NORMAL_SCREEN, "한 칸 내리기", KeyEvent.VK_DOWN),

	/** 일반 모드: 왼쪽 이동 */
	TETRIS_LEFT(Names.NORMAL_TAB, Names.NORMAL_SCREEN, Names.MOVE_LEFT, KeyEvent.VK_LEFT),

	/** 일반 모드: 오른쪽 이동 */
	TETRIS_RIGHT(Names.NORMAL_TAB, Names.NORMAL_SCREEN, Names.MOVE_RIGHT, KeyEvent.VK_RIGHT),

	/** 일반 모드: 아이템 내게 쓰기 */
	TETRIS_ITEM_SELF(Names.NORMAL_TAB, Names.NORMAL_SCREEN, "아이템 내게 쓰기", KeyEvent.VK_1),

	/** 일반 모드: 아이템 적에게 쓰기 */
	TETRIS_ITEM_ENEMY(Names.NORMAL_TAB, Names.NORMAL_SCREEN, "아이템 적에게 쓰기", KeyEvent.VK_2),

	/** 일반 모드: 아이템 지우기 */
	TETRIS_ITEM_DELETE(Names.NORMAL_TAB, Names.NORMAL_SCREEN, "아이템 지우기", KeyEvent.VK_3),

	/** 일반 모드: 바로 떨어뜨리기 */
	TETRIS_DROP(Names.NORMAL_TAB, Names.NORMAL_SCREEN, "바로 떨어뜨리기", KeyEvent.VK_SPACE),

	/** 일반 모드: 회전 */
	TETRIS_ROTATE(Names.NORMAL_TAB, Names.NORMAL_SCREEN, "회전", KeyEvent.VK_UP),

	/** 일반 모드: 블록 홀드(보관) */
	TETRIS_HOLD(Names.NORMAL_TAB, Names.NORMAL_SCREEN, "블록 홀드", KeyEvent.VK_C),

	/** 갤러그: 전투기 왼쪽 이동 */
	GALAGA_LEFT(Names.GALAGA_TAB, Names.NORMAL_SCREEN, Names.MOVE_LEFT, KeyEvent.VK_A),

	/** 갤러그: 전투기 오른쪽 이동 */
	GALAGA_RIGHT(Names.GALAGA_TAB, Names.NORMAL_SCREEN, Names.MOVE_RIGHT, KeyEvent.VK_D),

	/** 갤러그: 발사 */
	GALAGA_SHOOT(Names.GALAGA_TAB, Names.NORMAL_SCREEN, "발사(누르고 있기)", KeyEvent.VK_W),

	/** 똥피하기: 떨어지는 블록 왼쪽 이동 */
	DODGE_BLOCK_LEFT(Names.DODGE, Names.DODGE, "블록 왼쪽 이동", KeyEvent.VK_A),

	/** 똥피하기: 떨어지는 블록 오른쪽 이동 */
	DODGE_BLOCK_RIGHT(Names.DODGE, Names.DODGE, "블록 오른쪽 이동", KeyEvent.VK_D),

	/** 똥피하기: 떨어지는 블록 회전 */
	DODGE_BLOCK_ROTATE(Names.DODGE, Names.DODGE, "블록 회전", KeyEvent.VK_W),

	/** 똥피하기: 떨어지는 블록 한 칸 내리기 */
	DODGE_BLOCK_DOWN(Names.DODGE, Names.DODGE, "블록 내리기", KeyEvent.VK_S),

	/** 똥피하기: 고양이 왼쪽 이동 */
	DODGE_CAT_LEFT(Names.DODGE, Names.DODGE, "고양이 왼쪽 이동", KeyEvent.VK_LEFT),

	/** 똥피하기: 고양이 오른쪽 이동 */
	DODGE_CAT_RIGHT(Names.DODGE, Names.DODGE, "고양이 오른쪽 이동", KeyEvent.VK_RIGHT),

	/** 똥피하기: 고양이 점프 */
	DODGE_CAT_JUMP(Names.DODGE, Names.DODGE, "고양이 점프", KeyEvent.VK_UP),
	/** 똥피하기: 일시정지 */
	DODGE_PAUSE(Names.DODGE, Names.DODGE, "일시정지", KeyEvent.VK_P),

	/** 똥피하기: 다시 시작 (게임 중이나 끝난 뒤) */
	DODGE_RESTART(Names.DODGE, Names.DODGE, "다시 시작", KeyEvent.VK_R),

	/** 3D 테트리스: 위쪽으로 이동 */
	TETRIS3D_UP(Names.TETRIS3D, Names.TETRIS3D, "위로 이동", KeyEvent.VK_W),

	/** 3D 테트리스: 아래쪽으로 이동 */
	TETRIS3D_DOWN(Names.TETRIS3D, Names.TETRIS3D, "아래로 이동", KeyEvent.VK_S),

	/** 3D 테트리스: 왼쪽으로 이동 */
	TETRIS3D_LEFT(Names.TETRIS3D, Names.TETRIS3D, Names.MOVE_LEFT, KeyEvent.VK_A),

	/** 3D 테트리스: 오른쪽으로 이동 */
	TETRIS3D_RIGHT(Names.TETRIS3D, Names.TETRIS3D, Names.MOVE_RIGHT, KeyEvent.VK_D),

	/** 3D 테트리스: X축 회전 */
	TETRIS3D_ROTATE_X(Names.TETRIS3D, Names.TETRIS3D, "X축 회전", KeyEvent.VK_J),

	/** 3D 테트리스: Y축 회전 */
	TETRIS3D_ROTATE_Y(Names.TETRIS3D, Names.TETRIS3D, "Y축 회전", KeyEvent.VK_K),

	/** 3D 테트리스: Z축 회전 */
	TETRIS3D_ROTATE_Z(Names.TETRIS3D, Names.TETRIS3D, "Z축 회전", KeyEvent.VK_L),

	/** 3D 테트리스: 바로 떨어뜨리기 */
	TETRIS3D_DROP(Names.TETRIS3D, Names.TETRIS3D, "바로 떨어뜨리기", KeyEvent.VK_SPACE),

	/** 3D 테트리스: 게임오버 후 다시 시작 */
	TETRIS3D_RESTART(Names.TETRIS3D, Names.TETRIS3D, "다시 시작", KeyEvent.VK_R);

	/**
	 * 여러 동작이 함께 쓰는 모드·화면·동작 이름.
	 * enum 상수는 자기 static 상수를 앞에서 참조할 수 없어서(illegal forward reference) 중첩 클래스에 둔다.
	 */
	private static final class Names
	{
		/** 일반 모드 탭 이름 */
		static final String NORMAL_TAB = "일반 모드";
		/** 갤러그 탭 이름 */
		static final String GALAGA_TAB = "갤러그";
		/** 똥피하기 탭·화면 이름 */
		static final String DODGE = "똥피하기";
		/** 3D 테트리스 탭·화면 이름 */
		static final String TETRIS3D = "3D 테트리스";
		/** 일반 모드와 갤러그가 함께 쓰는 화면 이름 */
		static final String NORMAL_SCREEN = "일반/갤러그";
		/** 왼쪽 이동 동작 이름 */
		static final String MOVE_LEFT = "왼쪽 이동";
		/** 오른쪽 이동 동작 이름 */
		static final String MOVE_RIGHT = "오른쪽 이동";

		/** 상수만 모은 클래스라 객체를 만들지 않는다 */
		private Names()
		{
		}
	}

	/** 이 동작이 속한 모드 이름(키 설정 창의 탭 이름) */
	private final String group;

	/** 이 동작이 쓰이는 게임 화면 이름. 같은 화면에서는 같은 키를 두 동작에 쓸 수 없다. */
	private final String screen;

	/** 화면에 보여 줄 동작 이름 */
	private final String label;

	/** 기본 키 코드 */
	private final int defaultKey;

	/**
	 * 동작을 만든다.
	 *
	 * @param group 속한 모드 이름
	 * @param screen 쓰이는 게임 화면 이름
	 * @param label 화면에 보여 줄 동작 이름
	 * @param defaultKey 기본 키 코드(KeyEvent.VK_ 값)
	 */
	private KeyAction(String group, String screen, String label, int defaultKey)
	{
		this.group = group;
		this.screen = screen;
		this.label = label;
		this.defaultKey = defaultKey;
	}

	/**
	 * 이 동작이 속한 모드 이름을 알려 준다.
	 *
	 * @return 모드 이름
	 */
	public String getGroup()
	{
		return group;
	}

	/**
	 * 이 동작이 쓰이는 게임 화면 이름을 알려 준다. 일반 모드와 갤러그는 같은 화면을 함께 쓴다.
	 *
	 * @return 게임 화면 이름
	 */
	public String getScreen()
	{
		return screen;
	}

	/**
	 * 화면에 보여 줄 동작 이름을 알려 준다.
	 *
	 * @return 동작 이름
	 */
	public String getLabel()
	{
		return label;
	}

	/**
	 * 이 동작의 기본 키를 알려 준다.
	 *
	 * @return 기본 키 코드
	 */
	public int getDefaultKey()
	{
		return defaultKey;
	}
}
