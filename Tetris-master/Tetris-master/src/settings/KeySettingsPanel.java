package settings;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;
import javax.swing.*;
import settings.GameSettings.Game;

/** 게임별 동작을 선택한 뒤 원하는 단일 키를 누른다. ESC는 취소한다. */
public final class KeySettingsPanel extends JPanel {
    private final JComboBox<Game> game = new JComboBox<>(Game.values());
    private final JPanel rows = new JPanel(new GridLayout(0, 2, 8, 6));
    private final JLabel status = new JLabel("변경할 동작을 누른 후 키를 입력하세요. ESC: 취소");
    public KeySettingsPanel() {
        super(new BorderLayout(8, 8));
        game.setRenderer(new DefaultListCellRenderer() {
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean selected, boolean focus) {
                return super.getListCellRendererComponent(list, value instanceof Game ? gameName((Game) value) : value,
                        index, selected, focus);
            }
        });
        add(game, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(rows);
        scroll.setPreferredSize(new Dimension(480, 360));
        add(scroll, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout());
        JButton reset = new JButton("이 게임의 기본 키로 초기화");
        reset.addActionListener(event -> { GameControls.reset(selected()); refresh(); saved(); });
        footer.add(reset, BorderLayout.NORTH); footer.add(status, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);
        game.addActionListener(event -> refresh());
        refresh();
    }
    private Game selected() { return (Game) game.getSelectedItem(); }
    private void saved() {
        status.setText(GameControls.getSaveError() == null ? "적용하고 저장했습니다." : GameControls.getSaveError());
    }
    void refresh() {
        rows.removeAll();
        for (Map.Entry<String, Integer> entry : GameControls.settings().getKeys(selected()).entrySet()) {
            rows.add(new JLabel(actionName(selected(), entry.getKey())));
            JButton button = new JButton("변경: " + KeyEvent.getKeyText(entry.getValue()));
            button.addActionListener(event -> capture(entry.getKey()));
            rows.add(button);
        }
        rows.revalidate(); rows.repaint();
    }
    private void capture(String action) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "새 조작 키", java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        JTextField input = new JTextField("원하는 키를 누르세요. ESC: 취소", 30);
        input.setEditable(false); input.setFocusTraversalKeysEnabled(false);
        input.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent event) {
                event.consume();
                if (event.getKeyCode() == KeyEvent.VK_ESCAPE) { dialog.dispose(); return; }
                if (event.isAltDown() || event.isControlDown() || event.isMetaDown() || event.isShiftDown()) {
                    input.setText("조합 키는 사용할 수 없습니다."); return;
                }
                try {
                    GameControls.setKey(selected(), action, event.getKeyCode());
                    refresh(); saved(); dialog.dispose();
                } catch (IllegalArgumentException e) { input.setText(e.getMessage()); }
            }
        });
        dialog.add(input); dialog.pack(); dialog.setLocationRelativeTo(this);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent event) { input.requestFocusInWindow(); }
        });
        dialog.setVisible(true);
    }
    public static String gameName(Game game) {
        switch (game) {
        case TETRIS: return "테트리스";
        case DODGE: return "똥피하기";
        case GALAGA: return "갤러그 협동";
        default: return "3D 테트리스";
        }
    }
    public static String actionName(Game game, String action) {
        switch (action) {
        case "LEFT": return game == Game.DODGE ? "P2 왼쪽 이동" : "왼쪽 이동";
        case "RIGHT": return game == Game.DODGE ? "P2 오른쪽 이동" : "오른쪽 이동";
        case "DOWN": return "한 칸 내리기";
        case "ROTATE": return "회전";
        case "HARD_DROP": return "빠른 낙하";
        case "HOLD": return "홀드";
        case "USE_ITEM": return "아이템 사용";
        case "ATTACK_ITEM": return "공격 아이템";
        case "DELETE_ITEM": return "아이템 삭제";
        case "TETRIS_LEFT": return "P1 왼쪽 이동";
        case "TETRIS_RIGHT": return "P1 오른쪽 이동";
        case "TETRIS_ROTATE": return "P1 회전";
        case "TETRIS_DOWN": return "P1 한 칸 내리기";
        case "JUMP": return "P2 점프";
        case "PAUSE": return "일시정지";
        case "RESTART": return "재시작";
        case "FORWARD": return "앞 이동";
        case "BACK": return "뒤 이동";
        case "ROTATE_X": return "X축 회전";
        case "ROTATE_Y": return "Y축 회전";
        case "ROTATE_Z": return "Z축 회전";
        case "FIGHTER_LEFT": return "전투기 왼쪽 이동";
        case "FIGHTER_RIGHT": return "전투기 오른쪽 이동";
        case "SHOOT": return "전투기 발사";
        default: return action;
        }
    }
    public static String help(Game game) {
        StringBuilder text = new StringBuilder(gameName(game) + "\n");
        for (Map.Entry<String, Integer> entry : GameControls.settings().getKeys(game).entrySet())
            text.append(KeyEvent.getKeyText(entry.getValue())).append(" : ").append(actionName(game, entry.getKey())).append('\n');
        return text.toString();
    }
    public static void show(java.awt.Component owner) {
        EnvironmentSettingsPanel.show(owner);
    }
    public static void install(JComponent owner) {
        owner.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "key-settings");
        owner.getActionMap().put("key-settings", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent event) { show(owner); }
        });
    }
}
