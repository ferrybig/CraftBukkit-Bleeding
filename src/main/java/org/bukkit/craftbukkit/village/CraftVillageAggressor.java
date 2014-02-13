package org.bukkit.craftbukkit.village;

import net.minecraft.server.VillageAggressor;

import org.apache.commons.lang.Validate;

import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.village.Village;

public class CraftVillageAggressor implements org.bukkit.village.VillageAggressor {
    private final VillageAggressor aggressor;

    public CraftVillageAggressor(VillageAggressor aggressor) {
        this.aggressor = aggressor;
    }

    public LivingEntity getEntity() {
        return (CraftLivingEntity) aggressor.a.getBukkitEntity(); //should be VillageAggressor.entity
    }

    public Village getVillage() {
        return aggressor.c.getVillage(); //should be VillageAggressor.village
    }

    public int getAggressionTicks() {
        return aggressor.c.maxAggressionTicks - (aggressor.c.time - aggressor.b); //should be VillageAggressor.village and VillageAggressor.aggressionTicks
    }

    public void setAggressionTicks(int ticks) {
        Validate.isTrue(ticks >= 0, "AggressionTicks cannot be less than 0!");
        Validate.isTrue(ticks <= aggressor.c.maxAggressionTicks, "AggressionTicks cannot be greater than " + aggressor.c.maxAggressionTicks + "!");
        aggressor.b = aggressor.c.time - (aggressor.c.maxAggressionTicks - ticks); //should be VillageAggressor.village
    }

    public VillageAggressor getHandle() {
        return aggressor;
    }
}
