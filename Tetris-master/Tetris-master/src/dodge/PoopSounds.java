package dodge;

import common.Sound;
import common.SoundManager;

/** 똥피하기 게임 이벤트를 효과음으로 바꿔 준다. */
public class PoopSounds implements GameEvents {

	@Override
	public void onPieceMoved() {
		SoundManager.play(Sound.MOVE);
	}

	@Override
	public void onPieceRotated() {
		SoundManager.play(Sound.ROTATE);
	}

	@Override
	public void onPieceLocked() {
		SoundManager.play(Sound.LOCK);
	}

	@Override
	public void onLinesCleared(int lines) {
		SoundManager.play(Sound.LINE_CLEAR);
	}

	@Override
	public void onJump() {
		SoundManager.play(Sound.ITEM);
	}

	@Override
	public void onCrushed() {
		SoundManager.play(Sound.EXPLODE);
	}

	@Override
	public void onGameEnd(GameRules.Result result) {
		// 블록이 천장까지 쌓여 끝난 경우만 게임오버 소리, 나머지는 승리 소리
		if (result == GameRules.Result.STACK_FULL)
			SoundManager.play(Sound.GAME_OVER);
		else
			SoundManager.play(Sound.WIN);
	}
}
