package net.minecraft.server;

// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
// CraftBukkit end

final class DispenseBehaviorFlintAndSteel extends DispenseBehaviorItem {

    private boolean b = true;

    DispenseBehaviorFlintAndSteel() {}

    protected ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
        EnumFacing enumfacing = BlockDispenser.b(isourceblock.h());
        World world = isourceblock.k();
        int i = isourceblock.getBlockX() + enumfacing.getAdjacentX();
        int j = isourceblock.getBlockY() + enumfacing.getAdjacentY();
        int k = isourceblock.getBlockZ() + enumfacing.getAdjacentZ();

        // CraftBukkit start
        if (!BlockDispenser.eventFired) {
            CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack);
            org.bukkit.event.block.BlockDispenseEvent event = CraftEventFactory.callBlockDispenseEvent(isourceblock, craftItem, i, j, k);

            if (event.isCancelled() || event.getItem().equals(craftItem)) {
                return this.eventProcessing(event, isourceblock, itemstack, craftItem, false);
            }
        }
        // CraftBukkit end

        if (world.isEmpty(i, j, k)) {
            // CraftBukkit start - Ignition by dispensing flint and steel
            if (!CraftEventFactory.callBlockIgniteEvent(world, i, j, k, isourceblock.getBlockX(), isourceblock.getBlockY(), isourceblock.getBlockZ()).isCancelled()) {
                world.setTypeUpdate(i, j, k, Blocks.FIRE);
                if (itemstack.isDamaged(1, world.random)) {
                    itemstack.count = 0;
                }
            }
            // CraftBukkit end
        } else if (world.getType(i, j, k) == Blocks.TNT) {
            Blocks.TNT.postBreak(world, i, j, k, 1);
            world.setAir(i, j, k);
        } else {
            this.b = false;
        }

        return itemstack;
    }

    protected void a(ISourceBlock isourceblock) {
        if (this.b) {
            isourceblock.k().triggerEffect(1000, isourceblock.getBlockX(), isourceblock.getBlockY(), isourceblock.getBlockZ(), 0);
        } else {
            isourceblock.k().triggerEffect(1001, isourceblock.getBlockX(), isourceblock.getBlockY(), isourceblock.getBlockZ(), 0);
        }
    }
}
