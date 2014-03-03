package net.minecraft.server;

import java.util.Random;

import org.bukkit.craftbukkit.inventory.CraftItemStack; // CraftBukkit

final class DispenseBehaviorFireball extends DispenseBehaviorItem {

    DispenseBehaviorFireball() {}

    public ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
        EnumFacing enumfacing = BlockDispenser.b(isourceblock.h());
        IPosition iposition = BlockDispenser.a(isourceblock);
        double d0 = iposition.getX() + (double) ((float) enumfacing.getAdjacentX() * 0.3F);
        double d1 = iposition.getY() + (double) ((float) enumfacing.getAdjacentY() * 0.3F);
        double d2 = iposition.getZ() + (double) ((float) enumfacing.getAdjacentZ() * 0.3F);
        World world = isourceblock.k();
        Random random = world.random;
        double d3 = random.nextGaussian() * 0.05D + (double) enumfacing.getAdjacentX();
        double d4 = random.nextGaussian() * 0.05D + (double) enumfacing.getAdjacentY();
        double d5 = random.nextGaussian() * 0.05D + (double) enumfacing.getAdjacentZ();

        // CraftBukkit start
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack.a(1));

        if (!BlockDispenser.eventFired) {
            org.bukkit.event.block.BlockDispenseEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockDispenseEvent(isourceblock, craftItem, d3, d4, d5);

            if (event.isCancelled() || event.getItem().equals(craftItem)) {
                return this.eventProcessing(event, isourceblock, itemstack, craftItem, true);
            }

            org.bukkit.util.Vector vector = event.getVelocity();
            d3 = vector.getX();
            d4 = vector.getY();
            d5 = vector.getZ();
        }

        EntitySmallFireball entitysmallfireball = new EntitySmallFireball(world, d0, d1, d2, d3, d4, d5);
        entitysmallfireball.projectileSource = new org.bukkit.craftbukkit.projectiles.CraftBlockProjectileSource((TileEntityDispenser) isourceblock.getTileEntity());

        world.addEntity(entitysmallfireball);
        // itemstack.a(1); // Handled during event processing
        // CraftBukkit end

        return itemstack;
    }

    protected void a(ISourceBlock isourceblock) {
        isourceblock.k().triggerEffect(1009, isourceblock.getBlockX(), isourceblock.getBlockY(), isourceblock.getBlockZ(), 0);
    }
}
