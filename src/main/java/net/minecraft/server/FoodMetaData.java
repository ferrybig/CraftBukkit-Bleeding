package net.minecraft.server;

import org.bukkit.craftbukkit.event.CraftEventFactory;

public class FoodMetaData {

    // CraftBukkit start - All made public
    public int foodLevel = 20;
    public float saturationLevel = 5.0F;
    public float exhaustionLevel;
    public int foodTickTimer;
    private EntityHuman entityhuman;
    // CraftBukkit end
    private int e = 20;

    public FoodMetaData() { throw new AssertionError("Whoopsie, we missed the bukkit."); } // CraftBukkit start - throw an error

    // CraftBukkit start - added EntityHuman constructor
    public FoodMetaData(EntityHuman entityhuman) {
        org.apache.commons.lang.Validate.notNull(entityhuman);
        this.entityhuman = entityhuman;
    }
    // CraftBukkit end

    public void eat(int i, float f) {
        this.foodLevel = Math.min(i + this.foodLevel, 20);
        this.saturationLevel = Math.min(this.saturationLevel + (float) i * f * 2.0F, (float) this.foodLevel);
    }

    public void a(ItemFood itemfood, ItemStack itemstack) {
        // CraftBukkit start
        /* this.eat(itemfood.getNutrition(itemstack), itemfood.getSaturationModifier(itemstack)); */
        CraftEventFactory.handleFoodLevelChangeEvent(entityhuman, itemfood.getNutrition(itemstack), itemfood.getSaturationModifier(itemstack), true);
        // CraftBukkit end
    }

    public void a(EntityHuman entityhuman) {
        EnumDifficulty enumdifficulty = entityhuman.world.difficulty;

        this.e = this.foodLevel;
        if (this.exhaustionLevel > 4.0F) {
            this.exhaustionLevel -= 4.0F;
            if (this.saturationLevel > 0.0F) {
                this.saturationLevel = Math.max(this.saturationLevel - 1.0F, 0.0F);
            } else if (enumdifficulty != EnumDifficulty.PEACEFUL) {
                // CraftBukkit start
                /* this.foodLevel = Math.max(this.foodLevel - 1, 0); */
                CraftEventFactory.handleFoodLevelChangeEvent(entityhuman, Math.max(this.foodLevel - 1, 0), this.saturationLevel, false);
                // CraftBukkit end
            }
        }

        if (entityhuman.world.getGameRules().getBoolean("naturalRegeneration") && this.foodLevel >= 18 && entityhuman.bQ()) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 80) {
                entityhuman.heal(1.0F, org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason.SATIATED); // CraftBukkit - added RegainReason
                this.a(3.0F);
                this.foodTickTimer = 0;
            }
        } else if (this.foodLevel <= 0) {
            ++this.foodTickTimer;
            if (this.foodTickTimer >= 80) {
                if (entityhuman.getHealth() > 10.0F || enumdifficulty == EnumDifficulty.HARD || entityhuman.getHealth() > 1.0F && enumdifficulty == EnumDifficulty.NORMAL) {
                    entityhuman.damageEntity(DamageSource.STARVE, 1.0F);
                }

                this.foodTickTimer = 0;
            }
        } else {
            this.foodTickTimer = 0;
        }
    }

    public void a(NBTTagCompound nbttagcompound) {
        if (nbttagcompound.hasKeyOfType("foodLevel", 99)) {
            this.foodLevel = nbttagcompound.getInt("foodLevel");
            this.foodTickTimer = nbttagcompound.getInt("foodTickTimer");
            this.saturationLevel = nbttagcompound.getFloat("foodSaturationLevel");
            this.exhaustionLevel = nbttagcompound.getFloat("foodExhaustionLevel");
        }
    }

    public void b(NBTTagCompound nbttagcompound) {
        nbttagcompound.setInt("foodLevel", this.foodLevel);
        nbttagcompound.setInt("foodTickTimer", this.foodTickTimer);
        nbttagcompound.setFloat("foodSaturationLevel", this.saturationLevel);
        nbttagcompound.setFloat("foodExhaustionLevel", this.exhaustionLevel);
    }

    public int a() {
        return this.foodLevel;
    }

    public boolean c() {
        return this.foodLevel < 20;
    }

    public void a(float f) {
        this.exhaustionLevel = Math.min(this.exhaustionLevel + f, 40.0F);
    }

    public float e() {
        return this.saturationLevel;
    }
}
