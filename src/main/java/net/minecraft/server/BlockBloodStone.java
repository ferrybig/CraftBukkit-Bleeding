package net.minecraft.server;

public class BlockBloodStone extends Block {

    public BlockBloodStone() {
        super(Material.STONE);
        this.a(CreativeModeTab.b);
    }

    public MaterialMapColor f(int i) {
        return MaterialMapColor.K;
    }

    // CraftBukkit start
    public void doPhysics(World world, int i, int j, int k, int l) {
        Block block = Block.e(l);

        if (block != null && block.isPowerSource()) {
            org.bukkit.craftbukkit.event.CraftEventFactory.callRedstoneChange(world, i, j, k);
        }
    }
    // CraftBukkit end
}
