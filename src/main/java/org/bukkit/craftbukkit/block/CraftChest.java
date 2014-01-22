package org.bukkit.craftbukkit.block;

import net.minecraft.server.TileEntityChest;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest;
import org.bukkit.inventory.Inventory;

public class CraftChest extends CraftBlockState implements Chest {
    private final CraftWorld world;
    private final TileEntityChest chest;

    public CraftChest(final Block block) {
        super(block);

        world = (CraftWorld) block.getWorld();
        chest = (TileEntityChest) world.getTileEntityAt(getX(), getY(), getZ());
    }

    public Inventory getBlockInventory() {
        return new CraftInventory(chest);
    }

    public Inventory getInventory() {
        int x = getX();
        int y = getY();
        int z = getZ();
        // The logic here is basically identical to the logic in BlockChest.interact
        CraftInventory inventory = new CraftInventory(chest);
        Material material;
        if (world.getBlockTypeAt(x, y, z) == Material.CHEST) {
            material = Material.CHEST;
        } else if (world.getBlockTypeAt(x, y, z) == Material.TRAPPED_CHEST) {
            material = Material.TRAPPED_CHEST;
        } else {
            throw new IllegalStateException("CraftChest is not a chest but is instead " + world.getBlockAt(x, y, z));
        }

        if (world.getBlockTypeAt(x - 1, y, z) == material) {
            CraftInventory left = new CraftInventory((TileEntityChest)world.getHandle().getTileEntity(x - 1, y, z));
            inventory = new CraftInventoryDoubleChest(left, inventory);
        }
        if (world.getBlockTypeAt(x + 1, y, z) == material) {
            CraftInventory right = new CraftInventory((TileEntityChest) world.getHandle().getTileEntity(x + 1, y, z));
            inventory = new CraftInventoryDoubleChest(inventory, right);
        }
        if (world.getBlockTypeAt(x, y, z - 1) == material) {
            CraftInventory left = new CraftInventory((TileEntityChest) world.getHandle().getTileEntity(x, y, z - 1));
            inventory = new CraftInventoryDoubleChest(left, inventory);
        }
        if (world.getBlockTypeAt(x, y, z + 1) == material) {
            CraftInventory right = new CraftInventory((TileEntityChest) world.getHandle().getTileEntity(x, y, z + 1));
            inventory = new CraftInventoryDoubleChest(inventory, right);
        }
        return inventory;
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        boolean result = super.update(force, applyPhysics);

        if (result) {
            chest.update();
        }

        return result;
    }
}
