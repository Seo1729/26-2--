package dodge;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/** Synthetic transparent layers exercise real gameplay painting, not shipped items. */
public final class CharacterGameplayCheck {
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    private static BufferedImage render(GamePanel game) {
        BufferedImage image = new BufferedImage(320, 704, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        game.paint(g); g.dispose(); return image;
    }
    private static void pixel(BufferedImage image, int x, int y, Color color, String message) {
        check(image.getRGB(x, y) == color.getRGB(), message);
    }
    public static void main(String[] args) throws Exception {
        Path resources = Paths.get(args[0]);
        boolean restoring = args.length > 1;
        Path settings = restoring ? Paths.get(args[1])
            : Files.createTempDirectory("appearance-game-").resolve("appearance.properties");
        java.util.List<CharacterItem> items = new ArrayList<>();
        Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW};
        for (CharacterItem.Category category : CharacterItem.Category.values()) {
            Map<String, String> frames = new LinkedHashMap<>();
            for (int i = 0; i < DodgerSprite.NAMES.length; i++) {
                String name = category.name() + "_" + DodgerSprite.NAMES[i] + ".png";
                BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = image.createGraphics();
                g.setColor(category == CharacterItem.Category.SKIN ? colors[i]
                    : category == CharacterItem.Category.OUTFIT ? Color.WHITE : Color.BLACK);
                if (category == CharacterItem.Category.SKIN) g.fillRect(0, 0, 8, 8);
                else if (category == CharacterItem.Category.OUTFIT) g.fillRect(1, 2, 6, 5);
                else g.fillRect(1, 1, 3, 3);
                g.dispose();
                ImageIO.write(image, "png", resources.resolve(name).toFile());
                frames.put(DodgerSprite.NAMES[i], "/" + name);
            }
            items.add(new CharacterItem(category, category.name(), "fixture", frames));
        }
        SwingUtilities.invokeAndWait(() -> {
            try {
                CharacterManager manager = new CharacterManager(settings, items);
                CharacterAppearance selected = new CharacterAppearance("SKIN", "HAT", "OUTFIT");
                if (restoring) {
                    check(manager.getAppearance().getSkinId().equals("SKIN")
                        && manager.getAppearance().getHatId().equals("HAT")
                        && manager.getAppearance().getOutfitId().equals("OUTFIT"), "New process lost appearance");
                    selected = manager.getAppearance();
                } else manager.applyAndSave(selected);
                GamePanel game = new GamePanel(manager);
                game.setSize(320, 704);
                Dodger d = game.dodger;
                d.reset(); d.x = 3; d.drawX = 2.5;
                // Half-cell position verifies accessories follow smooth movement.
                for (int state = 0; state < 5; state++) {
                    d.crushed = state == 4; d.jumpLeft = state == 2 ? 1 : 0;
                    d.y = state == 3 ? 1 : 0; d.drawY = d.y;
                    d.drawX = state == 1 ? 2.5 : d.x;
                    d.lastMoveTime = 0;
                    for (int walk = 0; walk < (state == 1 ? 2 : 1); walk++) {
                        d.walkFrame = walk;
                        int frame = state == 1 ? 1 + walk : state == 0 ? 0 : state + 1;
                        for (boolean right : new boolean[] {true, false}) {
                            d.facingRight = right;
                            BufferedImage image = render(game);
                            int x = (int) Math.round(d.drawX * 32);
                            int y = (int) Math.round((21 - d.drawY) * 32);
                            pixel(image, x + (right ? 2 : 29), y + 2, colors[frame], "Skin animation");
                            pixel(image, x + (right ? 20 : 11), y + 20, Color.WHITE, "Outfit alignment");
                            pixel(image, x + (right ? 6 : 25), y + 10, Color.BLACK, "Hat above outfit / facing");
                        }
                    }
                }
                BufferedImage preview = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = preview.createGraphics();
                game.dodgerSprite.drawPreview(g, CharacterAppearance.defaults(), 0, 0, 32, 32);
                g.dispose();
                check(game.dodgerSprite.pickImage(0, 0).getRGB(0, 0) == Color.RED.getRGB(),
                    "Preview changed live skin cache");
                game.result = 1; game.restart(); game.timer.stop();
                check(manager.getAppearance() == selected && !d.crushed && d.x == 0 && d.y == 0,
                    "Restart lost appearance or failed gameplay reset");
                pixel(render(game), 6, 682, Color.BLACK, "Restart did not render selected hat");
                CharacterManager restored = new CharacterManager(settings, items);
                check(restored.getAppearance().getHatId().equals("HAT")
                    && restored.getAppearance().getOutfitId().equals("OUTFIT")
                    && restored.getAppearance().getSkinId().equals("SKIN"), "Persisted appearance lost");
                game.stop();
            } catch (Exception e) { throw new RuntimeException(e); }
        });
        if (!restoring) {
            Process child = new ProcessBuilder(
                Paths.get(System.getProperty("java.home"), "bin", "java.exe").toString(),
                "-Djava.awt.headless=true", "-Duser.home=" + System.getProperty("user.home"),
                "-cp", System.getProperty("java.class.path"), CharacterGameplayCheck.class.getName(),
                resources.toString(), settings.toString()).inheritIO().start();
            check(child.waitFor() == 0, "Appearance verification in new Java process failed");
        }
        System.out.println("Gameplay layers, all animation states, smooth movement, preview isolation and restart passed.");
    }
}
