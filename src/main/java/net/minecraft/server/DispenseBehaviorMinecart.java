package net.minecraft.server;

import org.bukkit.craftbukkit.inventory.CraftItemStack; // CraftBukkit

final class DispenseBehaviorMinecart extends DispenseBehaviorItem {

    private final DispenseBehaviorItem b = new DispenseBehaviorItem();

    DispenseBehaviorMinecart() {}

    public ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
        EnumFacing enumfacing = BlockDispenser.b(isourceblock.h());
        World world = isourceblock.k();
        double d0 = isourceblock.getX() + (double) ((float) enumfacing.getAdjacentX() * 1.125F);
        double d1 = isourceblock.getY() + (double) ((float) enumfacing.getAdjacentY() * 1.125F);
        double d2 = isourceblock.getZ() + (double) ((float) enumfacing.getAdjacentZ() * 1.125F);
        int i = isourceblock.getBlockX() + enumfacing.getAdjacentX();
        int j = isourceblock.getBlockY() + enumfacing.getAdjacentY();
        int k = isourceblock.getBlockZ() + enumfacing.getAdjacentZ();
        Block block = world.getType(i, j, k);
        double d3;

        if (BlockMinecartTrackAbstract.a(block)) {
            d3 = 0.0D;
        } else {
            if (block.getMaterial() != Material.AIR || !BlockMinecartTrackAbstract.a(world.getType(i, j - 1, k))) {
                return this.b.a(isourceblock, itemstack);
            }

            d3 = -1.0D;
        }

        // CraftBukkit start
        ItemStack itemstack1 = itemstack.a(1);

        if (!BlockDispenser.eventFired) {
            CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);
            org.bukkit.event.block.BlockDispenseEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockDispenseEvent(isourceblock, craftItem, d0, d1 + d3, d2);

            if (event.isCancelled() || event.getItem().equals(craftItem)) {
                return this.eventProcessing(event, isourceblock, itemstack, craftItem, true);
            }

            org.bukkit.util.Vector vector = event.getVelocity();
            d0 = vector.getX();
            d1 = vector.getY() - d3;
            d2 = vector.getZ();

            itemstack1 = CraftItemStack.asNMSCopy(event.getItem());
        }

        EntityMinecartAbstract entityminecartabstract = EntityMinecartAbstract.a(world, d0, d1, d2, ((ItemMinecart) itemstack1.getItem()).a); // itemstack -> itemstack1

        if (itemstack1.hasName()) { // itemstack -> itemstack1
            entityminecartabstract.a(itemstack1.getName()); // itemstack -> itemstack1
        }

        world.addEntity(entityminecartabstract);
        // itemstack.a(1); // handled during event processing
        // CraftBukkit end
        return itemstack;
    }

    protected void a(ISourceBlock isourceblock) {
        isourceblock.k().triggerEffect(1000, isourceblock.getBlockX(), isourceblock.getBlockY(), isourceblock.getBlockZ(), 0);
    }
}
