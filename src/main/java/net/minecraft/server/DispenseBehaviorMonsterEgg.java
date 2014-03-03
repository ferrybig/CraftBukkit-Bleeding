package net.minecraft.server;

import org.bukkit.craftbukkit.inventory.CraftItemStack; // CraftBukkit

final class DispenseBehaviorMonsterEgg extends DispenseBehaviorItem {

    DispenseBehaviorMonsterEgg() {}

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

        Entity entity = ItemMonsterEgg.spawnCreature(isourceblock.k(), itemstack1.getData(), d0, d1, d2, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.DISPENSE_EGG); // itemstack -> itemstack1, add spawnReason

        if (entity instanceof EntityLiving && itemstack1.hasName()) { // itemstack -> itemstack1
            ((EntityInsentient) entity).setCustomName(itemstack1.getName()); // itemstack -> itemstack1
        }

        // itemstack.a(1); // Handled during event processing
        // CraftBukkit end
        return itemstack;
    }
}
