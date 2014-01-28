package org.bukkit.craftbukkit.block;

import net.minecraft.server.Chunk;
import net.minecraft.server.PacketPlayOutBlockChange;
import net.minecraft.server.TileEntityFlowerPot;
import net.minecraft.server.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.FlowerPot;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class CraftFlowerPot extends CraftBlockState implements FlowerPot {
    private final CraftWorld world;
    private final TileEntityFlowerPot flowerPot;

    public CraftFlowerPot(final Block block) {
        super(block);

        world = (CraftWorld) block.getWorld();
        flowerPot = (TileEntityFlowerPot) world.getTileEntityAt(getX(), getY(), getZ());
    }

    @Override
    public ItemStack getPlant() {
        Material type = CraftMagicNumbers.getMaterial(flowerPot.a());

        return type == null ? null : new MaterialData(type, (byte) flowerPot.b()).toItemStack(1);
    }

    @Override
    public void setPlant(ItemStack plant) {
        flowerPot.a(CraftMagicNumbers.getItem(plant.getType()), plant.getData().getData());
        flowerPot.update();
        setRawData((byte) flowerPot.b());
    }
}
