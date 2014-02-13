package org.bukkit.craftbukkit.village;

import net.minecraft.server.Village;
import net.minecraft.server.VillageDoor;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;

public class CraftVillageDoor implements org.bukkit.village.VillageDoor {
    private final VillageDoor door;
    private final CraftVillage village;
    private final int x;
    private final int y;
    private final int z;

    public CraftVillageDoor(VillageDoor door, Village village) {
        this.door = door;
        this.village = village.getVillage();
        this.x = door.locX;
        this.y = door.locY;
        this.z = door.locZ;
    }

    public Location getLocation() {
        return new Location(getWorld(), x, y, z);
    }

    public Location getLocation(Location location) {
        if (location != null) {
            location.setWorld(getWorld());
            location.setX(x);
            location.setY(y);
            location.setZ(z);
            location.setYaw(0);
            location.setPitch(0);
        }

        return location;
    }

    public CraftVillage getVillage() {
        return village;
    }

    public CraftWorld getWorld() {
        return village.getWorld();
    }

    public int getAge() {
        return village.getHandle().time - door.addedTime;
    }

    public void setAge(int ticks) {
        door.addedTime = village.getHandle().time - ticks;
    }

    public VillageDoor getHandle() {
        return door;
    }

    @Override
    public String toString() {
        return "CraftVillageDoor{" + "village=" + village + ",x=" + x + ",y=" + y + ",z=" + z + ",age=" + getAge() + "}";
    }
}
