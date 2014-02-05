package net.minecraft.server;

// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
// CraftBukkit end

public class PathfinderGoalBeg extends PathfinderGoal {

    private EntityWolf a;
    private EntityHuman b;
    private World c;
    private float d;
    private int e;

    public PathfinderGoalBeg(EntityWolf entitywolf, float f) {
        this.a = entitywolf;
        this.c = entitywolf.world;
        this.d = f;
        this.a(2);
    }

    public boolean a() {
        this.b = this.c.findNearbyPlayer(this.a, (double) this.d);
        // CraftBukkit start - call EntityTargetEvent
        if (this.b == null || !this.a(this.b)) {
            return false;
        }

        org.bukkit.event.entity.EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(this.a, this.b, EntityTargetEvent.TargetReason.LURED);

        if (event.isCancelled()) {
            return false;
        } else if (event.getTarget() == null) {
            this.b = null;
        } else {
            EntityLiving target = ((org.bukkit.craftbukkit.entity.CraftLivingEntity) event.getTarget()).getHandle();

            if (target instanceof EntityHuman) {
                this.b = (EntityHuman) target;
            }
        }

        return this.b != null;
        // CraftBukkit end
    }

    public boolean b() {
        return !this.b.isAlive() ? false : (this.a.e(this.b) > (double) (this.d * this.d) ? false : this.e > 0 && this.a(this.b));
    }

    public void c() {
        this.a.m(true);
        this.e = 40 + this.a.aI().nextInt(40);
    }

    public void d() {
        this.a.m(false);
        // CraftBukkit start
        EntityTargetEvent.TargetReason reason = this.b.isAlive() ? EntityTargetEvent.TargetReason.FORGOT_TARGET : EntityTargetEvent.TargetReason.TARGET_DIED;
        CraftEventFactory.callEntityTargetEvent(this.b, null, reason);
        // CraftBukkit end
        this.b = null;
    }

    public void e() {
        this.a.getControllerLook().a(this.b.locX, this.b.locY + (double) this.b.getHeadHeight(), this.b.locZ, 10.0F, (float) this.a.x());
        --this.e;
    }

    private boolean a(EntityHuman entityhuman) {
        ItemStack itemstack = entityhuman.inventory.getItemInHand();

        return itemstack == null ? false : (!this.a.isTamed() && itemstack.getItem() == Items.BONE ? true : this.a.c(itemstack));
    }
}
