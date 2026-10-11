package dodge;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.swing.SwingUtilities;

/** Run without character PNGs on the classpath to verify the procedural fallback. */
public final class CharacterMissingResourceCheck {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        check(CharacterManager.class.getResource("/dodge/images/dodger_idle.png") == null,
            "This check requires a classpath without character resources");
        Path file = Files.createTempDirectory("missing-cosmetics-").resolve("appearance.properties");
        Files.writeString(file, "skin=broken-skin\nhat=broken-hat\noutfit=broken-outfit\n");
        java.util.List<CharacterItem> broken = new ArrayList<>();
        for (CharacterItem.Category category : CharacterItem.Category.values()) {
            String id = "broken-" + category.name().toLowerCase(java.util.Locale.ROOT);
            broken.add(new CharacterItem(category, id, id,
                Collections.singletonMap("idle", "/absent-" + id + ".png")));
        }
        CharacterManager manager = new CharacterManager(file, broken);
        check(manager.getAppearance().getSkinId().equals("default")
            && manager.getAppearance().getHatId().equals("none")
            && manager.getAppearance().getOutfitId().equals("none"), "Missing items not reset safely");
        SwingUtilities.invokeAndWait(() -> {
            GamePanel game = new GamePanel(manager);
            try {
                game.setSize(320, 704); game.start(); game.timer.stop();
                for (int state = 0; state < 5; state++) {
                    Dodger d = game.dodger;
                    d.reset(); d.crushed = state == 4;
                    d.jumpLeft = state == 2 ? 1 : 0;
                    d.y = state == 3 ? 1 : 0; d.drawY = d.y;
                    d.x = 2; d.drawX = state == 1 ? 1.5 : 2;
                    BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = image.createGraphics();
                    game.dodgerSprite.draw(g, d, 0, 0, 64, 64); g.dispose();
                    boolean visible = false;
                    for (int y = 0; y < 64; y++) for (int x = 0; x < 64; x++)
                        visible |= (image.getRGB(x, y) >>> 24) != 0;
                    check(visible, "Fallback invisible in state " + state);
                    BufferedImage board = new BufferedImage(320, 704, BufferedImage.TYPE_INT_ARGB);
                    g = board.createGraphics(); game.paint(g); g.dispose();
                }
                game.dodger.reset(); game.dodger.moveRight();
                check(game.dodger.x == 1, "Missing images broke movement");
                game.result = 1; game.restart(); game.timer.stop();
                check(game.isStarted && game.dodger.x == 0, "Missing images broke restart");
            } finally { game.stop(); }
        });
        System.out.println("Missing default/optional resources: fallback, all states, gameplay and restart passed.");
    }
}
