package net.minecraft.server;

import org.bukkit.craftbukkit.inventory.CraftItemStack; // CraftBukkit

final class DispenseBehaviorBoat extends DispenseBehaviorItem {

    private final DispenseBehaviorItem b = new DispenseBehaviorItem();

    DispenseBehaviorBoat() {}

    public ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
        EnumFacing enumfacing = BlockDispenser.b(isourceblock.h());
        World world = isourceblock.k();
        double d0 = isourceblock.getX() + (double) ((float) enumfacing.getAdjacentX() * 1.125F);
        double d1 = isourceblock.getY() + (double) ((float) enumfacing.getAdjacentY() * 1.125F);
        double d2 = isourceblock.getZ() + (double) ((float) enumfacing.getAdjacentZ() * 1.125F);
        int i = isourceblock.getBlockX() + enumfacing.getAdjacentX();
        int j = isourceblock.getBlockY() + enumfacing.getAdjacentY();
        int k = isourceblock.getBlockZ() + enumfacing.getAdjacentZ();
        Material material = world.getType(i, j, k).getMaterial();
        double d3;

        if (Material.WATER.equals(material)) {
            d3 = 1.0D;
        } else {
            if (!Material.AIR.equals(material) || !Material.WATER.equals(world.getType(i, j - 1, k).getMaterial())) {
                return this.b.a(isourceblock, itemstack);
            }

            d3 = 0.0D;
        }

        // CraftBukkit start
        CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack.a(1));

        if (!BlockDispenser.eventFired) {
            org.bukkit.event.block.BlockDispenseEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockDispenseEvent(isourceblock, craftItem, d0, d1 + d3, d2);

            if (event.isCancelled() || event.getItem().equals(craftItem)) {
                return this.eventProcessing(event, isourceblock, itemstack, craftItem, true);
            }

            org.bukkit.util.Vector vector = event.getVelocity();
            d0 = vector.getX();
            d1 = vector.getY() - d3;
            d2 = vector.getZ();
        }
        // CraftBukkit end

        EntityBoat entityboat = new EntityBoat(world, d0, d1 + d3, d2);

        world.addEntity(entityboat);
        // itemstack.a(1); // CraftBukkit - handled during event processing
        return itemstack;
    }

    protected void a(ISourceBlock isourceblock) {
        isourceblock.k().triggerEffect(1000, isourceblock.getBlockX(), isourceblock.getBlockY(), isourceblock.getBlockZ(), 0);
    }
}
