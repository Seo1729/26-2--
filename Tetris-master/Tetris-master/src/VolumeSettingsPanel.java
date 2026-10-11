import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

/** 드래그 중에도 재생 음량과 파일 설정을 즉시 변경한다. */
public final class VolumeSettingsPanel extends JPanel {
    private final JLabel status = new JLabel("0은 음소거입니다. 변경 사항은 자동 저장됩니다.");
    public VolumeSettingsPanel() {
        setLayout(new GridLayout(0, 1, 4, 4));
        addVolume("배경음악", Settings.getBgmVolume(), true);
        addVolume("효과음", Settings.getSfxVolume(), false);
        add(status);
    }

    private void addVolume(String title, int value, boolean bgm) {
        JLabel label = new JLabel(title + ": " + value);
        JSlider slider = new JSlider(0, 100, value);
        slider.setMajorTickSpacing(25);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.getAccessibleContext().setAccessibleName(title);
        slider.addChangeListener(event -> {
            label.setText(title + ": " + slider.getValue());
            if (bgm) Settings.setBgmVolume(slider.getValue());
            else Settings.setSfxVolume(slider.getValue());
            status.setText(Settings.getSaveError() == null ? "변경 사항이 저장되었습니다." : Settings.getSaveError());
        });
        add(label);
        add(slider);
    }
}
