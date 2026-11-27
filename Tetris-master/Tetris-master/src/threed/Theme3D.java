package threed;

import java.awt.Color;

// 3D 화면에서 쓰는 UI 색을 한곳에 모은 팔레트 (블록 색은 BlockColors)
// 같은 색 숫자가 여러 화면에 흩어져 있던 것을 이름 있는 상수로 바꿨다.
public final class Theme3D
{
	public static final Color PANEL_BACKGROUND = new Color(13, 18, 29);   // 바깥 패널, 오른쪽 정보창
	public static final Color VIEW_BACKGROUND = new Color(19, 25, 38);    // 3D 화면, 위에서 본 화면, 카드
	public static final Color TEXT = new Color(220, 230, 245);            // 기본 글자
	public static final Color MUTED_TEXT = new Color(145, 166, 193);      // 보조 글자, 격자선
	public static final Color DANGER_TEXT = new Color(245, 98, 121);      // GAME OVER 글자
	public static final Color EMPTY_CELL = new Color(30, 40, 57);         // 위에서 본 화면의 빈 칸
	public static final Color BACK_GRID = new Color(65, 79, 101);         // 3D 화면 뒤쪽 벽·바닥 선
	public static final Color FRAME = new Color(145, 166, 193, 160);      // 3D 화면 바깥 테두리(반투명)
	public static final Color FACE_OUTLINE = new Color(25, 32, 47);       // 고정 블록 면 테두리
	public static final Color ACTIVE_OUTLINE = new Color(255, 255, 240);  // 떨어지는 블록 테두리
	public static final Color GHOST = new Color(114, 233, 255);           // 고스트(착지 위치) 표시
	public static final Color GHOST_TRANSLUCENT = new Color(114, 233, 255, 190); // 3D 화면의 고스트 점선

	private Theme3D() { }
}
