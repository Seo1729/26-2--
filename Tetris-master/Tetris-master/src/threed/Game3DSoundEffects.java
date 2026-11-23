package threed;

import common.Bgm;
import common.Sound;
import common.SoundManager;

// 게임에서 일어난 일에 맞춰 효과음과 배경음악을 낸다 (기본 Game3DListener)
public class Game3DSoundEffects implements Game3DListener
{
	@Override
	public void blockMoved()
	{
		SoundManager.play(Sound.MOVE);
	}

	@Override
	public void blockRotated()
	{
		SoundManager.play(Sound.ROTATE);
	}

	@Override
	public void blockLocked()
	{
		SoundManager.play(Sound.LOCK);
	}

	@Override
	public void layersCleared(int count)
	{
		SoundManager.play(Sound.LINE_CLEAR);
	}

	// 게임오버가 되면 배경음악을 멈추고 게임오버 소리를 낸다
	@Override
	public void gameOver()
	{
		SoundManager.stopBgm();
		SoundManager.play(Sound.GAME_OVER);
	}

	// 게임오버에서 멈췄던 배경음악을 다시 튼다
	@Override
	public void gameRestarted()
	{
		SoundManager.startBgm(Bgm.GAME);
	}
}
