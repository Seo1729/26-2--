package common;

import java.awt.event.KeyEvent;

/**
 * 키로 조작하는 동작의 목록. 모드별로 묶여 있으며 각 동작마다 기본 키가 정해져 있다.
 * 실제로 지금 어떤 키가 지정돼 있는지는 KeySettings가 관리한다.
 */
public enum KeyAction
{
	/** 일반 모드: 한 칸 내리기 */
	TETRIS_DOWN("일반 모드", "일반/갤러그", "한 칸 내리기", KeyEvent.VK_DOWN),

	/** 일반 모드: 왼쪽 이동 */
	TETRIS_LEFT("일반 모드", "일반/갤러그", "왼쪽 이동", KeyEvent.VK_LEFT),

	/** 일반 모드: 오른쪽 이동 */
	TETRIS_RIGHT("일반 모드", "일반/갤러그", "오른쪽 이동", KeyEvent.VK_RIGHT),

	/** 일반 모드: 아이템 내게 쓰기 */
	TETRIS_ITEM_SELF("일반 모드", "일반/갤러그", "아이템 내게 쓰기", KeyEvent.VK_1),

	/** 일반 모드: 아이템 적에게 쓰기 */
	TETRIS_ITEM_ENEMY("일반 모드", "일반/갤러그", "아이템 적에게 쓰기", KeyEvent.VK_2),

	/** 일반 모드: 아이템 지우기 */
	TETRIS_ITEM_DELETE("일반 모드", "일반/갤러그", "아이템 지우기", KeyEvent.VK_3),

	/** 일반 모드: 바로 떨어뜨리기 */
	TETRIS_DROP("일반 모드", "일반/갤러그", "바로 떨어뜨리기", KeyEvent.VK_SPACE),

	/** 일반 모드: 회전 */
	TETRIS_ROTATE("일반 모드", "일반/갤러그", "회전", KeyEvent.VK_UP),

	/** 갤러그: 전투기 왼쪽 이동 */
	GALAGA_LEFT("갤러그", "일반/갤러그", "왼쪽 이동", KeyEvent.VK_A),

	/** 갤러그: 전투기 오른쪽 이동 */
	GALAGA_RIGHT("갤러그", "일반/갤러그", "오른쪽 이동", KeyEvent.VK_D),

	/** 갤러그: 발사 */
	GALAGA_SHOOT("갤러그", "일반/갤러그", "발사(누르고 있기)", KeyEvent.VK_W),

	/** 똥피하기: 떨어지는 블록 왼쪽 이동 */
	DODGE_BLOCK_LEFT("똥피하기", "똥피하기", "블록 왼쪽 이동", KeyEvent.VK_A),

	/** 똥피하기: 떨어지는 블록 오른쪽 이동 */
	DODGE_BLOCK_RIGHT("똥피하기", "똥피하기", "블록 오른쪽 이동", KeyEvent.VK_D),

	/** 똥피하기: 떨어지는 블록 회전 */
	DODGE_BLOCK_ROTATE("똥피하기", "똥피하기", "블록 회전", KeyEvent.VK_W),

	/** 똥피하기: 떨어지는 블록 한 칸 내리기 */
	DODGE_BLOCK_DOWN("똥피하기", "똥피하기", "블록 내리기", KeyEvent.VK_S),

	/** 똥피하기: 고양이 왼쪽 이동 */
	DODGE_CAT_LEFT("똥피하기", "똥피하기", "고양이 왼쪽 이동", KeyEvent.VK_LEFT),

	/** 똥피하기: 고양이 오른쪽 이동 */
	DODGE_CAT_RIGHT("똥피하기", "똥피하기", "고양이 오른쪽 이동", KeyEvent.VK_RIGHT),

	/** 똥피하기: 고양이 점프 */
	DODGE_CAT_JUMP("똥피하기", "똥피하기", "고양이 점프", KeyEvent.VK_UP),

	/** 똥피하기: 일시정지 */
	DODGE_PAUSE("똥피하기", "똥피하기", "일시정지", KeyEvent.VK_P),

	/** 똥피하기: 끝난 뒤 다시 시작 */
	DODGE_RESTART("똥피하기", "똥피하기", "다시 시작", KeyEvent.VK_R),

	/** 3D 테트리스: 위쪽으로 이동 */
	TETRIS3D_UP("3D 테트리스", "3D 테트리스", "위로 이동", KeyEvent.VK_W),

	/** 3D 테트리스: 아래쪽으로 이동 */
	TETRIS3D_DOWN("3D 테트리스", "3D 테트리스", "아래로 이동", KeyEvent.VK_S),

	/** 3D 테트리스: 왼쪽으로 이동 */
	TETRIS3D_LEFT("3D 테트리스", "3D 테트리스", "왼쪽 이동", KeyEvent.VK_A),

	/** 3D 테트리스: 오른쪽으로 이동 */
	TETRIS3D_RIGHT("3D 테트리스", "3D 테트리스", "오른쪽 이동", KeyEvent.VK_D),

	/** 3D 테트리스: X축 회전 */
	TETRIS3D_ROTATE_X("3D 테트리스", "3D 테트리스", "X축 회전", KeyEvent.VK_J),

	/** 3D 테트리스: Y축 회전 */
	TETRIS3D_ROTATE_Y("3D 테트리스", "3D 테트리스", "Y축 회전", KeyEvent.VK_K),

	/** 3D 테트리스: Z축 회전 */
	TETRIS3D_ROTATE_Z("3D 테트리스", "3D 테트리스", "Z축 회전", KeyEvent.VK_L),

	/** 3D 테트리스: 바로 떨어뜨리기 */
	TETRIS3D_DROP("3D 테트리스", "3D 테트리스", "바로 떨어뜨리기", KeyEvent.VK_SPACE),

	/** 3D 테트리스: 게임오버 후 다시 시작 */
	TETRIS3D_RESTART("3D 테트리스", "3D 테트리스", "다시 시작", KeyEvent.VK_R);

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
