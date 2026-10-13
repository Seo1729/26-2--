package dodge;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import javax.swing.SwingUtilities;

public final class CharacterManagerCheck {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    private static CharacterItem fixture(CharacterItem.Category category, String id, String frame) {
        // Existing character PNGs used strictly as test layers, not shipped clothing.
        return new CharacterItem(category, id, "테스트 전용", Collections.singletonMap(
            "idle", "/dodge/images/dodger_" + frame + ".png"));
    }
    private static BufferedImage draw(DodgerSprite sprite, Dodger dodger) {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        sprite.draw(graphics, dodger, 0, 0, 64, 64);
        graphics.dispose();
        return image;
    }
    private static boolean equal(BufferedImage a, BufferedImage b) {
        for (int y = 0; y < 64; y++) for (int x = 0; x < 64; x++)
            if (a.getRGB(x, y) != b.getRGB(x, y)) return false;
        return true;
    }
    private static void rejected(Runnable action) {
        try { action.run(); throw new AssertionError("Invalid selection/resource accepted"); }
        catch (IllegalArgumentException expected) { }
    }
    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("character-check-");
        Path file = directory.resolve("appearance.properties");
        CharacterManager characters = new CharacterManager(file);
        check(characters.getItems(CharacterItem.Category.SKIN).size() == 1, "Unexpected shipped skins");
        check(characters.getItems(CharacterItem.Category.HAT).isEmpty()
            && characters.getItems(CharacterItem.Category.OUTFIT).isEmpty(), "Invented clothing");
        check(!Files.exists(file), "Constructor wrote settings");
        CharacterAppearance before = characters.getAppearance();
        rejected(() -> characters.selectHat("missing"));
        rejected(() -> characters.select(new CharacterAppearance("default", "none", "missing")));
        check(characters.getAppearance() == before, "Invalid selection changed appearance");
        rejected(() -> characters.register(fixture(CharacterItem.Category.HAT, "missing", "absent")));
        check(characters.getItems(CharacterItem.Category.HAT).isEmpty(), "Missing resource registered");
        List<CharacterItem> fixtures = Arrays.asList(
            fixture(CharacterItem.Category.SKIN, "test-skin", "walk2"),
            fixture(CharacterItem.Category.HAT, "test-hat", "jump"),
            fixture(CharacterItem.Category.OUTFIT, "test-outfit", "walk1"));
        for (CharacterItem fixture : fixtures) characters.register(fixture);
        rejected(() -> characters.register(fixtures.get(0)));
        characters.select(new CharacterAppearance("test-skin", "test-hat", "test-outfit"));
        characters.save();
        CharacterManager restored = new CharacterManager(file, fixtures);
        check(restored.getAppearance().getSkinId().equals("test-skin")
            && restored.getAppearance().getHatId().equals("test-hat")
            && restored.getAppearance().getOutfitId().equals("test-outfit"), "Persistence round trip");
        CharacterManager removed = new CharacterManager(file);
        check(removed.getAppearance().getSkinId().equals("default")
            && removed.getAppearance().getHatId().equals("none")
            && removed.getAppearance().getOutfitId().equals("none"), "Removed item fallback");
        Files.write(file, "skin=default\nhat=unknown\noutfit=unknown\n".getBytes(StandardCharsets.UTF_8));
        restored.load();
        check(restored.getAppearance().getHatId().equals("none"), "Unknown persisted ID");
        Files.write(file, "skin=\\uZZZZ".getBytes(StandardCharsets.UTF_8));
        CharacterAppearance valid = restored.getAppearance();
        try { restored.load(); throw new AssertionError("Malformed settings accepted"); }
        catch (java.io.IOException expected) { }
        check(restored.getAppearance() == valid, "Failed load replaced valid selection");
        Path directoryTarget = directory.resolve("not-a-file");
        Files.createDirectory(directoryTarget);
        Files.write(directoryTarget.resolve("keep"), new byte[] { 1 });
        try { new CharacterManager(directoryTarget).save(); throw new AssertionError("Failed save swallowed"); }
        catch (java.io.IOException expected) { }

        SwingUtilities.invokeAndWait(() -> {
            CharacterManager model = new CharacterManager(directory.resolve("render.properties"), fixtures);
            GamePanel game = new GamePanel(model);
            Dodger d = game.dodger;
            d.reset();
            DodgerSprite sprite = game.dodgerSprite;
            BufferedImage original = draw(sprite, d);
            BufferedImage expected = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = expected.createGraphics();
            g.drawImage(DodgerSprite.load("images/dodger_idle.png"), 0, 0, 64, 64, null);
            g.dispose();
            check(equal(original, expected), "Default appearance changed");
            model.selectSkin("test-skin");
            check(!equal(original, draw(sprite, d)), "Live skin selection not applied");
            model.selectSkin("default");
            model.selectOutfit("test-outfit");
            check(!equal(original, draw(sprite, d)), "Outfit not applied");
            BufferedImage outfit = draw(sprite, d);
            model.selectHat("test-hat");
            check(!equal(outfit, draw(sprite, d)), "Hat not applied above outfit");
            BufferedImage right = draw(sprite, d);
            d.facingRight = false;
            BufferedImage left = draw(sprite, d);
            for (int y = 0; y < 64; y++) for (int x = 0; x < 64; x++)
                check(right.getRGB(x, y) == left.getRGB(63 - x, y), "Layers not mirrored together");
            d.crush();
            BufferedImage crushedWithItems = draw(sprite, d);
            model.select(CharacterAppearance.defaults());
            check(equal(crushedWithItems, draw(sprite, d)), "Unsupported crushed clothing not hidden");
            model.select(new CharacterAppearance("test-skin", "test-hat", "test-outfit"));
            d.reset();
            d.moveLeft();
            check(d.x == 0, "Left boundary changed");
            d.moveRight();
            check(d.x == 1, "Movement distance changed");
            game.cells[2] = 1;
            d.moveRight();
            check(d.x == 1, "Fixed block collision changed");
            d.jump();
            d.physicsStep();
            d.physicsStep();
            check(d.y == 2 && d.jumpLeft == 0, "Two-cell jump changed");
            d.physicsStep();
            check(d.y == 1, "Gravity changed");
            d.reset();
            game.curPiece.setShape(5);
            game.curX = 0;
            game.curY = 2;
            game.oneLineDown();
            check(d.crushed, "Falling block crush changed");
            game.isStarted = true;
            game.refresh();
            check(game.result == 1 && game.tetrisWins == 1, "Crush winner changed");
            game.stop();
        });
        System.out.println("Character catalog, persistence, rendering and physics checks passed.");
    }
}
