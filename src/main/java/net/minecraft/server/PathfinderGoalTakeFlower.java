package net.minecraft.server;

import java.util.Iterator;
import java.util.List;

// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTargetEvent;
// CraftBukkit end

public class PathfinderGoalTakeFlower extends PathfinderGoal {

    private EntityVillager a;
    private EntityIronGolem b;
    private int c;
    private boolean d;

    public PathfinderGoalTakeFlower(EntityVillager entityvillager) {
        this.a = entityvillager;
        this.a(3);
    }

    public boolean a() {
        if (this.a.getAge() >= 0) {
            return false;
        } else if (!this.a.world.v()) {
            return false;
        } else {
            List list = this.a.world.a(EntityIronGolem.class, this.a.boundingBox.grow(6.0D, 2.0D, 6.0D));

            if (list.isEmpty()) {
                return false;
            } else {
                Iterator iterator = list.iterator();

                while (iterator.hasNext()) {
                    EntityIronGolem entityirongolem = (EntityIronGolem) iterator.next();

                    if (entityirongolem.bZ() > 0) {
                        // CraftBukkit start - call EntityTargetEvent
                        org.bukkit.event.entity.EntityTargetLivingEntityEvent event = CraftEventFactory.callEntityTargetLivingEvent(this.a, this.b, EntityTargetEvent.TargetReason.LURED);

                        if (event.isCancelled()) {
                            return false;
                        } else if (event.getTarget() == null) {
                            entityirongolem = null;
                        } else {
                            EntityLiving target = ((org.bukkit.craftbukkit.entity.CraftLivingEntity) event.getTarget()).getHandle();

                            if (target instanceof EntityIronGolem) {
                                entityirongolem = (EntityIronGolem) target;
                            }
                        }
                        // CraftBukkit end
                        this.b = entityirongolem;
                        break;
                    }
                }

                return this.b != null;
            }
        }
    }

    public boolean b() {
        return this.b.bZ() > 0;
    }

    public void c() {
        this.c = this.a.aI().nextInt(320);
        this.d = false;
        this.b.getNavigation().h();
    }

    public void d() {
        // CraftBukkit start
        EntityTargetEvent.TargetReason reason = this.b.isAlive() ? EntityTargetEvent.TargetReason.FORGOT_TARGET : EntityTargetEvent.TargetReason.TARGET_DIED;
        CraftEventFactory.callEntityTargetEvent(this.b, null, reason);
        // CraftBukkit end
        this.b = null;
        this.a.getNavigation().h();
    }

    public void e() {
        this.a.getControllerLook().a(this.b, 30.0F, 30.0F);
        if (this.b.bZ() == this.c) {
            this.a.getNavigation().a((Entity) this.b, 0.5D);
            this.d = true;
        }

        if (this.d && this.a.e(this.b) < 4.0D) {
            this.b.a(false);
            this.a.getNavigation().h();
        }
    }
}
