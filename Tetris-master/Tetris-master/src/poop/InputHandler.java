package poop;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

import common.KeyAction;
import common.KeySettings;

/**
 * 한 키보드로 두 명이 동시에 조작하기 위한 입력 처리.
 *
 * OS의 키 자동 반복은 다른 키를 누르는 순간 끊기기 때문에 쓰지 않는다.
 * 대신 "지금 눌려 있는 키 집합"을 직접 관리하고, 게임 루프가 매 프레임 update()를 불러
 * 꾹 누르고 있는 키의 반복 입력을 플레이어별로 따로 만들어 준다.
 *
 * 키 동작은 KeyAction → Runnable 표로 등록하므로, 동작을 추가해도 이 클래스는 고치지 않는다.
 * 실제 키 번호는 누를 때마다 KeySettings에서 읽어서 설정 화면에서 바꾼 키가 바로 적용된다.
 */
public class InputHandler extends KeyAdapter {

	/** 꾹 눌렀을 때 첫 반복까지 기다리는 시간(ms) */
	public static final long REPEAT_DELAY_MS = 170;

	private final BooleanSupplier playing;
	private final Runnable afterAction;

	/** 언제든 받는 키 (일시정지, 다시 시작). 등록한 순서대로 먼저 맞는 것이 이긴다. */
	private final Map<KeyAction, Runnable> controls = new LinkedHashMap<>();
	/** 설정에서 바꿀 수 없는 고정 키 (ESC 설정창) */
	private final Map<Integer, Runnable> fixedKeys = new HashMap<>();
	/** 게임 중에만 받는 키 */
	private final Map<KeyAction, Runnable> actions = new EnumMap<>(KeyAction.class);
	/** 꾹 누르면 반복되는 키와 그 간격(ms) */
	private final Map<KeyAction, Long> repeatIntervals = new EnumMap<>(KeyAction.class);

	private final Set<Integer> held = new HashSet<>();
	private final Map<Integer, KeyAction> repeating = new HashMap<>();
	private final Map<Integer, Long> nextRepeat = new HashMap<>();

	/**
	 * @param playing     지금 게임 키를 받아도 되는지 (시작했고 멈춰 있지 않음)
	 * @param afterAction 게임 키를 처리한 뒤 할 일 (다시 그리기)
	 */
	public InputHandler(BooleanSupplier playing, Runnable afterAction) {
		this.playing = playing;
		this.afterAction = afterAction;
	}

	public InputHandler control(KeyAction action, Runnable run) {
		controls.put(action, run);
		return this;
	}

	public InputHandler fixedKey(int keyCode, Runnable run) {
		fixedKeys.put(keyCode, run);
		return this;
	}

	public InputHandler action(KeyAction action, Runnable run) {
		actions.put(action, run);
		return this;
	}

	public InputHandler repeatingAction(KeyAction action, long intervalMs, Runnable run) {
		actions.put(action, run);
		repeatIntervals.put(action, intervalMs);
		return this;
	}

	public void clear() {
		held.clear();
		repeating.clear();
		nextRepeat.clear();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		// 이미 눌려 있던 키면 OS 자동 반복 이벤트이므로 무시
		if (!held.add(code))
			return;

		KeyAction control = find(controls.keySet(), code);
		if (control != null) {
			controls.get(control).run();
			return;
		}
		Runnable fixed = fixedKeys.get(code);
		if (fixed != null) {
			fixed.run();
			return;
		}
		if (!playing.getAsBoolean())
			return;

		KeyAction action = find(actions.keySet(), code);
		if (action != null) {
			actions.get(action).run();
			if (repeatIntervals.containsKey(action)) {
				repeating.put(code, action);
				nextRepeat.put(code, System.currentTimeMillis() + REPEAT_DELAY_MS);
			}
		}
		afterAction.run();
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int code = e.getKeyCode();
		held.remove(code);
		repeating.remove(code);
		nextRepeat.remove(code);
	}

	/** 게임 루프가 매 프레임 호출. 꾹 누른 키의 반복 입력을 처리한다. */
	public void update() {
		if (!playing.getAsBoolean())
			return;

		long now = System.currentTimeMillis();
		for (Map.Entry<Integer, Long> entry : nextRepeat.entrySet()) {
			if (now >= entry.getValue()) {
				KeyAction action = repeating.get(entry.getKey());
				actions.get(action).run();
				entry.setValue(now + repeatIntervals.get(action));
			}
		}
	}

	private static KeyAction find(Iterable<KeyAction> candidates, int code) {
		for (KeyAction action : candidates) {
			if (KeySettings.getKey(action) == code)
				return action;
		}
		return null;
	}
}
