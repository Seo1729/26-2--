package settings;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** 음량과 게임별 키 설정을 한 화면에서 즉시 적용하고 저장한다. */
public final class EnvironmentSettingsPanel extends JPanel {
    private static final Color MENU_BLUE = new Color(30, 160, 255);
    private final JSlider bgm = new JSlider(0, 100);
    private final JSlider sfx = new JSlider(0, 100);
    private final JLabel bgmLabel = new JLabel();
    private final JLabel sfxLabel = new JLabel();
    private final JLabel status = new JLabel("변경 즉시 적용·자동 저장됩니다. 0은 음소거입니다.");
    private final KeySettingsPanel keys = new KeySettingsPanel();
    private boolean refreshing;

    public EnvironmentSettingsPanel(Runnable close) {
        super(new BorderLayout(12, 12));
        setBackground(MENU_BLUE);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JLabel title = new JLabel("환경 설정");
        title.setFont(new Font("Dialog", Font.BOLD, 22)); title.setForeground(Color.WHITE);
        add(title, BorderLayout.NORTH);
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setOpaque(false);
        JPanel audio = new JPanel(new GridLayout(0, 1, 0, 4));
        audio.setBorder(BorderFactory.createTitledBorder("음량"));
        audio.add(bgmLabel); audio.add(bgm); audio.add(sfxLabel); audio.add(sfx);
        for (JSlider slider : new JSlider[] {bgm, sfx}) {
            slider.setMajorTickSpacing(25); slider.setPaintTicks(true); slider.setPaintLabels(true);
        }
        bgm.getAccessibleContext().setAccessibleName("배경음악 음량");
        sfx.getAccessibleContext().setAccessibleName("효과음 음량");
        content.add(audio, BorderLayout.NORTH);
        keys.setBorder(BorderFactory.createTitledBorder("게임별 조작 키"));
        content.add(keys, BorderLayout.CENTER);
        whiteBackground(audio); whiteBackground(keys);
        add(content, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(0, 8)); footer.setOpaque(false);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT)); buttons.setOpaque(false);
        JButton reset = button("기본값 복원");
        JButton save = button("저장");
        JButton exit = button("닫기");
        reset.addActionListener(event -> {
            GameControls.resetAll(); refresh(); keys.refresh(); saved();
        });
        save.addActionListener(event -> { GameControls.save(); saved(); });
        exit.addActionListener(event -> close.run());
        buttons.add(reset); buttons.add(save); buttons.add(exit);
        status.setForeground(Color.WHITE);
        footer.add(status, BorderLayout.NORTH); footer.add(buttons, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);
        refresh();
        bgm.addChangeListener(event -> {
            bgmLabel.setText("배경음악: " + bgm.getValue());
            if (!refreshing) { GameControls.setBgmVolume(bgm.getValue()); saved(); }
        });
        sfx.addChangeListener(event -> {
            sfxLabel.setText("효과음: " + sfx.getValue());
            if (!refreshing) { GameControls.setSfxVolume(sfx.getValue()); saved(); }
        });
    }
    private void refresh() {
        refreshing = true;
        try {
            bgm.setValue(GameControls.settings().getBgmVolume());
            sfx.setValue(GameControls.settings().getSfxVolume());
            bgmLabel.setText("배경음악: " + bgm.getValue());
            sfxLabel.setText("효과음: " + sfx.getValue());
        } finally { refreshing = false; }
    }
    private void saved() {
        status.setText(GameControls.getSaveError() == null ? "적용하고 저장했습니다." : GameControls.getSaveError());
    }
    private static JButton button(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Dialog", Font.BOLD, 14));
        button.setBackground(Color.WHITE); button.setFocusPainted(false);
        return button;
    }
    private static void whiteBackground(Container container) {
        container.setBackground(Color.WHITE);
        for (Component component : container.getComponents()) {
            if (component instanceof Container) whiteBackground((Container) component);
        }
    }
    public static void show(Component owner) {
        if (GameControls.isInputBlocked()) return;
        Component focus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        Window window = owner instanceof Window ? (Window) owner : SwingUtilities.getWindowAncestor(owner);
        JDialog dialog = new JDialog(window, "환경 설정", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(new EnvironmentSettingsPanel(dialog::dispose));
        dialog.pack();
        // 작은 화면에서도 닫기 버튼에 접근할 수 있게 높이를 제한한다.
        Rectangle screen = owner.getGraphicsConfiguration() == null ? GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds() : owner.getGraphicsConfiguration().getBounds();
        dialog.setSize(Math.min(dialog.getWidth(), screen.width - 40), Math.min(dialog.getHeight(), screen.height - 80));
        dialog.setLocationRelativeTo(owner);
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-settings");
        dialog.getRootPane().getActionMap().put("close-settings", new AbstractAction() {
            public void actionPerformed(ActionEvent event) { dialog.dispose(); }
        });
        GameControls.beginSettings();
        try { dialog.setVisible(true); }
        finally {
            dialog.dispose(); GameControls.endSettings();
            if (focus != null && focus.isDisplayable()) focus.requestFocusInWindow();
            else owner.requestFocusInWindow();
        }
    }
}
