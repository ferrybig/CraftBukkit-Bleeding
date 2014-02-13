package org.bukkit.craftbukkit.village;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.server.Village;
import net.minecraft.server.VillageDoor;
import net.minecraft.server.WorldServer;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.village.VillageManager;

public class CraftVillageManager implements VillageManager {
    private final WorldServer world;
    private final Map<Village, CraftVillage> villages = new HashMap<Village, CraftVillage>();
    private final Map<VillageDoor, CraftVillageDoor> doors = new HashMap<VillageDoor, CraftVillageDoor>();

    public CraftVillageManager(WorldServer world) {
        this.world = world;
    }

    public List<org.bukkit.village.Village> getVillages() {
        return new ArrayList<org.bukkit.village.Village>(villages.values());
    }

    public org.bukkit.village.Village getVillage(Location location) {
        return getVillage(location, 16);
    }

    public org.bukkit.village.Village getVillage(Location location, int range) {
        Validate.isTrue(range >= 1, "Range cannot be less than 1!");
        Validate.isTrue(range <= 64, "Range cannot be greater than 64!");
        Village found = world.villages.getClosestVillage(location.getBlockX(), location.getBlockY(), location.getBlockZ(), range);

        return found == null ? null : villages.get(found);
    }

    public List<org.bukkit.village.VillageDoor> getDoors(Village village) {
        List<org.bukkit.village.VillageDoor> doors = new ArrayList<org.bukkit.village.VillageDoor>();

        for (Iterator i = this.doors.entrySet().iterator(); i.hasNext();) {
            Map.Entry<VillageDoor, CraftVillageDoor> entry = (Map.Entry<VillageDoor, CraftVillageDoor>) i.next();

            if (village.getDoors().contains(entry.getKey())) {
                doors.add(entry.getValue());
            }
        }

        return doors;
    }

    public org.bukkit.village.Village getVillageUnderSiege() {
        return world.siegeManager.b ? villages.get(world.siegeManager.f) : null; //should be VillageSiege.isSiegeActive and VillageSiege.village
    }

    public void addVillage(Village village) {
        villages.put(village, new CraftVillage(village));
    }

    public void addVillage(CraftVillage village) {
        villages.put(village.getHandle(), village);
    }

    public CraftVillage getVillage(Village village) {
        return villages.get(village);
    }

    public void removeVillage(Village village) {
        villages.remove(village);
    }

    public void addDoor(CraftVillageDoor door) {
        doors.put(door.getHandle(), door);
    }

    public void removeDoor(VillageDoor door) {
        doors.remove(door);
    }
}
