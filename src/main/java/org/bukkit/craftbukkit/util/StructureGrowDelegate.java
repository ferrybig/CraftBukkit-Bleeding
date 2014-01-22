package org.bukkit.craftbukkit.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.World;

import org.bukkit.BlockChangeDelegate;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.material.MaterialData;

public class StructureGrowDelegate implements BlockChangeDelegate {
    private final CraftWorld world;
    private final List<BlockState> blocks = new ArrayList<BlockState>();

    public StructureGrowDelegate(World world) {
        this.world = world.getWorld();
    }

    @Deprecated
    public boolean setRawTypeId(int x, int y, int z, int type) {
        return setRawTypeIdAndData(x, y, z, type, 0);
    }

    public boolean setRawType(int x, int y, int z, Material type) {
        return setRawTypeAndData(x, y, z, new MaterialData(type));
    }

    @Deprecated
    public boolean setRawTypeIdAndData(int x, int y, int z, int type, int data) {
        BlockState state = world.getBlockAt(x, y, z).getState();
        state.setTypeId(type);
        state.setData(new MaterialData(type, (byte) data));
        blocks.add(state);
        return true;
    }

    public boolean setRawTypeAndData(int x, int y, int z, MaterialData data) {
        BlockState state = world.getBlockAt(x, y, z).getState();
        state.setType(data.getItemType());
        state.setData(data);
        blocks.add(state);
        return true;
    }

    @Deprecated
    public boolean setTypeId(int x, int y, int z, int typeId) {
        return setRawTypeId(x, y, z, typeId);
    }

    public boolean setType(int x, int y, int z, Material type) {
        return setTypeAndData(x, y, z, new MaterialData(type));
    }

    @Deprecated
    public boolean setTypeIdAndData(int x, int y, int z, int typeId, int data) {
        return setRawTypeIdAndData(x, y, z, typeId, data);
    }

    public boolean setTypeAndData(int x, int y, int z, MaterialData data) {
        return setRawTypeAndData(x, y, z, data);
    }

    @Deprecated
    public int getTypeId(int x, int y, int z) {
        return world.getBlockTypeIdAt(x, y, z);
    }

    public Material getMaterialType(int x, int y, int z) {
        return world.getBlockTypeAt(x, y, z);
    }

    public int getHeight() {
        return world.getMaxHeight();
    }

    public List<BlockState> getBlocks() {
        return blocks;
    }

    public boolean isEmpty(int x, int y, int z) {
        return world.getBlockAt(x, y, z).isEmpty();
    }
}
