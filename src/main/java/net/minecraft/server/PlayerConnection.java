package net.minecraft.server;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.Callable;

import net.minecraft.util.com.google.common.base.Charsets;
import net.minecraft.util.com.google.common.collect.Lists;
import net.minecraft.util.io.netty.buffer.Unpooled;
import net.minecraft.util.io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// CraftBukkit start
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.craftbukkit.util.Waitable;

import org.bukkit.Location;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.NumberConversions;
// CraftBukkit end

public class PlayerConnection implements PacketPlayInListener {

    private static final Logger c = LogManager.getLogger();
    public final NetworkManager networkManager;
    private final MinecraftServer minecraftServer;
    public EntityPlayer player;
    private int e;
    private int f;
    private boolean g;
    private int h;
    private long i;
    private static Random j = new Random();
    private long k;
    private volatile int chatThrottle; private static final AtomicIntegerFieldUpdater chatSpamField = AtomicIntegerFieldUpdater.newUpdater(PlayerConnection.class, "chatThrottle"); // CraftBukkit - multithreaded field
    private int x;
    private IntHashMap n = new IntHashMap();
    private double y;
    private double z;
    private double q;
    public boolean checkMovement = true; // CraftBukkit - private -> public
    private boolean processedDisconnect; // CraftBukkit - added

    public PlayerConnection(MinecraftServer minecraftserver, NetworkManager networkmanager, EntityPlayer entityplayer) {
        this.minecraftServer = minecraftserver;
        this.networkManager = networkmanager;
        networkmanager.a((PacketListener) this);
        this.player = entityplayer;
        entityplayer.playerConnection = this;

        // CraftBukkit start - add fields and methods
        this.server = minecraftserver.server;
    }

    private final org.bukkit.craftbukkit.CraftServer server;
    private int lastTick = MinecraftServer.currentTick;
    private int lastDropTick = MinecraftServer.currentTick;
    private int dropCount = 0;
    private static final int SURVIVAL_PLACE_DISTANCE_SQUARED = 6 * 6;
    private static final int CREATIVE_PLACE_DISTANCE_SQUARED = 7 * 7;

    // Get position of last block hit for BlockDamageLevel.STOPPED
    private double lastPosX = Double.MAX_VALUE;
    private double lastPosY = Double.MAX_VALUE;
    private double lastPosZ = Double.MAX_VALUE;
    private float lastPitch = Float.MAX_VALUE;
    private float lastYaw = Float.MAX_VALUE;
    private boolean justTeleported = false;

    // For the PacketPlayOutBlockPlace hack :(
    Long lastPacket;

    // Store the last block right clicked and what type it was
    private Item lastMaterial;

    public CraftPlayer getPlayer() {
        return (this.player == null) ? null : this.player.getBukkitEntity();
    }

    private final static java.util.Set<Integer> invalidItems = new java.util.HashSet<Integer>(java.util.Arrays.asList(8, 9, 10, 11, 26, 34, 36, 43, 51, 52, 55, 59, 60, 62, 63, 64, 68, 71, 74, 75, 83, 90, 92, 93, 94, 104, 105, 115, 117, 118, 119, 125, 127, 132, 137, 140, 141, 142, 144)); // TODO: Check after every update.
    // CraftBukkit end

    public void a() {
        this.g = false;
        ++this.e;
        this.minecraftServer.methodProfiler.a("keepAlive");
        if ((long) this.e - this.k > 40L) {
            this.k = (long) this.e;
            this.i = this.d();
            this.h = (int) this.i;
            this.sendPacket(new PacketPlayOutKeepAlive(this.h));
        }

        // CraftBukkit start
        for (int spam; (spam = this.chatThrottle) > 0 && !chatSpamField.compareAndSet(this, spam, spam - 1); ) ;
        /* Use thread-safe field access instead
        if (this.chatThrottle > 0) {
            --this.chatThrottle;
        }
        */
        // CraftBukkit end

        if (this.x > 0) {
            --this.x;
        }

        this.minecraftServer.methodProfiler.c("playerTick");
        this.minecraftServer.methodProfiler.b();
    }

    public NetworkManager b() {
        return this.networkManager;
    }

    public void disconnect(String s) {
        // CraftBukkit start - fire PlayerKickEvent
        org.bukkit.event.player.PlayerKickEvent event = CraftEventFactory.callPlayerKickEvent(this.player, this.minecraftServer, s);

        if (event.isCancelled()) {
            // Do not kick the player
            return;
        }
        // Send the possibly modified leave message
        s = event.getReason();
        // CraftBukkit end
        ChatComponentText chatcomponenttext = new ChatComponentText(s);

        this.networkManager.handle(new PacketPlayOutKickDisconnect(chatcomponenttext), new GenericFutureListener[] { new PlayerConnectionFuture(this, chatcomponenttext)});
        this.a(chatcomponenttext); // CraftBukkit - Process quit immediately
        this.networkManager.g();
    }

    public void a(PacketPlayInSteerVehicle packetplayinsteervehicle) {
        this.player.a(packetplayinsteervehicle.c(), packetplayinsteervehicle.d(), packetplayinsteervehicle.e(), packetplayinsteervehicle.f());
    }

    public void a(PacketPlayInFlying packetplayinflying) {
        // CraftBukkit start - Check for NaN
        if (Double.isNaN(packetplayinflying.x) || Double.isNaN(packetplayinflying.y) || Double.isNaN(packetplayinflying.z) || Double.isNaN(packetplayinflying.stance)) {
            c.warn(player.getName() + " was caught trying to crash the server with an invalid position.");
            this.player.getBukkitEntity().kickPlayer("Nope!");
            return;
        }
        // CraftBukkit end
        WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);

        this.g = true;
        if (!this.player.viewingCredits) {
            double d0;

            if (!this.checkMovement) {
                d0 = packetplayinflying.d() - this.z;
                if (packetplayinflying.c() == this.y && d0 * d0 < 0.01D && packetplayinflying.e() == this.q) {
                    this.checkMovement = true;
                }
            }

            // CraftBukkit start - fire PlayerMoveEvent
            CraftPlayer player = this.player.getBukkitEntity();
            Location from = new Location(player.getWorld(), this.lastPosX, this.lastPosY, this.lastPosZ, this.lastYaw, this.lastPitch); // Get the Players previous Event location.
            Location to = player.getLocation().clone(); // Start off the To location as the Players current location.

            // If the packet contains movement information then we update the To location with the correct XYZ.
            if (packetplayinflying.hasPos && !(packetplayinflying.hasPos && packetplayinflying.y == -999.0D && packetplayinflying.stance == -999.0D)) {
                to.setX(packetplayinflying.x);
                to.setY(packetplayinflying.y);
                to.setZ(packetplayinflying.z);
            }

            // If the packet contains look information then we update the To location with the correct Yaw & Pitch.
            if (packetplayinflying.hasLook) {
                to.setYaw(packetplayinflying.yaw);
                to.setPitch(packetplayinflying.pitch);
            }

            // Prevent 40 event-calls for less than a single pixel of movement >.>
            double delta = Math.pow(this.lastPosX - to.getX(), 2) + Math.pow(this.lastPosY - to.getY(), 2) + Math.pow(this.lastPosZ - to.getZ(), 2);
            float deltaAngle = Math.abs(this.lastYaw - to.getYaw()) + Math.abs(this.lastPitch - to.getPitch());

            if ((delta > 1f / 256 || deltaAngle > 10f) && (this.checkMovement && !this.player.dead)) {
                this.lastPosX = to.getX();
                this.lastPosY = to.getY();
                this.lastPosZ = to.getZ();
                this.lastYaw = to.getYaw();
                this.lastPitch = to.getPitch();

                // Skip the first time we do this
                if (from.getX() != Double.MAX_VALUE) {
                    org.bukkit.event.player.PlayerMoveEvent event = CraftEventFactory.callPlayerMoveEvent(player, from, to);

                    // If the event is cancelled we move the player back to their old location.
                    if (event.isCancelled()) {
                        this.player.playerConnection.sendPacket(new PacketPlayOutPosition(from.getX(), from.getY() + 1.6200000047683716D, from.getZ(), from.getYaw(), from.getPitch(), false));
                        return;
                    }

                    /* If a Plugin has changed the To destination then we teleport the Player
                    there to avoid any 'Moved wrongly' or 'Moved too quickly' errors.
                    We only do this if the Event was not cancelled. */
                    Location eventTo = event.getTo();
                    if (!to.equals(eventTo) && !event.isCancelled()) {
                        this.player.getBukkitEntity().teleport(eventTo, PlayerTeleportEvent.TeleportCause.UNKNOWN);
                        return;
                    }

                    /* Check to see if the Players Location has some how changed during the call of the event.
                    This can happen due to a plugin teleporting the player instead of using .setTo() */
                    if (!from.equals(this.player.getBukkitEntity().getLocation()) && this.justTeleported) {
                        this.justTeleported = false;
                        return;
                    }
                }
            }

            if (this.checkMovement && !this.player.dead) {
                // CraftBukkit end
                double d1;
                double d2;
                double d3;

                if (this.player.vehicle != null) {
                    float f = this.player.yaw;
                    float f1 = this.player.pitch;

                    this.player.vehicle.ab();
                    d1 = this.player.locX;
                    d2 = this.player.locY;
                    d3 = this.player.locZ;
                    if (packetplayinflying.k()) {
                        f = packetplayinflying.g();
                        f1 = packetplayinflying.h();
                    }

                    this.player.onGround = packetplayinflying.i();
                    this.player.i();
                    this.player.V = 0.0F;
                    this.player.setLocation(d1, d2, d3, f, f1);
                    if (this.player.vehicle != null) {
                        this.player.vehicle.ab();
                    }

                    this.minecraftServer.getPlayerList().d(this.player);
                    if (this.checkMovement) {
                        this.y = this.player.locX;
                        this.z = this.player.locY;
                        this.q = this.player.locZ;
                    }

                    worldserver.playerJoinedWorld(this.player);
                    return;
                }

                if (this.player.isSleeping()) {
                    this.player.i();
                    this.player.setLocation(this.y, this.z, this.q, this.player.yaw, this.player.pitch);
                    worldserver.playerJoinedWorld(this.player);
                    return;
                }

                d0 = this.player.locY;
                this.y = this.player.locX;
                this.z = this.player.locY;
                this.q = this.player.locZ;
                d1 = this.player.locX;
                d2 = this.player.locY;
                d3 = this.player.locZ;
                float f2 = this.player.yaw;
                float f3 = this.player.pitch;

                if (packetplayinflying.j() && packetplayinflying.d() == -999.0D && packetplayinflying.f() == -999.0D) {
                    packetplayinflying.a(false);
                }

                double d4;

                if (packetplayinflying.j()) {
                    d1 = packetplayinflying.c();
                    d2 = packetplayinflying.d();
                    d3 = packetplayinflying.e();
                    d4 = packetplayinflying.f() - packetplayinflying.d();
                    if (!this.player.isSleeping() && (d4 > 1.65D || d4 < 0.1D)) {
                        this.disconnect("Illegal stance");
                        c.warn(this.player.getName() + " had an illegal stance: " + d4);
                        return;
                    }

                    if (Math.abs(packetplayinflying.c()) > 3.2E7D || Math.abs(packetplayinflying.e()) > 3.2E7D) {
                        this.disconnect("Illegal position");
                        return;
                    }
                }

                if (packetplayinflying.k()) {
                    f2 = packetplayinflying.g();
                    f3 = packetplayinflying.h();
                }

                this.player.i();
                this.player.V = 0.0F;
                this.player.setLocation(this.y, this.z, this.q, f2, f3);
                if (!this.checkMovement) {
                    return;
                }

                d4 = d1 - this.player.locX;
                double d5 = d2 - this.player.locY;
                double d6 = d3 - this.player.locZ;
                // CraftBukkit start - min to max
                double d7 = Math.max(Math.abs(d4), Math.abs(this.player.motX));
                double d8 = Math.max(Math.abs(d5), Math.abs(this.player.motY));
                double d9 = Math.max(Math.abs(d6), Math.abs(this.player.motZ));
                // CraftBukkit end
                double d10 = d7 * d7 + d8 * d8 + d9 * d9;

                if (d10 > 100.0D && this.checkMovement && (!this.minecraftServer.N() || !this.minecraftServer.M().equals(this.player.getName()))) { // CraftBukkit - Added this.checkMovement condition to solve this check being triggered by teleports
                    c.warn(this.player.getName() + " moved too quickly! " + d4 + "," + d5 + "," + d6 + " (" + d7 + ", " + d8 + ", " + d9 + ")");
                    this.a(this.y, this.z, this.q, this.player.yaw, this.player.pitch);
                    return;
                }

                float f4 = 0.0625F;
                boolean flag = worldserver.getCubes(this.player, this.player.boundingBox.clone().shrink((double) f4, (double) f4, (double) f4)).isEmpty();

                if (this.player.onGround && !packetplayinflying.i() && d5 > 0.0D) {
                    this.player.bi();
                }

                this.player.move(d4, d5, d6);
                this.player.onGround = packetplayinflying.i();
                this.player.checkMovement(d4, d5, d6);
                double d11 = d5;

                d4 = d1 - this.player.locX;
                d5 = d2 - this.player.locY;
                if (d5 > -0.5D || d5 < 0.5D) {
                    d5 = 0.0D;
                }

                d6 = d3 - this.player.locZ;
                d10 = d4 * d4 + d5 * d5 + d6 * d6;
                boolean flag1 = false;

                if (d10 > 0.0625D && !this.player.isSleeping() && !this.player.playerInteractManager.isCreative()) {
                    flag1 = true;
                    c.warn(this.player.getName() + " moved wrongly!");
                }

                this.player.setLocation(d1, d2, d3, f2, f3);
                boolean flag2 = worldserver.getCubes(this.player, this.player.boundingBox.clone().shrink((double) f4, (double) f4, (double) f4)).isEmpty();

                if (flag && (flag1 || !flag2) && !this.player.isSleeping()) {
                    this.a(this.y, this.z, this.q, f2, f3);
                    return;
                }

                AxisAlignedBB axisalignedbb = this.player.boundingBox.clone().grow((double) f4, (double) f4, (double) f4).a(0.0D, -0.55D, 0.0D);

                if (!this.minecraftServer.getAllowFlight() && !this.player.abilities.canFly && !worldserver.c(axisalignedbb)) { // CraftBukkit - check abilities instead of creative mode
                    if (d11 >= -0.03125D) {
                        ++this.f;
                        if (this.f > 80) {
                            c.warn(this.player.getName() + " was kicked for floating too long!");
                            this.disconnect("Flying is not enabled on this server");
                            return;
                        }
                    }
                } else {
                    this.f = 0;
                }

                this.player.onGround = packetplayinflying.i();
                this.minecraftServer.getPlayerList().d(this.player);
                this.player.b(this.player.locY - d0, packetplayinflying.i());
            } else if (this.e % 20 == 0) {
                this.a(this.y, this.z, this.q, this.player.yaw, this.player.pitch);
            }
        }
    }

    public void a(double d0, double d1, double d2, float f, float f1) {
        // CraftBukkit start - Delegate to teleport(Location)
        CraftPlayer player = this.player.getBukkitEntity();
        PlayerTeleportEvent event = CraftEventFactory.callPlayerTeleportEvent(player, new org.bukkit.Location(player.getWorld(), d0, d1, d2, f, f1), PlayerTeleportEvent.TeleportCause.UNKNOWN);
        this.teleport(event.isCancelled() ? event.getFrom() : event.getTo());
    }

    public void teleport(Location dest) {
        double d0, d1, d2;
        float f, f1;

        d0 = dest.getX();
        d1 = dest.getY();
        d2 = dest.getZ();
        f = dest.getYaw();
        f1 = dest.getPitch();

        // TODO: make sure this is the best way to address this.
        if (Float.isNaN(f)) {
            f = 0;
        }

        if (Float.isNaN(f1)) {
            f1 = 0;
        }

        this.lastPosX = d0;
        this.lastPosY = d1;
        this.lastPosZ = d2;
        this.lastYaw = f;
        this.lastPitch = f1;
        this.justTeleported = true;
        // CraftBukkit end

        this.checkMovement = false;
        this.y = d0;
        this.z = d1;
        this.q = d2;
        this.player.setLocation(d0, d1, d2, f, f1);
        this.player.playerConnection.sendPacket(new PacketPlayOutPosition(d0, d1 + 1.6200000047683716D, d2, f, f1, false));
    }

    public void a(PacketPlayInBlockDig packetplayinblockdig) {
        if (this.player.dead) return; // CraftBukkit
        WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);

        this.player.v();
        if (packetplayinblockdig.g() == 4) {
            // CraftBukkit start - limit how quickly items can be dropped
            // If the ticks aren't the same then the count starts from 0 and we update the lastDropTick.
            if (this.lastDropTick != MinecraftServer.currentTick) {
                this.dropCount = 0;
                this.lastDropTick = MinecraftServer.currentTick;
            } else {
                // Else we increment the drop count and check the amount.
                this.dropCount++;
                if (this.dropCount >= 20) {
                    this.c.warn(this.player.getName() + " dropped their items too quickly!");
                    this.disconnect("You dropped your items too quickly (Hacking?)");
                    return;
                }
            }
            // CraftBukkit end
            this.player.a(false);
        } else if (packetplayinblockdig.g() == 3) {
            this.player.a(true);
        } else if (packetplayinblockdig.g() == 5) {
            this.player.bz();
        } else {
            boolean flag = false;

            if (packetplayinblockdig.g() == 0) {
                flag = true;
            }

            if (packetplayinblockdig.g() == 1) {
                flag = true;
            }

            if (packetplayinblockdig.g() == 2) {
                flag = true;
            }

            int i = packetplayinblockdig.c();
            int j = packetplayinblockdig.d();
            int k = packetplayinblockdig.e();

            if (flag) {
                double d0 = this.player.locX - ((double) i + 0.5D);
                double d1 = this.player.locY - ((double) j + 0.5D) + 1.5D;
                double d2 = this.player.locZ - ((double) k + 0.5D);
                double d3 = d0 * d0 + d1 * d1 + d2 * d2;

                if (d3 > 36.0D) {
                    return;
                }

                if (j >= this.minecraftServer.getMaxBuildHeight()) {
                    return;
                }
            }

            if (packetplayinblockdig.g() == 0) {
                if (!this.minecraftServer.a(worldserver, i, j, k, this.player)) {
                    this.player.playerInteractManager.dig(i, j, k, packetplayinblockdig.f());
                } else {
                    // CraftBukkit start - fire PlayerInteractEvent
                    CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_BLOCK, i, j, k, packetplayinblockdig.f(), this.player.inventory.getItemInHand());
                    this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(i, j, k, worldserver));
                    // Update any tile entity data for this block
                    TileEntity tileentity = worldserver.getTileEntity(i, j, k);
                    if (tileentity != null) {
                        this.player.playerConnection.sendPacket(tileentity.getUpdatePacket());
                    }
                    // CraftBukkit end
                }
            } else if (packetplayinblockdig.g() == 2) {
                this.player.playerInteractManager.a(i, j, k);
                if (worldserver.getType(i, j, k).getMaterial() != Material.AIR) {
                    this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(i, j, k, worldserver));
                }
            } else if (packetplayinblockdig.g() == 1) {
                this.player.playerInteractManager.c(i, j, k);
                if (worldserver.getType(i, j, k).getMaterial() != Material.AIR) {
                    this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(i, j, k, worldserver));
                }
            }
        }
    }

    public void a(PacketPlayInBlockPlace packetplayinblockplace) {
        WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);

        // CraftBukkit start
        if (this.player.dead) return;

        // This is a horrible hack needed because the client sends 2 packets on 'right mouse click'
        // aimed at a block. We shouldn't need to get the second packet if the data is handled
        // but we cannot know what the client will do, so we might still get it
        //
        // If the time between packets is small enough, and the 'signature' similar, we discard the
        // second one. This sadly has to remain until Mojang makes their packets saner. :(
        //  -- Grum
        if (packetplayinblockplace.getFace() == 255) {
            if (packetplayinblockplace.getItemStack() != null && packetplayinblockplace.getItemStack().getItem() == this.lastMaterial && this.lastPacket != null && packetplayinblockplace.timestamp - this.lastPacket < 100) {
                this.lastPacket = null;
                return;
            }
        } else {
            this.lastMaterial = packetplayinblockplace.getItemStack() == null ? null : packetplayinblockplace.getItemStack().getItem();
            this.lastPacket = packetplayinblockplace.timestamp;
        }
        // CraftBukkit - if rightclick decremented the item, always send the update packet. */
        // this is not here for CraftBukkit's own functionality; rather it is to fix
        // a notch bug where the item doesn't update correctly.
        boolean always = false;
        // CraftBukkit end

        ItemStack itemstack = this.player.inventory.getItemInHand();
        boolean flag = false;
        int i = packetplayinblockplace.c();
        int j = packetplayinblockplace.d();
        int k = packetplayinblockplace.e();
        int l = packetplayinblockplace.getFace();

        this.player.v();
        if (packetplayinblockplace.getFace() == 255) {
            if (itemstack == null) {
                return;
            }

            // CraftBukkit start
            int itemstackAmount = itemstack.count;
            if (CraftEventFactory.callPlayerInteractEvent(this.player, Action.RIGHT_CLICK_AIR, itemstack).useItemInHand() != org.bukkit.event.Event.Result.DENY) {
                this.player.playerInteractManager.useItem(this.player, this.player.world, itemstack);
            }

            // CraftBukkit - notch decrements the counter by 1 in the above method with food,
            // snowballs and so forth, but he does it in a place that doesn't cause the
            // inventory update packet to get sent
            always = (itemstack.count != itemstackAmount);
            // CraftBukkit end
        } else if (packetplayinblockplace.d() >= this.minecraftServer.getMaxBuildHeight() - 1 && (packetplayinblockplace.getFace() == 1 || packetplayinblockplace.d() >= this.minecraftServer.getMaxBuildHeight())) {
            ChatMessage chatmessage = new ChatMessage("build.tooHigh", new Object[] { Integer.valueOf(this.minecraftServer.getMaxBuildHeight())});

            chatmessage.getChatModifier().setColor(EnumChatFormat.RED);
            this.player.playerConnection.sendPacket(new PacketPlayOutChat(chatmessage));
            flag = true;
        } else {
            // CraftBukkit start - Check if we can actually do something over this large a distance
            Location eyeLoc = this.player.getBukkitEntity().getEyeLocation();
            double reachDistance = NumberConversions.square(eyeLoc.getX() - i) + NumberConversions.square(eyeLoc.getY() - j) + NumberConversions.square(eyeLoc.getZ() - k);
            if (reachDistance > (this.player.getBukkitEntity().getGameMode() == org.bukkit.GameMode.CREATIVE ? CREATIVE_PLACE_DISTANCE_SQUARED : SURVIVAL_PLACE_DISTANCE_SQUARED)) {
                return;
            }

            this.player.playerInteractManager.interact(this.player, worldserver, itemstack, i, j, k, l, packetplayinblockplace.h(), packetplayinblockplace.i(), packetplayinblockplace.j());
            // CraftBukkit end

            flag = true;
        }

        if (flag) {
            this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(i, j, k, worldserver));
            if (l == 0) {
                --j;
            }

            if (l == 1) {
                ++j;
            }

            if (l == 2) {
                --k;
            }

            if (l == 3) {
                ++k;
            }

            if (l == 4) {
                --i;
            }

            if (l == 5) {
                ++i;
            }

            this.player.playerConnection.sendPacket(new PacketPlayOutBlockChange(i, j, k, worldserver));
        }

        itemstack = this.player.inventory.getItemInHand();
        if (itemstack != null && itemstack.count == 0) {
            this.player.inventory.items[this.player.inventory.itemInHandIndex] = null;
            itemstack = null;
        }

        if (itemstack == null || itemstack.n() == 0) {
            this.player.g = true;
            this.player.inventory.items[this.player.inventory.itemInHandIndex] = ItemStack.b(this.player.inventory.items[this.player.inventory.itemInHandIndex]);
            Slot slot = this.player.activeContainer.a((IInventory) this.player.inventory, this.player.inventory.itemInHandIndex);

            this.player.activeContainer.b();
            this.player.g = false;
            // CraftBukkit - TODO CHECK IF NEEDED -- new if structure might not need 'always'. Kept it in for now, but may be able to remove in future
            if (!ItemStack.matches(this.player.inventory.getItemInHand(), packetplayinblockplace.getItemStack()) || always) {
                this.sendPacket(new PacketPlayOutSetSlot(this.player.activeContainer.windowId, slot.rawSlotIndex, this.player.inventory.getItemInHand()));
            }
        }
    }

    public void a(IChatBaseComponent ichatbasecomponent) {
        // CraftBukkit start - Rarely it would send a disconnect line twice
        if (this.processedDisconnect) {
            return;
        } else {
            this.processedDisconnect = true;
        }
        // CraftBukkit end
        c.info(this.player.getName() + " lost connection: " + ichatbasecomponent.c()); // CraftBukkit - Don't toString the component
        this.minecraftServer.az();
        // CraftBukkit start - Replace vanilla quit message handling with our own.
        /*
        ChatMessage chatmessage = new ChatMessage("multiplayer.player.left", new Object[] { this.player.getScoreboardDisplayName()});

        chatmessage.getChatModifier().setColor(EnumChatFormat.YELLOW);
        this.minecraftServer.getPlayerList().sendMessage(chatmessage);
        */

        this.player.n();
        String quitMessage = this.minecraftServer.getPlayerList().disconnect(this.player);
        if ((quitMessage != null) && (quitMessage.length() > 0)) {
            this.minecraftServer.getPlayerList().sendMessage(org.bukkit.craftbukkit.util.CraftChatMessage.fromString(quitMessage));
        }
        // CraftBukkit end
        if (this.minecraftServer.N() && this.player.getName().equals(this.minecraftServer.M())) {
            c.info("Stopping singleplayer server as player logged out");
            this.minecraftServer.safeShutdown();
        }
    }

    public void sendPacket(Packet packet) {
        if (packet instanceof PacketPlayOutChat) {
            PacketPlayOutChat packetplayoutchat = (PacketPlayOutChat) packet;
            EnumChatVisibility enumchatvisibility = this.player.getChatFlags();

            if (enumchatvisibility == EnumChatVisibility.HIDDEN) {
                return;
            }

            if (enumchatvisibility == EnumChatVisibility.SYSTEM && !packetplayoutchat.d()) {
                return;
            }
        }

        // CraftBukkit start
        if (packet == null) {
            return;
        } else if (packet instanceof PacketPlayOutSpawnPosition) {
            PacketPlayOutSpawnPosition packet6 = (PacketPlayOutSpawnPosition) packet;
            this.player.compassTarget = new Location(this.player.getBukkitEntity().getWorld(), packet6.x, packet6.y, packet6.z);
        }
        // CraftBukkit end

        try {
            this.networkManager.handle(packet, new GenericFutureListener[0]);
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.a(throwable, "Sending packet");
            CrashReportSystemDetails crashreportsystemdetails = crashreport.a("Packet being sent");

            crashreportsystemdetails.a("Packet class", (Callable) (new CrashReportConnectionPacketClass(this, packet)));
            throw new ReportedException(crashreport);
        }
    }

    public void a(PacketPlayInHeldItemSlot packetplayinhelditemslot) {
        // CraftBukkit start
        if (this.player.dead) {
            return;
        }
        // CraftBukkit end
        if (packetplayinhelditemslot.c() >= 0 && packetplayinhelditemslot.c() < PlayerInventory.getHotbarSize()) {
            // CraftBukkit start
            if (!CraftEventFactory.handlePlayerItemHeldEvent(this, packetplayinhelditemslot)) {
                return;
            }
            // CraftBukkit end

            this.player.inventory.itemInHandIndex = packetplayinhelditemslot.c();
            this.player.v();
        } else {
            c.warn(this.player.getName() + " tried to set an invalid carried item");
            this.disconnect("Nope!"); // CraftBukkit
        }
    }

    public void a(PacketPlayInChat packetplayinchat) {
        if (this.player.dead || this.player.getChatFlags() == EnumChatVisibility.HIDDEN) { // CraftBukkit - dead men tell no tales
            ChatMessage chatmessage = new ChatMessage("chat.cannotSend", new Object[0]);

            chatmessage.getChatModifier().setColor(EnumChatFormat.RED);
            this.sendPacket(new PacketPlayOutChat(chatmessage));
        } else {
            this.player.v();
            String s = packetplayinchat.c();

            s = StringUtils.normalizeSpace(s);

            for (int i = 0; i < s.length(); ++i) {
                if (!SharedConstants.isAllowedChatCharacter(s.charAt(i))) {
                    // CraftBukkit start - threadsafety
                    if (packetplayinchat.a()) {
                        Waitable waitable = new Waitable() {
                            @Override
                            protected Object evaluate() {
                                PlayerConnection.this.disconnect("Illegal characters in chat");
                                return null;
                            }
                        };

                        this.minecraftServer.processQueue.add(waitable);

                        try {
                            waitable.get();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } catch (ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    } else {
                        this.disconnect("Illegal characters in chat");
                    }
                    // CraftBukkit end
                    return;
                }
            }

            if (s.startsWith("/")) {
                // CraftBukkit start
                try {
                    this.minecraftServer.server.playerCommandState = true;
                    this.handleCommand(s);
                } finally {
                    this.minecraftServer.server.playerCommandState = false;
                }
            } else if (s.isEmpty()) {
                c.warn(this.player.getName() + " tried to send an empty message");
            } else if (this.player.getBukkitEntity().isConversing()) {
                this.player.getBukkitEntity().acceptConversationInput(s);
            } else if (this.player.getChatFlags() == EnumChatVisibility.SYSTEM) { // Re-add "Command Only" flag check
                ChatMessage chatmessage = new ChatMessage("chat.cannotSend", new Object[0]);

                chatmessage.getChatModifier().setColor(EnumChatFormat.RED);
                this.sendPacket(new PacketPlayOutChat(chatmessage));
            } else if (true) {
                this.chat(s, packetplayinchat.a());
                // CraftBukkit end - the below is for reference. :)
            } else {
                ChatMessage chatmessage1 = new ChatMessage("chat.type.text", new Object[] { this.player.getScoreboardDisplayName(), s});

                this.minecraftServer.getPlayerList().sendMessage(chatmessage1, false);
            }

            // CraftBukkit start - replaced with thread safe throttle
            // this.chatThrottle += 20;
            if (chatSpamField.addAndGet(this, 20) > 200 && !this.minecraftServer.getPlayerList().isOp(this.player.getProfile())) {
                if (packetplayinchat.a()) {
                    Waitable waitable = new Waitable() {
                        @Override
                        protected Object evaluate() {
                            PlayerConnection.this.disconnect("disconnect.spam");
                            return null;
                        }
                    };

                    this.minecraftServer.processQueue.add(waitable);

                    try {
                        waitable.get();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    this.disconnect("disconnect.spam");
                }
                // CraftBukkit end
            }
        }
    }

    // CraftBukkit start - add method
    public void chat(String s, boolean async) {
        if (s.isEmpty() || this.player.getChatFlags() == EnumChatVisibility.HIDDEN) {
            return;
        }

        if (s.startsWith("/")) {
            this.handleCommand(s);
        } else if (this.player.getChatFlags() == EnumChatVisibility.SYSTEM) {
            // Do nothing, this is coming from a plugin
        } else {
            CraftEventFactory.handlePlayerChatEvent(async, this.player.getBukkitEntity(), s, minecraftServer);
        }
    }
    // CraftBukkit end

    private void handleCommand(String s) {
        // CraftBukkit start
        /* this.minecraftServer.getCommandHandler().a(this.player, s); */
        CraftEventFactory.handlePlayerCommandPreprocessEvent(s, this.player.getBukkitEntity(), this.c, this.server);
        // CraftBukkit end
    }

    public void a(PacketPlayInArmAnimation packetplayinarmanimation) {
        // CraftBukkit start
        if (this.player.dead) {
            return;
        }
        // CraftBukkit end
        this.player.v();
        if (packetplayinarmanimation.d() == 1) {
            // CraftBukkit start - Raytrace to look for 'rogue armswings'
            float f = 1.0F;
            float f1 = this.player.lastPitch + (this.player.pitch - this.player.lastPitch) * f;
            float f2 = this.player.lastYaw + (this.player.yaw - this.player.lastYaw) * f;
            double d0 = this.player.lastX + (this.player.locX - this.player.lastX) * (double) f;
            double d1 = this.player.lastY + (this.player.locY - this.player.lastY) * (double) f + 1.62D - (double) this.player.height;
            double d2 = this.player.lastZ + (this.player.locZ - this.player.lastZ) * (double) f;
            Vec3D vec3d = Vec3D.a(d0, d1, d2);

            float f3 = MathHelper.cos(-f2 * 0.017453292F - 3.1415927F);
            float f4 = MathHelper.sin(-f2 * 0.017453292F - 3.1415927F);
            float f5 = -MathHelper.cos(-f1 * 0.017453292F);
            float f6 = MathHelper.sin(-f1 * 0.017453292F);
            float f7 = f4 * f5;
            float f8 = f3 * f5;
            double d3 = 5.0D;
            Vec3D vec3d1 = vec3d.add((double) f7 * d3, (double) f6 * d3, (double) f8 * d3);
            MovingObjectPosition movingobjectposition = this.player.world.rayTrace(vec3d, vec3d1, false);

            if (movingobjectposition == null || movingobjectposition.type != EnumMovingObjectType.BLOCK) {
                CraftEventFactory.callPlayerInteractEvent(this.player, Action.LEFT_CLICK_AIR, this.player.inventory.getItemInHand());
            }

            // Arm swing animation
            if (CraftEventFactory.callPlayerAnimationEvent(this.player.getBukkitEntity()).isCancelled()) {
                return;
            }
            // CraftBukkit end

            this.player.aZ();
        }
    }

    public void a(PacketPlayInEntityAction packetplayinentityaction) {
        // CraftBukkit start
        if (this.player.dead) {
            return;
        }
        // CraftBukkit end
        this.player.v();
        // CraftBukkit start
        if (CraftEventFactory.handlePlayerToggleSneakOrSprintEvent(this.player, packetplayinentityaction.d())) {
            return;
        }
        // CraftBukkit end
        if (packetplayinentityaction.d() == 1) {
            this.player.setSneaking(true);
        } else if (packetplayinentityaction.d() == 2) {
            this.player.setSneaking(false);
        } else if (packetplayinentityaction.d() == 4) {
            this.player.setSprinting(true);
        } else if (packetplayinentityaction.d() == 5) {
            this.player.setSprinting(false);
        } else if (packetplayinentityaction.d() == 3) {
            this.player.a(false, true, true);
            //this.checkMovement = false; // CraftBukkit - this is handled in teleport
        } else if (packetplayinentityaction.d() == 6) {
            if (this.player.vehicle != null && this.player.vehicle instanceof EntityHorse) {
                ((EntityHorse) this.player.vehicle).w(packetplayinentityaction.e());
            }
        } else if (packetplayinentityaction.d() == 7 && this.player.vehicle != null && this.player.vehicle instanceof EntityHorse) {
            ((EntityHorse) this.player.vehicle).g(this.player);
        }
    }

    public void a(PacketPlayInUseEntity packetplayinuseentity) {
        // CraftBukkit start
        if (this.player.dead) {
            return;
        }
        // CraftBukkit end
        WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);
        Entity entity = packetplayinuseentity.a((World) worldserver);

        this.player.v();
        if (entity != null) {
            boolean flag = this.player.p(entity);
            double d0 = 36.0D;

            if (!flag) {
                d0 = 9.0D;
            }

            if (this.player.f(entity) < d0) {
                if (packetplayinuseentity.c() == EnumEntityUseAction.INTERACT) {
                    // CraftBukkit start
                    if (CraftEventFactory.handlePlayerInteractEntityEvent(this, entity)) {
                        return;
                    }
                    // CraftBukkit end

                    this.player.q(entity);

                    // CraftBukkit start
                    ItemStack itemstack = this.player.inventory.getItemInHand();
                    if (itemstack != null && itemstack.count <= -1) {
                        this.player.updateInventory(this.player.activeContainer);
                    }
                    // CraftBukkit end
                } else if (packetplayinuseentity.c() == EnumEntityUseAction.ATTACK) {
                    if (entity instanceof EntityItem || entity instanceof EntityExperienceOrb || entity instanceof EntityArrow || entity == this.player) {
                        this.disconnect("Attempting to attack an invalid entity");
                        this.minecraftServer.warning("Player " + this.player.getName() + " tried to attack an invalid entity");
                        return;
                    }

                    this.player.attack(entity);

                    // CraftBukkit start
                    ItemStack itemstack = this.player.inventory.getItemInHand();
                    if (itemstack != null && itemstack.count <= -1) {
                        this.player.updateInventory(this.player.activeContainer);
                    }
                    // CraftBukkit end
                }
            }
        }
    }

    public void a(PacketPlayInClientCommand packetplayinclientcommand) {
        this.player.v();
        EnumClientCommand enumclientcommand = packetplayinclientcommand.c();

        switch (ClientCommandOrdinalWrapper.a[enumclientcommand.ordinal()]) {
        case 1:
            if (this.player.viewingCredits) {
                // CraftBukkit start - reroute logic through custom portal management
                /* this.player = this.minecraftServer.getPlayerList().moveToWorld(this.player, 0, true); */
                CraftEventFactory.handlePlayerPortalEvent(this.minecraftServer, this.player, 0, PlayerTeleportEvent.TeleportCause.END_PORTAL);
                // CraftBukkit end
            } else if (this.player.r().getWorldData().isHardcore()) {
                if (this.minecraftServer.N() && this.player.getName().equals(this.minecraftServer.M())) {
                    this.player.playerConnection.disconnect("You have died. Game over, man, it\'s game over!");
                    this.minecraftServer.U();
                } else {
                    GameProfileBanEntry gameprofilebanentry = new GameProfileBanEntry(this.player.getProfile(), (Date) null, "(You just lost the game)", (Date) null, "Death in Hardcore");

                    this.minecraftServer.getPlayerList().getProfileBans().add(gameprofilebanentry);
                    this.player.playerConnection.disconnect("You have died. Game over, man, it\'s game over!");
                }
            } else {
                if (this.player.getHealth() > 0.0F) {
                    return;
                }

                this.player = this.minecraftServer.getPlayerList().moveToWorld(this.player, 0, false);
            }
            break;

        case 2:
            this.player.getStatisticManager().a(this.player);
            break;

        case 3:
            this.player.a((Statistic) AchievementList.f);
        }
    }

    public void a(PacketPlayInCloseWindow packetplayinclosewindow) {
        // CraftBukkit start
        if (this.player.dead) {
            return;
        }
        CraftEventFactory.handleInventoryCloseEvent(this.player);
        // CraftBukkit end
        this.player.m();
    }

    public void a(PacketPlayInWindowClick packetplayinwindowclick) {
        // CraftBukkit start
        if (this.player.dead) {
            return;
        }
        // CraftBukkit end

        this.player.v();
        if (this.player.activeContainer.windowId == packetplayinwindowclick.c() && this.player.activeContainer.c(this.player)) {
            // CraftBukkit start - Call InventoryClickEvent
            if (packetplayinwindowclick.d() < -1 && packetplayinwindowclick.d() != -999) {
                return;
            }

            org.bukkit.inventory.InventoryView inventory = this.player.activeContainer.getBukkitView();
            SlotType type = org.bukkit.craftbukkit.inventory.CraftInventoryView.getSlotType(inventory, packetplayinwindowclick.d());

            ClickType click = ClickType.UNKNOWN;
            InventoryAction action = InventoryAction.UNKNOWN;

            ItemStack itemstack = null;

            if (packetplayinwindowclick.d() == -1) {
                click = packetplayinwindowclick.e() == 0 ? ClickType.WINDOW_BORDER_LEFT : ClickType.WINDOW_BORDER_RIGHT;
                action = InventoryAction.NOTHING;
            } else if (packetplayinwindowclick.h() == 0) {
                if (packetplayinwindowclick.e() == 0) {
                    click = ClickType.LEFT;
                } else if (packetplayinwindowclick.e() == 1) {
                    click = ClickType.RIGHT;
                }
                if (packetplayinwindowclick.e() == 0 || packetplayinwindowclick.e() == 1) {
                    action = InventoryAction.NOTHING; // Don't want to repeat ourselves
                    if (packetplayinwindowclick.d() == -999) {
                        if (player.inventory.getCarried() != null) {
                            action = packetplayinwindowclick.e() == 0 ? InventoryAction.DROP_ALL_CURSOR : InventoryAction.DROP_ONE_CURSOR;
                        }
                    } else {
                        Slot slot = this.player.activeContainer.getSlot(packetplayinwindowclick.d());
                        if (slot != null) {
                            ItemStack clickedItem = slot.getItem();
                            ItemStack cursor = player.inventory.getCarried();
                            if (clickedItem == null) {
                                if (cursor != null) {
                                    action = packetplayinwindowclick.e() == 0 ? InventoryAction.PLACE_ALL : InventoryAction.PLACE_ONE;
                                }
                            } else if (slot.isAllowed(player)) {
                                if (cursor == null) {
                                    action = packetplayinwindowclick.e() == 0 ? InventoryAction.PICKUP_ALL : InventoryAction.PICKUP_HALF;
                                } else if (slot.isAllowed(cursor)) {
                                    if (clickedItem.doMaterialsMatch(cursor) && ItemStack.equals(clickedItem, cursor)) {
                                        int toPlace = packetplayinwindowclick.e() == 0 ? cursor.count : 1;
                                        toPlace = Math.min(toPlace, clickedItem.getMaxStackSize() - clickedItem.count);
                                        toPlace = Math.min(toPlace, slot.inventory.getMaxStackSize() - clickedItem.count);
                                        if (toPlace == 1) {
                                            action = InventoryAction.PLACE_ONE;
                                        } else if (toPlace == cursor.count) {
                                            action = InventoryAction.PLACE_ALL;
                                        } else if (toPlace < 0) {
                                            action = toPlace != -1 ? InventoryAction.PICKUP_SOME : InventoryAction.PICKUP_ONE; // this happens with oversized stacks
                                        } else if (toPlace != 0) {
                                            action = InventoryAction.PLACE_SOME;
                                        }
                                    } else if (cursor.count <= slot.getMaxStackSize()) {
                                        action = InventoryAction.SWAP_WITH_CURSOR;
                                    }
                                } else if (cursor.getItem() == clickedItem.getItem() && (!cursor.usesData() || cursor.getData() == clickedItem.getData()) && ItemStack.equals(cursor, clickedItem)) {
                                    if (clickedItem.count >= 0) {
                                        if (clickedItem.count + cursor.count <= cursor.getMaxStackSize()) {
                                            // As of 1.5, this is result slots only
                                            action = InventoryAction.PICKUP_ALL;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (packetplayinwindowclick.h() == 1) {
                if (packetplayinwindowclick.e() == 0) {
                    click = ClickType.SHIFT_LEFT;
                } else if (packetplayinwindowclick.e() == 1) {
                    click = ClickType.SHIFT_RIGHT;
                }
                if (packetplayinwindowclick.e() == 0 || packetplayinwindowclick.e() == 1) {
                    if (packetplayinwindowclick.d() < 0) {
                        action = InventoryAction.NOTHING;
                    } else {
                        Slot slot = this.player.activeContainer.getSlot(packetplayinwindowclick.d());
                        if (slot != null && slot.isAllowed(this.player) && slot.hasItem()) {
                            action = InventoryAction.MOVE_TO_OTHER_INVENTORY;
                        } else {
                            action = InventoryAction.NOTHING;
                        }
                    }
                }
            } else if (packetplayinwindowclick.h() == 2) {
                if (packetplayinwindowclick.e() >= 0 && packetplayinwindowclick.e() < 9) {
                    click = ClickType.NUMBER_KEY;
                    Slot clickedSlot = this.player.activeContainer.getSlot(packetplayinwindowclick.d());
                    if (clickedSlot.isAllowed(player)) {
                        ItemStack hotbar = this.player.inventory.getItem(packetplayinwindowclick.e());
                        boolean canCleanSwap = hotbar == null || (clickedSlot.inventory == player.inventory && clickedSlot.isAllowed(hotbar)); // the slot will accept the hotbar item
                        if (clickedSlot.hasItem()) {
                            if (canCleanSwap) {
                                action = InventoryAction.HOTBAR_SWAP;
                            } else {
                                int firstEmptySlot = player.inventory.getFirstEmptySlotIndex();
                                if (firstEmptySlot > -1) {
                                    action = InventoryAction.HOTBAR_MOVE_AND_READD;
                                } else {
                                    action = InventoryAction.NOTHING; // This is not sane! Mojang: You should test for other slots of same type
                                }
                            }
                        } else if (!clickedSlot.hasItem() && hotbar != null && clickedSlot.isAllowed(hotbar)) {
                            action = InventoryAction.HOTBAR_SWAP;
                        } else {
                            action = InventoryAction.NOTHING;
                        }
                    } else {
                        action = InventoryAction.NOTHING;
                    }
                }
            } else if (packetplayinwindowclick.h() == 3) {
                if (packetplayinwindowclick.e() == 2) {
                    click = ClickType.MIDDLE;
                    if (packetplayinwindowclick.d() == -999) {
                        action = InventoryAction.NOTHING;
                    } else {
                        Slot slot = this.player.activeContainer.getSlot(packetplayinwindowclick.d());
                        if (slot != null && slot.hasItem() && player.abilities.canInstantlyBuild && player.inventory.getCarried() == null) {
                            action = InventoryAction.CLONE_STACK;
                        } else {
                            action = InventoryAction.NOTHING;
                        }
                    }
                } else {
                    click = ClickType.UNKNOWN;
                    action = InventoryAction.UNKNOWN;
                }
            } else if (packetplayinwindowclick.h() == 4) {
                if (packetplayinwindowclick.d() >= 0) {
                    if (packetplayinwindowclick.e() == 0) {
                        click = ClickType.DROP;
                        Slot slot = this.player.activeContainer.getSlot(packetplayinwindowclick.d());
                        if (slot != null && slot.hasItem() && slot.isAllowed(player) && slot.getItem() != null && slot.getItem().getItem() != Item.getItemOf(Blocks.AIR)) {
                            action = InventoryAction.DROP_ONE_SLOT;
                        } else {
                            action = InventoryAction.NOTHING;
                        }
                    } else if (packetplayinwindowclick.e() == 1) {
                        click = ClickType.CONTROL_DROP;
                        Slot slot = this.player.activeContainer.getSlot(packetplayinwindowclick.d());
                        if (slot != null && slot.hasItem() && slot.isAllowed(player) && slot.getItem() != null && slot.getItem().getItem() != Item.getItemOf(Blocks.AIR)) {
                            action = InventoryAction.DROP_ALL_SLOT;
                        } else {
                            action = InventoryAction.NOTHING;
                        }
                    }
                } else {
                    // Sane default (because this happens when they are holding nothing. Don't ask why.)
                    click = ClickType.LEFT;
                    if (packetplayinwindowclick.e() == 1) {
                        click = ClickType.RIGHT;
                    }
                    action = InventoryAction.NOTHING;
                }
            } else if (packetplayinwindowclick.h() == 5) {
                itemstack = this.player.activeContainer.clickItem(packetplayinwindowclick.d(), packetplayinwindowclick.e(), packetplayinwindowclick.h(), this.player);
            } else if (packetplayinwindowclick.h() == 6) {
                click = ClickType.DOUBLE_CLICK;
                action = InventoryAction.NOTHING;
                if (packetplayinwindowclick.d() >= 0 && this.player.inventory.getCarried() != null) {
                    ItemStack cursor = this.player.inventory.getCarried();
                    action = InventoryAction.NOTHING;
                    // Quick check for if we have any of the item
                    org.bukkit.Material material = org.bukkit.craftbukkit.util.CraftMagicNumbers.getMaterial(cursor.getItem());
                    if (inventory.getTopInventory().contains(material) || inventory.getBottomInventory().contains(material)) {
                        action = InventoryAction.COLLECT_TO_CURSOR;
                    }
                }
            }
            // TODO check on updates

            if (packetplayinwindowclick.h() != 5) {
                org.bukkit.event.inventory.InventoryClickEvent event = CraftEventFactory.callInventoryClickEvent(inventory, type, click, action, packetplayinwindowclick);

                switch (event.getResult()) {
                    case ALLOW:
                    case DEFAULT:
                        itemstack = this.player.activeContainer.clickItem(packetplayinwindowclick.d(), packetplayinwindowclick.e(), packetplayinwindowclick.h(), this.player);
                        break;
                    case DENY:
                        /* Needs enum constructor in InventoryAction
                        if (action.modifiesOtherSlots()) {

                        } else {
                            if (action.modifiesCursor()) {
                                this.player.playerConnection.sendPacket(new Packet103SetSlot(-1, -1, this.player.inventory.getCarried()));
                            }
                            if (action.modifiesClicked()) {
                                this.player.playerConnection.sendPacket(new Packet103SetSlot(this.player.activeContainer.windowId, packet102windowclick.slot, this.player.activeContainer.getSlot(packet102windowclick.slot).getItem()));
                            }
                        }*/
                        switch (action) {
                            // Modified other slots
                            case PICKUP_ALL:
                            case MOVE_TO_OTHER_INVENTORY:
                            case HOTBAR_MOVE_AND_READD:
                            case HOTBAR_SWAP:
                            case COLLECT_TO_CURSOR:
                            case UNKNOWN:
                                this.player.updateInventory(this.player.activeContainer);
                                break;
                            // Modified cursor and clicked
                            case PICKUP_SOME:
                            case PICKUP_HALF:
                            case PICKUP_ONE:
                            case PLACE_ALL:
                            case PLACE_SOME:
                            case PLACE_ONE:
                            case SWAP_WITH_CURSOR:
                                this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, this.player.inventory.getCarried()));
                                this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(this.player.activeContainer.windowId, packetplayinwindowclick.d(), this.player.activeContainer.getSlot(packetplayinwindowclick.d()).getItem()));
                                break;
                            // Modified clicked only
                            case DROP_ALL_SLOT:
                            case DROP_ONE_SLOT:
                                this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(this.player.activeContainer.windowId, packetplayinwindowclick.d(), this.player.activeContainer.getSlot(packetplayinwindowclick.d()).getItem()));
                                break;
                            // Modified cursor only
                            case DROP_ALL_CURSOR:
                            case DROP_ONE_CURSOR:
                            case CLONE_STACK:
                                this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, this.player.inventory.getCarried()));
                                break;
                            // Nothing
                            case NOTHING:
                                break;
                        }
                        return;
                }
            }
            // CraftBukkit end

            if (ItemStack.matches(packetplayinwindowclick.g(), itemstack)) {
                this.player.playerConnection.sendPacket(new PacketPlayOutTransaction(packetplayinwindowclick.c(), packetplayinwindowclick.f(), true));
                this.player.g = true;
                this.player.activeContainer.b();
                this.player.broadcastCarriedItem();
                this.player.g = false;
            } else {
                this.n.a(this.player.activeContainer.windowId, Short.valueOf(packetplayinwindowclick.f()));
                this.player.playerConnection.sendPacket(new PacketPlayOutTransaction(packetplayinwindowclick.c(), packetplayinwindowclick.f(), false));
                this.player.activeContainer.a(this.player, false);
                ArrayList arraylist = new ArrayList();

                for (int i = 0; i < this.player.activeContainer.c.size(); ++i) {
                    arraylist.add(((Slot) this.player.activeContainer.c.get(i)).getItem());
                }

                this.player.a(this.player.activeContainer, arraylist);

                // CraftBukkit start - Send a Set Slot to update the crafting result slot
                if (type == SlotType.RESULT && itemstack != null) {
                    this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(this.player.activeContainer.windowId, 0, itemstack));
                }
                // CraftBukkit end
            }
        }
    }

    public void a(PacketPlayInEnchantItem packetplayinenchantitem) {
        this.player.v();
        if (this.player.activeContainer.windowId == packetplayinenchantitem.c() && this.player.activeContainer.c(this.player)) {
            this.player.activeContainer.a((EntityHuman) this.player, packetplayinenchantitem.d());
            this.player.activeContainer.b();
        }
    }

    public void a(PacketPlayInSetCreativeSlot packetplayinsetcreativeslot) {
        if (this.player.playerInteractManager.isCreative()) {
            boolean flag = packetplayinsetcreativeslot.c() < 0;
            ItemStack itemstack = packetplayinsetcreativeslot.getItemStack();
            boolean flag1 = packetplayinsetcreativeslot.c() >= 1 && packetplayinsetcreativeslot.c() < 36 + PlayerInventory.getHotbarSize();
            // CraftBukkit - Add invalidItems check
            boolean flag2 = itemstack == null || itemstack.getItem() != null && !invalidItems.contains(Item.b(itemstack.getItem()));
            boolean flag3 = itemstack == null || itemstack.getData() >= 0 && itemstack.count <= 64 && itemstack.count > 0;

            if (flag || (flag1 && !ItemStack.matches(this.player.defaultContainer.getSlot(packetplayinsetcreativeslot.c()).getItem(), packetplayinsetcreativeslot.getItemStack()))) { // Insist on valid slot
                org.bukkit.event.inventory.InventoryCreativeEvent event = CraftEventFactory.handleInventoryCreativeEvent(this.player, packetplayinsetcreativeslot);
                itemstack = org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(event.getCursor());

                switch (event.getResult()) {
                case ALLOW:
                    // Plugin cleared the id / stacksize checks
                    flag2 = flag3 = true;
                    break;
                case DEFAULT:
                    break;
                case DENY:
                    // Reset the slot
                    if (packetplayinsetcreativeslot.c() >= 0) {
                        this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(this.player.defaultContainer.windowId, packetplayinsetcreativeslot.c(), this.player.defaultContainer.getSlot(packetplayinsetcreativeslot.c()).getItem()));
                        this.player.playerConnection.sendPacket(new PacketPlayOutSetSlot(-1, -1, null));
                    }
                    return;
                }
            }
            // CraftBukkit end

            if (flag1 && flag2 && flag3) {
                if (itemstack == null) {
                    this.player.defaultContainer.setItem(packetplayinsetcreativeslot.c(), (ItemStack) null);
                } else {
                    this.player.defaultContainer.setItem(packetplayinsetcreativeslot.c(), itemstack);
                }

                this.player.defaultContainer.a(this.player, true);
            } else if (flag && flag2 && flag3 && this.x < 200) {
                this.x += 20;
                EntityItem entityitem = this.player.drop(itemstack, true);

                if (entityitem != null) {
                    entityitem.e();
                }
            }
        }
    }

    public void a(PacketPlayInTransaction packetplayintransaction) {
        if (this.player.dead) return; // CraftBukkit
        Short oshort = (Short) this.n.get(this.player.activeContainer.windowId);

        if (oshort != null && packetplayintransaction.d() == oshort.shortValue() && this.player.activeContainer.windowId == packetplayintransaction.c() && !this.player.activeContainer.c(this.player)) {
            this.player.activeContainer.a(this.player, true);
        }
    }

    public void a(PacketPlayInUpdateSign packetplayinupdatesign) {
        if (this.player.dead) return; // CraftBukkit

        this.player.v();
        WorldServer worldserver = this.minecraftServer.getWorldServer(this.player.dimension);

        if (worldserver.isLoaded(packetplayinupdatesign.c(), packetplayinupdatesign.d(), packetplayinupdatesign.e())) {
            TileEntity tileentity = worldserver.getTileEntity(packetplayinupdatesign.c(), packetplayinupdatesign.d(), packetplayinupdatesign.e());

            if (tileentity instanceof TileEntitySign) {
                TileEntitySign tileentitysign = (TileEntitySign) tileentity;

                if (!tileentitysign.a() || tileentitysign.b() != this.player) {
                    this.minecraftServer.warning("Player " + this.player.getName() + " just tried to change non-editable sign");
                    this.sendPacket(new PacketPlayOutUpdateSign(packetplayinupdatesign.c(), packetplayinupdatesign.d(), packetplayinupdatesign.e(), tileentitysign.lines)); // CraftBukkit
                    return;
                }
            }

            int i;
            int j;

            for (j = 0; j < 4; ++j) {
                boolean flag = true;

                if (packetplayinupdatesign.f()[j].length() > 15) {
                    flag = false;
                } else {
                    for (i = 0; i < packetplayinupdatesign.f()[j].length(); ++i) {
                        if (!SharedConstants.isAllowedChatCharacter(packetplayinupdatesign.f()[j].charAt(i))) {
                            flag = false;
                        }
                    }
                }

                if (!flag) {
                    packetplayinupdatesign.f()[j] = "!?";
                }
            }

            if (tileentity instanceof TileEntitySign) {
                j = packetplayinupdatesign.c();
                int k = packetplayinupdatesign.d();

                i = packetplayinupdatesign.e();
                TileEntitySign tileentitysign1 = (TileEntitySign) tileentity;

                // CraftBukkit start
                /* System.arraycopy(packetplayinupdatesign.f(), 0, tileentitysign1.lines, 0, 4); */
                CraftEventFactory.handleSignChangeEvent(this.player, j, k, i, tileentitysign1, packetplayinupdatesign.f());
                // CraftBukkit end
                tileentitysign1.update();
                worldserver.notify(j, k, i);
            }
        }
    }

    public void a(PacketPlayInKeepAlive packetplayinkeepalive) {
        if (packetplayinkeepalive.c() == this.h) {
            int i = (int) (this.d() - this.i);

            this.player.ping = (this.player.ping * 3 + i) / 4;
        }
    }

    private long d() {
        return System.nanoTime() / 1000000L;
    }

    public void a(PacketPlayInAbilities packetplayinabilities) {
        // CraftBukkit start
        /* this.player.abilities.isFlying = packetplayinabilities.isFlying() && this.player.abilities.canFly; */
        CraftEventFactory.handlePlayerToggleFlightEvent(this.player, packetplayinabilities);
        // CraftBukkit end
    }

    public void a(PacketPlayInTabComplete packetplayintabcomplete) {
        ArrayList arraylist = Lists.newArrayList();
        Iterator iterator = this.minecraftServer.a(this.player, packetplayintabcomplete.c()).iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();

            arraylist.add(s);
        }

        this.player.playerConnection.sendPacket(new PacketPlayOutTabComplete((String[]) arraylist.toArray(new String[arraylist.size()])));
    }

    public void a(PacketPlayInSettings packetplayinsettings) {
        this.player.a(packetplayinsettings);
    }

    public void a(PacketPlayInCustomPayload packetplayincustompayload) {
        PacketDataSerializer packetdataserializer;
        ItemStack itemstack;
        ItemStack itemstack1;

        // CraftBukkit start - Ignore empty payloads
        if (packetplayincustompayload.length <= 0) {
            return;
        }
        // CraftBukkit end

        if ("MC|BEdit".equals(packetplayincustompayload.c())) {
            packetdataserializer = new PacketDataSerializer(Unpooled.wrappedBuffer(packetplayincustompayload.e()));

            try {
                itemstack = packetdataserializer.c();
                if (itemstack == null) {
                    return;
                }

                if (!ItemBookAndQuill.a(itemstack.getTag())) {
                    throw new IOException("Invalid book tag!");
                }

                itemstack1 = this.player.inventory.getItemInHand();
                if (itemstack1 != null) {
                    if (itemstack.getItem() == Items.BOOK_AND_QUILL && itemstack.getItem() == itemstack1.getItem()) {
                        CraftEventFactory.handleEditBookEvent(player, itemstack); // CraftBukkit
                    }

                    return;
                }
                // CraftBukkit start
            } catch (Exception exception) {
                c.error("Couldn\'t handle book info", exception);
                this.disconnect("Invalid book data!");
                return;
                // CraftBukkit end
            } finally {
                packetdataserializer.release();
            }

            return;
        } else if ("MC|BSign".equals(packetplayincustompayload.c())) {
            packetdataserializer = new PacketDataSerializer(Unpooled.wrappedBuffer(packetplayincustompayload.e()));

            try {
                itemstack = packetdataserializer.c();
                if (itemstack != null) {
                    if (!ItemWrittenBook.a(itemstack.getTag())) {
                        throw new IOException("Invalid book tag!");
                    }

                    itemstack1 = this.player.inventory.getItemInHand();
                    if (itemstack1 == null) {
                        return;
                    }

                    if (itemstack.getItem() == Items.WRITTEN_BOOK && itemstack1.getItem() == Items.BOOK_AND_QUILL) {
                        CraftEventFactory.handleEditBookEvent(player, itemstack); // CraftBukkit
                    }

                    return;
                }
                // CraftBukkit start
            } catch (Throwable exception1) {
                c.error("Couldn\'t sign book", exception1);
                this.disconnect("Invalid book data!");
                // CraftBukkit end
                return;
            } finally {
                packetdataserializer.release();
            }

            return;
        } else {
            int i;
            DataInputStream datainputstream;

            if ("MC|TrSel".equals(packetplayincustompayload.c())) {
                try {
                    datainputstream = new DataInputStream(new ByteArrayInputStream(packetplayincustompayload.e()));
                    i = datainputstream.readInt();
                    Container container = this.player.activeContainer;

                    if (container instanceof ContainerMerchant) {
                        ((ContainerMerchant) container).e(i);
                    }
                    // CraftBukkit start
                } catch (Throwable exception2) {
                    c.error("Couldn\'t select trade", exception2);
                    this.disconnect("Invalid trade data!");
                    // CraftBukkit end
                }
            } else if ("MC|AdvCdm".equals(packetplayincustompayload.c())) {
                if (!this.minecraftServer.getEnableCommandBlock()) {
                    this.player.sendMessage(new ChatMessage("advMode.notEnabled", new Object[0]));
                } else if (this.player.a(2, "") && this.player.abilities.canInstantlyBuild) {
                    packetdataserializer = new PacketDataSerializer(Unpooled.wrappedBuffer(packetplayincustompayload.e()));

                    try {
                        byte b0 = packetdataserializer.readByte();
                        CommandBlockListenerAbstract commandblocklistenerabstract = null;

                        if (b0 == 0) {
                            TileEntity tileentity = this.player.world.getTileEntity(packetdataserializer.readInt(), packetdataserializer.readInt(), packetdataserializer.readInt());

                            if (tileentity instanceof TileEntityCommand) {
                                commandblocklistenerabstract = ((TileEntityCommand) tileentity).a();
                            }
                        } else if (b0 == 1) {
                            Entity entity = this.player.world.getEntity(packetdataserializer.readInt());

                            if (entity instanceof EntityMinecartCommandBlock) {
                                commandblocklistenerabstract = ((EntityMinecartCommandBlock) entity).e();
                            }
                        }

                        String s = packetdataserializer.c(packetdataserializer.readableBytes());

                        if (commandblocklistenerabstract != null) {
                            commandblocklistenerabstract.a(s);
                            commandblocklistenerabstract.e();
                            this.player.sendMessage(new ChatMessage("advMode.setCommand.success", new Object[] { s}));
                        }
                        // CraftBukkit start
                    } catch (Throwable exception3) {
                        c.error("Couldn\'t set command block", exception3);
                        this.disconnect("Invalid CommandBlock data!");
                        // CraftBukkit end
                    } finally {
                        packetdataserializer.release();
                    }
                } else {
                    this.player.sendMessage(new ChatMessage("advMode.notAllowed", new Object[0]));
                }
            } else if ("MC|Beacon".equals(packetplayincustompayload.c())) {
                if (this.player.activeContainer instanceof ContainerBeacon) {
                    try {
                        datainputstream = new DataInputStream(new ByteArrayInputStream(packetplayincustompayload.e()));
                        i = datainputstream.readInt();
                        int j = datainputstream.readInt();
                        ContainerBeacon containerbeacon = (ContainerBeacon) this.player.activeContainer;
                        Slot slot = containerbeacon.getSlot(0);

                        if (slot.hasItem()) {
                            slot.a(1);
                            TileEntityBeacon tileentitybeacon = containerbeacon.e();

                            tileentitybeacon.d(i);
                            tileentitybeacon.e(j);
                            tileentitybeacon.update();
                        }
                        // CraftBukkit start
                    } catch (Throwable exception4) {
                        c.error("Couldn\'t set beacon", exception4);
                        this.disconnect("Invalid beacon data!");
                        // CraftBukkit end
                    }
                }
            } else if ("MC|ItemName".equals(packetplayincustompayload.c()) && this.player.activeContainer instanceof ContainerAnvil) {
                ContainerAnvil containeranvil = (ContainerAnvil) this.player.activeContainer;

                if (packetplayincustompayload.e() != null && packetplayincustompayload.e().length >= 1) {
                    String s1 = SharedConstants.a(new String(packetplayincustompayload.e(), Charsets.UTF_8));

                    if (s1.length() <= 30) {
                        containeranvil.a(s1);
                    }
                } else {
                    containeranvil.a("");
                }
            }
            // CraftBukkit start
            else if (packetplayincustompayload.c().equals("REGISTER")) {
                try {
                    String channels = new String(packetplayincustompayload.e(), "UTF8");
                    for (String channel : channels.split("\0")) {
                        this.player.getBukkitEntity().addChannel(channel);
                    }
                } catch (UnsupportedEncodingException ex) {
                    throw new AssertionError(ex);
                }
            } else if (packetplayincustompayload.c().equals("UNREGISTER")) {
                try {
                    String channels = new String(packetplayincustompayload.e(), "UTF8");
                    for (String channel : channels.split("\0")) {
                        this.player.getBukkitEntity().removeChannel(channel);
                    }
                } catch (UnsupportedEncodingException ex) {
                    throw new AssertionError(ex);
                }
            } else {
                this.server.getMessenger().dispatchIncomingMessage(this.player.getBukkitEntity(), packetplayincustompayload.c(), packetplayincustompayload.e());
            }
            // CraftBukkit end
        }
    }

    public void a(EnumProtocol enumprotocol, EnumProtocol enumprotocol1) {
        if (enumprotocol1 != EnumProtocol.PLAY) {
            throw new IllegalStateException("Unexpected change in protocol!");
        }
    }

    // CraftBukkit start - Add "isDisconnected" method
    public final boolean isDisconnected() {
        return !this.player.joining && !NetworkManager.a(this.networkManager).config().isAutoRead();
    }
    // CraftBukkit end
}
