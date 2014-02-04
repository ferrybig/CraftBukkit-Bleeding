package net.minecraft.server;

// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
// CraftBukkit end

public class PathfinderGoalTempt extends PathfinderGoal {

    private EntityCreature a;
    private double b;
    private double c;
    private double d;
    private double e;
    private double f;
    private double g;
    private Entity h; // CraftBukkit - changed to Entity
    private int i;
    private boolean j;
    private Item k;
    private boolean l;
    private boolean m;

    public PathfinderGoalTempt(EntityCreature entitycreature, double d0, Item item, boolean flag) {
        this.a = entitycreature;
        this.b = d0;
        this.k = item;
        this.l = flag;
        this.a(3);
    }

    public boolean a() {
        if (this.i > 0) {
            --this.i;
            return false;
        } else {
            this.h = this.a.world.findNearbyPlayer(this.a, 10.0D);
            if (this.h == null) {
                return false;
            } else {
                ItemStack itemstack = ((EntityHuman) this.h).bD(); // CraftBukkit - cast to EntityHuman

                // CraftBukkit start - call EntityTargetEvent
                if (itemstack == null || itemstack.getItem() != this.k) {
                    return false;
                } else {
                    EntityTargetEvent event = CraftEventFactory.callEntityTargetEvent(this.a, this.h, EntityTargetEvent.TargetReason.LURED);

                    if (event.isCancelled()) {
                        return false;
                    }

                    this.h = event.getTarget() == null ? null : ((org.bukkit.craftbukkit.entity.CraftEntity) event.getTarget()).getHandle();
                    return this.h != null;
                }
                // CraftBukkit end
            }
        }
    }

    public boolean b() {
        if (this.l) {
            if (this.a.e(this.h) < 36.0D) {
                if (this.h.e(this.c, this.d, this.e) > 0.010000000000000002D) {
                    return false;
                }

                if (Math.abs((double) this.h.pitch - this.f) > 5.0D || Math.abs((double) this.h.yaw - this.g) > 5.0D) {
                    return false;
                }
            } else {
                this.c = this.h.locX;
                this.d = this.h.locY;
                this.e = this.h.locZ;
            }

            this.f = (double) this.h.pitch;
            this.g = (double) this.h.yaw;
        }

        return this.a();
    }

    public void c() {
        this.c = this.h.locX;
        this.d = this.h.locY;
        this.e = this.h.locZ;
        this.j = true;
        this.m = this.a.getNavigation().a();
        this.a.getNavigation().a(false);
    }

    public void d() {
        // CraftBukkit start
        EntityTargetEvent.TargetReason reason = this.h.isAlive() ? EntityTargetEvent.TargetReason.FORGOT_TARGET : EntityTargetEvent.TargetReason.TARGET_DIED;
        CraftEventFactory.callEntityTargetEvent(this.a, null, reason);
        // CraftBukkit end
        this.h = null;
        this.a.getNavigation().h();
        this.i = 100;
        this.j = false;
        this.a.getNavigation().a(this.m);
    }

    public void e() {
        this.a.getControllerLook().a(this.h, 30.0F, (float) this.a.x());
        if (this.a.e(this.h) < 6.25D) {
            this.a.getNavigation().h();
        } else {
            this.a.getNavigation().a((Entity) this.h, this.b);
        }
    }

    public boolean f() {
        return this.j;
    }
}
