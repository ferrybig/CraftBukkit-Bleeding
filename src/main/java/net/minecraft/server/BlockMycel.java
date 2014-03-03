package net.minecraft.server;

import java.util.Random;

import org.bukkit.craftbukkit.event.CraftEventFactory; // CraftBukkit

public class BlockMycel extends Block {

    protected BlockMycel() {
        super(Material.GRASS);
        this.a(true);
        this.a(CreativeModeTab.b);
    }

    public void a(World world, int i, int j, int k, Random random) {
        if (!world.isStatic) {
            if (world.getLightLevel(i, j + 1, k) < 4 && world.getType(i, j + 1, k).k() > 2) {
                // CraftBukkit start
                /* world.setTypeUpdate(i, j, k, Blocks.DIRT); */
                CraftEventFactory.handleBlockFadeEvent(world, i, j, k, Blocks.DIRT);
                // CraftBukkit end
            } else if (world.getLightLevel(i, j + 1, k) >= 9) {
                for (int l = 0; l < 4; ++l) {
                    int i1 = i + random.nextInt(3) - 1;
                    int j1 = j + random.nextInt(5) - 3;
                    int k1 = k + random.nextInt(3) - 1;
                    Block block = world.getType(i1, j1 + 1, k1);

                    if (world.getType(i1, j1, k1) == Blocks.DIRT && world.getData(i1, j1, k1) == 0 && world.getLightLevel(i1, j1 + 1, k1) >= 4 && block.k() <= 2) {
                        // CraftBukkit start
                        /* world.setTypeUpdate(i1, j1, k1, this); */
                        CraftEventFactory.handleBlockSpreadEvent(world, i1, j1, k1, i, j, k, this, 0);
                        // CraftBukkit end
                    }
                }
            }
        }
    }

    public Item getDropType(int i, Random random, int j) {
        return Blocks.DIRT.getDropType(0, random, j);
    }
}
