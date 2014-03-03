package net.minecraft.server;

import org.bukkit.craftbukkit.inventory.CraftItemStack; // CraftBukkit

final class DispenseBehaviorFilledBucket extends DispenseBehaviorItem {

    private final DispenseBehaviorItem b = new DispenseBehaviorItem();

    DispenseBehaviorFilledBucket() {}

    public ItemStack b(ISourceBlock isourceblock, ItemStack itemstack) {
        ItemBucket itembucket = (ItemBucket) itemstack.getItem();
        int i = isourceblock.getBlockX();
        int j = isourceblock.getBlockY();
        int k = isourceblock.getBlockZ();
        EnumFacing enumfacing = BlockDispenser.b(isourceblock.h());

        // CraftBukkit start
        World world = isourceblock.k();
        int x = i + enumfacing.getAdjacentX();
        int y = j + enumfacing.getAdjacentY();
        int z = k + enumfacing.getAdjacentZ();
        if (world.isEmpty(x, y, z) || !world.getType(x, y, z).getMaterial().isBuildable()) {
            if (!BlockDispenser.eventFired) {
                CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack);
                org.bukkit.event.block.BlockDispenseEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callBlockDispenseEvent(isourceblock, craftItem, x, y, z);

                if (event.isCancelled() || event.getItem().equals(craftItem)) {
                    return this.eventProcessing(event, isourceblock, itemstack, craftItem, false);
                }

                itembucket = (ItemBucket) CraftItemStack.asNMSCopy(event.getItem()).getItem();
            }
        }
        // CraftBukkit end

        if (itembucket.a(isourceblock.k(), i + enumfacing.getAdjacentX(), j + enumfacing.getAdjacentY(), k + enumfacing.getAdjacentZ())) {
            // CraftBukkit start - Handle stacked buckets
            Item item = Items.BUCKET;
            if (--itemstack.count == 0) {
                itemstack.setItem(Items.BUCKET);
                itemstack.count = 1;
            } else if (((TileEntityDispenser) isourceblock.getTileEntity()).addItem(new ItemStack(item)) < 0) {
                this.b.a(isourceblock, new ItemStack(item));
            }
            // CraftBukkit end

            return itemstack;
        } else {
            return this.b.a(isourceblock, itemstack);
        }
    }
}
