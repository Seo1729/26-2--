package kr.ac.jbnu.se.tetris.dodge;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * P2 캐릭터 그리기.
 *
 * images 폴더(이 클래스와 같은 패키지 아래 images/)에 PNG가 있으면 그 그림을 쓰고,
 * 없으면 코드로 그린 기본 캐릭터를 쓴다. 그림은 모두 "오른쪽을 보는" 모습으로 그리면
 * 왼쪽으로 갈 때는 좌우를 뒤집어서 그린다.
 *
 * 파일 이름 (없는 건 괄호 안 그림으로 대신한다):
 *   dodger_idle.png     가만히 서 있기 (필수, 없으면 기본 캐릭터)
 *   dodger_walk1.png    걷기 1 (idle)
 *   dodger_walk2.png    걷기 2 (walk1)
 *   dodger_jump.png     점프로 올라가는 중 (idle)
 *   dodger_fall.png     떨어지는 중 (jump)
 *   dodger_crushed.png  압사 (idle을 회색으로)
 */
public class DodgerSprite {

	private static final String[] NAMES = { "idle", "walk1", "walk2", "jump", "fall", "crushed" };

	private final Map<String, BufferedImage> images = new HashMap<>();

	public DodgerSprite() {
		for (String name : NAMES) {
			BufferedImage img = load("images/dodger_" + name + ".png");
			if (img != null)
				images.put(name, img);
		}
	}

	private static BufferedImage load(String path) {
		URL url = DodgerSprite.class.getResource(path);
		if (url == null)
			return null;
		try {
			return ImageIO.read(url);
		} catch (IOException e) {
			System.err.println("캐릭터 그림을 읽지 못함: " + path);
			return null;
		}
	}

	/** (x, y)부터 w x h 칸 하나에 캐릭터를 그린다 */
	public void draw(Graphics g, Dodger dodger, int x, int y, int w, int h) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Dodger.State state = dodger.getState();
		BufferedImage img = pickImage(state, dodger.getWalkFrame());
		if (img != null) {
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			if (dodger.isFacingRight())
				g2.drawImage(img, x, y, w, h, null);
			else
				g2.drawImage(img, x + w, y, -w, h, null); // 좌우 뒤집기
			if (state == Dodger.State.CRUSHED && !images.containsKey("crushed")) {
				g2.setColor(new Color(128, 128, 128, 160));
				g2.fillRect(x, y, w, h);
			}
		} else {
			drawDefault(g2, state, dodger.isFacingRight(), dodger.getWalkFrame(), x, y, w, h);
		}
		g2.dispose();
	}

	private BufferedImage pickImage(Dodger.State state, int walkFrame) {
		BufferedImage idle = images.get("idle");
		if (idle == null)
			return null;
		switch (state) {
		case WALK:
			if (walkFrame == 0)
				return images.getOrDefault("walk1", idle);
			return images.getOrDefault("walk2", images.getOrDefault("walk1", idle));
		case JUMP:
			return images.getOrDefault("jump", idle);
		case FALL:
			return images.getOrDefault("fall", images.getOrDefault("jump", idle));
		case CRUSHED:
			return images.getOrDefault("crushed", idle);
		default:
			return idle;
		}
	}

	/** 그림 파일이 없을 때 쓰는 기본 캐릭터 (동그란 몸 + 눈 + 발) */
	private static void drawDefault(Graphics2D g, Dodger.State state, boolean right, int walkFrame, int x, int y,
			int w, int h) {
		Color body = new Color(255, 140, 0);
		int dir = right ? 1 : -1;
		g.setStroke(new BasicStroke(1f));

		if (state == Dodger.State.CRUSHED) {
			// 납작하게 눌린 회색 몸 + X 눈
			int bh = h / 3;
			g.setColor(Color.GRAY);
			g.fillRoundRect(x + 1, y + h - bh - 1, w - 2, bh, bh, bh);
			g.setColor(Color.BLACK);
			g.drawRoundRect(x + 1, y + h - bh - 1, w - 2, bh, bh, bh);
			int ex = x + w / 2 - 2;
			int ey = y + h - bh / 2 - 3;
			g.drawLine(ex - 3, ey, ex + 1, ey + 4);
			g.drawLine(ex + 1, ey, ex - 3, ey + 4);
			g.drawLine(ex + 4, ey, ex + 8, ey + 4);
			g.drawLine(ex + 8, ey, ex + 4, ey + 4);
			return;
		}

		// 몸 크기와 위치: 점프는 위로 늘어나고, 떨어질 때는 살짝 퍼진다
		int bw = w * 7 / 10;
		int bh = h * 6 / 10;
		int by = y + h / 10;
		if (state == Dodger.State.JUMP) {
			bw = w * 6 / 10;
			bh = h * 7 / 10;
			by = y;
		} else if (state == Dodger.State.FALL) {
			bw = w * 8 / 10;
			bh = h * 55 / 100;
		}
		int bx = x + (w - bw) / 2;

		// 발: 걷기는 앞뒤로 교대, 점프는 몸 아래로 접고, 떨어질 때는 벌린다
		int fw = Math.max(3, w / 4);
		int fh = Math.max(2, h / 6);
		int fy = y + h - fh - 1;
		int cx = x + w / 2;
		int frontX = cx + dir * w / 8;
		int backX = cx - dir * w / 8;
		if (state == Dodger.State.WALK) {
			int swing = (walkFrame == 0 ? 1 : -1) * w / 8;
			frontX += dir * swing;
			backX -= dir * swing;
		} else if (state == Dodger.State.JUMP) {
			fy = by + bh - fh / 2;
			frontX = cx + dir * w / 12;
			backX = cx - dir * w / 12;
		} else if (state == Dodger.State.FALL) {
			frontX = cx + dir * w / 4;
			backX = cx - dir * w / 4;
		}
		g.setColor(body.darker());
		g.fillOval(backX - fw / 2, fy, fw, fh);
		g.fillOval(frontX - fw / 2, fy, fw, fh);

		// 팔: 점프는 앞팔을 위로, 떨어질 때는 양팔을 위로
		g.setStroke(new BasicStroke(Math.max(1.5f, w / 12f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.setColor(body.darker());
		int armY = by + bh / 2;
		int frontSide = right ? bx + bw : bx;
		int backSide = right ? bx : bx + bw;
		if (state == Dodger.State.JUMP) {
			g.drawLine(frontSide, armY, frontSide + dir * w / 8, by - h / 10);
		} else if (state == Dodger.State.FALL) {
			g.drawLine(frontSide, armY, frontSide + dir * w / 8, by);
			g.drawLine(backSide, armY, backSide - dir * w / 8, by);
		} else {
			g.drawLine(frontSide, armY, frontSide + dir * w / 10, armY + h / 8);
		}

		// 몸
		g.setStroke(new BasicStroke(1f));
		g.setColor(body);
		g.fillRoundRect(bx, by, bw, bh, bw * 3 / 4, bh * 3 / 4);
		g.setColor(Color.BLACK);
		g.drawRoundRect(bx, by, bw, bh, bw * 3 / 4, bh * 3 / 4);

		// 눈: 보는 방향 쪽으로 치우치고, 눈동자도 그 방향을 본다
		int er = Math.max(3, w / 6);
		int ex = cx + dir * bw / 5 - er / 2;
		int ey = by + bh / 4;
		g.setColor(Color.WHITE);
		g.fillOval(ex, ey, er, er);
		g.setColor(Color.BLACK);
		g.drawOval(ex, ey, er, er);
		int pr = Math.max(2, er / 2);
		int px = ex + er / 2 - pr / 2 + dir * (er / 4);
		int py = ey + er / 2 - pr / 2 + (state == Dodger.State.FALL ? -er / 4 : 0);
		g.fillOval(px, py, pr, pr);
	}
}
