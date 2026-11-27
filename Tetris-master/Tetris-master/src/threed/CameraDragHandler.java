package threed;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

// 3D 화면의 마우스 입력: 왼쪽 버튼 드래그 = 카메라 회전, 휠 = 확대/축소
// 입력 처리만 하고, 실제 각도 계산은 Camera3D에 맡긴다.
final class CameraDragHandler extends MouseAdapter
{
	private final Camera3D camera;
	private final Component view;   // 카메라가 바뀌면 다시 그릴 화면
	private int dragX;
	private int dragY;
	private boolean dragging;

	CameraDragHandler(Camera3D camera, Component view)
	{
		this.camera = camera;
		this.view = view;
	}

	@Override
	public void mousePressed(MouseEvent event)
	{
		if (event.getButton() != MouseEvent.BUTTON1) return;
		dragging = true;
		dragX = event.getX();
		dragY = event.getY();
	}

	@Override
	public void mouseReleased(MouseEvent event)
	{
		if (event.getButton() == MouseEvent.BUTTON1) dragging = false;
	}

	@Override
	public void mouseDragged(MouseEvent event)
	{
		if (!dragging || (event.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == 0) return;
		camera.orbit(event.getX() - dragX, event.getY() - dragY);
		dragX = event.getX();
		dragY = event.getY();
		view.repaint();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent event)
	{
		camera.zoomBy(event.getPreciseWheelRotation());
		view.repaint();
	}
}
