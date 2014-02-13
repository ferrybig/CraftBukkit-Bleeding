package org.bukkit.craftbukkit.village;

import net.minecraft.server.ChunkCoordinates;
import net.minecraft.server.EntityIronGolem;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityVillager;
import net.minecraft.server.GroupDataEntity;
import net.minecraft.server.MathHelper;
import net.minecraft.server.Vec3D;
import net.minecraft.server.Village;
import net.minecraft.server.VillageAggressor;

import org.apache.commons.lang.Validate;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftIronGolem;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftVillager;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.village.VillageDoor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CraftVillage implements org.bukkit.village.Village {
    private final Village village;

    public CraftVillage(Village village) {
        this.village = village;
    }

    public Location getLocation() {
        ChunkCoordinates center = village.getCenter();
        return new Location(getWorld(), center.x, center.y, center.z);
    }

    public Location getLocation(Location location) {
        if (location != null) {
            ChunkCoordinates center = village.getCenter();

            location.setWorld(getWorld());
            location.setX(center.x);
            location.setY(center.y);
            location.setZ(center.z);
            location.setYaw(0);
            location.setPitch(0);
        }

        return location;
    }

    public int getRadius() {
        return village.getSize();
    }

    public int getMaxAggressionTicks() {
        return village.maxAggressionTicks;
    }

    public void setMaxAggressionTicks(int ticks) {
        village.maxAggressionTicks = ticks;
    }

    public boolean isAggressor(LivingEntity entity) {
        for (Object aggressor : village.aggressors) {
            if (((VillageAggressor) aggressor).a.getBukkitEntity() == entity) { //should be VillageAggressor.entity
                return true;
            }
        }

        return false;
    }

    public void addAggressor(LivingEntity entity) {
        village.a(((CraftLivingEntity) entity).getHandle()); //should be Village.addAggressor
    }

    public void removeAggressor(LivingEntity entity) {
        Iterator iterator = village.aggressors.iterator();

        while (iterator.hasNext()) {
            EntityLiving entityliving = ((VillageAggressor) iterator.next()).a; //should be VillageAggressor.entity

            if (entityliving == ((CraftLivingEntity) entity).getHandle()) {
                iterator.remove();
            }
        }
    }

    public List<LivingEntity> getAggressors() {
        List<LivingEntity> aggressors = new ArrayList<LivingEntity>(village.aggressors.size());

        for (Object aggressor : village.aggressors) {
            aggressors.add((CraftLivingEntity) ((VillageAggressor) aggressor).a.getBukkitEntity()); //should be VillageAggressor.entity
        }

        return aggressors;
    }

    public int getAggressorCount() {
        return village.aggressors.size();
    }

    public int getPopularity(Player player) {
        return village.a(player.getName()); //should be Village.getPopularity
    }

    public void setPopularity(Player player, int popularity) {
        Validate.isTrue(popularity >= -30, "Popularity cannot be less than -30!");
        Validate.isTrue(popularity <= 10, "Popularity cannot be greater than 10!");
        village.playerStandings.put(player.getName(), Integer.valueOf(popularity));
    }

    public int getBreedWaitTime() {
        return village.breedWaitTime;
    }

    public void setBreedWaitTime(int waitTime) {
        village.breedWaitTime = waitTime;
    }

    public int getNoBreedTicks() {
        return village.noBreedTicks;
    }

    public void setNoBreedTicks(int noBreedTicks) {
        village.noBreedTicks = noBreedTicks;
    }

    public boolean canBreed() {
        return village.i(); //should be Village.canBreed
    }

    public boolean isAbandoned() {
        return village.isAbandoned();
    }

    public void abandon() {
        village.getDoors().clear();
    }

    public int getDoorIdleTime() {
        return village.doorIdleTime;
    }

    public void setDoorIdleTime(int idleTime) {
        village.doorIdleTime = idleTime;
    }

    public List<VillageDoor> getDoors() {
        return getWorld().getVillageManager().getDoors(village);
    }

    public int getDoorCount() {
        return village.getDoorCount();
    }

    public List<Villager> getVillagers() {
        List<Villager> villagers = new ArrayList<Villager>();

        for (Villager villager : village.world.getWorld().getEntitiesByClass(Villager.class)) {
            if (villager.getVillage() == this.village.getVillage()) {
                villagers.add(villager);
            }
        }

        return villagers;
    }

    public List<Villager> getVillagersByProfession(Villager.Profession profession) {
        List<Villager> villagers = new ArrayList<Villager>();

        for (Villager villager : getWorld().getEntitiesByClass(Villager.class)) {
            if (villager.getVillage() == this.village.getVillage() && villager.getProfession() == profession) {
                villagers.add(villager);
            }
        }

        return villagers;
    }

    public Villager spawnVillager() {
        return spawnVillager(village.world.random.nextInt(5), false);
    }

    public Villager spawnVillager(Villager.Profession profession) {
        return spawnVillager(CraftVillager.getProfessionId(profession), false);
    }

    public Villager spawnVillager(Villager.Profession profession, boolean isBaby) {
        return spawnVillager(CraftVillager.getProfessionId(profession), isBaby);
    }

    private Villager spawnVillager(int professionId, boolean isBaby) {
        EntityVillager villager = new EntityVillager(village.world);
        villager.a((GroupDataEntity) null);
        villager.setProfession(professionId);
        villager.village = this.village;

        if (isBaby) {
            villager.setAge(-24000);
        }

        ChunkCoordinates center = village.getCenter();
        Vec3D vec3D = village.a(MathHelper.d((float) center.x), MathHelper.d((float) center.y), MathHelper.d((float) center.z), 2, 4, 2);
        if (vec3D == null) {
            return null;
        }

        villager.setPosition(vec3D.c, vec3D.d, vec3D.e);
        village.world.addEntity(villager, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return (CraftVillager) villager.getBukkitEntity();
    }


    public int getVillagerPopulation() {
        return village.getPopulationCount();
    }

    public List<IronGolem> getIronGolems() {
        List<IronGolem> golems = new ArrayList<IronGolem>();

        for (IronGolem golem : getWorld().getEntitiesByClass(IronGolem.class)) {
            if (golem.getVillage() == this.village.getVillage()) {
                golems.add(golem);
            }
        }

        return golems;
    }

    public IronGolem spawnIronGolem() {
        EntityIronGolem golem = new EntityIronGolem(village.world);
        golem.a((GroupDataEntity) null);
        golem.bp = this.village;

        ChunkCoordinates center = village.getCenter();
        Vec3D vec3D = village.a(MathHelper.d((float) center.x), MathHelper.d((float) center.y), MathHelper.d((float) center.z), 2, 4, 2);
        if (vec3D == null) {
            return null;
        }

        golem.setPosition(vec3D.c, vec3D.d, vec3D.e);
        village.world.addEntity(golem, CreatureSpawnEvent.SpawnReason.CUSTOM);
        village.ironGolemCount++;
        return (CraftIronGolem) golem.getBukkitEntity();
    }

    public int getIronGolemPopulation() {
        return village.ironGolemCount;
    }

    public boolean isUnderSiege() {
        return this.equals(getWorld().getVillageManager().getVillageUnderSiege());
    }

    public Village getHandle() {
        return village;
    }

    public CraftWorld getWorld() {
        return village.world.getWorld();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CraftVillage)) {
            return false;
        }

        return object == this;
    }

    @Override
    public String toString() {
        return "CraftVillage{" + "world=" + village.world + ",x=" + village.getCenter().x + ",y=" + village.getCenter().y + ",z=" + village.getCenter().z + ",size=" + getRadius() + "}";
    }
}
