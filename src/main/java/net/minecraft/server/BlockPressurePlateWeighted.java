package net.minecraft.server;

public class BlockPressurePlateWeighted extends BlockPressurePlateAbstract {
    private final int a;

    protected BlockPressurePlateWeighted(String s, Material material, int i) {
        super(s, material);
        this.a = i;
    }

    protected int e(World world, int i, int j, int k) {
        // CraftBukkit start
        int l = 0; /* Math.min(world.a(Entity.class, this.a(i, j, k)).size(), this.a); */

        for (Object entity : world.a(Entity.class, this.a(i, j, k))) {
            // We only want to block turning the plate on if all events are cancelled
            if (org.bukkit.craftbukkit.event.CraftEventFactory.handleInteractEvent((Entity) entity, world, i, j, k)) {
                l++;
            }
        }

        l = Math.min(l, this.a);
        // CraftBukkit end

        if (l <= 0) {
            return 0;
        }

        float f = (float) Math.min(this.a, l) / (float) this.a;
        return MathHelper.f(f * 15.0F);
    }

    protected int c(int i) {
        return i;
    }

    protected int d(int i) {
        return i;
    }

    public int a(World world) {
        return 10;
    }
}
