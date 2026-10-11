import java.lang.reflect.Field;
import javax.swing.ImageIcon;

public final class ResourceLoadingCheck {
    public static void main(String[] args) throws Exception {
        new ImageSource();
        int icons = 0;
        for (Field field : ImageSource.class.getDeclaredFields()) {
            if (field.getType() != ImageIcon.class) continue;
            field.setAccessible(true);
            ImageIcon icon = (ImageIcon) field.get(null);
            if (icon == null || icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0)
                throw new AssertionError("Invalid image: " + field.getName());
            icons++;
        }
        for (Sound sound : Sound.values()) if (SoundManager.makeSfx(sound).length == 0)
            throw new AssertionError("Empty sound: " + sound);
        for (Bgm bgm : Bgm.values()) if (SoundManager.makeBgm(bgm).length == 0)
            throw new AssertionError("Empty BGM: " + bgm);
        System.out.println("All " + icons + " image icons and all synthesized sound/BGM buffers loaded.");
    }
}
