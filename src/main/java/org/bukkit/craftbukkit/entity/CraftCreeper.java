package org.bukkit.craftbukkit.entity;

import net.minecraft.server.EntityCreeper;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreeperPowerEvent;

public class CraftCreeper extends CraftMonster implements Creeper {

    public CraftCreeper(CraftServer server, EntityCreeper entity) {
        super(server, entity);
    }

    public boolean isPowered() {
        return getHandle().isPowered();
    }

    public void setPowered(boolean powered) {
        if (CraftEventFactory.callCreeperPowerEvent(getHandle(), null, powered ? CreeperPowerEvent.PowerCause.SET_ON : CreeperPowerEvent.PowerCause.SET_OFF).isCancelled()) {
            getHandle().getDataWatcher().watch(17, Byte.valueOf((byte) (powered ? 1 : 0)));
        }
    }

    @Override
    public EntityCreeper getHandle() {
        return (EntityCreeper) entity;
    }

    @Override
    public String toString() {
        return "CraftCreeper";
    }

    public EntityType getType() {
        return EntityType.CREEPER;
    }
}
