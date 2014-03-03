package net.minecraft.server;

// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.player.PlayerTeleportEvent;
// CraftBukkit end

public class EntityEnderPearl extends EntityProjectile {

    public EntityEnderPearl(World world) {
        super(world);
    }

    public EntityEnderPearl(World world, EntityLiving entityliving) {
        super(world, entityliving);
    }

    protected void a(MovingObjectPosition movingobjectposition) {
        if (movingobjectposition.entity != null) {
            movingobjectposition.entity.damageEntity(DamageSource.projectile(this, this.getShooter()), 0.0F);
        }

        for (int i = 0; i < 32; ++i) {
            this.world.addParticle("portal", this.locX, this.locY + this.random.nextDouble() * 2.0D, this.locZ, this.random.nextGaussian(), 0.0D, this.random.nextGaussian());
        }

        if (!this.world.isStatic) {
            if (this.getShooter() != null && this.getShooter() instanceof EntityPlayer) {
                EntityPlayer entityplayer = (EntityPlayer) this.getShooter();

                if (entityplayer.playerConnection.b().isConnected() && entityplayer.world == this.world) {
                    /* CraftBukkit start - Fire PlayerTeleportEvent
                    if (this.getShooter().al()) {
                        this.getShooter().mount((Entity) null);
                    }

                    this.getShooter().enderTeleportTo(this.locX, this.locY, this.locZ);
                    this.getShooter().fallDistance = 0.0F;
                    this.getShooter().damageEntity(DamageSource.FALL, 5.0F);
                    */
                    org.bukkit.craftbukkit.entity.CraftPlayer player = entityplayer.getBukkitEntity();
                    org.bukkit.Location pearlLocation = this.getBukkitEntity().getLocation();
                    pearlLocation.setPitch(entityplayer.pitch);
                    pearlLocation.setYaw(entityplayer.yaw);

                    PlayerTeleportEvent teleEvent = CraftEventFactory.callPlayerTeleportEvent(player, pearlLocation, PlayerTeleportEvent.TeleportCause.ENDER_PEARL);

                    if (!teleEvent.isCancelled() && !entityplayer.playerConnection.isDisconnected()) {
                        entityplayer.playerConnection.teleport(teleEvent.getTo());
                        this.getShooter().fallDistance = 0.0F;

                        org.bukkit.event.entity.EntityDamageEvent damageEvent = CraftEventFactory.callEntityDamageEvent(this, entityplayer, org.bukkit.event.entity.EntityDamageByEntityEvent.DamageCause.FALL, 5.0D);

                        if (!damageEvent.isCancelled() && !entityplayer.playerConnection.isDisconnected()) {
                            entityplayer.invulnerableTicks = -1; // Remove spawning invulnerability
                            entityplayer.damageEntity(DamageSource.FALL, (float) damageEvent.getDamage());
                        }
                    }
                    // CraftBukkit end
                }
            }

            this.die();
        }
    }
}
