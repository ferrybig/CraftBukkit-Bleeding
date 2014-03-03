package net.minecraft.server;

import org.bukkit.craftbukkit.inventory.CraftItemStack; // CraftBukkit

final class DispenseBehaviorFireworks extends DispenseBehaviorItem {

    DispenseBehaviorFireworks() {}

    public ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
        EnumFacing enumfacing = BlockDispenser.b(isourceblock.h());
        double d0 = isourceblock.getX() + (double) enumfacing.getAdjacentX();
        double d1 = (double) ((float) isourceblock.getBlockY() + 0.2F);
        double d2 = isourceblock.getZ() + (double) enumfacing.getAdjacentZ();
        // CraftBukkit start
        ItemStack itemstack1 = itemstack.a(1);

        if (!BlockDispenser.eventFired) {
            CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);
            org.bukkit.event.block.BlockDispenseEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockDispenseEvent(isourceblock, craftItem, d0, d1, d2);

            if (event.isCancelled() || event.getItem().equals(craftItem)) {
                return this.eventProcessing(event, isourceblock, itemstack, craftItem, true);
            }

            org.bukkit.util.Vector vector = event.getVelocity();
            d0 = vector.getX();
            d1 = vector.getY();
            d2 = vector.getZ();

            itemstack1 = CraftItemStack.asNMSCopy(event.getItem());
        }
        // CraftBukkit end
        EntityFireworks entityfireworks = new EntityFireworks(isourceblock.k(), d0, d1, d2, itemstack1); // CraftBukkit - itemstack -> itemstack1

        isourceblock.k().addEntity(entityfireworks);
        // itemstack.a(1); // CraftBukkit - handled during event processing
        return itemstack;
    }

    protected void a(ISourceBlock isourceblock) {
        isourceblock.k().triggerEffect(1002, isourceblock.getBlockX(), isourceblock.getBlockY(), isourceblock.getBlockZ(), 0);
    }
}
