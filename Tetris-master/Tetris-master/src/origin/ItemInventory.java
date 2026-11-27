package origin;

import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * 모아 둔 아이템 목록과 그 목록을 보여 주는 칸(화면 아래 검은 줄).
 * 전에는 GamePanel이 JLabel 목록과 그 이름 문자열("0"~"9")로 아이템을 관리했다. 이제 종류는 ItemType으로 들고,
 * 화면 칸 배치는 이 클래스 안에서만 한다.
 */
final class ItemInventory
{
	static final int MAX_ITEMS = 7; // 한 번에 보관할 수 있는 최대 개수
	private static final int SLOT_SIZE = 30; // 아이콘 한 칸 크기(px)

	private final JPanel box;
	private final ArrayList<ItemType> types = new ArrayList<>();
	private final ArrayList<JLabel> labels = new ArrayList<>();

	ItemInventory(JPanel box)
	{
		this.box = box;
	}

	boolean isEmpty()
	{
		return types.isEmpty();
	}

	boolean isFull()
	{
		return types.size() >= MAX_ITEMS;
	}

	/** 맨 앞(가장 먼저 얻은) 아이템 */
	ItemType first()
	{
		return types.get(0);
	}

	/** 아이템을 뒤에 추가하고 칸을 다시 배치한다 */
	void add(ItemType type)
	{
		JLabel label = new JLabel();
		label.setIcon(type.getItemIcon());
		types.add(type);
		labels.add(label);
		box.removeAll();
		layoutLabels();
	}

	/** 맨 앞 아이템을 버리고 칸을 다시 배치한다 */
	void removeFirst()
	{
		if (types.isEmpty())
			return;
		box.removeAll();
		box.repaint();
		types.remove(0);
		labels.remove(0);
		layoutLabels();
	}

	// 남은 아이콘을 왼쪽부터 한 칸씩 붙여 놓는다
	private void layoutLabels()
	{
		for (int i = 0; i < labels.size(); i++)
		{
			box.add(labels.get(i));
			labels.get(i).setBounds(SLOT_SIZE * i, 0, SLOT_SIZE, SLOT_SIZE);
		}
	}
}
