/** Stores a 2D block type, independently of its position and rotation. */
public final class HoldManager
{
    private int heldBlock;
    private boolean used;

    public boolean canHold() { return !used; }
    public int getHeldBlock() { return heldBlock; }

    /** Returns zero for an empty slot, or the block to spawn on a swap. */
    public int hold(int currentBlock)
    {
        if (used) throw new IllegalStateException("Hold already used for this piece");
        if (currentBlock < 1 || currentBlock > 7)
            throw new IllegalArgumentException("Invalid 2D block type");
        int replacement = heldBlock;
        heldBlock = currentBlock;
        used = true;
        return replacement;
    }

    public void onBlockLocked() { used = false; }
}
