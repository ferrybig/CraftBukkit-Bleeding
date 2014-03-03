package net.minecraft.server;

import java.util.Calendar;

// CraftBukkit start
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
// CraftBukkit end

public class EntitySkeleton extends EntityMonster implements IRangedEntity {

    private PathfinderGoalArrowAttack bp = new PathfinderGoalArrowAttack(this, 1.0D, 20, 60, 15.0F);
    private PathfinderGoalMeleeAttack bq = new PathfinderGoalMeleeAttack(this, EntityHuman.class, 1.2D, false);

    public EntitySkeleton(World world) {
        super(world);
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalRestrictSun(this));
        this.goalSelector.a(3, new PathfinderGoalFleeSun(this, 1.0D));
        this.goalSelector.a(5, new PathfinderGoalRandomStroll(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, false));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, 0, true));
        if (world != null && !world.isStatic) {
            this.bZ();
        }
    }

    protected void aC() {
        super.aC();
        this.getAttributeInstance(GenericAttributes.d).setValue(0.25D);
    }

    protected void c() {
        super.c();
        this.datawatcher.a(13, new Byte((byte) 0));
    }

    public boolean bj() {
        return true;
    }

    protected String t() {
        return "mob.skeleton.say";
    }

    protected String aS() {
        return "mob.skeleton.hurt";
    }

    protected String aT() {
        return "mob.skeleton.death";
    }

    protected void a(int i, int j, int k, Block block) {
        this.makeSound("mob.skeleton.step", 0.15F, 1.0F);
    }

    public boolean n(Entity entity) {
        if (super.n(entity)) {
            if (this.getSkeletonType() == 1 && entity instanceof EntityLiving) {
                ((EntityLiving) entity).addEffect(new MobEffect(MobEffectList.WITHER.id, 200));
            }

            return true;
        } else {
            return false;
        }
    }

    public EnumMonsterType getMonsterType() {
        return EnumMonsterType.UNDEAD;
    }

    public void e() {
        if (this.world.w() && !this.world.isStatic) {
            float f = this.d(1.0F);

            if (f > 0.5F && this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F && this.world.i(MathHelper.floor(this.locX), MathHelper.floor(this.locY), MathHelper.floor(this.locZ))) {
                boolean flag = true;
                ItemStack itemstack = this.getEquipment(4);

                if (itemstack != null) {
                    if (itemstack.g()) {
                        itemstack.setData(itemstack.j() + this.random.nextInt(2));
                        if (itemstack.j() >= itemstack.l()) {
                            this.a(itemstack);
                            this.setEquipment(4, (ItemStack) null);
                        }
                    }

                    flag = false;
                }

                if (flag) {
                    this.setOnFire(CraftEventFactory.handleEntityCombustEvent(this, 8)); // CraftBukkit - call EntityCombustEvent
                }
            }
        }

        if (this.world.isStatic && this.getSkeletonType() == 1) {
            this.a(0.72F, 2.34F);
        }

        super.e();
    }

    public void aa() {
        super.aa();
        if (this.vehicle instanceof EntityCreature) {
            EntityCreature entitycreature = (EntityCreature) this.vehicle;

            this.aM = entitycreature.aM;
        }
    }

    public void die(DamageSource damagesource) {
        super.die(damagesource);
        if (damagesource.i() instanceof EntityArrow && damagesource.getEntity() instanceof EntityHuman) {
            EntityHuman entityhuman = (EntityHuman) damagesource.getEntity();
            double d0 = entityhuman.locX - this.locX;
            double d1 = entityhuman.locZ - this.locZ;

            if (d0 * d0 + d1 * d1 >= 2500.0D) {
                entityhuman.a((Statistic) AchievementList.v);
            }
        }
    }

    protected Item getLoot() {
        return Items.ARROW;
    }

    protected void dropDeathLoot(boolean flag, int i) {
        int j;
        int k;
        java.util.List<org.bukkit.inventory.ItemStack> loot = new java.util.ArrayList<org.bukkit.inventory.ItemStack>(); // CraftBukkit

        if (this.getSkeletonType() == 1) {
            j = this.random.nextInt(3 + i) - 1;

            /* CraftBukkit start
            for (k = 0; k < j; ++k) {
                this.a(Items.COAL, 1);
            }
            */
            loot.add(CraftItemStack.asNewCraftStack(Items.COAL, j));
            // CraftBukkit end
        } else {
            j = this.random.nextInt(3 + i);

            /* CraftBukkit start
            for (k = 0; k < j; ++k) {
                this.a(Items.ARROW, 1);
            }
            */
            loot.add(CraftItemStack.asNewCraftStack(Items.ARROW, j));
            // CraftBukkit end
        }

        j = this.random.nextInt(3 + i);

        /* CraftBukkit start
        for (k = 0; k < j; ++k) {
            this.a(Items.BONE, 1);
        }
        */
        loot.add(CraftItemStack.asNewCraftStack(Items.BONE, j));

        // CraftBukkit start - Determine rare item drops and add them to the loot
        if (this.lastDamageByPlayerTime > 0) {
            int l = this.random.nextInt(200) - i;

            if (l < 5) {
                ItemStack itemstack = this.getRareDrop(l <= 0 ? 1 : 0);
                if (itemstack != null) {
                    loot.add(CraftItemStack.asCraftMirror(itemstack));
                }
            }
        }
        CraftEventFactory.callEntityDeathEvent(this, loot);
        // CraftBukkit end
    }

    // CraftBukkit - return rare dropped item instead of dropping it
    protected ItemStack getRareDrop(int i) {
        if (this.getSkeletonType() == 1) {
            return new ItemStack(Items.SKULL, 1, 1); // CraftBukkit
        }

        return null; // CraftBukkit
    }

    protected void bC() {
        super.bC();
        this.setEquipment(0, new ItemStack(Items.BOW));
    }

    public GroupDataEntity a(GroupDataEntity groupdataentity) {
        groupdataentity = super.a(groupdataentity);
        if (this.world.worldProvider instanceof WorldProviderHell && this.aH().nextInt(5) > 0) {
            this.goalSelector.a(4, this.bq);
            this.setSkeletonType(1);
            this.setEquipment(0, new ItemStack(Items.STONE_SWORD));
            this.getAttributeInstance(GenericAttributes.e).setValue(4.0D);
        } else {
            this.goalSelector.a(4, this.bp);
            this.bC();
            this.bD();
        }

        this.h(this.random.nextFloat() < 0.55F * this.world.b(this.locX, this.locY, this.locZ));
        if (this.getEquipment(4) == null) {
            Calendar calendar = this.world.V();

            if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31 && this.random.nextFloat() < 0.25F) {
                this.setEquipment(4, new ItemStack(this.random.nextFloat() < 0.1F ? Blocks.JACK_O_LANTERN : Blocks.PUMPKIN));
                this.dropChances[4] = 0.0F;
            }
        }

        return groupdataentity;
    }

    public void bZ() {
        this.goalSelector.a((PathfinderGoal) this.bq);
        this.goalSelector.a((PathfinderGoal) this.bp);
        ItemStack itemstack = this.bd();

        if (itemstack != null && itemstack.getItem() == Items.BOW) {
            this.goalSelector.a(4, this.bp);
        } else {
            this.goalSelector.a(4, this.bq);
        }
    }

    public void a(EntityLiving entityliving, float f) {
        EntityArrow entityarrow = new EntityArrow(this.world, this, entityliving, 1.6F, (float) (14 - this.world.difficulty.a() * 4));
        int i = EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_DAMAGE.id, this.bd());
        int j = EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK.id, this.bd());

        entityarrow.b((double) (f * 2.0F) + this.random.nextGaussian() * 0.25D + (double) ((float) this.world.difficulty.a() * 0.11F));
        if (i > 0) {
            entityarrow.b(entityarrow.e() + (double) i * 0.5D + 0.5D);
        }

        if (j > 0) {
            entityarrow.setKnockbackStrength(j);
        }

        if (EnchantmentManager.getEnchantmentLevel(Enchantment.ARROW_FIRE.id, this.bd()) > 0 || this.getSkeletonType() == 1) {
            entityarrow.setOnFire(100);
        }

        // CraftBukkit start
        if (!CraftEventFactory.handleEntityShootBowEvent(this.world, this, this.bd(), entityarrow, 0.8F)) {
            return;
        }
        // CraftBukkit end

        this.makeSound("random.bow", 1.0F, 1.0F / (this.aH().nextFloat() * 0.4F + 0.8F));
        // this.world.addEntity(entityarrow); // CraftBukkit - moved to event processing
    }

    public int getSkeletonType() {
        return this.datawatcher.getByte(13);
    }

    public void setSkeletonType(int i) {
        this.datawatcher.watch(13, Byte.valueOf((byte) i));
        this.fireProof = i == 1;
        if (i == 1) {
            this.a(0.72F, 2.34F);
        } else {
            this.a(0.6F, 1.8F);
        }
    }

    public void a(NBTTagCompound nbttagcompound) {
        super.a(nbttagcompound);
        if (nbttagcompound.hasKeyOfType("SkeletonType", 99)) {
            byte b0 = nbttagcompound.getByte("SkeletonType");

            this.setSkeletonType(b0);
        }

        this.bZ();
    }

    public void b(NBTTagCompound nbttagcompound) {
        super.b(nbttagcompound);
        nbttagcompound.setByte("SkeletonType", (byte) this.getSkeletonType());
    }

    public void setEquipment(int i, ItemStack itemstack) {
        super.setEquipment(i, itemstack);
        if (!this.world.isStatic && i == 0) {
            this.bZ();
        }
    }

    public double ac() {
        return super.ac() - 0.5D;
    }
}
