package galag;

import java.util.ArrayList;

import common.Sound;
import common.SoundManager;

/**
 * 갤러그 협동 모드의 규칙과 상태 (전투기, 총알, 외계인 12마리, 웨이브).
 *
 * 전에는 GalagaLayer.update 메서드 하나(약 160줄, 복잡도 40)에 모든 규칙이 들어 있었고 화면 그리기와도 섞여 있었다
 * (Long Method, SRP 위반). 이제 규칙은 이 클래스가 단계별 메서드로 나눠 갖고, 그리기와 타이머는 GalagaLayer가 맡는다.
 * 보드와 주고받는 일은 GalagaTarget 인터페이스로만 한다.
 */
final class GalagaGame
{
	static final int AREA_WIDTH = 240; // 테트리스 보드와 같은 크기
	static final int AREA_HEIGHT = 480;
	static final int FIGHTER_WIDTH = 24;
	static final int FIGHTER_HEIGHT = 20;
	static final int LAST_WAVE = 3;

	private static final int ALIEN_COUNT = 12;
	private static final int CELL = 24; // 보드 한 칸 크기(px). 외계인 위치를 보드 칸으로 바꿀 때 쓴다
	private static final int SWAY_LIMIT = 30; // 대열이 좌우로 흔들리는 최대 거리
	private static final int FIGHTER_STEP = 4;
	private static final int SHOT_DELAY = 7; // 총을 쏜 뒤 다음 총알까지 기다리는 틱 수
	private static final int FIRST_DIVE_TIME = 100; // 1웨이브에서 한 마리가 내려오기까지 기다리는 틱 수
	private static final int DIVE_TIME_STEP = 20; // 웨이브마다 줄어드는 대기 틱 수
	private static final int FIRST_DIVE_SPEED = 2; // 1웨이브에서 내려오는 속도(px)

	private final GalagaTarget board;
	private final Alien[] aliens = new Alien[ALIEN_COUNT];
	private final ArrayList<Bullet> bullets = new ArrayList<>();

	private int fighterX;
	private int fighterY;
	private int cool = 0; // 다음 총알까지 남은 틱
	private int diveSpeed;
	private int diveTime;
	private int diveTimer = 0;
	private int offset = 0; // 대열이 흔들린 거리
	private int dir = 1; // 대열이 흔들리는 방향 (1 오른쪽, -1 왼쪽)
	private int wave = 1;

	GalagaGame(GalagaTarget board)
	{
		this.board = board;

		// 전투기 위치 (아래 가운데)
		fighterX = (AREA_WIDTH - FIGHTER_WIDTH) / 2;
		fighterY = AREA_HEIGHT - FIGHTER_HEIGHT;

		for (int i = 0; i < ALIEN_COUNT; i++)
			aliens[i] = new Alien();
		startWave();
	}

	// 외계인을 대열로 다시 세우고, 웨이브가 높을수록 자주·빨리 내려오게 한다
	private void startWave()
	{
		for (int i = 0; i < ALIEN_COUNT; i++)
			aliens[i].placeInFormation(i);
		diveTime = FIRST_DIVE_TIME - (wave - 1) * DIVE_TIME_STEP;
		diveSpeed = FIRST_DIVE_SPEED + (wave - 1);
		diveTimer = 0;
		offset = 0;
		dir = 1;
	}

	/** 30ms마다 한 번 부른다. 순서는 외계인 → 급강하 선택 → 전투기 → 총알 → 웨이브 클리어 */
	void update(GalagaControls keys)
	{
		moveAliens();
		pickDiver();
		moveFighter(keys);
		shoot(keys);
		moveBullets();
		checkWaveCleared();
	}

	// 대열을 좌우로 흔들고, 내려오는 외계인은 아래로 움직여 보드에 닿으면 그 위 칸에 회색 블록을 남긴다
	private void moveAliens()
	{
		if (offset + dir > SWAY_LIMIT || offset + dir < -SWAY_LIMIT)
			dir = -dir;
		offset = offset + dir;

		for (int i = ALIEN_COUNT - 1; i >= 0; i--)
		{
			Alien alien = aliens[i];
			if (!alien.alive)
				continue;
			if (alien.diving)
				dive(alien);
			else
				alien.x = alien.x + dir;
		}
	}

	private void dive(Alien alien)
	{
		alien.y = alien.y + diveSpeed;

		// 바닥이나 블록에 닿았는지
		int col = (alien.x + 10) / CELL;
		int row = (alien.y + 16) / CELL;
		if (board.isBlocked(row, col))
		{
			if (row - 1 >= 0)
				board.placeGarbage(row - 1, col);
			alien.alive = false;
			SoundManager.play(Sound.EXPLODE);
		}
	}

	// 지금 내려오는 외계인이 없으면 시간을 재다가 살아 있는 외계인 중 한 마리를 무작위로 내려보낸다
	private void pickDiver()
	{
		for (int i = 0; i < ALIEN_COUNT; i++)
			if (aliens[i].alive && aliens[i].diving)
				return;

		diveTimer++;
		int alive = countAlive();
		if (diveTimer >= diveTime && alive > 0)
		{
			int pick = (int) (Math.random() * alive);
			int n = 0;
			for (int i = 0; i < ALIEN_COUNT; i++)
			{
				if (aliens[i].alive)
				{
					if (n == pick)
						aliens[i].diving = true;
					n++;
				}
			}
			diveTimer = 0;
		}
	}

	private void moveFighter(GalagaControls keys)
	{
		if (keys.left)
			fighterX = Math.max(0, fighterX - FIGHTER_STEP);
		if (keys.right)
			fighterX = Math.min(AREA_WIDTH - FIGHTER_WIDTH, fighterX + FIGHTER_STEP);
	}

	private void shoot(GalagaControls keys)
	{
		if (cool > 0)
			cool--;
		if (keys.shoot && cool == 0)
		{
			bullets.add(new Bullet(fighterX + FIGHTER_WIDTH / 2 - 1, fighterY - Bullet.HEIGHT));
			cool = SHOT_DELAY;
			SoundManager.play(Sound.SHOOT);
		}
	}

	// 총알을 올리고, 화면 밖으로 나가거나 외계인을 맞히면 없앤다 (뒤에서부터 지워야 번호가 안 꼬인다)
	private void moveBullets()
	{
		for (int i = bullets.size() - 1; i >= 0; i--)
		{
			Bullet bullet = bullets.get(i);
			bullet.y = bullet.y - Bullet.SPEED;
			if (bullet.y + Bullet.HEIGHT < 0 || hitAlien(bullet))
				bullets.remove(i);
		}
	}

	// 총알에 처음 맞은 외계인 한 마리를 없앤다. 맞혔으면 true
	private boolean hitAlien(Bullet bullet)
	{
		for (int j = 0; j < ALIEN_COUNT; j++)
		{
			Alien alien = aliens[j];
			if (alien.alive && alien.overlaps(bullet.x, bullet.y, Bullet.WIDTH, Bullet.HEIGHT))
			{
				alien.alive = false;
				SoundManager.play(Sound.EXPLODE);
				return true;
			}
		}
		return false;
	}

	// 다 잡았으면 마지막 웨이브는 클리어, 아니면 다음 웨이브
	private void checkWaveCleared()
	{
		if (countAlive() > 0)
			return;
		if (wave == LAST_WAVE)
		{
			SoundManager.play(Sound.WIN);
			board.endGame("CLEAR");
		}
		else
		{
			wave++;
			startWave();
		}
	}

	private int countAlive()
	{
		int count = 0;
		for (int i = 0; i < ALIEN_COUNT; i++)
			if (aliens[i].alive)
				count++;
		return count;
	}

	/******************** 그리기용 읽기 ********************/

	Alien[] getAliens()
	{
		return aliens;
	}

	ArrayList<Bullet> getBullets()
	{
		return bullets;
	}

	int getFighterX()
	{
		return fighterX;
	}

	int getFighterY()
	{
		return fighterY;
	}

	int getWave()
	{
		return wave;
	}
}
