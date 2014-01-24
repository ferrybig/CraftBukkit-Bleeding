package net.minecraft.server;

public class ItemSaddle extends Item {

    public ItemSaddle() {
        this.maxStackSize = 1;
        this.a(CreativeModeTab.e);
    }

    public boolean a(ItemStack itemstack, EntityHuman entityhuman, EntityLiving entityliving) {
        if (entityliving instanceof EntityPig) {
            EntityPig entitypig = (EntityPig) entityliving;

            if (!entitypig.hasSaddle() && !entitypig.isBaby()) {
                entitypig.setSaddle(true);
                entitypig.world.makeSound(entitypig, "mob.horse.leather", 0.5F, 1.0F);
                --itemstack.count;
                entitypig.world.getServer().getPluginManager().callEvent(new org.bukkit.event.vehicle.VehicleCreateEvent((org.bukkit.entity.Vehicle) entitypig.getBukkitEntity())); // CraftBukkit - call VehicleCreateEvent
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean a(ItemStack itemstack, EntityLiving entityliving, EntityLiving entityliving1) {
        this.a(itemstack, null, entityliving);
        return true;
    }
}
