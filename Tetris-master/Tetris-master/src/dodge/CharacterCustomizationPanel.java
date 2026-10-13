package dodge;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;

/** A draft editor: preview/reset/back never modify the saved or live appearance. */
public final class CharacterCustomizationPanel extends JPanel {
    private final CharacterManager manager;
    private final DodgerSprite sprite;
    private final JComboBox<Option> skins = new JComboBox<>();
    private final JComboBox<Option> hats = new JComboBox<>();
    private final JComboBox<Option> outfits = new JComboBox<>();
    private final JLabel status = new JLabel("선택한 외형은 적용 버튼을 누르면 저장됩니다.");
    private final JPanel preview;
    private CharacterAppearance draft;
    private boolean refreshing;
    private static final class Option {
        final String id, label;
        Option(String id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }
    public CharacterCustomizationPanel(CharacterManager manager, Runnable back) {
        super(new BorderLayout(12, 12));
        this.manager = java.util.Objects.requireNonNull(manager);
        sprite = new DodgerSprite(manager);
        draft = manager.getAppearance();
        setBackground(new Color(30, 160, 255));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        setPreferredSize(new Dimension(460, 530));
        JLabel title = new JLabel("캐릭터 관리");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Dialog", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);
        JPanel content = new JPanel(new BorderLayout(8, 12));
        content.setOpaque(false);
        preview = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int size = Math.max(1, Math.min(160, Math.min(getWidth() - 24, getHeight() - 40)));
                sprite.drawPreview(g, draft, (getWidth() - size) / 2, (getHeight() - size) / 2, size, size);
            }
        };
        preview.setBackground(new Color(32, 34, 44));
        preview.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.WHITE),
            "미리보기", 0, 0, new Font("Dialog", Font.BOLD, 14), Color.WHITE));
        preview.setPreferredSize(new Dimension(240, 220));
        preview.setName("character-preview");
        content.add(preview, BorderLayout.CENTER);
        JPanel choices = new JPanel(new GridLayout(0, 2, 10, 10));
        choices.setBackground(Color.WHITE);
        choices.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        fill(skins, CharacterItem.Category.SKIN);
        fill(hats, CharacterItem.Category.HAT);
        fill(outfits, CharacterItem.Category.OUTFIT);
        for (Object[] row : new Object[][] {{"캐릭터 스킨", skins}, {"모자", hats}, {"의상", outfits}}) {
            choices.add(new JLabel((String) row[0]));
            JComboBox<?> combo = (JComboBox<?>) row[1];
            combo.getAccessibleContext().setAccessibleName((String) row[0]);
            choices.add(combo);
            combo.addActionListener(event -> updateDraft());
        }
        content.add(choices, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(0, 8));
        footer.setOpaque(false);
        status.setForeground(Color.WHITE);
        status.setFont(new Font("Dialog", Font.PLAIN, 12));
        footer.add(status, BorderLayout.NORTH);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttons.setOpaque(false);
        JButton apply = button("적용");
        JButton reset = button("기본 외형 복원");
        JButton exit = button("뒤로 가기");
        apply.addActionListener(event -> apply());
        reset.addActionListener(event -> {
            draft = CharacterAppearance.defaults(); refresh();
            status.setText("기본 외형을 선택했습니다. 적용하면 저장됩니다.");
        });
        exit.addActionListener(event -> back.run());
        buttons.add(apply); buttons.add(reset); buttons.add(exit);
        footer.add(buttons, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);
        refresh();
    }
    private static JButton button(String label) {
        JButton button = new JButton(label);
        button.setFont(new Font("Dialog", Font.BOLD, 14));
        button.setBackground(Color.WHITE); button.setFocusPainted(false);
        return button;
    }
    private void fill(JComboBox<Option> combo, CharacterItem.Category category) {
        if (category != CharacterItem.Category.SKIN) combo.addItem(new Option("none", "미착용"));
        for (CharacterItem item : manager.getItems(category)) combo.addItem(new Option(item.getId(), item.getName()));
        if (combo.getItemCount() == 1 && category != CharacterItem.Category.SKIN)
            combo.setToolTipText("등록된 이미지가 있는 아이템이 없습니다.");
    }
    private static void choose(JComboBox<Option> combo, String id) {
        for (int i = 0; i < combo.getItemCount(); i++)
            if (combo.getItemAt(i).id.equals(id)) { combo.setSelectedIndex(i); return; }
        combo.setSelectedIndex(0);
    }
    private void refresh() {
        refreshing = true;
        try {
            choose(skins, draft.getSkinId()); choose(hats, draft.getHatId()); choose(outfits, draft.getOutfitId());
        } finally { refreshing = false; }
        updateDraft();
    }
    private void updateDraft() {
        if (refreshing) return;
        draft = new CharacterAppearance(((Option) skins.getSelectedItem()).id,
            ((Option) hats.getSelectedItem()).id, ((Option) outfits.getSelectedItem()).id);
        preview.repaint();
    }
    private void apply() {
        try {
            manager.applyAndSave(draft);
            status.setText("외형을 적용하고 저장했습니다.");
            status.setToolTipText(null);
        } catch (IOException | IllegalArgumentException e) {
            status.setText("저장하지 못했습니다. 기존 외형을 유지합니다.");
            status.setToolTipText(e.getMessage());
        }
    }
    public static void show(Component owner) {
        Window window = SwingUtilities.getWindowAncestor(owner);
        JDialog dialog = new JDialog(window, "캐릭터 관리", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        CharacterCustomizationPanel panel = new CharacterCustomizationPanel(CharacterManager.getInstance(), dialog::dispose);
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        dialog.setContentPane(scroll);
        dialog.pack();
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        dialog.setSize(Math.min(dialog.getWidth(), screen.width), Math.min(dialog.getHeight(), screen.height));
        dialog.setLocationRelativeTo(owner);
        dialog.getRootPane().registerKeyboardAction(event -> dialog.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        try { dialog.setVisible(true); }
        finally { dialog.dispose(); owner.requestFocusInWindow(); }
    }
}
