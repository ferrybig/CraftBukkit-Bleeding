package net.minecraft.server;

import java.util.Iterator;
import java.util.List;

import org.bukkit.craftbukkit.event.CraftEventFactory; // CraftBukkit

public class ItemLeash extends Item {

    public ItemLeash() {
        this.a(CreativeModeTab.i);
    }

    public boolean interactWith(ItemStack itemstack, EntityHuman entityhuman, World world, int i, int j, int k, int l, float f, float f1, float f2) {
        Block block = world.getType(i, j, k);

        if (block.b() == 11) {
            if (world.isStatic) {
                return true;
            } else {
                a(entityhuman, world, i, j, k);
                return true;
            }
        } else {
            return false;
        }
    }

    public static boolean a(EntityHuman entityhuman, World world, int i, int j, int k) {
        EntityLeash entityleash = EntityLeash.b(world, i, j, k);
        boolean flag = false;
        double d0 = 7.0D;
        List list = world.a(EntityInsentient.class, AxisAlignedBB.a((double) i - d0, (double) j - d0, (double) k - d0, (double) i + d0, (double) j + d0, (double) k + d0));

        if (list != null) {
            Iterator iterator = list.iterator();

            while (iterator.hasNext()) {
                EntityInsentient entityinsentient = (EntityInsentient) iterator.next();

                if (entityinsentient.bN() && entityinsentient.getLeashHolder() == entityhuman) {
                    if (entityleash == null) {
                        entityleash = EntityLeash.a(world, i, j, k);

                        // CraftBukkit start - fire HangingPlaceEvent
                        if (CraftEventFactory.handleHangingPlaceEvent(entityleash, entityhuman, world, i, j, k, org.bukkit.block.BlockFace.SELF)) {
                            entityleash.die();
                            return false;
                        }
                        // CraftBukkit end
                    }

                    // CraftBukkit start
                    if (!CraftEventFactory.handlePlayerLeashEntityEvent(entityinsentient, entityleash, entityhuman)) {
                        continue;
                    }
                    // CraftBukkit end

                    entityinsentient.setLeashHolder(entityleash, true);
                    flag = true;
                }
            }
        }

        return flag;
    }
}
