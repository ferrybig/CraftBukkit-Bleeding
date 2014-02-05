package net.minecraft.server;

// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
// CraftBukkit end

public class PathfinderGoalOfferFlower extends PathfinderGoal {

    private EntityIronGolem a;
    private EntityLiving b; // CraftBukkit - changed to EntityLiving
    private int c;

    public PathfinderGoalOfferFlower(EntityIronGolem entityirongolem) {
        this.a = entityirongolem;
        this.a(3);
    }

    public boolean a() {
        if (!this.a.world.v()) {
            return false;
        } else if (this.a.aI().nextInt(8000) != 0) {
            return false;
        } else {
            this.b = (EntityVillager) this.a.world.a(EntityVillager.class, this.a.boundingBox.grow(6.0D, 2.0D, 6.0D), (Entity) this.a);
            // CraftBukkit start - call EntityTargetEvent
            if (this.b == null) {
                return false;
            }

            org.bukkit.event.entity.EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(this.a, this.b, EntityTargetEvent.TargetReason.OFFER_FLOWER);

            if (event.isCancelled()) {
                return false;
            }

            this.b = event.getTarget() == null ? null : ((org.bukkit.craftbukkit.entity.CraftLivingEntity) event.getTarget()).getHandle();
            // CraftBukkit end
            return this.b != null;
        }
    }

    public boolean b() {
        return this.c > 0;
    }

    public void c() {
        this.c = 400;
        this.a.a(true);
    }

    public void d() {
        this.a.a(false);
        // CraftBukkit start
        EntityTargetEvent.TargetReason reason = this.b.isAlive() ? EntityTargetEvent.TargetReason.FORGOT_TARGET : EntityTargetEvent.TargetReason.TARGET_DIED;
        CraftEventFactory.callEntityTargetEvent(this.b, null, reason);
        // CraftBukkit end
        this.b = null;
    }

    public void e() {
        this.a.getControllerLook().a(this.b, 30.0F, 30.0F);
        --this.c;
    }
}
