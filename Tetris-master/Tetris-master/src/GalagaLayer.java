import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * 갤러그 협동 모드에서 테트리스 영역 위에 겹쳐 그려지는 투명 레이어.
 * 전투기 등 갤러그 요소를 그리는 역할을 맡는다.
 */
public class GalagaLayer extends JPanel
{
	/** 하단에서 외계인을 격추하는 전투기 */
	private final Fighter fighter;

	/**
	 * 지정한 위치와 크기로 투명 레이어를 만든다.
	 *
	 * @param x      레이어 왼쪽 위 x좌표(프레임 기준 픽셀)
	 * @param y      레이어 왼쪽 위 y좌표(프레임 기준 픽셀)
	 * @param width  레이어 너비(픽셀). 테트리스 영역 너비와 같게 준다.
	 * @param height 레이어 높이(픽셀). 테트리스 영역 높이와 같게 준다.
	 */
	public GalagaLayer(int x, int y, int width, int height)
	{
		// 아래의 테트리스 블록이 비쳐 보이도록 배경을 칠하지 않는다
		setOpaque(false);
		setBounds(x, y, width, height);
		fighter = new Fighter(width, height);
	}

	/**
	 * 레이어 위에 갤러그 요소를 그린다.
	 *
	 * @param g 그리기에 사용할 그래픽 객체
	 */
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		fighter.draw(g);
	}
}
