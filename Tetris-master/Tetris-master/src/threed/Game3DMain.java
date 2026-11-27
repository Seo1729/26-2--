package threed;

import javax.swing.SwingUtilities;

// 3D 테트리스만 따로 실행할 때의 시작점
//   java -cp "out;images;src" threed.Game3DMain            → 창으로 실행
//   java -cp "out;images;src" threed.Game3DMain --console  → 글자로 테스트
// 게임 규칙 클래스(Game3D)가 화면 클래스(Game3DFrame)를 알지 않도록 시작점을 따로 뺐다.
public final class Game3DMain
{
	private Game3DMain() { }

	public static void main(String[] args) throws InterruptedException
	{
		if (args.length == 0 || !"--console".equals(args[0]))
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					new Game3DFrame().setVisible(true);
				}
			});
			return;
		}
		Game3DConsole.run();
	}
}
