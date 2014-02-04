package net.minecraft.server;

// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
// CraftBukkit end

public class PathfinderGoalFollowOwner extends PathfinderGoal {

    private EntityTameableAnimal d;
    private EntityLiving e;
    World a;
    private double f;
    private Navigation g;
    private int h;
    float b;
    float c;
    private boolean i;

    public PathfinderGoalFollowOwner(EntityTameableAnimal entitytameableanimal, double d0, float f, float f1) {
        this.d = entitytameableanimal;
        this.a = entitytameableanimal.world;
        this.f = d0;
        this.g = entitytameableanimal.getNavigation();
        this.c = f;
        this.b = f1;
        this.a(3);
    }

    public boolean a() {
        EntityLiving entityliving = this.d.getOwner();

        if (entityliving == null) {
            return false;
        } else if (this.d.isSitting()) {
            return false;
        } else if (this.d.e(entityliving) < (double) (this.c * this.c)) {
            return false;
        } else {
            // CraftBukkit start - call EntityTargetEvent
            org.bukkit.event.entity.EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(this.d, entityliving, EntityTargetEvent.TargetReason.FOLLOW);

            if (event.isCancelled()) {
                return false;
            }

            entityliving = event.getTarget() == null ? null : ((org.bukkit.craftbukkit.entity.CraftLivingEntity) event.getTarget()).getHandle();
            this.e = entityliving;
            return this.e != null;
            // CraftBukkit end
        }
    }

    public boolean b() {
        return !this.g.g() && this.d.e(this.e) > (double) (this.b * this.b) && !this.d.isSitting();
    }

    public void c() {
        this.h = 0;
        this.i = this.d.getNavigation().a();
        this.d.getNavigation().a(false);
    }

    public void d() {
        // CraftBukkit start
        EntityTargetEvent.TargetReason reason = this.e.isAlive() ? EntityTargetEvent.TargetReason.FORGOT_TARGET : EntityTargetEvent.TargetReason.TARGET_DIED;
        CraftEventFactory.callEntityTargetEvent(this.e, null, reason);
        // CraftBukkit end
        this.e = null;
        this.g.h();
        this.d.getNavigation().a(this.i);
    }

    public void e() {
        this.d.getControllerLook().a(this.e, 10.0F, (float) this.d.x());
        if (!this.d.isSitting()) {
            if (--this.h <= 0) {
                this.h = 10;
                if (!this.g.a((Entity) this.e, this.f)) {
                    if (!this.d.bL()) {
                        if (this.d.e(this.e) >= 144.0D) {
                            int i = MathHelper.floor(this.e.locX) - 2;
                            int j = MathHelper.floor(this.e.locZ) - 2;
                            int k = MathHelper.floor(this.e.boundingBox.b);

                            for (int l = 0; l <= 4; ++l) {
                                for (int i1 = 0; i1 <= 4; ++i1) {
                                    if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && World.a((IBlockAccess) this.a, i + l, k - 1, j + i1) && !this.a.getType(i + l, k, j + i1).r() && !this.a.getType(i + l, k + 1, j + i1).r()) {
                                        this.d.setPositionRotation((double) ((float) (i + l) + 0.5F), (double) k, (double) ((float) (j + i1) + 0.5F), this.d.yaw, this.d.pitch);
                                        this.g.h();
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
