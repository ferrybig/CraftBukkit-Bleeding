package net.minecraft.server;

// CraftBukkit start
import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
// CraftBukkit end

public class EntityEgg extends EntityProjectile {

    public EntityEgg(World world) {
        super(world);
    }

    public EntityEgg(World world, EntityLiving entityliving) {
        super(world, entityliving);
    }

    public EntityEgg(World world, double d0, double d1, double d2) {
        super(world, d0, d1, d2);
    }

    protected void a(MovingObjectPosition movingobjectposition) {
        if (movingobjectposition.entity != null) {
            movingobjectposition.entity.damageEntity(DamageSource.projectile(this, this.getShooter()), 0.0F);
        }

        byte hatching = 0; // CraftBukkit
        if (!this.world.isStatic && this.random.nextInt(8) == 0) {
            byte b0 = 1;

            if (this.random.nextInt(32) == 0) {
                b0 = 4;
            }

            /* CraftBukkit start
            for (int i = 0; i < b0; ++i) {
                EntityChicken entitychicken = new EntityChicken(this.world);

                entitychicken.setAge(-24000);
                entitychicken.setPositionRotation(this.locX, this.locY, this.locZ, this.yaw, 0.0F);
                this.world.addEntity(entitychicken);
            }
            */
            hatching = b0;
            // CraftBukkit end
        }

        // CraftBukkit start - Fire PlayerEggThrowEvent
        EntityType hatchingType = EntityType.CHICKEN;
        if (this.getShooter() instanceof EntityPlayer) {
            org.bukkit.event.player.PlayerEggThrowEvent event = org.bukkit.craftbukkit.event.CraftEventFactory.callPlayerEggThrowEvent(this.shooter, this, hatching != 0, hatching, hatchingType);
            hatching = event.isHatching() ? event.getNumHatches() : 0;
            hatchingType = event.getHatchingType();
        }

        org.bukkit.craftbukkit.CraftWorld craftWorld = this.world.getWorld();
        Class clazz = hatchingType.getEntityClass();
        Location location = new Location(craftWorld, this.locX, this.locY, this.locZ, this.yaw, 0.0F);
        for (int i = 0; i < hatching; i++) {
            org.bukkit.entity.Entity entity = craftWorld.spawn(location, clazz, org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason.EGG);
            if (entity instanceof Ageable) {
                ((Ageable) entity).setBaby();
            }
        }
        // CraftBukkit end

        for (int j = 0; j < 8; ++j) {
            this.world.addParticle("snowballpoof", this.locX, this.locY, this.locZ, 0.0D, 0.0D, 0.0D);
        }

        if (!this.world.isStatic) {
            this.die();
        }
    }
}
