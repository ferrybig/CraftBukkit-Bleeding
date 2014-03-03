package net.minecraft.server;

import org.bukkit.craftbukkit.inventory.CraftItemStack; // CraftBukkit

final class DispenseBehaviorTNT extends DispenseBehaviorItem {

    DispenseBehaviorTNT() {}

    protected ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
        EnumFacing enumfacing = BlockDispenser.b(isourceblock.h());
        World world = isourceblock.k();
        // CraftBukkit start
        double i = isourceblock.getBlockX() + enumfacing.getAdjacentX(); // int -> double
        double j = isourceblock.getBlockY() + enumfacing.getAdjacentY(); // int -> double
        double k = isourceblock.getBlockZ() + enumfacing.getAdjacentZ(); // int -> double

        ItemStack itemstack1 = itemstack.a(1);

        if (!BlockDispenser.eventFired) {
            CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);
            org.bukkit.event.block.BlockDispenseEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockDispenseEvent(isourceblock, craftItem, i + 0.5, j + 0.5, k + 0.5);

            if (event.isCancelled() || event.getItem().equals(craftItem)) {
                return this.eventProcessing(event, isourceblock, itemstack, craftItem, true);
            }

            org.bukkit.util.Vector vector = event.getVelocity();
            i = vector.getX() - 0.5;
            j = vector.getY() - 0.5;
            k = vector.getZ() - 0.5;
        }
        // CraftBukkit end
        EntityTNTPrimed entitytntprimed = new EntityTNTPrimed(world, i + 0.5, j + 0.5, k + 0.5, (EntityLiving) null);

        world.addEntity(entitytntprimed);
        // --itemstack.count; // CraftBukkit - handled above
        return itemstack;
    }
}
