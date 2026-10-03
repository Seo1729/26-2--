package kr.ac.jbnu.se.tetris.dodge;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 한 키보드로 두 명이 동시에 조작하기 위한 입력 처리.
 *
 * OS의 키 자동 반복은 다른 키를 누르는 순간 끊기기 때문에 쓰지 않는다.
 * 대신 "지금 눌려 있는 키 집합"을 직접 관리하고, 게임 루프가 매 프레임 update()를 불러
 * 꾹 누르고 있는 키의 반복 입력을 플레이어별로 따로 만들어 준다.
 */
public class InputHandler extends KeyAdapter {

	// P1 테트리스 (왼손)
	public static final int P1_LEFT = KeyEvent.VK_A;
	public static final int P1_RIGHT = KeyEvent.VK_D;
	public static final int P1_ROTATE = KeyEvent.VK_W;
	public static final int P1_DOWN = KeyEvent.VK_S;
	// P2 똥피하기 (오른손)
	public static final int P2_LEFT = KeyEvent.VK_LEFT;
	public static final int P2_RIGHT = KeyEvent.VK_RIGHT;
	public static final int P2_JUMP = KeyEvent.VK_UP;
	// 공용
	public static final int PAUSE = KeyEvent.VK_P;
	public static final int RESTART = KeyEvent.VK_R;

	/** 꾹 눌렀을 때 첫 반복까지 기다리는 시간(ms) */
	private static final long REPEAT_DELAY = 170;
	/** 이후 반복 간격(ms) */
	private static final long P1_REPEAT_INTERVAL = 50;
	private static final long P2_REPEAT_INTERVAL = 90;

	private final GamePanel panel;
	private final TetrisPlayer tetris;
	private final Dodger dodger;

	private final Set<Integer> held = new HashSet<>();
	private final Map<Integer, Long> nextRepeat = new HashMap<>();

	public InputHandler(GamePanel panel, TetrisPlayer tetris, Dodger dodger) {
		this.panel = panel;
		this.tetris = tetris;
		this.dodger = dodger;
		// 창이 포커스를 잃으면 키가 눌린 채로 남지 않게 비운다
		panel.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				clear();
			}
		});
	}

	public void clear() {
		held.clear();
		nextRepeat.clear();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		// 이미 눌려 있던 키면 OS 자동 반복 이벤트이므로 무시
		if (!held.add(code))
			return;

		if (code == PAUSE) {
			panel.togglePause();
			return;
		}
		if (code == RESTART) {
			panel.restart();
			return;
		}
		if (!panel.isRunning())
			return;

		act(code);
		if (isRepeatable(code))
			nextRepeat.put(code, System.currentTimeMillis() + REPEAT_DELAY);
		panel.repaint();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		held.remove(e.getKeyCode());
		nextRepeat.remove(e.getKeyCode());
	}

	/** 게임 루프가 매 프레임 호출. 꾹 누른 키의 반복 입력을 처리한다. */
	public void update() {
		if (!panel.isRunning())
			return;

		long now = System.currentTimeMillis();
		for (Map.Entry<Integer, Long> entry : nextRepeat.entrySet()) {
			if (now >= entry.getValue()) {
				int code = entry.getKey();
				act(code);
				entry.setValue(now + repeatInterval(code));
			}
		}
	}

	private void act(int code) {
		switch (code) {
		case P1_LEFT:
			tetris.moveLeft();
			break;
		case P1_RIGHT:
			tetris.moveRight();
			break;
		case P1_ROTATE:
			tetris.rotateLeft();
			break;
		case P1_DOWN:
			tetris.oneLineDown();
			break;
		case P2_LEFT:
			dodger.moveLeft();
			break;
		case P2_RIGHT:
			dodger.moveRight();
			break;
		case P2_JUMP:
			dodger.jump();
			break;
		}
	}

	private boolean isRepeatable(int code) {
		return code == P1_LEFT || code == P1_RIGHT || code == P1_DOWN || code == P2_LEFT || code == P2_RIGHT;
	}

	private long repeatInterval(int code) {
		return (code == P2_LEFT || code == P2_RIGHT) ? P2_REPEAT_INTERVAL : P1_REPEAT_INTERVAL;
	}
}
