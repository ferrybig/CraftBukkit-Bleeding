package net.minecraft.server;

import org.bukkit.craftbukkit.inventory.CraftItemStack; // CraftBukkit

final class DispenseBehaviorBonemeal extends DispenseBehaviorItem {

    private boolean b = true;

    DispenseBehaviorBonemeal() {}

    protected ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
        if (itemstack.getData() == 15) {
            EnumFacing enumfacing = BlockDispenser.b(isourceblock.h());
            World world = isourceblock.k();
            int i = isourceblock.getBlockX() + enumfacing.getAdjacentX();
            int j = isourceblock.getBlockY() + enumfacing.getAdjacentY();
            int k = isourceblock.getBlockZ() + enumfacing.getAdjacentZ();

            // CraftBukkit start
            if (!BlockDispenser.eventFired) {
                CraftItemStack craftItem = CraftItemStack.asNewCraftStack(itemstack.getItem());

                org.bukkit.event.block.BlockDispenseEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockDispenseEvent(isourceblock, craftItem, i, j, k);
                if (event.isCancelled() || event.getItem().equals(craftItem)) {
                    return this.eventProcessing(event, isourceblock, itemstack, craftItem, false);
                }
            }
            // CraftBukkit end

            if (ItemDye.a(itemstack, world, i, j, k)) {
                if (!world.isStatic) {
                    world.triggerEffect(2005, i, j, k, 0);
                }
            } else {
                this.b = false;
            }

            return itemstack;
        } else {
            return super.b(isourceblock, itemstack);
        }
    }

    protected void a(ISourceBlock isourceblock) {
        if (this.b) {
            isourceblock.k().triggerEffect(1000, isourceblock.getBlockX(), isourceblock.getBlockY(), isourceblock.getBlockZ(), 0);
        } else {
            isourceblock.k().triggerEffect(1001, isourceblock.getBlockX(), isourceblock.getBlockY(), isourceblock.getBlockZ(), 0);
        }
    }
}
