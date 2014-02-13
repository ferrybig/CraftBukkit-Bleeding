package net.minecraft.server;

public class VillageAggressor { // CraftBukkit - made public

    public EntityLiving a;
    public int b;
    public final Village c; // CraftBukkit - made public

    VillageAggressor(Village village, EntityLiving entityliving, int i) {
        this.c = village;
        this.a = entityliving;
        this.b = i;
    }
}
