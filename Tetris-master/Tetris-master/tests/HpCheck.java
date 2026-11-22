import java.util.ArrayList;
import java.util.List;

/** HP 규칙(HpSystem)을 화면 없이 확인한다. */
public final class HpCheck
{
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }

    public static void main(String[] args)
    {
        // 일반 모드만 켜짐
        check(HpSystem.forMode(HpSystem.NORMAL_MODE).isEnabled(), "Normal mode has no HP");
        for (int mode : new int[] { 2, 3, 4 })
            check(!HpSystem.forMode(mode).isEnabled(), "HP on in mode " + mode);

        HpSystem hp = HpSystem.forMode(HpSystem.NORMAL_MODE);
        List<String> events = new ArrayList<>();
        hp.setListener((now, max, lost) -> events.add(now + "/" + max + (lost ? "!" : "")));
        check(events.equals(List.of("3/3")), "Listener not told starting HP: " + events);

        int[] cleared = { 0 };
        HpSystem.BottomClearer board = () -> cleared[0]++;

        // 3 -> 2 -> 1: 매번 4줄 지우고 계속
        check(hp.tryRevive(board) && hp.getHp() == 2 && cleared[0] == HpSystem.CLEAR_LINES, "First hit");
        check(hp.tryRevive(board) && hp.getHp() == 1 && cleared[0] == 2 * HpSystem.CLEAR_LINES, "Second hit");
        // 1 -> 0: 게임 오버, 줄은 안 지움
        check(!hp.tryRevive(board) && hp.getHp() == 0 && cleared[0] == 2 * HpSystem.CLEAR_LINES, "Last hit");
        // 0에서 더 불러도 그대로
        check(!hp.tryRevive(board) && hp.getHp() == 0, "Hit below zero");
        check(events.equals(List.of("3/3", "2/3!", "1/3!", "0/3!")), "Listener events: " + events);

        // HP 안 쓰는 모드는 바로 게임 오버, 줄도 HP도 그대로
        HpSystem off = HpSystem.forMode(2);
        cleared[0] = 0;
        check(!off.tryRevive(board) && off.getHp() == HpSystem.MAX_HP && cleared[0] == 0, "Disabled HP revived");

        check(HpView.hearts(2, 3).equals("♥♥♡") && HpView.hearts(0, 3).equals("♡♡♡"), "Heart text");
        System.out.println("HP limits, line clearing, game-over, disabled modes and listener events passed.");
    }
}
