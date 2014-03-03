package net.minecraft.server;

import org.bukkit.craftbukkit.inventory.CraftItemStack; // CraftBukkit

public abstract class DispenseBehaviorProjectile extends DispenseBehaviorItem {

    public DispenseBehaviorProjectile() {}

    public ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
        World world = isourceblock.k();
        IPosition iposition = BlockDispenser.a(isourceblock);
        EnumFacing enumfacing = BlockDispenser.b(isourceblock.h());
        IProjectile iprojectile = this.a(world, iposition);

        // CraftBukkit start
        ItemStack itemstack1 = itemstack.a(1);
        double d0 = (double) enumfacing.c();
        double d1 = (double) ((float) enumfacing.d() + 0.1F);
        double d2 = (double) enumfacing.e();

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
        }

        iprojectile.shoot(d0, d1, d2, this.b(), this.a());
        ((Entity) iprojectile).projectileSource = new org.bukkit.craftbukkit.projectiles.CraftBlockProjectileSource((TileEntityDispenser) isourceblock.getTileEntity());
        // CraftBukkit end

        world.addEntity((Entity) iprojectile);
        // itemstack.a(1); // CraftBukkit - Handled during event processing
        return itemstack;
    }

    protected void a(ISourceBlock isourceblock) {
        isourceblock.k().triggerEffect(1002, isourceblock.getBlockX(), isourceblock.getBlockY(), isourceblock.getBlockZ(), 0);
    }

    protected abstract IProjectile a(World world, IPosition iposition);

    protected float a() {
        return 6.0F;
    }

    protected float b() {
        return 1.1F;
    }
}
