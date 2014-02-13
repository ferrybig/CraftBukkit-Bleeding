package org.bukkit.craftbukkit.entity;

import net.minecraft.server.EntityVillager;

import org.apache.commons.lang.Validate;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.village.CraftVillage;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.village.Village;

public class CraftVillager extends CraftAgeable implements Villager {
    public CraftVillager(CraftServer server, EntityVillager entity) {
        super(server, entity);
    }

    @Override
    public EntityVillager getHandle() {
        return (EntityVillager) entity;
    }

    @Override
    public String toString() {
        return "CraftVillager";
    }

    public EntityType getType() {
        return EntityType.VILLAGER;
    }

    public Profession getProfession() {
        switch (getHandle().getProfession()) {
            case 0:
            default:
                return Profession.FARMER;
            case 1:
                return Profession.LIBRARIAN;
            case 2:
                return Profession.PRIEST;
            case 3:
                return Profession.BLACKSMITH;
            case 4:
                return Profession.BUTCHER;
        }
    }

    public void setProfession(Profession profession) {
        Validate.notNull(profession);
        getHandle().setProfession(getProfessionId(profession));
    }

    public Village getVillage() {
        return getHandle().village == null ? null : getHandle().village.getVillage();
    }

    public void setVillage(Village village) {
        if (village instanceof CraftVillage) {
            getHandle().village = ((CraftVillage) village).getHandle();
        }
    }

    public static int getProfessionId(Profession profession) {
        switch (profession) {
            case FARMER:
            default:
                return 0;
            case LIBRARIAN:
                return 1;
            case PRIEST:
                return 2;
            case BLACKSMITH:
                return 3;
            case BUTCHER:
                return 4;
        }
    }
}
