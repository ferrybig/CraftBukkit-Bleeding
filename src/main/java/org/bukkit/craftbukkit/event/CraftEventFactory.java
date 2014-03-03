package org.bukkit.craftbukkit.event;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import net.minecraft.server.Blocks;
import net.minecraft.server.ChatComponentText;
import net.minecraft.server.Chunk;
import net.minecraft.server.ChunkCoordinates;
import net.minecraft.server.Container;
import net.minecraft.server.DamageSource;
import net.minecraft.server.EnchantmentInstance;
import net.minecraft.server.EnchantmentManager;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityAnimal;
import net.minecraft.server.EntityArrow;
import net.minecraft.server.EntityDamageSource;
import net.minecraft.server.EntityDamageSourceIndirect;
import net.minecraft.server.EntityEgg;
import net.minecraft.server.EntityFishingHook;
import net.minecraft.server.EntityGhast;
import net.minecraft.server.EntityGolem;
import net.minecraft.server.EntityHanging;
import net.minecraft.server.EntityHorse;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityInsentient;
import net.minecraft.server.EntityItem;
import net.minecraft.server.EntityLightning;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityMonster;
import net.minecraft.server.EntityPainting;
import net.minecraft.server.EntityPig;
import net.minecraft.server.EntityPigZombie;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EntityPotion;
import net.minecraft.server.EntitySheep;
import net.minecraft.server.EntitySlime;
import net.minecraft.server.EntityWaterAnimal;
import net.minecraft.server.FoodMetaData;
import net.minecraft.server.IChatBaseComponent;
import net.minecraft.server.IInventory;
import net.minecraft.server.ISourceBlock;
import net.minecraft.server.InventoryCrafting;
import net.minecraft.server.ItemStack;
import net.minecraft.server.ItemSword;
import net.minecraft.server.Items;
import net.minecraft.server.LoginListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PacketPlayInAbilities;
import net.minecraft.server.PacketPlayInCloseWindow;
import net.minecraft.server.PacketPlayInHeldItemSlot;
import net.minecraft.server.PacketPlayInSetCreativeSlot;
import net.minecraft.server.PacketPlayInWindowClick;
import net.minecraft.server.PacketPlayOutAttachEntity;
import net.minecraft.server.PacketPlayOutBlockChange;
import net.minecraft.server.PacketPlayOutEntityMetadata;
import net.minecraft.server.PacketPlayOutHeldItemSlot;
import net.minecraft.server.PacketPlayOutSetSlot;
import net.minecraft.server.PacketPlayOutUpdateHealth;
import net.minecraft.server.PathfinderGoalDefendVillage;
import net.minecraft.server.PathfinderGoalHurtByTarget;
import net.minecraft.server.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.PathfinderGoalOwnerHurtByTarget;
import net.minecraft.server.PathfinderGoalOwnerHurtTarget;
import net.minecraft.server.PathfinderGoalTarget;
import net.minecraft.server.PlayerConnection;
import net.minecraft.server.PlayerInteractManager;
import net.minecraft.server.PlayerInventory;
import net.minecraft.server.PlayerList;
import net.minecraft.server.RemoteControlCommandListener;
import net.minecraft.server.ServerCommand;
import net.minecraft.server.ServerPing;
import net.minecraft.server.ServerPingPlayerSample;
import net.minecraft.server.ServerPingServerData;
import net.minecraft.server.Slot;
import net.minecraft.server.TileEntity;
import net.minecraft.server.TileEntityFurnace;
import net.minecraft.server.TileEntitySign;
import net.minecraft.server.World;
import net.minecraft.server.WorldServer;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import org.apache.logging.log4j.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.Statistic;
import org.bukkit.Statistic.Type;
import org.bukkit.TravelAgent;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftStatistic;
import org.bukkit.craftbukkit.CraftTravelAgent;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.CraftSign;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.map.CraftMapView;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftDamageSource;
import org.bukkit.craftbukkit.util.CraftIconCache;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.util.LazyPlayerSet;
import org.bukkit.craftbukkit.util.Waitable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Horse;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Pig;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Slime;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.*;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.painting.PaintingBreakByEntityEvent;
import org.bukkit.event.painting.PaintingBreakEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.event.server.RemoteServerCommandEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.SpawnChangeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.util.CachedServerIcon;
import org.bukkit.util.StringUtil;
import org.bukkit.util.Vector;

public class CraftEventFactory {
    public static final DamageSource MELTING = CraftDamageSource.copyOf(DamageSource.BURN);
    public static final DamageSource POISON = CraftDamageSource.copyOf(DamageSource.MAGIC);

    // helper methods
    private static boolean canBuild(CraftWorld world, Player player, int x, int z) {
        WorldServer worldServer = world.getHandle();
        CraftServer server = worldServer.getServer();
        int spawnSize = server.getSpawnRadius();

        if (world.getHandle().dimension != 0 || spawnSize <= 0 || server.getHandle().getOPs().d() || player.isOp()) { // d() should be isEmpty()
            return true;
        }

        ChunkCoordinates chunkcoordinates = worldServer.getSpawn();
        int distanceFromSpawn = Math.max(Math.abs(x - chunkcoordinates.x), Math.abs(z - chunkcoordinates.z));

        return distanceFromSpawn > spawnSize;
    }

    public static <T extends Event> T callEvent(T event) {
        Bukkit.getServer().getPluginManager().callEvent(event);
        return event;
    }

    /**
     * Block place methods
     */
    public static BlockPlaceEvent callBlockPlaceEvent(World world, EntityHuman who, BlockState replacedBlockState, int clickedX, int clickedY, int clickedZ) {
        CraftWorld craftWorld = world.getWorld();
        Player player = (Player) who.getBukkitEntity();

        Block placedBlock = replacedBlockState.getBlock();

        boolean canBuild = canBuild(craftWorld, player, placedBlock.getX(), placedBlock.getZ());
        return callEvent(new BlockPlaceEvent(placedBlock, replacedBlockState, craftWorld.getBlockAt(clickedX, clickedY, clickedZ), player.getItemInHand(), player, canBuild));
    }

    public static PlayerBucketEvent handlePlayerBucketEvent(EntityHuman entity, int x, int y, int z, int face, ItemStack inHand, net.minecraft.server.Item bucketItem) {
        Player player = (Player) entity.getBukkitEntity();
        // This is correct, I swear.
        CraftItemStack craftInHand = CraftItemStack.asNewCraftStack(bucketItem); // The item held AFTER the event
        Material bucket = CraftMagicNumbers.getMaterial(inHand.getItem()); // The type of bucket used BEFORE the event
        CraftWorld world = (CraftWorld) player.getWorld();
        Block block = world.getBlockAt(x, y, z);
        BlockFace blockFace =  CraftBlock.notchToBlockFace(face);

        PlayerBucketEvent event;

        if (bucket == Material.BUCKET) {
            event = new PlayerBucketFillEvent(player, block, blockFace, bucket, craftInHand);
        } else {
            event = new PlayerBucketEmptyEvent(player, block, blockFace, bucket, craftInHand);
        }

        event.setCancelled(!canBuild(world, player, x, z));
        return callEvent(event);
    }

    /**
     * Player Interact event
     */
    public static PlayerInteractEvent callPlayerInteractEvent(EntityHuman who, Action action, ItemStack itemstack) {
        return callPlayerInteractEvent(who, action, 0, who.world.getHeight(), 0, 0, itemstack);
    }

    public static PlayerInteractEvent callPlayerInteractEvent(EntityHuman who, Action action, int clickedX, int clickedY, int clickedZ, int clickedFace, ItemStack itemstack) {
        CraftItemStack itemInHand = CraftItemStack.asCraftMirror(itemstack);
        Block blockClicked = who.world.getWorld().getBlockAt(clickedX, clickedY, clickedZ);

        if (clickedY >= who.world.getHeight()) {
            blockClicked = null;
            switch (action) {
            case LEFT_CLICK_BLOCK:
                action = Action.LEFT_CLICK_AIR;
                break;
            case RIGHT_CLICK_BLOCK:
                action = Action.RIGHT_CLICK_AIR;
                break;
            }
        }

        if (itemInHand.getType() == Material.AIR || itemInHand.getAmount() == 0) {
            itemInHand = null;
        }

        return callEvent(new PlayerInteractEvent((Player) who.getBukkitEntity(), action, itemInHand, blockClicked, CraftBlock.notchToBlockFace(clickedFace)));
    }

    /**
     * EntityShootBowEvent
     */
    public static boolean handleEntityShootBowEvent(World world, EntityLiving who, ItemStack itemstack, EntityArrow entityArrow, float force) {
        Arrow arrow = (Arrow) entityArrow.getBukkitEntity();
        EntityShootBowEvent event = callEvent(new EntityShootBowEvent((LivingEntity) who.getBukkitEntity(), CraftItemStack.asCraftMirror(itemstack), arrow, force));

        if (event.isCancelled()) {
            event.getProjectile().remove();
            return false;
        }

        if (event.getProjectile() == arrow) {
            world.addEntity(entityArrow);
        }

        return true;
    }

    /**
     * BlockDamageEvent
     */
    public static BlockDamageEvent callBlockDamageEvent(EntityHuman who, int x, int y, int z, ItemStack itemstack, boolean instaBreak) {
        return callEvent(new BlockDamageEvent((Player) who.getBukkitEntity(), who.world.getWorld().getBlockAt(x, y, z), CraftItemStack.asCraftMirror(itemstack), instaBreak));
    }

    /**
     * EntityTameEvent
     */
    public static EntityTameEvent callEntityTameEvent(EntityInsentient entity, EntityHuman tamer) {
        entity.persistent = true;

        return callEvent(new EntityTameEvent((LivingEntity) entity.getBukkitEntity(), (tamer != null ? tamer.getBukkitEntity() : null)));
    }

    /**
     * ItemDespawnEvent
     */
    public static boolean handleItemDespawnEvent(EntityItem entityitem) {
        Item entity = (Item) entityitem.getBukkitEntity();
        boolean cancelled = callEvent(new ItemDespawnEvent(entity, entity.getLocation())).isCancelled();

        if (cancelled) {
            entityitem.age = 0;
        }

        return cancelled;
    }

    /**
     * PotionSplashEvent
     */
    public static PotionSplashEvent callPotionSplashEvent(EntityPotion potion, Map<LivingEntity, Double> affectedEntities) {
        return callEvent(new PotionSplashEvent((ThrownPotion) potion.getBukkitEntity(), affectedEntities));
    }

    /**
     * BlockFadeEvent
     */
    public static BlockFadeEvent callBlockFadeEvent(World world, int x, int y, int z, net.minecraft.server.Block type) {
        Block block = world.getWorld().getBlockAt(x, y, z);
        BlockState state = block.getState();
        state.setType(CraftMagicNumbers.getMaterial(type));

        return callEvent(new BlockFadeEvent(block, state));
    }

    public static void handleBlockFadeEvent(World world, int x, int y, int z, net.minecraft.server.Block type) {
        Block block = world.getWorld().getBlockAt(x, y, z);
        BlockState state = block.getState();
        state.setType(CraftMagicNumbers.getMaterial(type));

        if (!callEvent(new BlockFadeEvent(block, state)).isCancelled()) {
            state.update(true);
        }
    }

    public static void handleBlockSpreadEvent(World world, int x, int y, int z, int sourceX, int sourceY, int sourceZ, net.minecraft.server.Block type, int data) {
        CraftWorld bukkitWorld = world.getWorld();

        handleBlockSpreadEvent(bukkitWorld.getBlockAt(x, y, z), bukkitWorld.getBlockAt(sourceX, sourceY, sourceZ), type, data);
    }

    public static void handleBlockSpreadEvent(Block block, Block source, net.minecraft.server.Block type, int data) {
        BlockState state = block.getState();
        state.setType(CraftMagicNumbers.getMaterial(type));
        state.setRawData((byte) data);

        if (!callEvent(new BlockSpreadEvent(block, source, state)).isCancelled()) {
            state.update(true);
        }
    }

    public static EntityDeathEvent callEntityDeathEvent(EntityLiving victim) {
        return callEntityDeathEvent(victim, new ArrayList<org.bukkit.inventory.ItemStack>(0));
    }

    public static EntityDeathEvent callEntityDeathEvent(EntityLiving victim, List<org.bukkit.inventory.ItemStack> drops) {
        CraftLivingEntity entity = (CraftLivingEntity) victim.getBukkitEntity();
        EntityDeathEvent event = callEvent(new EntityDeathEvent(entity, drops, victim.getExpReward()));
        victim.expToDrop = event.getDroppedExp();

        CraftWorld world = (CraftWorld) entity.getWorld();
        Location location = entity.getLocation();

        for (org.bukkit.inventory.ItemStack stack : event.getDrops()) {
            if (stack == null || stack.getType() == Material.AIR || stack.getAmount() == 0) {
                continue;
            }

            world.dropItemNaturally(location, stack);
        }

        return event;
    }

    public static ServerPing handleServerListPingEvent(final MinecraftServer minecraftServer, final SocketAddress socketAddress) {
        final PlayerList playerList = minecraftServer.getPlayerList();
        final Object[] players = playerList.players.toArray();
        class ServerListPingEvent extends org.bukkit.event.server.ServerListPingEvent {
            CraftIconCache icon = minecraftServer.server.getServerIcon();

            ServerListPingEvent() {
                super(((InetSocketAddress) socketAddress).getAddress(), minecraftServer.getMotd(), playerList.getMaxPlayers());
            }

            @Override
            public void setServerIcon(CachedServerIcon icon) {
                if (!(icon instanceof CraftIconCache)) {
                    throw new IllegalArgumentException(icon + " was not created by " + CraftServer.class);
                }
                this.icon = (CraftIconCache) icon;
            }

            @Override
            public Iterator<Player> iterator() throws UnsupportedOperationException {
                return new Iterator<Player>() {
                    int i;
                    int ret = Integer.MIN_VALUE;
                    EntityPlayer player;

                    @Override
                    public boolean hasNext() {
                        if (player != null) {
                            return true;
                        }
                        final Object[] currentPlayers = players;
                        for (int length = currentPlayers.length, i = this.i; i < length; i++) {
                            final EntityPlayer player = (EntityPlayer) currentPlayers[i];
                            if (player != null) {
                                this.i = i + 1;
                                this.player = player;
                                return true;
                            }
                        }
                        return false;
                    }

                    @Override
                    public Player next() {
                        if (!hasNext()) {
                            throw new java.util.NoSuchElementException();
                        }
                        final EntityPlayer player = this.player;
                        this.player = null;
                        this.ret = this.i - 1;
                        return player.getBukkitEntity();
                    }

                    @Override
                    public void remove() {
                        final Object[] currentPlayers = players;
                        final int i = this.ret;
                        if (i < 0 || currentPlayers[i] == null) {
                            throw new IllegalStateException();
                        }
                        currentPlayers[i] = null;
                    }
                };
            }
        }

        ServerListPingEvent event = callEvent(new ServerListPingEvent());

        List<GameProfile> profiles = new ArrayList<GameProfile>(players.length);
        for (Object player : players) {
            if (player != null) {
                profiles.add(((EntityPlayer) player).getProfile());
            }
        }

        ServerPingPlayerSample playerSample = new ServerPingPlayerSample(event.getMaxPlayers(), profiles.size());
        playerSample.a(profiles.toArray(new GameProfile[profiles.size()]));

        ServerPing ping = new ServerPing();
        ping.setFavicon(event.icon.value);
        ping.setMOTD(new ChatComponentText(event.getMotd()));
        ping.setPlayerSample(playerSample);
        ping.setServerInfo(new ServerPingServerData(minecraftServer.getServerModName() + " " + minecraftServer.getVersion(), 5)); // TODO: Update when protocol changes
        return ping;
    }

    /**
     * EntityDamage(ByEntityEvent)
     */
    public static EntityDamageEvent callEntityDamageEvent(Entity damager, Entity damagee, DamageCause cause, double damage) {
        EntityDamageEvent event;
        if (damager != null) {
            event = new EntityDamageByEntityEvent(damager.getBukkitEntity(), damagee.getBukkitEntity(), cause, damage);
        } else {
            event = new EntityDamageEvent(damagee.getBukkitEntity(), cause, damage);
        }

        callEvent(event);

        if (!event.isCancelled()) {
            event.getEntity().setLastDamageCause(event);
        }

        return event;
    }

    public static EntityDamageEvent handleEntityDamageEvent(Entity entity, DamageSource source, float damage) {
        if (source.isExplosion()) {
            return null;
        } else if (source instanceof EntityDamageSource) {
            Entity damager = source.getEntity();
            DamageCause cause = DamageCause.ENTITY_ATTACK;

            if (source instanceof EntityDamageSourceIndirect) {
                damager = ((EntityDamageSourceIndirect) source).getProximateDamageSource();
                if (damager.getBukkitEntity() instanceof ThrownPotion) {
                    cause = DamageCause.MAGIC;
                } else if (damager.getBukkitEntity() instanceof Projectile) {
                    cause = DamageCause.PROJECTILE;
                }
            } else if ("thorns".equals(source.translationIndex)) {
                cause = DamageCause.THORNS;
            }

            return callEntityDamageEvent(damager, entity, cause, damage);
        } else if (source == DamageSource.OUT_OF_WORLD) {
            return handleEntityDamageByBlockEvent(null, entity, DamageCause.VOID, damage);
        }

        DamageCause cause = null;
        if (source == DamageSource.FIRE) {
            cause = DamageCause.FIRE;
        } else if (source == DamageSource.STARVE) {
            cause = DamageCause.STARVATION;
        } else if (source == DamageSource.WITHER) {
            cause = DamageCause.WITHER;
        } else if (source == DamageSource.STUCK) {
            cause = DamageCause.SUFFOCATION;
        } else if (source == DamageSource.DROWN) {
            cause = DamageCause.DROWNING;
        } else if (source == DamageSource.BURN) {
            cause = DamageCause.FIRE_TICK;
        } else if (source == MELTING) {
            cause = DamageCause.MELTING;
        } else if (source == POISON) {
            cause = DamageCause.POISON;
        } else if (source == DamageSource.MAGIC) {
            cause = DamageCause.MAGIC;
        }

        if (cause != null) {
            return callEntityDamageEvent(null, entity, cause, damage);
        }

        // If an event was called earlier, we return null.
        // EG: Cactus, Lava, EntityEnderPearl "fall", FallingSand
        return null;
    }

    // Non-Living Entities such as EntityEnderCrystal need to call this
    public static boolean handleNonLivingEntityDamageEvent(Entity entity, DamageSource source, float damage) {
        if (!(source instanceof EntityDamageSource)) {
            return false;
        }

        EntityDamageEvent event = handleEntityDamageEvent(entity, source, damage);
        return event != null && (event.isCancelled() || event.getDamage() == 0);
    }

    public static EntityDamageByBlockEvent handleEntityDamageByBlockEvent(Block block, Entity entity, DamageCause cause, float damage) {
        EntityDamageByBlockEvent event = callEvent(new EntityDamageByBlockEvent(block, entity == null ? null : entity.getBukkitEntity(), cause, damage));

        if (!event.isCancelled()) {
            event.getEntity().setLastDamageCause(event);
        }

        return event;
    }

    public static PlayerLevelChangeEvent callPlayerLevelChangeEvent(Player player, int oldLevel, int newLevel) {
        return callEvent(new PlayerLevelChangeEvent(player, oldLevel, newLevel));
    }

    public static PlayerExpChangeEvent callPlayerExpChangeEvent(EntityHuman entity, int expAmount) {
        return callEvent(new PlayerExpChangeEvent((Player) entity.getBukkitEntity(), expAmount));
    }

    public static void handleBlockGrowEvent(World world, int x, int y, int z, net.minecraft.server.Block type, int data) {
        Block block = world.getWorld().getBlockAt(x, y, z);
        CraftBlockState state = (CraftBlockState) block.getState();
        state.setTypeId(net.minecraft.server.Block.b(type));
        state.setRawData((byte) data);

        if (!callEvent(new BlockGrowEvent(block, state)).isCancelled()) {
            state.update(true);
        }
    }

    public static void handleFoodLevelChangeEvent(EntityHuman entity, int level, float saturationLevel, boolean eating) {
        FoodMetaData foodData = entity.getFoodData();
        int oldFoodLevel = foodData.foodLevel;

        FoodLevelChangeEvent event;
        if (eating) {
            event = callEvent(new FoodLevelChangeEvent(entity.getBukkitEntity(), level + oldFoodLevel));
        } else {
            event = callEvent(new FoodLevelChangeEvent(entity.getBukkitEntity(), level));
        }

        if (!event.isCancelled()) {
            if (eating) {
                foodData.eat(event.getFoodLevel() - oldFoodLevel, saturationLevel);
            } else {
                foodData.foodLevel = event.getFoodLevel();
                foodData.saturationLevel = saturationLevel;
            }
        }

        EntityPlayer player = (EntityPlayer) entity;
        player.playerConnection.sendPacket(new PacketPlayOutUpdateHealth(player.getBukkitEntity().getScaledHealth(), foodData.foodLevel, foodData.saturationLevel));
    }

    public static PigZapEvent callPigZapEvent(EntityPig pig, EntityLightning lightning, EntityPigZombie pigzombie) {
        return callEvent(new PigZapEvent((Pig) pig.getBukkitEntity(), (LightningStrike) lightning.getBukkitEntity(), (PigZombie) pigzombie.getBukkitEntity()));
    }

    public static HorseJumpEvent callHorseJumpEvent(EntityHorse horse, float power) {
        return callEvent(new HorseJumpEvent((Horse) horse.getBukkitEntity(), power));
    }

    public static EntityChangeBlockEvent callEntityChangeBlockEvent(Entity entity, int x, int y, int z, net.minecraft.server.Block type, int data, boolean cancelled) {
        EntityChangeBlockEvent event = new EntityChangeBlockEvent(entity.getBukkitEntity(), entity.world.getWorld().getBlockAt(x, y, z), CraftMagicNumbers.getMaterial(type), (byte) data);
        event.setCancelled(cancelled);

        return callEvent(event);
    }

    public static EntityChangeBlockEvent callEntityChangeBlockEvent(Entity entity, int x, int y, int z, net.minecraft.server.Block type, int data) {
        return callEntityChangeBlockEvent(entity, x, y, z, type, data, false);
    }

    public static CreeperPowerEvent callCreeperPowerEvent(Entity creeper, Entity lightning, CreeperPowerEvent.PowerCause cause) {
        if (lightning == null) {
            return callEvent(new CreeperPowerEvent((Creeper) creeper.getBukkitEntity(), cause));
        } else {
            return callEvent(new CreeperPowerEvent((Creeper) creeper.getBukkitEntity(), (LightningStrike) lightning.getBukkitEntity(), cause));
        }
    }

    public static EntityTargetEvent callEntityTargetEvent(Entity entity, Entity target, EntityTargetEvent.TargetReason reason) {
        return callEvent(new EntityTargetEvent(entity.getBukkitEntity(), target == null ? null : target.getBukkitEntity(), reason));
    }

    public static Entity handleEntityTargetEvent(Entity entity, Entity oldTarget, Entity newTarget, EntityTargetEvent.TargetReason reason) {
        EntityTargetEvent event = callEvent(new EntityTargetEvent(entity.getBukkitEntity(), newTarget == null ? null : newTarget.getBukkitEntity(), reason));

        if (event.isCancelled()) {
            return oldTarget;
        } else {
            org.bukkit.entity.Entity bukkitTarget = event.getTarget();
            return bukkitTarget == null ? null : ((CraftEntity) bukkitTarget).getHandle();
        }
    }

    public static boolean handleEntityTargetLivingEvent(PathfinderGoalTarget goalTarget, EntityLiving target) {
        EntityTargetEvent.TargetReason reason = EntityTargetEvent.TargetReason.RANDOM_TARGET;

        if (goalTarget instanceof PathfinderGoalDefendVillage) {
            reason = EntityTargetEvent.TargetReason.DEFEND_VILLAGE;
        } else if (goalTarget instanceof PathfinderGoalHurtByTarget) {
            reason = EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY;
        } else if (goalTarget instanceof PathfinderGoalNearestAttackableTarget) {
            if (target instanceof EntityHuman) {
                reason = EntityTargetEvent.TargetReason.CLOSEST_PLAYER;
            }
        } else if (goalTarget instanceof PathfinderGoalOwnerHurtByTarget) {
            reason = EntityTargetEvent.TargetReason.TARGET_ATTACKED_OWNER;
        } else if (goalTarget instanceof PathfinderGoalOwnerHurtTarget) {
            reason = EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET;
        }

        EntityTargetLivingEntityEvent event = callEvent(new EntityTargetLivingEntityEvent(goalTarget.c.getBukkitEntity(), (LivingEntity) target.getBukkitEntity(), reason));
        LivingEntity eventTarget = event.getTarget();

        if (event.isCancelled() || eventTarget == null) {
            goalTarget.c.setGoalTarget(null);
            return false;
        } else if (target.getBukkitEntity() != eventTarget) {
            goalTarget.c.setGoalTarget((EntityLiving) ((CraftEntity) eventTarget).getHandle());
        }

        goalTarget.c.target = ((CraftEntity) eventTarget).getHandle();
        return true;
    }

    public static EntityBreakDoorEvent callEntityBreakDoorEvent(Entity entity, int x, int y, int z) {
        return callEvent(new EntityBreakDoorEvent((LivingEntity) entity.getBukkitEntity(), entity.world.getWorld().getBlockAt(x, y, z)));
    }

    public static Container callInventoryOpenEvent(EntityPlayer player, Container container) {
        if (player.activeContainer != player.defaultContainer) { // fire INVENTORY_CLOSE if one already open
            player.playerConnection.a(new PacketPlayInCloseWindow(player.activeContainer.windowId));
        }

        CraftPlayer craftPlayer = player.getBukkitEntity();
        player.activeContainer.transferTo(container, craftPlayer);

        if (callEvent(new InventoryOpenEvent(container.getBukkitView())).isCancelled()) {
            container.transferTo(player.activeContainer, craftPlayer);
            return null;
        }

        return container;
    }

    public static ItemStack callPreCraftEvent(InventoryCrafting matrix, ItemStack result, InventoryView lastCraftView, boolean isRepair) {
        CraftInventoryCrafting inventory = new CraftInventoryCrafting(matrix, matrix.resultInventory);
        inventory.setResult(CraftItemStack.asCraftMirror(result));

        return CraftItemStack.asNMSCopy(callEvent(new PrepareItemCraftEvent(inventory, lastCraftView, isRepair)).getInventory().getResult());
    }

    public static boolean handleEntitySpawnEvent(Entity entity, SpawnReason spawnReason) {
        Cancellable event = null;
        org.bukkit.entity.Entity bukkitEntity = entity.getBukkitEntity();

        if (entity instanceof EntityLiving && !(entity instanceof EntityPlayer)) {
            boolean isAnimal = entity instanceof EntityAnimal || entity instanceof EntityWaterAnimal || entity instanceof EntityGolem;
            boolean isMonster = entity instanceof EntityMonster || entity instanceof EntityGhast || entity instanceof EntitySlime;

            if (spawnReason != SpawnReason.CUSTOM) {
                if (isAnimal && !entity.world.allowAnimals || isMonster && !entity.world.allowMonsters)  {
                    entity.dead = true;
                    return false;
                }
            }

            event = callEvent(new CreatureSpawnEvent((LivingEntity) bukkitEntity, spawnReason));
        } else if (entity instanceof EntityItem) {
            event = callEvent(new ItemSpawnEvent((Item) bukkitEntity, bukkitEntity.getLocation()));
        } else if (bukkitEntity instanceof Projectile) {
            // Not all projectiles extend EntityProjectile, so check for Bukkit interface instead
            event = callEvent(new ProjectileLaunchEvent(bukkitEntity));
        }

        if (event != null && (event.isCancelled() || entity.dead)) {
            entity.dead = true;
            return false;
        }

        return true;
    }

    public static ProjectileHitEvent callProjectileHitEvent(Entity entity) {
        return callEvent(new ProjectileHitEvent((Projectile) entity.getBukkitEntity()));
    }

    public static int handleExpBottleEvent(Entity entity, int exp) {
        ExpBottleEvent event = callEvent(new ExpBottleEvent((ThrownExpBottle) entity.getBukkitEntity(), exp));

        if (event.getShowEffect()) {
            entity.world.triggerEffect(2002, (int) Math.round(entity.locX), (int) Math.round(entity.locY), (int) Math.round(entity.locZ), 0);
        }

        return event.getExperience();
    }

    public static BlockRedstoneEvent callRedstoneChange(World world, int x, int y, int z, int oldCurrent, int newCurrent) {
        return callEvent(new BlockRedstoneEvent(world.getWorld().getBlockAt(x, y, z), oldCurrent, newCurrent));
    }

    public static BlockRedstoneEvent callRedstoneChange(World world, int x, int y, int z) {
        Block block = world.getWorld().getBlockAt(x, y, z);
        int power = block.getBlockPower();

        return callEvent(new BlockRedstoneEvent(world.getWorld().getBlockAt(x, y, z), power, power));
    }

    public static NotePlayEvent callNotePlayEvent(World world, int x, int y, int z, byte instrument, byte note) {
        return callEvent(new NotePlayEvent(world.getWorld().getBlockAt(x, y, z), org.bukkit.Instrument.getByType(instrument), new org.bukkit.Note(note)));
    }

    public static void callPlayerItemBreakEvent(EntityHuman human, ItemStack brokenItem) {
        callEvent(new PlayerItemBreakEvent((Player) human.getBukkitEntity(), CraftItemStack.asCraftMirror(brokenItem)));
    }

    public static void handleBlockIgniteEvent(World world, int x, int y, int z, int igniterX, int igniterY, int igniterZ) {
        if (!callBlockIgniteEvent(world, x, y, z, igniterX, igniterY, igniterZ).isCancelled()) {
            world.setTypeUpdate(x, y, z, Blocks.FIRE);
        }
    }

    public static BlockIgniteEvent callBlockIgniteEvent(World world, int x, int y, int z, int igniterX, int igniterY, int igniterZ) {
        org.bukkit.World bukkitWorld = world.getWorld();
        Block igniter = bukkitWorld.getBlockAt(igniterX, igniterY, igniterZ);
        IgniteCause cause;
        switch (igniter.getType()) {
            case LAVA:
            case STATIONARY_LAVA:
                cause = IgniteCause.LAVA;
                break;
            case DISPENSER:
                cause = IgniteCause.FLINT_AND_STEEL;
                break;
            case FIRE: // Fire or any other unknown block counts as SPREAD.
            default:
                cause = IgniteCause.SPREAD;
        }

        return callEvent(new BlockIgniteEvent(bukkitWorld.getBlockAt(x, y, z), cause, igniter));
    }

    public static void handleBlockIgniteEvent(World world, int x, int y, int z, Entity igniter) {
        if (callBlockIgniteEvent(world, x, y, z, igniter).isCancelled()) {
            world.setTypeUpdate(x, y, z, Blocks.FIRE);
        }
    }

    public static BlockIgniteEvent callBlockIgniteEvent(World world, int x, int y, int z, Entity igniter) {
        org.bukkit.entity.Entity bukkitIgniter = igniter.getBukkitEntity();
        IgniteCause cause;
        switch (bukkitIgniter.getType()) {
        case ENDER_CRYSTAL:
            cause = IgniteCause.ENDER_CRYSTAL;
            break;
        case LIGHTNING:
            cause = IgniteCause.LIGHTNING;
            break;
        case SMALL_FIREBALL:
        case FIREBALL:
            cause = IgniteCause.FIREBALL;
            break;
        default:
            cause = IgniteCause.FLINT_AND_STEEL;
        }

        return callEvent(new BlockIgniteEvent(world.getWorld().getBlockAt(x, y, z), cause, bukkitIgniter));
    }

    public static void handleBlockIgniteEvent(World world, int x, int y, int z, IgniteCause cause, Entity igniter) {
        if (!callBlockIgniteEvent(world, x, y, z, cause, igniter).isCancelled()) {
            world.setTypeUpdate(x, y, z, Blocks.FIRE);
        }
    }

    public static BlockIgniteEvent callBlockIgniteEvent(World world, int x, int y, int z, IgniteCause cause, Entity igniter) {
        return callEvent(new BlockIgniteEvent(world.getWorld().getBlockAt(x, y, z), cause, igniter == null ? null : igniter.getBukkitEntity()));
    }

    public static void handleInventoryCloseEvent(EntityHuman human) {
        callEvent(new InventoryCloseEvent(human.activeContainer.getBukkitView()));
        human.activeContainer.transferTo(human.defaultContainer, human.getBukkitEntity());
    }

    public static void handleEditBookEvent(EntityPlayer player, ItemStack newBookItem) {
        int itemInHandIndex = player.inventory.itemInHandIndex;

        PlayerEditBookEvent editBookEvent = callEvent(new PlayerEditBookEvent(player.getBukkitEntity(), player.inventory.itemInHandIndex, (BookMeta) CraftItemStack.getItemMeta(player.inventory.getItemInHand()), (BookMeta) CraftItemStack.getItemMeta(newBookItem), newBookItem.getItem() == Items.WRITTEN_BOOK));
        ItemStack itemInHand = player.inventory.getItem(itemInHandIndex);

        // If they've got the same item in their hand, it'll need to be updated.
        if (itemInHand.getItem() == Items.BOOK_AND_QUILL) {
            if (!editBookEvent.isCancelled()) {
                CraftItemStack.setItemMeta(itemInHand, editBookEvent.getNewBookMeta());
                if (editBookEvent.isSigning()) {
                    itemInHand.setItem(Items.WRITTEN_BOOK);
                }
            }

            // Client will have updated its idea of the book item; we need to overwrite that
            Slot slot = player.activeContainer.a(player.inventory, itemInHandIndex);
            player.playerConnection.sendPacket(new PacketPlayOutSetSlot(player.activeContainer.windowId, slot.rawSlotIndex, itemInHand));
        }
    }

    public static PlayerUnleashEntityEvent callPlayerUnleashEntityEvent(EntityInsentient entity, EntityHuman player) {
        return callEvent(new PlayerUnleashEntityEvent(entity.getBukkitEntity(), (Player) player.getBukkitEntity()));
    }

    public static boolean handlePlayerLeashEntityEvent(EntityInsentient entity, Entity leashHolder, EntityHuman player) {
        boolean cancelled = callEvent(new PlayerLeashEntityEvent(entity.getBukkitEntity(), leashHolder.getBukkitEntity(), (Player) player.getBukkitEntity())).isCancelled();

        if (cancelled) {
            ((EntityPlayer) player).playerConnection.sendPacket(new PacketPlayOutAttachEntity(1, entity, entity.getLeashHolder()));
        }

        return !cancelled;
    }

    public static boolean handleInteractEvent(Entity entity, World world, int x, int y, int z) {
        if (entity instanceof EntityHuman) {
            return !callEvent(new PlayerInteractEvent((Player) entity.getBukkitEntity(), Action.PHYSICAL, null, world.getWorld().getBlockAt(x, y, z), BlockFace.SELF)).isCancelled();
        } else {
            return !callEvent(new EntityInteractEvent(entity.getBukkitEntity(), world.getWorld().getBlockAt(x, y, z))).isCancelled();
        }
    }

    public static Cancellable handleStatisticsIncrease(EntityHuman entityHuman, net.minecraft.server.Statistic statistic, int current, int incrementation) {
        Player player = ((EntityPlayer) entityHuman).getBukkitEntity();
        Event event;
        if (statistic instanceof net.minecraft.server.Achievement) {
            if (current != 0) {
                return null;
            }
            event = new PlayerAchievementAwardedEvent(player, CraftStatistic.getBukkitAchievement((net.minecraft.server.Achievement) statistic));
        } else {
            Statistic stat = CraftStatistic.getBukkitStatistic(statistic);
            switch (stat) {
                case FALL_ONE_CM:
                case BOAT_ONE_CM:
                case CLIMB_ONE_CM:
                case DIVE_ONE_CM:
                case FLY_ONE_CM:
                case HORSE_ONE_CM:
                case MINECART_ONE_CM:
                case PIG_ONE_CM:
                case PLAY_ONE_TICK:
                case SWIM_ONE_CM:
                case WALK_ONE_CM:
                    // Do not process event for these - too spammy
                    return null;
                default:
            }
            if (stat.getType() == Type.UNTYPED) {
                event = new PlayerStatisticIncrementEvent(player, stat, current, current + incrementation);
            } else if (stat.getType() == Type.ENTITY) {
                EntityType entityType = CraftStatistic.getEntityTypeFromStatistic(statistic);
                event = new PlayerStatisticIncrementEvent(player, stat, current, current + incrementation, entityType);
            } else {
                Material material = CraftStatistic.getMaterialFromStatistic(statistic);
                event = new PlayerStatisticIncrementEvent(player, stat, current, current + incrementation, material);
            }
        }

        return (Cancellable) callEvent(event);
    }

    public static BlockFromToEvent callBlockFromToEvent(World world, int x, int y, int z, int toX, int toY, int toZ) {
        CraftWorld bukkitWorld = world.getWorld();

        return callEvent(new BlockFromToEvent(bukkitWorld.getBlockAt(x, y, z), bukkitWorld.getBlockAt(toX, toY, toZ)));
    }

    public static BlockFromToEvent callBlockFromToEvent(Block block, BlockFace face) {
        return callEvent(new BlockFromToEvent(block, face));
    }

    public static InventoryMoveItemEvent callInventoryMoveItemEvent(Inventory sourceInventory, CraftItemStack itemStack, Inventory destinationInventory, boolean didSourceInitiate) {
        return callEvent(new InventoryMoveItemEvent(sourceInventory, itemStack, destinationInventory, didSourceInitiate));
    }

    public static boolean handleInventoryDragEvent(InventoryView view, ItemStack itemstack, int amount, int dragButton, PlayerInventory inventory, Map<Integer, org.bukkit.inventory.ItemStack> draggedSlots) {
        org.bukkit.inventory.ItemStack newCursor = CraftItemStack.asCraftMirror(itemstack);
        newCursor.setAmount(amount);

        // It's essential that we set the cursor to the new value here to prevent item duplication if a plugin closes the inventory.
        ItemStack oldCursor = inventory.getCarried();
        inventory.setCarried(CraftItemStack.asNMSCopy(newCursor));

        InventoryDragEvent event = callEvent(new InventoryDragEvent(view, CraftItemStack.asBukkitCopy(oldCursor), (newCursor.getType() != Material.AIR ? newCursor : null), dragButton == 1, draggedSlots));
        Event.Result result = event.getResult();
        // Whether or not a change was made to the inventory that requires an update.
        boolean needsUpdate = result != Event.Result.DEFAULT;

        if (result != Event.Result.DENY) {
            for (Map.Entry<Integer, org.bukkit.inventory.ItemStack> slot : draggedSlots.entrySet()) {
                view.setItem(slot.getKey(), slot.getValue());
            }
            // The only time the carried item will be set to null is if the inventory is closed by the server.
            // If the inventory is closed by the server, then the cursor items are dropped.  This is why we change the cursor early.
            if (inventory.getCarried() != null) {
                inventory.setCarried(CraftItemStack.asNMSCopy(event.getCursor()));
                needsUpdate = true;

            }
        } else {
            inventory.setCarried(oldCursor);
        }

        return needsUpdate;
    }

    public static EntityPortalEnterEvent callEntityPortalEnterEvent(Entity entity, World world, int x, int y, int z) {
        return callEvent(new EntityPortalEnterEvent(entity.getBukkitEntity(), new Location(world.getWorld(), x, y, z)));
    }

    public static BlockBurnEvent callBlockBurnEvent(World world, int x, int y, int z) {
        return callEvent(new BlockBurnEvent(world.getWorld().getBlockAt(x, y, z)));
    }

    public static LeavesDecayEvent callLeavesDecayEvent(World world, int x, int y, int z) {
        return callEvent(new LeavesDecayEvent(world.getWorld().getBlockAt(x, y, z)));
    }

    public static BlockPistonEvent callBlockPistonEvent(World world, int x, int y, int z, int length, int blockFace) {
        if (length <= 0) {
            return callEvent(new BlockPistonRetractEvent(world.getWorld().getBlockAt(x, y, z), CraftBlock.notchToBlockFace(blockFace)));
        } else {
            return callEvent(new BlockPistonExtendEvent(world.getWorld().getBlockAt(x, y, z), length, CraftBlock.notchToBlockFace(blockFace)));
        }
    }

    public static FurnaceSmeltEvent callFurnaceSmeltEvent(World world, int x, int y, int z, ItemStack source, ItemStack result) {
        return callEvent(new FurnaceSmeltEvent(world.getWorld().getBlockAt(x, y, z), CraftItemStack.asCraftMirror(source), CraftItemStack.asBukkitCopy(result)));
    }

    public static FurnaceBurnEvent callFurnaceBurnEvent(World world, int x, int y, int z, ItemStack fuel) {
        return callEvent(new FurnaceBurnEvent(world.getWorld().getBlockAt(x, y, z), CraftItemStack.asCraftMirror(fuel), TileEntityFurnace.fuelTime(fuel)));
    }

    public static FurnaceExtractEvent callFurnaceExtractEvent(EntityHuman entity, TileEntityFurnace furnace, ItemStack itemStack, int xp) {
        return callEvent(new FurnaceExtractEvent((Player) entity.getBukkitEntity(), entity.world.getWorld().getBlockAt(furnace.x, furnace.y, furnace.z), CraftMagicNumbers.getMaterial(itemStack.getItem()), itemStack.count, xp));
    }

    public static BrewEvent callBrewEvent(World world, int x, int y, int z, BrewerInventory inventory) {
        return callEvent(new BrewEvent(world.getWorld().getBlockAt(x, y, z), inventory));
    }

    public static ThunderChangeEvent callThunderChangeEvent(CraftWorld world, boolean isThundering) {
        return callEvent(new ThunderChangeEvent(world, isThundering));
    }

    public static WeatherChangeEvent callWeatherChangeEvent(CraftWorld world, boolean isStorming) {
        return callEvent(new WeatherChangeEvent(world, isStorming));
    }

    public static EntityUnleashEvent callEntityUnleashEvent(Entity entity, EntityUnleashEvent.UnleashReason reason) {
        return callEvent(new EntityUnleashEvent(entity.getBukkitEntity(), reason));
    }

    public static SlimeSplitEvent callSlimeSplitEvent(Entity entity, int count) {
        return callEvent(new SlimeSplitEvent((Slime) entity.getBukkitEntity(), count));
    }

    public static BlockCanBuildEvent callBlockCanBuildEvent(World world, int x, int y, int z, net.minecraft.server.Block block, boolean canBuild) {
        return callEvent(new BlockCanBuildEvent(world.getWorld().getBlockAt(x, y, z), CraftMagicNumbers.getId(block), canBuild));
    }

    public static BlockDispenseEvent callBlockDispenseEvent(ISourceBlock isourceblock, CraftItemStack item, double vectorX, double vectorY, double vectorZ) {
        return callEvent(new BlockDispenseEvent(isourceblock.k().getWorld().getBlockAt(isourceblock.getBlockX(), isourceblock.getBlockY(), isourceblock.getBlockZ()), item.clone(), new Vector(vectorX, vectorY, vectorZ)));
    }

    public static void handleBlockFormEvent(World world, int x, int y, int z, net.minecraft.server.Block type) {
        Block block = world.getWorld().getBlockAt(x, y, z);
        BlockState state = block.getState();
        state.setType(CraftMagicNumbers.getMaterial(type));

        if (!callEvent(new BlockFormEvent(block, state)).isCancelled()) {
            state.update(true);
        }
    }

    public static BlockPhysicsEvent callBlockPhysicsEvent(World world, int x, int y, int z, net.minecraft.server.Block type) {
        return callEvent(new BlockPhysicsEvent(world.getWorld().getBlockAt(x, y, z), CraftMagicNumbers.getId(type)));
    }

    public static void handleEntityBlockFormEvent(Entity entity, int x, int y, int z, net.minecraft.server.Block type) {
        Block block = entity.world.getWorld().getBlockAt(x, y, z);
        BlockState state = block.getState();
        state.setType(CraftMagicNumbers.getMaterial(type));

        if (!callEvent(new EntityBlockFormEvent(entity.getBukkitEntity(), block, state)).isCancelled()) {
            state.update(true);
        }
    }

    public static void handleSignChangeEvent(EntityPlayer entity, int x, int y, int z, TileEntitySign sign, String[] lines) {
        Player player = entity.getBukkitEntity();
        SignChangeEvent event = callEvent(new SignChangeEvent(player.getWorld().getBlockAt(x, y, z), player, lines));

        if (!event.isCancelled()) {
            sign.lines = CraftSign.sanitizeLines(event.getLines());
            sign.isEditable = false;
        }
    }

    public static boolean handleEnchantItemEvent(List list, ItemStack itemstack, EntityHuman entityhuman, InventoryView view, World world, int x, int y, int z, int level, int button, boolean isBook) {
        Map<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();

        for (Object obj : list) {
            EnchantmentInstance instance = (EnchantmentInstance) obj;
            enchants.put(Enchantment.getById(instance.enchantment.id), instance.level);
        }

        CraftItemStack item = CraftItemStack.asCraftMirror(itemstack);

        EnchantItemEvent event = callEvent(new EnchantItemEvent((Player) entityhuman.getBukkitEntity(), view, world.getWorld().getBlockAt(x, y, z), item, level, enchants, button));
        level = event.getExpLevelCost();
        enchants = event.getEnchantsToAdd();

        if (event.isCancelled() || (level > entityhuman.expLevel && !entityhuman.abilities.canInstantlyBuild) || enchants.isEmpty()) {
            return false;
        }

        if (isBook) {
            itemstack.setItem(Items.ENCHANTED_BOOK);
        }

        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            try {
                if (isBook) {
                    int enchantId = entry.getKey().getId();
                    if (net.minecraft.server.Enchantment.byId[enchantId] == null) {
                        continue;
                    }

                    Items.ENCHANTED_BOOK.a(itemstack, new EnchantmentInstance(enchantId, entry.getValue()));
                } else {
                    item.addEnchantment(entry.getKey(), entry.getValue());
                }
            } catch (IllegalArgumentException e) {
                /* Just swallow invalid enchantments */
            }
        }

        entityhuman.levelDown(-level);
        return true;
    }

    public static PrepareItemEnchantEvent callPrepareItemEnchantEvent(Player player, InventoryView view, World world, int x, int y, int z, ItemStack itemstack, int[] levels, int bonus) {
        CraftItemStack item = CraftItemStack.asCraftMirror(itemstack);

        PrepareItemEnchantEvent event = new PrepareItemEnchantEvent(player, view, world.getWorld().getBlockAt(x, y, z), item, levels, bonus);
        event.setCancelled(!itemstack.x());
        return callEvent(event);
    }

    public static int handleEntityCombustByBlockEvent(Block combuster, Entity combustee, int duration) {
        EntityCombustEvent event = callEvent(new EntityCombustByBlockEvent(combuster, combustee.getBukkitEntity(), duration));

        return event.isCancelled() ? 0 : event.getDuration();
    }

    public static int handleEntityCombustByEntityEvent(Entity combuster, Entity combustee, int duration) {
        EntityCombustEvent event = callEvent(new EntityCombustByEntityEvent(combuster.getBukkitEntity(), combustee.getBukkitEntity(), duration));

        return event.isCancelled() ? 0 : event.getDuration();
    }

    public static int handleEntityCombustEvent(Entity combustee, int duration) {
        EntityCombustEvent event = callEvent(new EntityCombustEvent(combustee.getBukkitEntity(), duration));

        return event.isCancelled() ? 0 : event.getDuration();
    }

    public static void handleEntityCreatePortalEvent(EntityLiving entity, List<BlockState> states, PortalType type) {
        EntityCreatePortalEvent event = callEvent(new EntityCreatePortalEvent((LivingEntity) entity.getBukkitEntity(), states, type));

        for (BlockState state : event.getBlocks()) {
            if (!event.isCancelled()) {
                state.update(true);
            } else {
                PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(state.getX(), state.getY(), state.getZ(), entity.world);
                for (Iterator it = entity.world.players.iterator(); it.hasNext();) {
                    EntityHuman human = (EntityHuman) it.next();
                    if (human instanceof EntityPlayer) {
                        ((EntityPlayer) human).playerConnection.sendPacket(packet);
                    }
                }
            }
        }
    }

    public static SheepRegrowWoolEvent callSheepRegrowWoolEvent(EntitySheep entity) {
        return callEvent(new SheepRegrowWoolEvent((Sheep) entity.getBukkitEntity()));
    }

    public static SheepDyeWoolEvent callSheepDyeWoolEvent(EntitySheep entity, byte color) {
        return callEvent(new SheepDyeWoolEvent((Sheep) entity.getBukkitEntity(), DyeColor.getByWoolData(color)));
    }

    public static void handlePlayerDeathEvent(EntityPlayer player) {
        if (player.dead) {
            return;
        }

        List<org.bukkit.inventory.ItemStack> loot = new ArrayList<org.bukkit.inventory.ItemStack>();
        World world = player.world;
        boolean keepInventory = world.getGameRules().getBoolean("keepInventory");

        if (!keepInventory) {
            for (int i = 0; i < player.inventory.items.length; ++i) {
                if (player.inventory.items[i] != null) {
                    loot.add(CraftItemStack.asCraftMirror(player.inventory.items[i]));
                }
            }

            for (int i = 0; i < player.inventory.armor.length; ++i) {
                if (player.inventory.armor[i] != null) {
                    loot.add(CraftItemStack.asCraftMirror(player.inventory.armor[i]));
                }
            }
        }

        IChatBaseComponent chatmessage = player.aV().b();
        String deathmessage = chatmessage.c();

        CraftPlayer entity = player.getBukkitEntity();
        PlayerDeathEvent event = callEvent(new PlayerDeathEvent(entity, loot, player.getExpReward(), 0, deathmessage));

        player.keepLevel = event.getKeepLevel();
        player.newLevel = event.getNewLevel();
        player.newTotalExp = event.getNewTotalExp();
        player.expToDrop = event.getDroppedExp();
        player.newExp = event.getNewExp();

        Location location = entity.getLocation();
        for (org.bukkit.inventory.ItemStack stack : event.getDrops()) {
            if (stack == null || stack.getType() == Material.AIR || stack.getAmount() == 0) {
                continue;
            }

            world.getWorld().dropItemNaturally(location, stack);
        }

        String deathMessage = event.getDeathMessage();

        if (deathMessage != null && deathMessage.length() > 0) {
            if (deathMessage.equals(deathmessage)) {
                player.server.getPlayerList().sendMessage(chatmessage);
            } else {
                player.server.getPlayerList().sendMessage(CraftChatMessage.fromString(deathMessage));
            }
        }

        // we clean the player's inventory after the EntityDeathEvent is called so plugins can get the exact state of the inventory.
        if (!keepInventory) {
            for (int i = 0; i < player.inventory.items.length; ++i) {
                player.inventory.items[i] = null;
            }

            for (int i = 0; i < player.inventory.armor.length; ++i) {
                player.inventory.armor[i] = null;
            }
        }

        player.closeInventory();
    }

    public static boolean handleExplosionPrimeEvent(Entity entity, double x, double y, double z, float radius, boolean fire, boolean breakBlocks) {
        ExplosionPrimeEvent event = callEvent(new ExplosionPrimeEvent(entity.getBukkitEntity(), radius, fire));
        boolean cancelled = event.isCancelled();

        if (!cancelled) {
            entity.world.createExplosion(entity, x, y, z, event.getRadius(), event.getFire(), breakBlocks);
        }

        return cancelled;
    }

    public static EntityTeleportEvent callEntityTeleportEvent(Entity entity, double x, double y, double z, double toX, double toY, double toZ) {
        CraftWorld world = entity.world.getWorld();

        return callEvent(new EntityTeleportEvent(entity.getBukkitEntity(), new Location(world, x, y, z), new Location(world, toX, toY, toZ)));
    }

    public static float handleEntityRegainHealthEvent(Entity entity, float amount, EntityRegainHealthEvent.RegainReason reason) {
        EntityRegainHealthEvent event = callEvent(new EntityRegainHealthEvent(entity.getBukkitEntity(), amount, reason));

        return event.isCancelled() ? 0.0F : (float) event.getAmount();
    }

    public static EntityPortalExitEvent callEntityPortalExitEvent(Entity entity, Location from, Location to, Vector before, Vector after) {
        return callEvent(new EntityPortalExitEvent(entity.getBukkitEntity(), from, to, before, after));
    }

    public static void handlePlayerPortalEvent(MinecraftServer minecraftserver, EntityPlayer entityplayer, int dimension, PlayerTeleportEvent.TeleportCause cause) {
        WorldServer exitWorld = null;
        if (entityplayer.dimension < CraftWorld.CUSTOM_DIMENSION_OFFSET) { // plugins must specify exit from custom Bukkit worlds
            // only target existing worlds (compensate for allow-nether/allow-end as false)
            for (WorldServer world : minecraftserver.worlds) {
                if (world.dimension == dimension) {
                    exitWorld = world;
                }
            }
        }

        PlayerList playerList = minecraftserver.getPlayerList();
        Player player = entityplayer.getBukkitEntity();
        Location enter = player.getLocation();
        Location exit = null;
        boolean useTravelAgent = false; // don't use agent for custom worlds or return from THE_END

        if (exitWorld != null) {
            if ((cause == PlayerTeleportEvent.TeleportCause.END_PORTAL) && (dimension == 0)) {
                // THE_END -> NORMAL; use bed if available, otherwise default spawn
                exit = player.getBedSpawnLocation();
                if (exit == null || ((CraftWorld) exit.getWorld()).getHandle().dimension != 0) {
                    exit = exitWorld.getWorld().getSpawnLocation();
                }
            } else {
                // NORMAL <-> NETHER or NORMAL -> THE_END
                exit = playerList.calculateTarget(enter, exitWorld);
                useTravelAgent = true;
            }
        }

        TravelAgent agent = exit != null ? (TravelAgent) ((CraftWorld) exit.getWorld()).getHandle().getTravelAgent() : CraftTravelAgent.DEFAULT; // return arbitrary TA to compensate for implementation dependent plugins
        PlayerPortalEvent event = new PlayerPortalEvent(player, enter, exit, agent, cause);
        event.useTravelAgent(useTravelAgent);
        callEvent(event);
        Location to = event.getTo();
        if (event.isCancelled() || to == null) {
            return;
        }

        exit = event.useTravelAgent() ? event.getPortalTravelAgent().findOrCreate(to) : to;
        if (exit == null) {
            return;
        }
        exitWorld = ((CraftWorld) exit.getWorld()).getHandle();

        Vector velocity = player.getVelocity();
        boolean before = exitWorld.chunkProviderServer.forceChunkLoad;
        exitWorld.chunkProviderServer.forceChunkLoad = true;
        exitWorld.getTravelAgent().adjustExit(entityplayer, exit, velocity);
        exitWorld.chunkProviderServer.forceChunkLoad = before;

        playerList.moveToWorld(entityplayer, exitWorld.dimension, true, exit, false); // Vanilla doesn't check for suffocation when handling portals, so neither should we
        if (entityplayer.motX != velocity.getX() || entityplayer.motY != velocity.getY() || entityplayer.motZ != velocity.getZ()) {
            entityplayer.getBukkitEntity().setVelocity(velocity);
        }
    }

    public static EntityPortalEvent handleEntityPortalEvent(MinecraftServer minecraftserver, Entity entity, int dimension) {
        WorldServer exitWorld = null;

        if (entity.dimension < CraftWorld.CUSTOM_DIMENSION_OFFSET) { // Plugins must specify exit from custom Bukkit worlds
            // Only target existing worlds (compensate for allow-nether/allow-end as false)
            for (WorldServer world : minecraftserver.worlds) {
                if (world.dimension == dimension) {
                    exitWorld = world;
                }
            }
        }

        org.bukkit.entity.Entity bukkitEntity = entity.getBukkitEntity();
        Location enter = bukkitEntity.getLocation();
        Location exit = exitWorld != null ? minecraftserver.getPlayerList().calculateTarget(enter, minecraftserver.getWorldServer(dimension)) : null;
        boolean useTravelAgent = exitWorld != null && !(entity.dimension == 1 && exitWorld.dimension == 1); // don't use agent for custom worlds or return from THE_END

        TravelAgent agent = exit != null ? (TravelAgent) ((CraftWorld) exit.getWorld()).getHandle().getTravelAgent() : CraftTravelAgent.DEFAULT; // return arbitrary TA to compensate for implementation dependent plugins
        EntityPortalEvent event = new EntityPortalEvent(bukkitEntity, enter, exit, agent);
        event.useTravelAgent(useTravelAgent);
        return callEvent(event);
    }

    public static EntityExplodeEvent callEntityExplodeEvent(Entity entity, World world, double x, double y, double z, List<Block> blockList, float yield) {
        return callEvent(new EntityExplodeEvent(entity == null ? null : entity.getBukkitEntity(), new Location(world.getWorld(), x, y, z), blockList, yield));
    }

    public static boolean handleHangingBreakEvent(Entity entity, DamageSource damagesource) {
        boolean cancelled;
        Entity damager = damagesource.getEntity();

        if (damager != null) {
            cancelled = handleHangingBreakByEntityEvent(entity, damager);
        } else if (damagesource.isExplosion()) {
            cancelled = callEvent(new HangingBreakEvent((Hanging) entity.getBukkitEntity(), HangingBreakEvent.RemoveCause.EXPLOSION)).isCancelled();
        } else {
            cancelled = callEvent(new HangingBreakEvent((Hanging) entity.getBukkitEntity(), HangingBreakEvent.RemoveCause.DEFAULT)).isCancelled();
        }

        return entity.dead || cancelled;
    }

    public static boolean handleHangingBreakEvent(Entity entity, HangingBreakEvent.RemoveCause cause) {
        org.bukkit.entity.Entity bukkitEntity = entity.getBukkitEntity();
        boolean hangingCancelled = callEvent(new HangingBreakEvent((Hanging) bukkitEntity, cause)).isCancelled();
        boolean paintingCancelled = false;

        // Fire old painting event until it can be removed
        if (entity instanceof EntityPainting) {
            try {
                PaintingBreakEvent event = new PaintingBreakEvent((Painting) bukkitEntity, PaintingBreakEvent.RemoveCause.valueOf(cause.name()));
                event.setCancelled(hangingCancelled);
                paintingCancelled = callEvent(event).isCancelled();
            } catch (IllegalArgumentException e) {
                // ignore the error, don't call the event
            }
        }

        return entity.dead || hangingCancelled || paintingCancelled;
    }

    public static boolean handleHangingBreakByEntityEvent(Entity entity, Entity damager) {
        org.bukkit.entity.Entity bukkitDamager = damager.getBukkitEntity();
        org.bukkit.entity.Entity bukkitEntity = entity.getBukkitEntity();
        boolean hangingCancelled = callEvent(new HangingBreakByEntityEvent((Hanging) bukkitEntity, bukkitDamager)).isCancelled();
        boolean paintingCancelled = false;

        // Fire old painting event until it can be removed
        if (entity instanceof EntityPainting) {
            PaintingBreakEvent event = new PaintingBreakByEntityEvent((Painting) bukkitEntity, bukkitDamager);
            event.setCancelled(hangingCancelled);
            paintingCancelled = callEvent(event).isCancelled();
        }

        return hangingCancelled || paintingCancelled;
    }

    public static boolean handleHangingPlaceEvent(EntityHanging entityhanging, EntityHuman entityhuman, World world, int x, int y, int z, BlockFace face) {
        org.bukkit.entity.Entity hanging = entityhanging.getBukkitEntity();
        Player player = (entityhuman == null) ? null : (Player) entityhuman.getBukkitEntity();
        org.bukkit.block.Block blockClicked = world.getWorld().getBlockAt(x, y, z);
        boolean hangingCancelled = callEvent(new HangingPlaceEvent((Hanging) hanging, player, blockClicked, face)).isCancelled();
        boolean paintingCancelled = false;

        if (entityhanging instanceof EntityPainting) {
            // Fire old painting event until it can be removed
            PaintingPlaceEvent event = new PaintingPlaceEvent((Painting) hanging, player, blockClicked, face);
            event.setCancelled(hangingCancelled);
            paintingCancelled = callEvent(event).isCancelled();
        }

        return hangingCancelled || paintingCancelled;
    }

    public static InventoryClickEvent callInventoryClickEvent(InventoryView inventory, InventoryType.SlotType type, ClickType click, InventoryAction action, PacketPlayInWindowClick packetplayinwindowclick) {
        InventoryClickEvent event;

        if (click == ClickType.NUMBER_KEY) {
            event = new InventoryClickEvent(inventory, type, packetplayinwindowclick.d(), click, action, packetplayinwindowclick.e());
        } else {
            event = new InventoryClickEvent(inventory, type, packetplayinwindowclick.d(), click, action);
        }

        Inventory top = inventory.getTopInventory();
        if (packetplayinwindowclick.d() == 0 && top instanceof CraftingInventory) {
            Recipe recipe = ((CraftingInventory) top).getRecipe();
            if (recipe != null) {
                if (click == ClickType.NUMBER_KEY) {
                    event = new CraftItemEvent(recipe, inventory, type, packetplayinwindowclick.d(), click, action, packetplayinwindowclick.e());
                } else {
                    event = new CraftItemEvent(recipe, inventory, type, packetplayinwindowclick.d(), click, action);
                }
            }
        }

        return callEvent(event);
    }

    public static InventoryCreativeEvent handleInventoryCreativeEvent(EntityPlayer entity, PacketPlayInSetCreativeSlot packetplayinsetcreativeslot) {
        HumanEntity player = entity.getBukkitEntity();
        InventoryView inventory = new CraftInventoryView(player, player.getInventory(), entity.defaultContainer);
        org.bukkit.inventory.ItemStack item = CraftItemStack.asBukkitCopy(packetplayinsetcreativeslot.getItemStack());
        int slot = packetplayinsetcreativeslot.c();
        InventoryType.SlotType type;

        if (slot < 0) {
            type = InventoryType.SlotType.OUTSIDE;
        } else if (slot < 36) {
            if (slot >= 5 && slot < 9) {
                type = InventoryType.SlotType.ARMOR;
            } else {
                type = InventoryType.SlotType.CONTAINER;
            }
        } else {
            type = InventoryType.SlotType.QUICKBAR;
        }

        return callEvent(new InventoryCreativeEvent(inventory, type, type == InventoryType.SlotType.OUTSIDE ? -999 : packetplayinsetcreativeslot.c(), item));
    }

    public static InventoryPickupItemEvent callInventoryPickupItemEvent(IInventory inventory, EntityItem item) {
        return callEvent(new InventoryPickupItemEvent(inventory.getOwner().getInventory(), (Item) item.getBukkitEntity()));
    }

    public static void handlePlayerChatEvent(boolean isAsync, Player player, String message, final MinecraftServer minecraftServer) {
        AsyncPlayerChatEvent event = callEvent(new AsyncPlayerChatEvent(isAsync, player, message, new LazyPlayerSet()));

        if (PlayerChatEvent.getHandlerList().getRegisteredListeners().length != 0) {
            // Evil plugins still listening to deprecated event
            final PlayerChatEvent queueEvent = new PlayerChatEvent(player, event.getMessage(), event.getFormat(), event.getRecipients());
            queueEvent.setCancelled(event.isCancelled());
            Waitable waitable = new Waitable() {
                @Override
                protected Object evaluate() {
                    callEvent(queueEvent);

                    if (queueEvent.isCancelled()) {
                        return null;
                    }

                    String message = String.format(queueEvent.getFormat(), queueEvent.getPlayer().getDisplayName(), queueEvent.getMessage());
                    minecraftServer.console.sendMessage(message);
                    if (((LazyPlayerSet) queueEvent.getRecipients()).isLazy()) {
                        for (Object player : minecraftServer.getPlayerList().players) {
                            ((EntityPlayer) player).sendMessage(CraftChatMessage.fromString(message));
                        }
                    } else {
                        for (Player player : queueEvent.getRecipients()) {
                            player.sendMessage(message);
                        }
                    }
                    return null;
                }};
            if (isAsync) {
                minecraftServer.processQueue.add(waitable);
            } else {
                waitable.run();
            }
            try {
                waitable.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // This is proper habit for java. If we aren't handling it, pass it on!
            } catch (ExecutionException e) {
                throw new RuntimeException("Exception processing chat event", e.getCause());
            }
        } else {
            if (event.isCancelled()) {
                return;
            }

            message = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
            minecraftServer.console.sendMessage(message);
            Set<Player> players = event.getRecipients();

            if (((LazyPlayerSet) players).isLazy()) {
                for (Object recipient : minecraftServer.getPlayerList().players) {
                    ((EntityPlayer) recipient).sendMessage(CraftChatMessage.fromString(message));
                }
            } else {
                for (Player recipient : players) {
                    recipient.sendMessage(message);
                }
            }
        }
    }

    public static boolean handlePlayerPreLoginEvent(LoginListener loginListener, GameProfile gameProfile, MinecraftServer minecraftServer) throws InterruptedException, ExecutionException {
        String playerName = gameProfile.getName();
        InetAddress address = ((InetSocketAddress) loginListener.networkManager.getSocketAddress()).getAddress();
        UUID uniqueId = gameProfile.getId();

        AsyncPlayerPreLoginEvent asyncEvent = callEvent(new AsyncPlayerPreLoginEvent(playerName, address, uniqueId));

        if (PlayerPreLoginEvent.getHandlerList().getRegisteredListeners().length != 0) {
            final PlayerPreLoginEvent event = new PlayerPreLoginEvent(playerName, address, uniqueId);
            PlayerPreLoginEvent.Result result = asyncEvent.getResult();
            if (result != PlayerPreLoginEvent.Result.ALLOWED) {
                event.disallow(result, asyncEvent.getKickMessage());
            }
            Waitable<PlayerPreLoginEvent.Result> waitable = new Waitable<PlayerPreLoginEvent.Result>() {
                @Override
                protected PlayerPreLoginEvent.Result evaluate() {
                    callEvent(event);
                    return event.getResult();
                }};

            minecraftServer.processQueue.add(waitable);
            if (waitable.get() != PlayerPreLoginEvent.Result.ALLOWED) {
                loginListener.disconnect(event.getKickMessage());
                return false;
            }
        } else {
            if (asyncEvent.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
                loginListener.disconnect(asyncEvent.getKickMessage());
                return false;
            }
        }

        return true;
    }

    public static PlayerAnimationEvent callPlayerAnimationEvent(Player player) {
        return callEvent(new PlayerAnimationEvent(player));
    }

    public static PlayerBedLeaveEvent callPlayerBedLeaveEvent(EntityHuman entity, ChunkCoordinates chunkcoordinates) {
        Player player = (Player) entity.getBukkitEntity();
        return callEvent(new PlayerBedLeaveEvent(player, chunkcoordinates != null ? player.getWorld().getBlockAt(chunkcoordinates.x, chunkcoordinates.y, chunkcoordinates.z) : player.getWorld().getBlockAt(player.getLocation())));
    }

    public static PlayerBedEnterEvent callPlayerBedEnterEvent(EntityHuman entity, int x, int y, int z) {
        Player player = (Player) entity.getBukkitEntity();
        return callEvent(new PlayerBedEnterEvent(player, player.getWorld().getBlockAt(x, y, z)));
    }

    public static PlayerChangedWorldEvent callPlayerChangedWorldEvent(EntityPlayer entity, org.bukkit.World world) {
        return callEvent(new PlayerChangedWorldEvent(entity.getBukkitEntity(), world));
    }

    public static void handlePlayerCommandPreprocessEvent(String message, Player player, Logger logger, CraftServer server) {
        PlayerCommandPreprocessEvent event = callEvent(new PlayerCommandPreprocessEvent(player, message, new LazyPlayerSet()));

        if (event.isCancelled()) {
            return;
        }

        try {
            logger.info(event.getPlayer().getName() + " issued server command: " + event.getMessage());
            server.dispatchCommand(event.getPlayer(), event.getMessage().substring(1));
        } catch (org.bukkit.command.CommandException ex) {
            player.sendMessage(ChatColor.RED + "An internal error occurred while attempting to perform this command");
            java.util.logging.Logger.getLogger(PlayerConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static boolean handlePlayerDropItemEvent(EntityHuman entityhuman, EntityItem entityitem) {
        Player player = (Player) entityhuman.getBukkitEntity();
        Item drop = (Item) entityitem.getBukkitEntity();

        PlayerDropItemEvent event = callEvent(new PlayerDropItemEvent(player, drop));
        boolean cancelled = event.isCancelled();

        if (cancelled) {
            player.getInventory().addItem(drop.getItemStack());
        }

        return cancelled;
    }

    public static PlayerEggThrowEvent callPlayerEggThrowEvent(EntityLiving shooter, EntityEgg egg, boolean hatching, byte numHatching, EntityType hatchingType) {
        return callEvent(new PlayerEggThrowEvent((Player) shooter.getBukkitEntity(), (Egg) egg.getBukkitEntity(), hatching, numHatching, hatchingType));
    }

    public static PlayerFishEvent callPlayerFishEvent(EntityHuman player, Entity hooked, EntityFishingHook hook, PlayerFishEvent.State state, int exp) {
        return callEvent(new PlayerFishEvent((Player) player.getBukkitEntity(), hooked == null ? null : hooked.getBukkitEntity(), (Fish) hook.getBukkitEntity(), state, exp));
    }

    public static boolean handlePlayerInteractEntityEvent(PlayerConnection connection, Entity entity) {
        ItemStack itemInHand = connection.player.inventory.getItemInHand();
        net.minecraft.server.Item item = itemInHand == null? null : itemInHand.getItem();

        boolean triggerTagUpdate = item != null && item == Items.NAME_TAG && entity instanceof EntityInsentient;
        boolean triggerChestUpdate = item != null && item == net.minecraft.server.Item.getItemOf(Blocks.CHEST) && entity instanceof EntityHorse;
        boolean triggerLeashUpdate = item != null && item == Items.LEASH && entity instanceof EntityInsentient;

        PlayerInteractEntityEvent event = callEvent(new PlayerInteractEntityEvent(connection.getPlayer(), entity.getBukkitEntity()));
        boolean cancelled = event.isCancelled();
        itemInHand = connection.player.inventory.getItemInHand();
        item = itemInHand == null? null : itemInHand.getItem();

        if (triggerLeashUpdate && (cancelled || item == null || item != Items.LEASH)) {
            // Refresh the current leash state
            connection.sendPacket(new PacketPlayOutAttachEntity(1, entity, ((EntityInsentient) entity).getLeashHolder()));
        } else if (triggerTagUpdate && (cancelled || item == null || item != Items.NAME_TAG)) {
            // Refresh the current entity metadata
            connection.sendPacket(new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), true));
        } else if (triggerChestUpdate && (cancelled || item == null || item != net.minecraft.server.Item.getItemOf(Blocks.CHEST))) {
            connection.sendPacket(new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), true));
        }

        return cancelled;
    }

    public static BlockBreakEvent handleBlockBreakEvent(PlayerInteractManager interactManager, int x, int y, int z) {
        // Tell client the block is gone immediately then process events
        if (interactManager.world.getTileEntity(x, y, z) == null) {
            PacketPlayOutBlockChange packet = new PacketPlayOutBlockChange(x, y, z, interactManager.world);
            packet.block = Blocks.AIR;
            packet.data = 0;
            interactManager.player.playerConnection.sendPacket(packet);
        }

        Block block = interactManager.world.getWorld().getBlockAt(x, y, z);
        BlockBreakEvent event = new BlockBreakEvent(block, interactManager.player.getBukkitEntity());

        // Adventure mode pre-cancel
        event.setCancelled(interactManager.getGameMode().isAdventure() && !interactManager.player.d(x, y, z));

        // Sword + Creative mode pre-cancel
        event.setCancelled(event.isCancelled() || (interactManager.getGameMode().d() && interactManager.player.bd() != null && interactManager.player.bd().getItem() instanceof ItemSword));

        // Calculate default block experience
        net.minecraft.server.Block nmsBlock = interactManager.world.getType(x, y, z);

        if (nmsBlock != null && !event.isCancelled() && !interactManager.isCreative() && interactManager.player.a(nmsBlock)) {
            // Copied from block.a(world, entityhuman, int, int, int, int)
            if (!(nmsBlock.E() && EnchantmentManager.hasSilkTouchEnchantment(interactManager.player))) {
                int data = block.getData();
                int bonusLevel = EnchantmentManager.getBonusBlockLootEnchantmentLevel(interactManager.player);

                event.setExpToDrop(nmsBlock.getExpDrop(interactManager.world, data, bonusLevel));
            }
        }

        callEvent(event);

        if (event.isCancelled()) {
            // Let the client know the block still exists
            interactManager.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(x, y, z, interactManager.world));
            // Update any tile entity data for this block
            TileEntity tileentity = interactManager.world.getTileEntity(x, y, z);
            if (tileentity != null) {
                interactManager.player.playerConnection.sendPacket(tileentity.getUpdatePacket());
            }
        }

        return event;
    }

    public static boolean handlePlayerItemConsumeEvent(EntityHuman human, ItemStack item) {
        org.bukkit.inventory.ItemStack craftItem = CraftItemStack.asBukkitCopy(item);
        PlayerItemConsumeEvent event = callEvent(new PlayerItemConsumeEvent((Player) human.getBukkitEntity(), craftItem));

        if (event.isCancelled()) {
            // Update client
            if (human instanceof EntityPlayer) {
                ((EntityPlayer) human).playerConnection.sendPacket(new PacketPlayOutSetSlot((byte) 0, human.activeContainer.a(human.inventory, human.inventory.itemInHandIndex).index, item));
            }
            return false;
        }

        // Plugin modified the item, process it but don't remove it
        if (!craftItem.equals(event.getItem())) {
            CraftItemStack.asNMSCopy(event.getItem()).b(human.world, human);

            // Update client
            if (human instanceof EntityPlayer) {
                ((EntityPlayer) human).playerConnection.sendPacket(new PacketPlayOutSetSlot((byte) 0, human.activeContainer.a(human.inventory, human.inventory.itemInHandIndex).index, item));
            }
            return false;
        }

        return true;
    }

    public static boolean handlePlayerItemHeldEvent(PlayerConnection connection, PacketPlayInHeldItemSlot packetplayinhelditemslot) {
        PlayerItemHeldEvent event = callEvent(new PlayerItemHeldEvent(connection.getPlayer(), connection.player.inventory.itemInHandIndex, packetplayinhelditemslot.c()));

        if (event.isCancelled()) {
            connection.sendPacket(new PacketPlayOutHeldItemSlot(connection.player.inventory.itemInHandIndex));
            connection.player.v();
            return false;
        }

        return true;
    }

    public static PlayerJoinEvent callPlayerJoinEvent(EntityPlayer entityplayer) {
        return callEvent(new PlayerJoinEvent(entityplayer.getBukkitEntity(), "\u00A7e" + entityplayer.getName() + " joined the game."));
    }

    public static PlayerKickEvent callPlayerKickEvent(EntityPlayer player, MinecraftServer minecraftServer, String s) {
        String leaveMessage = ChatColor.YELLOW + player.getName() + " left the game.";
        PlayerKickEvent event = new PlayerKickEvent(player.getBukkitEntity(), s, leaveMessage);

        if (minecraftServer.isRunning()) {
            callEvent(event);
        }

        return event;
    }

    public static EntityPlayer handlePlayerLoginEvent(MinecraftServer server, GameProfile profile, String hostname, SocketAddress address, LoginListener loginListener, PlayerLoginEvent.Result result, String kickMessage) {
        WorldServer worldServer = server.getWorldServer(0);
        EntityPlayer player = new EntityPlayer(server, worldServer, profile, new PlayerInteractManager(worldServer));
        PlayerLoginEvent event = new PlayerLoginEvent(player.getBukkitEntity(), hostname, ((InetSocketAddress) address).getAddress());
        if (result != PlayerLoginEvent.Result.ALLOWED) {
            event.disallow(result, kickMessage);
        }

        callEvent(event);

        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            loginListener.disconnect(event.getKickMessage());
            return null;
        }

        return player;
    }

    public static PlayerMoveEvent callPlayerMoveEvent(Player player, Location from, Location to) {
        return callEvent(new PlayerMoveEvent(player, from, to));
    }

    public static PlayerPickupItemEvent callPlayerPickupItemEvent(EntityHuman entityHuman, CraftEntity item, int remaining) {
        PlayerPickupItemEvent event =  new PlayerPickupItemEvent((Player) entityHuman.getBukkitEntity(), (CraftItem) item, remaining);
        // event.setCancelled(!entityhuman.canPickUpLoot); TODO
        return callEvent(event);
    }

    public static String handlePlayerQuitEvent(EntityPlayer entityplayer) {
        CraftPlayer player = entityplayer.getBukkitEntity();
        PlayerQuitEvent playerQuitEvent = callEvent(new PlayerQuitEvent(player, "\u00A7e" + entityplayer.getName() + " left the game."));
        String quitMessage = playerQuitEvent.getQuitMessage();

        player.disconnect(quitMessage);
        return quitMessage;
    }

    public static PlayerRespawnEvent callPlayerRespawnEvent(EntityPlayer entityplayer, Location location, boolean isBedSpawn) {
        return callEvent(new PlayerRespawnEvent(entityplayer.getBukkitEntity(), location, isBedSpawn));
    }

    public static PlayerShearEntityEvent callPlayerShearEntityEvent(EntityHuman entityhuman, Entity entity) {
        return callEvent(new PlayerShearEntityEvent((Player) entityhuman.getBukkitEntity(), entity.getBukkitEntity()));
    }

    public static PlayerTeleportEvent callPlayerTeleportEvent(Player player, Location to, PlayerTeleportEvent.TeleportCause cause) {
        return callEvent(new PlayerTeleportEvent(player, player.getLocation(), to, cause));
    }

    public static void handlePlayerToggleFlightEvent(EntityPlayer player, PacketPlayInAbilities packetplayinabilities) {
        if (player.abilities.canFly && player.abilities.isFlying != packetplayinabilities.isFlying()) {
            if (!callEvent(new PlayerToggleFlightEvent(player.getBukkitEntity(), packetplayinabilities.isFlying())).isCancelled()) {
                player.abilities.isFlying = packetplayinabilities.isFlying(); // Actually set the player's flying status
            } else {
                player.updateAbilities(); // Tell the player their ability was reverted
            }
        }
    }

    public static boolean handlePlayerToggleSneakOrSprintEvent(EntityPlayer player, int action) {
        switch (action) {
        case 1:
        case 2:
            return callEvent(new PlayerToggleSneakEvent(player.getBukkitEntity(), action == 1)).isCancelled();
        case 4:
        case 5:
            return callEvent(new PlayerToggleSprintEvent(player.getBukkitEntity(), action == 4)).isCancelled();
        default:
            return false;
        }
    }

    public static boolean handlePlayerVelocityEvent(Entity entity) {
        boolean cancelled = false;

        if (entity instanceof EntityPlayer) {
            Player player = (Player) entity.getBukkitEntity();
            Vector velocity = player.getVelocity();
            PlayerVelocityEvent event = callEvent(new PlayerVelocityEvent(player, velocity));
            Vector eventVelocity = event.getVelocity();
            if (event.isCancelled()) {
                cancelled = true;
            } else if (!velocity.equals(eventVelocity)) {
                player.setVelocity(eventVelocity);
            }
        }

        return cancelled;
    }

    public static MapInitializeEvent callMapInitializeEvent(CraftMapView mapView) {
        return callEvent(new MapInitializeEvent(mapView));
    }

    public static String handleRemoteServerCommandEvent(final MinecraftServer server, final String s) {
        Waitable<String> waitable = new Waitable<String>() {
            @Override
            protected String evaluate() {
                RemoteControlCommandListener.instance.e();
                RemoteServerCommandEvent event = callEvent(new RemoteServerCommandEvent(server.remoteConsole, s));
                ServerCommand servercommand = new ServerCommand(event.getCommand(), RemoteControlCommandListener.instance);
                server.server.dispatchServerCommand(server.remoteConsole, servercommand);
                return RemoteControlCommandListener.instance.f();
            }};
        server.processQueue.add(waitable);
        try {
            return waitable.get();
        } catch (java.util.concurrent.ExecutionException e) {
            throw new RuntimeException("Exception processing rcon command " + s, e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Maintain interrupted state
            throw new RuntimeException("Interrupted processing rcon command " + s, e);
        }
    }

    public static ServerCommand handleServerCommandEvent(ConsoleCommandSender console, ServerCommand servercommand) {
        return new ServerCommand(callEvent(new ServerCommandEvent(console, servercommand.command)).getCommand(), servercommand.source);
    }

    public static void handleVehicleBlockCollisionEvent(Entity entity, double d1, double d2, double d3, double d4) {
        Vehicle vehicle = (Vehicle) entity.getBukkitEntity();
        Location loc = vehicle.getLocation();
        loc.setY(loc.getY() - entity.ac()); // account for entity height
        Block block = loc.getBlock();

        if (d2 > d1) {
            block = block.getRelative(BlockFace.EAST);
        } else if (d2 < d1) {
            block = block.getRelative(BlockFace.WEST);
        } else if (d4 > d3) {
            block = block.getRelative(BlockFace.SOUTH);
        } else if (d4 < d3) {
            block = block.getRelative(BlockFace.NORTH);
        }

        callEvent(new VehicleBlockCollisionEvent(vehicle, block));
    }

    public static void callVehicleCreateEvent(Entity entity) {
        callEvent(new VehicleCreateEvent((Vehicle) entity.getBukkitEntity()));
    }

    public static VehicleDamageEvent callVehicleDamageEvent(Entity vehicle, Entity attacker, float damage) {
        return callEvent(new VehicleDamageEvent((Vehicle) vehicle.getBukkitEntity(), attacker == null ? null : attacker.getBukkitEntity(), (double) damage));
    }

    public static VehicleDestroyEvent callVehicleDestroyEvent(Entity vehicle, Entity attacker) {
        return callEvent(new VehicleDestroyEvent((Vehicle) vehicle.getBukkitEntity(), attacker == null ? null : attacker.getBukkitEntity()));
    }

    public static VehicleEnterEvent callVehicleEnterEvent(Entity vehicle, Entity passenger) {
        return callEvent(new VehicleEnterEvent((Vehicle) vehicle.getBukkitEntity(), passenger.getBukkitEntity()));
    }

    public static VehicleExitEvent callVehicleExitEvent(Entity vehicle, Entity passenger) {
        return callEvent(new VehicleExitEvent((Vehicle) vehicle.getBukkitEntity(), (LivingEntity) passenger.getBukkitEntity()));
    }

    public static void handleVehicleMoveAndUpdateEvent(Entity entity, double prevX, double prevY, double prevZ, float prevYaw, float prevPitch) {
        Vehicle vehicle = (Vehicle) entity.getBukkitEntity();
        Location from = new Location(vehicle.getWorld(), prevX, prevY, prevZ, prevYaw, prevPitch);
        Location to = vehicle.getLocation();

        callEvent(new VehicleUpdateEvent(vehicle));

        if (!from.equals(to)) {
            callEvent(new VehicleMoveEvent(vehicle, from, to));
        }
    }

    public static VehicleEntityCollisionEvent callVehicleEntityCollisionEvent(Entity vehicle, Entity hit) {
        return callEvent(new VehicleEntityCollisionEvent((Vehicle) vehicle.getBukkitEntity(), hit.getBukkitEntity()));
    }

    public static LightningStrikeEvent callLightningStrikeEvent(Entity lightning) {
        return callEvent(new LightningStrikeEvent(lightning.world.getWorld(), (LightningStrike) lightning.getBukkitEntity()));
    }

    public static void callChunkLoadEvent(Chunk chunk, boolean newChunk) {
        callEvent(new ChunkLoadEvent(chunk.bukkitChunk, newChunk));
    }

    public static void callChunkPopulateEvent(Chunk chunk) {
        callEvent(new ChunkPopulateEvent(chunk.bukkitChunk));
    }

    public static ChunkUnloadEvent callChunkUnloadEvent(Chunk chunk) {
        return callEvent(new ChunkUnloadEvent(chunk.bukkitChunk));
    }

    public static PortalCreateEvent callPortalCreateEvent(Collection<Block> blocks, org.bukkit.World world, PortalCreateEvent.CreateReason createReason) {
        return callEvent(new PortalCreateEvent(blocks, world, createReason));
    }

    public static void callSpawnChangeEvent(CraftWorld world, Location previousLocation) {
        callEvent(new SpawnChangeEvent(world, previousLocation));
    }

    public static PlayerGameModeChangeEvent callPlayerGameModeChangeEvent(CraftPlayer player, GameMode mode) {
        return callEvent(new PlayerGameModeChangeEvent(player, mode));
    }

    public static void callPlayerChannelEvent(CraftPlayer player, String channel, boolean addChannel) {
        if (addChannel) {
            callEvent(new PlayerRegisterChannelEvent(player, channel));
        } else {
            callEvent(new PlayerUnregisterChannelEvent(player, channel));
        }
    }

    public static boolean handleStructureGrowEvent(World world, int x, int y, int z, TreeType type, boolean bonemeal, EntityHuman player, List<BlockState> blocks) {
        StructureGrowEvent event = callEvent(new StructureGrowEvent(new Location(world.getWorld(), x, y, z), type, bonemeal, (Player) player.getBukkitEntity(), blocks));
        boolean cancelled = event.isCancelled();

        if (!cancelled) {
            for (BlockState state : event.getBlocks()) {
                state.update(true);
            }
        }

        return !cancelled;
    }

    public static PlayerChatTabCompleteEvent handlePlayerChatTabCompleteEvent(Player player, String message) {
        List<String> completions = new ArrayList<String>();
        PlayerChatTabCompleteEvent event = new PlayerChatTabCompleteEvent(player, message, completions);
        String token = event.getLastToken();

        for (Player p : player.getServer().getOnlinePlayers()) {
            String name = p.getName();
            if (player.canSee(p) && StringUtil.startsWithIgnoreCase(name, token)) {
                completions.add(name);
            }
        }

        return callEvent(event);
    }

    public static void callWorldInitEvent(World world) {
        callEvent(new WorldInitEvent(world.getWorld()));
    }

    public static void callWorldLoadEvent(World world) {
        callEvent(new WorldLoadEvent(world.getWorld()));
    }

    public static void callWorldSaveEvent(World world) {
        callEvent(new WorldLoadEvent(world.getWorld()));
    }

    public static WorldUnloadEvent callWorldUnloadEvent(World world) {
        return callEvent(new WorldUnloadEvent(world.getWorld()));
    }
}
