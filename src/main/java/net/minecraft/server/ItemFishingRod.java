package net.minecraft.server;

public class ItemFishingRod extends Item {

    public ItemFishingRod() {
        this.setMaxDurability(64);
        this.e(1);
        this.a(CreativeModeTab.i);
    }

    public ItemStack a(ItemStack itemstack, World world, EntityHuman entityhuman) {
        if (entityhuman.hookedFish != null) {
            int i = entityhuman.hookedFish.e();

            itemstack.damage(i, entityhuman);
            entityhuman.aZ();
        } else {
            // CraftBukkit start
            EntityFishingHook hook = new EntityFishingHook(world, entityhuman);
            if (org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerFishEvent(entityhuman, null, hook, org.bukkit.event.player.PlayerFishEvent.State.FISHING, 0).isCancelled()) {
                return itemstack;
            }
            // CraftBukkit end
            world.makeSound(entityhuman, "random.bow", 0.5F, 0.4F / (g.nextFloat() * 0.4F + 0.8F));
            if (!world.isStatic) {
                world.addEntity(hook); // CraftBukkit - moved creation up
            }

            entityhuman.aZ();
        }

        return itemstack;
    }

    public boolean e_(ItemStack itemstack) {
        return super.e_(itemstack);
    }

    public int c() {
        return 1;
    }
}
