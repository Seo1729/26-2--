package threed;

import java.awt.geom.Point2D;

// 3D 화면의 카메라: 보는 각도(방위각, 고도), 확대 배율, 3D 좌표 → 화면 좌표 계산을 맡는다.
// 그리기(Board3DRenderer)와 마우스 입력(CameraDragHandler)이 이 객체 하나를 함께 쓴다.
final class Camera3D
{
	private static final double DRAG_SENSITIVITY = .008;          // 마우스 1픽셀당 회전 라디안
	private static final double MIN_ELEVATION = Math.toRadians(-85);
	private static final double MAX_ELEVATION = Math.toRadians(85);
	private static final double MIN_ZOOM = .3;
	private static final double MAX_ZOOM = 4;
	private static final double ZOOM_STEP = 1.12;                 // 휠 한 칸당 배율

	// 보드 가운데를 회전 중심으로 쓴다 (1.5, 1.5, 7.0)
	private static final double CENTER_X = Board3D.SIZE_X / 2.0;
	private static final double CENTER_Y = Board3D.SIZE_Y / 2.0;
	private static final double CENTER_Z = Board3D.SIZE_Z / 2.0;

	private double azimuth = Math.PI / 4;
	private double elevation = Math.toRadians(20);
	private double zoom = 1;
	private double scale;
	private double originX;
	private double originY;

	// 마우스로 끈 만큼 돌린다 (가로 = 방위각, 세로 = 고도, 고도는 ±85도까지)
	void orbit(int dragDx, int dragDy)
	{
		azimuth = Math.IEEEremainder(azimuth - dragDx * DRAG_SENSITIVITY, Math.PI * 2);
		elevation = Math.max(MIN_ELEVATION, Math.min(MAX_ELEVATION, elevation + dragDy * DRAG_SENSITIVITY));
	}

	// 휠을 굴린 만큼 확대/축소 (0.3배 ~ 4배)
	void zoomBy(double wheelRotation)
	{
		zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom * Math.pow(ZOOM_STEP, -wheelRotation)));
	}

	// 화면 크기가 정해질 때마다 배율과 화면 중심을 다시 맞춘다
	void fitTo(int width, int height, double diameter)
	{
		scale = Math.max(1, Math.min(width - 100.0, height - 110.0) / diameter) * zoom;
		originX = width / 2.0;
		originY = height / 2.0;
	}

	// 카메라가 바라보는 방향(단위 벡터): 보이는 면 고르기와 앞뒤 정렬에 쓴다
	double viewX() { return Math.cos(elevation) * Math.cos(azimuth); }
	double viewY() { return Math.cos(elevation) * Math.sin(azimuth); }
	double viewZ() { return Math.sin(elevation); }

	// 3D 좌표 -> 화면 좌표
	Point2D project(double x, double y, double z)
	{
		x -= CENTER_X;
		y -= CENTER_Y;
		z -= CENTER_Z;
		return new Point2D.Double(originX + scale * (Math.sin(azimuth) * x - Math.cos(azimuth) * y),
				originY + scale * (Math.sin(elevation) * (Math.cos(azimuth) * x + Math.sin(azimuth) * y)
						- Math.cos(elevation) * z));
	}
}
