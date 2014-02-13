package org.bukkit.craftbukkit.entity;

import net.minecraft.server.EntityIronGolem;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.village.CraftVillage;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.village.Village;

public class CraftIronGolem extends CraftGolem implements IronGolem {
    public CraftIronGolem(CraftServer server, EntityIronGolem entity) {
        super(server, entity);
    }

    @Override
    public EntityIronGolem getHandle() {
        return (EntityIronGolem) entity;
    }

    @Override
    public String toString() {
        return "CraftIronGolem";
    }

    public boolean isPlayerCreated() {
        return getHandle().isPlayerCreated();
    }

    public void setPlayerCreated(boolean playerCreated) {
        getHandle().setPlayerCreated(playerCreated);
    }

    @Override
    public EntityType getType() {
        return EntityType.IRON_GOLEM;
    }

    public Village getVillage() {
        return getHandle().bX() == null ? null : getHandle().bX().getVillage(); //should be EntityIronGolem.getVillage
    }

    public void setVillage(Village village) {
        if (village instanceof CraftVillage) {
            getHandle().bp = ((CraftVillage) village).getHandle(); //should be IronGolem.village
        }
    }
}
