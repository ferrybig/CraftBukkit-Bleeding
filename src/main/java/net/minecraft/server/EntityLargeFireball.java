package net.minecraft.server;

public class EntityLargeFireball extends EntityFireball {

    public int yield = 1;

    public EntityLargeFireball(World world) {
        super(world);
    }

    public EntityLargeFireball(World world, EntityLiving entityliving, double d0, double d1, double d2) {
        super(world, entityliving, d0, d1, d2);
    }

    protected void a(MovingObjectPosition movingobjectposition) {
        if (!this.world.isStatic) {
            if (movingobjectposition.entity != null) {
                movingobjectposition.entity.damageEntity(DamageSource.fireball(this, this.shooter), 6.0F);
            }

            // CraftBukkit start - fire ExplosionPrimeEvent
            /* this.world.createExplosion((Entity) null, this.locX, this.locY, this.locZ, (float) this.yield, true, this.world.getGameRules().getBoolean("mobGriefing")); */
            org.bukkit.craftbukkit.event.CraftEventFactory.handleExplosionPrimeEvent(this, this.locX, this.locY, this.locZ, (float) this.yield, this.isIncendiary, this.world.getGameRules().getBoolean("mobGriefing"));
            // CraftBukkit end
            this.die();
        }
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setInt("ExplosionPower", this.yield);
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        if (nbttagcompound.hasKeyOfType("ExplosionPower", 99)) {
            // CraftBukkit - set bukkitYield when setting explosionpower
            this.bukkitYield = this.yield = nbttagcompound.getInt("ExplosionPower");
        }
    }
}
