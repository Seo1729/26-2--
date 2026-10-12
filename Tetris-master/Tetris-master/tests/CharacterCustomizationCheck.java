package dodge;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class CharacterCustomizationCheck {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    private static java.util.List<Component> components(Container root) {
        java.util.List<Component> result = new ArrayList<>();
        for (Component child : root.getComponents()) {
            result.add(child);
            if (child instanceof Container) result.addAll(components((Container) child));
        }
        return result;
    }
    private static JComboBox<?> combo(Container root, String label) {
        for (Component c : components(root)) if (c instanceof JComboBox
            && label.equals(((JComboBox<?>) c).getAccessibleContext().getAccessibleName())) return (JComboBox<?>) c;
        throw new AssertionError("Missing selector: " + label);
    }
    private static void click(Container root, String label) {
        for (Component c : components(root)) if (c instanceof JButton && label.equals(((JButton)c).getText())) {
            ((JButton)c).doClick(); return;
        }
        throw new AssertionError("Missing button: " + label);
    }
    private static BufferedImage preview(Container root) {
        for (Component c : components(root)) if ("character-preview".equals(c.getName())) {
            c.setSize(240, 220);
            BufferedImage image = new BufferedImage(240, 220, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics(); c.paint(g); g.dispose(); return image;
        }
        throw new AssertionError("Missing preview");
    }
    private static boolean same(BufferedImage a, BufferedImage b) {
        for (int y = 0; y < a.getHeight(); y++) for (int x = 0; x < a.getWidth(); x++)
            if (a.getRGB(x, y) != b.getRGB(x, y)) return false;
        return true;
    }
    private static void layout(Container root) {
        root.doLayout();
        for (Component c : root.getComponents()) if (c instanceof Container) layout((Container)c);
    }
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("character-ui-");
        java.util.List<CharacterItem> items = Arrays.asList(
            new CharacterItem(CharacterItem.Category.SKIN, "test-skin", "테스트 스킨",
                Collections.singletonMap("idle", "/dodge/images/dodger_walk2.png")),
            new CharacterItem(CharacterItem.Category.HAT, "test-hat", "테스트 레이어",
                Collections.singletonMap("idle", "/dodge/images/dodger_jump.png")));
        SwingUtilities.invokeAndWait(() -> {
            try {
                CharacterManager manager = new CharacterManager(directory.resolve("appearance.properties"), items);
                boolean[] backed = {false};
                CharacterCustomizationPanel panel = new CharacterCustomizationPanel(manager, () -> backed[0] = true);
                BufferedImage original = preview(panel);
                combo(panel, "캐릭터 스킨").setSelectedIndex(1);
                check(!same(original, preview(panel)), "Draft skin preview unchanged");
                combo(panel, "모자").setSelectedIndex(1);
                check(manager.getAppearance().getSkinId().equals("default") && !Files.exists(manager.getFile()),
                    "Preview applied or saved draft");
                click(panel, "뒤로 가기");
                check(backed[0] && manager.getAppearance().getHatId().equals("none"), "Back applied draft");
                click(panel, "적용");
                CharacterManager restored = new CharacterManager(manager.getFile(), items);
                check(restored.getAppearance().getSkinId().equals("test-skin")
                    && restored.getAppearance().getHatId().equals("test-hat"), "Apply not persisted");
                click(panel, "기본 외형 복원");
                check(same(original, preview(panel)), "Reset preview not default");
                check(manager.getAppearance().getSkinId().equals("test-skin"), "Reset bypassed apply");
                click(panel, "적용");
                restored.load();
                check(restored.getAppearance().getSkinId().equals("default"), "Reset not saved");
                restored.selectSkin("test-skin"); restored.save();
                DodgeMode mode = new DodgeMode(manager);
                mode.start();
                check(manager.getAppearance().getSkinId().equals("test-skin"), "Game start did not reload saved skin");
                mode.stop();
                Path blocked = directory.resolve("blocked");
                Files.createDirectory(blocked); Files.write(blocked.resolve("keep"), new byte[] {1});
                CharacterManager failing = new CharacterManager(blocked, items);
                CharacterCustomizationPanel failedPanel = new CharacterCustomizationPanel(failing, () -> {});
                combo(failedPanel, "캐릭터 스킨").setSelectedIndex(1); click(failedPanel, "적용");
                check(failing.getAppearance().getSkinId().equals("default"), "Failed save changed live appearance");
                CharacterManager basic = new CharacterManager(directory.resolve("basic.properties"));
                CharacterCustomizationPanel empty = new CharacterCustomizationPanel(basic, () -> {});
                check(combo(empty, "모자").getItemCount() == 1 && combo(empty, "의상").getItemCount() == 1,
                    "Missing images advertised as items");
                empty.setSize(460, 530); layout(empty);
                BufferedImage image = new BufferedImage(460, 530, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = image.createGraphics(); empty.paint(g); g.dispose();
                if (args.length > 0) ImageIO.write(image, "png", Paths.get(args[0]).toFile());
                System.out.println("Character UI preview, apply/reset/back, save failure and startup checks passed.");
            } catch (Exception e) { throw new RuntimeException(e); }
        });
    }
}
