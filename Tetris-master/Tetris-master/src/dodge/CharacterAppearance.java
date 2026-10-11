package dodge;

import java.util.Objects;

/** Cosmetic IDs only; no movement or collision properties. */
public final class CharacterAppearance {
    public static final String DEFAULT_SKIN = "default";
    public static final String NONE = "none";
    private final String skinId, hatId, outfitId;

    public CharacterAppearance(String skinId, String hatId, String outfitId) {
        this.skinId = Objects.requireNonNull(skinId);
        this.hatId = Objects.requireNonNull(hatId);
        this.outfitId = Objects.requireNonNull(outfitId);
    }
    public String getSkinId() { return skinId; }
    public String getHatId() { return hatId; }
    public String getOutfitId() { return outfitId; }
    public static CharacterAppearance defaults() {
        return new CharacterAppearance(DEFAULT_SKIN, NONE, NONE);
    }
}
