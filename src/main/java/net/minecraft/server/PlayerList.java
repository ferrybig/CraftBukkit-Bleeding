package net.minecraft.server;

import java.io.File;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import net.minecraft.util.com.google.common.base.Charsets;
import net.minecraft.util.com.google.common.collect.Lists;
import net.minecraft.util.com.google.common.collect.Maps;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// CraftBukkit start
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.chunkio.ChunkIOExecutor;

import org.bukkit.Location;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.util.Vector;
// CraftBukkit end

public abstract class PlayerList {

    public static final File a = new File("banned-players.json");
    public static final File b = new File("banned-ips.json");
    public static final File c = new File("ops.json");
    public static final File d = new File("whitelist.json");
    private static final Logger g = LogManager.getLogger();
    private static final SimpleDateFormat h = new SimpleDateFormat("yyyy-MM-dd \'at\' HH:mm:ss z");
    private final MinecraftServer server;
    public final List players = new java.util.concurrent.CopyOnWriteArrayList(); // CraftBukkit - ArrayList -> CopyOnWriteArrayList: Iterator safety
    private final GameProfileBanList j;
    private final IpBanList k;
    private final OpList operators;
    private final WhiteList whitelist;
    private final Map n;
    public IPlayerFileData playerFileData; // CraftBukkit - private -> public
    public boolean hasWhitelist; // CraftBukkit - private -> public
    protected int maxPlayers;
    private int q;
    private EnumGamemode r;
    private boolean s;
    private int t;

    // CraftBukkit start
    private CraftServer cserver;

    public PlayerList(MinecraftServer minecraftserver) {
        minecraftserver.server = new CraftServer(minecraftserver, this);
        minecraftserver.console = org.bukkit.craftbukkit.command.ColouredConsoleSender.getInstance();
        minecraftserver.reader.addCompleter(new org.bukkit.craftbukkit.command.ConsoleCommandCompleter(minecraftserver.server));
        this.cserver = minecraftserver.server;
        // CraftBukkit end

        this.j = new GameProfileBanList(a);
        this.k = new IpBanList(b);
        this.operators = new OpList(c);
        this.whitelist = new WhiteList(d);
        this.n = Maps.newHashMap();
        this.server = minecraftserver;
        this.j.a(false);
        this.k.a(false);
        this.maxPlayers = 8;
    }

    public void a(NetworkManager networkmanager, EntityPlayer entityplayer) {
        GameProfile gameprofile = entityplayer.getProfile();
        UserCache usercache = this.server.getUserCache();
        GameProfile gameprofile1 = usercache.a(gameprofile.getId());
        String s = gameprofile1 == null ? gameprofile.getName() : gameprofile1.getName();

        usercache.a(gameprofile);
        NBTTagCompound nbttagcompound = this.a(entityplayer);

        entityplayer.spawnIn(this.server.getWorldServer(entityplayer.dimension));
        entityplayer.playerInteractManager.a((WorldServer) entityplayer.world);
        String s1 = "local";

        if (networkmanager.getSocketAddress() != null) {
            s1 = networkmanager.getSocketAddress().toString();
        }

        // CraftBukkit - Moved message to after join
        // g.info(entityplayer.getName() + "[" + s1 + "] logged in with entity id " + entityplayer.getId() + " at (" + entityplayer.locX + ", " + entityplayer.locY + ", " + entityplayer.locZ + ")");
        WorldServer worldserver = this.server.getWorldServer(entityplayer.dimension);
        ChunkCoordinates chunkcoordinates = worldserver.getSpawn();

        this.a(entityplayer, (EntityPlayer) null, worldserver);
        PlayerConnection playerconnection = new PlayerConnection(this.server, networkmanager, entityplayer);

        // CraftBukkit start - Don't send a higher than 60 MaxPlayer size, otherwise the PlayerInfo window won't render correctly.
        int maxPlayers = this.getMaxPlayers();
        if (maxPlayers > 60) {
            maxPlayers = 60;
        }
        playerconnection.sendPacket(new PacketPlayOutLogin(entityplayer.getId(), entityplayer.playerInteractManager.getGameMode(), worldserver.getWorldData().isHardcore(), worldserver.worldProvider.dimension, worldserver.difficulty, maxPlayers, worldserver.getWorldData().getType()));
        entityplayer.getBukkitEntity().sendSupportedChannels();
        // CraftBukkit end
        playerconnection.sendPacket(new PacketPlayOutCustomPayload("MC|Brand", this.getServer().getServerModName().getBytes(Charsets.UTF_8)));
        playerconnection.sendPacket(new PacketPlayOutSpawnPosition(chunkcoordinates.x, chunkcoordinates.y, chunkcoordinates.z));
        playerconnection.sendPacket(new PacketPlayOutAbilities(entityplayer.abilities));
        playerconnection.sendPacket(new PacketPlayOutHeldItemSlot(entityplayer.inventory.itemInHandIndex));
        entityplayer.getStatisticManager().d();
        entityplayer.getStatisticManager().updateStatistics(entityplayer);
        this.a((ScoreboardServer) worldserver.getScoreboard(), entityplayer);
        this.server.az();
        /* CraftBukkit start - login message is handled in the event
        ChatMessage chatmessage;

        if (!entityplayer.getName().equalsIgnoreCase(s)) {
            chatmessage = new ChatMessage("multiplayer.player.joined.renamed", new Object[] { entityplayer.getScoreboardDisplayName(), s});
        } else {
            chatmessage = new ChatMessage("multiplayer.player.joined", new Object[] { entityplayer.getScoreboardDisplayName()});
        }

        chatmessage.getChatModifier().setColor(EnumChatFormat.YELLOW);
        this.sendMessage(chatmessage);
        // CraftBukkit end */
        this.c(entityplayer);
        worldserver = this.server.getWorldServer(entityplayer.dimension); // CraftBukkit - Update in case join event changed it
        playerconnection.a(entityplayer.locX, entityplayer.locY, entityplayer.locZ, entityplayer.yaw, entityplayer.pitch);
        this.b(entityplayer, worldserver);
        if (this.server.getResourcePack().length() > 0) {
            entityplayer.setResourcePack(this.server.getResourcePack());
        }

        Iterator iterator = entityplayer.getEffects().iterator();

        while (iterator.hasNext()) {
            MobEffect mobeffect = (MobEffect) iterator.next();

            playerconnection.sendPacket(new PacketPlayOutEntityEffect(entityplayer.getId(), mobeffect));
        }

        entityplayer.syncInventory();
        if (nbttagcompound != null && nbttagcompound.hasKeyOfType("Riding", 10)) {
            Entity entity = EntityTypes.a(nbttagcompound.getCompound("Riding"), worldserver);

            if (entity != null) {
                entity.n = true;
                worldserver.addEntity(entity);
                entityplayer.mount(entity);
                entity.n = false;
            }
        }

        // CraftBukkit - Moved from above, added world
        g.info(entityplayer.getName() + "[" + s1 + "] logged in with entity id " + entityplayer.getId() + " at ([" + entityplayer.world.worldData.getName() + "] " + entityplayer.locX + ", " + entityplayer.locY + ", " + entityplayer.locZ + ")");
    }

    public void a(ScoreboardServer scoreboardserver, EntityPlayer entityplayer) { // CraftBukkit - protected -> public
        HashSet hashset = new HashSet();
        Iterator iterator = scoreboardserver.getTeams().iterator();

        while (iterator.hasNext()) {
            ScoreboardTeam scoreboardteam = (ScoreboardTeam) iterator.next();

            entityplayer.playerConnection.sendPacket(new PacketPlayOutScoreboardTeam(scoreboardteam, 0));
        }

        for (int i = 0; i < 3; ++i) {
            ScoreboardObjective scoreboardobjective = scoreboardserver.getObjectiveForSlot(i);

            if (scoreboardobjective != null && !hashset.contains(scoreboardobjective)) {
                List list = scoreboardserver.getScoreboardScorePacketsForObjective(scoreboardobjective);
                Iterator iterator1 = list.iterator();

                while (iterator1.hasNext()) {
                    Packet packet = (Packet) iterator1.next();

                    entityplayer.playerConnection.sendPacket(packet);
                }

                hashset.add(scoreboardobjective);
            }
        }
    }

    public void setPlayerFileData(WorldServer[] aworldserver) {
        if (this.playerFileData != null) return; // CraftBukkit
        this.playerFileData = aworldserver[0].getDataManager().getPlayerFileData();
    }

    public void a(EntityPlayer entityplayer, WorldServer worldserver) {
        WorldServer worldserver1 = entityplayer.r();

        if (worldserver != null) {
            worldserver.getPlayerChunkMap().removePlayer(entityplayer);
        }

        worldserver1.getPlayerChunkMap().addPlayer(entityplayer);
        worldserver1.chunkProviderServer.getChunkAt((int) entityplayer.locX >> 4, (int) entityplayer.locZ >> 4);
    }

    public int d() {
        return PlayerChunkMap.getFurthestViewableBlock(this.s());
    }

    public NBTTagCompound a(EntityPlayer entityplayer) {
        // CraftBukkit - fix reference to worldserver array
        NBTTagCompound nbttagcompound = this.server.worlds.get(0).getWorldData().i();
        NBTTagCompound nbttagcompound1;

        if (entityplayer.getName().equals(this.server.M()) && nbttagcompound != null) {
            entityplayer.f(nbttagcompound);
            nbttagcompound1 = nbttagcompound;
            g.debug("loading single player");
        } else {
            nbttagcompound1 = this.playerFileData.load(entityplayer);
        }

        return nbttagcompound1;
    }

    protected void b(EntityPlayer entityplayer) {
        this.playerFileData.save(entityplayer);
        ServerStatisticManager serverstatisticmanager = (ServerStatisticManager) this.n.get(entityplayer.getUniqueID());

        if (serverstatisticmanager != null) {
            serverstatisticmanager.b();
        }
    }

    public void c(EntityPlayer entityplayer) {
        this.cserver.detectListNameConflict(entityplayer); // CraftBukkit
        // this.sendAll(new PacketPlayOutPlayerInfo(entityplayer.getName(), true, 1000)); // CraftBukkit - replaced with loop below
        this.players.add(entityplayer);
        WorldServer worldserver = this.server.getWorldServer(entityplayer.dimension);

        // CraftBukkit start
        String joinMessage = CraftEventFactory.callPlayerJoinEvent(entityplayer).getJoinMessage();

        if ((joinMessage != null) && (joinMessage.length() > 0)) {
            for (IChatBaseComponent line : org.bukkit.craftbukkit.util.CraftChatMessage.fromString(joinMessage)) {
                this.sendAll(new PacketPlayOutChat(line));
            }
        }
        this.cserver.onPlayerJoin(entityplayer.getBukkitEntity());

        ChunkIOExecutor.adjustPoolSize(this.getPlayerCount());
        // CraftBukkit end

        // CraftBukkit start - Only add if the player wasn't moved in the event
        if (entityplayer.world == worldserver && !worldserver.players.contains(entityplayer)) {
            worldserver.addEntity(entityplayer);
            this.a(entityplayer, (WorldServer) null);
        }
        // CraftBukkit end

        // CraftBukkit start - sendAll above replaced with this loop
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(entityplayer.listName, true, 1000);
        for (int i = 0; i < this.players.size(); ++i) {
            EntityPlayer entityplayer1 = (EntityPlayer) this.players.get(i);

            if (entityplayer1.getBukkitEntity().canSee(entityplayer.getBukkitEntity())) {
                entityplayer1.playerConnection.sendPacket(packet);
            }
        }
        // CraftBukkit end

        for (int i = 0; i < this.players.size(); ++i) {
            EntityPlayer entityplayer1 = (EntityPlayer) this.players.get(i);

            // CraftBukkit start
            if (!entityplayer.getBukkitEntity().canSee(entityplayer1.getBukkitEntity())) {
                continue;
            }

            entityplayer.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(entityplayer1.listName, true, entityplayer1.ping)); // .getName() -> .listName
            // CraftBukkit end
        }
    }

    public void d(EntityPlayer entityplayer) {
        entityplayer.r().getPlayerChunkMap().movePlayer(entityplayer);
    }

    public String disconnect(EntityPlayer entityplayer) { // CraftBukkit - return string
        entityplayer.a(StatisticList.f);

        // CraftBukkit start - Quitting must be before we do final save of data, in case plugins need to modify it
        CraftEventFactory.handleInventoryCloseEvent(entityplayer);
        String quitMessage = CraftEventFactory.handlePlayerQuitEvent(entityplayer);
        // CraftBukkit end

        this.b(entityplayer);
        WorldServer worldserver = entityplayer.r();

        if (entityplayer.vehicle != null && !(entityplayer.vehicle instanceof EntityPlayer)) { // CraftBukkit - Don't remove players
            worldserver.removeEntity(entityplayer.vehicle);
            g.debug("removing player mount");
        }

        worldserver.kill(entityplayer);
        worldserver.getPlayerChunkMap().removePlayer(entityplayer);
        this.players.remove(entityplayer);
        this.n.remove(entityplayer.getUniqueID());
        ChunkIOExecutor.adjustPoolSize(this.getPlayerCount()); // CraftBukkit

        // CraftBukkit start - .name -> .listName, replace sendAll with loop
        // this.sendAll(new PacketPlayOutPlayerInfo(entityplayer.getName(), false, 9999));
        PacketPlayOutPlayerInfo packet = new PacketPlayOutPlayerInfo(entityplayer.listName, false, 9999);
        for (int i = 0; i < this.players.size(); ++i) {
            EntityPlayer entityplayer1 = (EntityPlayer) this.players.get(i);

            if (entityplayer1.getBukkitEntity().canSee(entityplayer.getBukkitEntity())) {
                entityplayer1.playerConnection.sendPacket(packet);
            } else {
                entityplayer1.getBukkitEntity().removeDisconnectingPlayer(entityplayer.getBukkitEntity());
            }
        }
        // This removes the scoreboard (and player reference) for the specific player in the manager
        this.cserver.getScoreboardManager().removePlayer(entityplayer.getBukkitEntity());

        return quitMessage;
        // CraftBukkit end
    }

    // CraftBukkit start - Whole method, SocketAddress to LoginListener, added hostname to signature, return EntityPlayer
    public EntityPlayer attemptLogin(LoginListener loginlistener, GameProfile gameprofile, String hostname) {
        // Instead of kicking then returning, we need to store the kick reason
        // in the event, check with plugins to see if it's ok, and THEN kick
        // depending on the outcome.
        SocketAddress socketaddress = loginlistener.networkManager.getSocketAddress();

        String s = "";
        Result result = Result.ALLOWED;

        if (this.j.isBanned(gameprofile) && !this.j.get(gameprofile).e()) { // add this.j.get(gameprofile).e(), should be hasExpired
            GameProfileBanEntry gameprofilebanentry = (GameProfileBanEntry) this.j.get(gameprofile);

            s = "You are banned from this server!\nReason: " + gameprofilebanentry.getReason();
            if (gameprofilebanentry.getExpires() != null) {
                s = s + "\nYour ban will be removed on " + h.format(gameprofilebanentry.getExpires());
            }

            // return s;
            result = Result.KICK_BANNED;
        } else if (!this.isWhitelisted(gameprofile)) {
            // return "You are not white-listed on this server!";
            s = "You are not white-listed on this server!";
            result = Result.KICK_WHITELIST;
        } else if (this.k.isBanned(socketaddress) && !this.j.get(gameprofile).e()) { // add this.j.get(gameprofile).e(), Should be hasExpired
            IpBanEntry ipbanentry = this.k.get(socketaddress);

            s = "Your IP address is banned from this server!\nReason: " + ipbanentry.getReason();
            if (ipbanentry.getExpires() != null) {
                s = s + "\nYour ban will be removed on " + h.format(ipbanentry.getExpires());
            }

            // return s;
            result = Result.KICK_BANNED;
        } else {
            // return this.players.size() >= this.maxPlayers ? "The server is full!" : null;
            if (this.players.size() >= this.maxPlayers) {
                s = "The server is full!";
                result = Result.KICK_FULL;
            }
        }

        return CraftEventFactory.handlePlayerLoginEvent(this.server, gameprofile, hostname, socketaddress, loginlistener, result, s);
        // CraftBukkit end
    }

    public EntityPlayer processLogin(GameProfile gameprofile, EntityPlayer player) { // CraftBukkit - added EntityPlayer
        UUID uuid = EntityHuman.a(gameprofile);
        ArrayList arraylist = Lists.newArrayList();

        EntityPlayer entityplayer;

        for (int i = 0; i < this.players.size(); ++i) {
            entityplayer = (EntityPlayer) this.players.get(i);
            if (entityplayer.getUniqueID().equals(uuid)) {
                arraylist.add(entityplayer);
            }
        }

        Iterator iterator = arraylist.iterator();

        while (iterator.hasNext()) {
            entityplayer = (EntityPlayer) iterator.next();
            entityplayer.playerConnection.disconnect("You logged in from another location");
        }

        /* CraftBukkit start
        Object object;

        if (this.server.R()) {
            object = new DemoPlayerInteractManager(this.server.getWorldServer(0));
        } else {
            object = new PlayerInteractManager(this.server.getWorldServer(0));
        }

        return new EntityPlayer(this.server, this.server.getWorldServer(0), gameprofile, (PlayerInteractManager) object);
        // */
        return player;
        // CraftBukkit end
    }

    // CraftBukkit start
    public EntityPlayer moveToWorld(EntityPlayer entityplayer, int i, boolean flag) {
        return this.moveToWorld(entityplayer, i, flag, null, true);
    }

    public EntityPlayer moveToWorld(EntityPlayer entityplayer, int i, boolean flag, Location location, boolean avoidSuffocation) {
        // CraftBukkit end
        entityplayer.r().getTracker().untrackPlayer(entityplayer);
        // entityplayer.r().getTracker().untrackEntity(entityplayer); // CraftBukkit
        entityplayer.r().getPlayerChunkMap().removePlayer(entityplayer);
        this.players.remove(entityplayer);
        this.server.getWorldServer(entityplayer.dimension).removeEntity(entityplayer);
        ChunkCoordinates chunkcoordinates = entityplayer.getBed();
        boolean flag1 = entityplayer.isRespawnForced();

        /* CraftBukkit start
        entityplayer.dimension = i;
        Object object;

        if (this.server.R()) {
            object = new DemoPlayerInteractManager(this.server.getWorldServer(entityplayer.dimension));
        } else {
            object = new PlayerInteractManager(this.server.getWorldServer(entityplayer.dimension));
        }

        EntityPlayer entityplayer1 = new EntityPlayer(this.server, this.server.getWorldServer(entityplayer.dimension), entityplayer.getProfile(), (PlayerInteractManager) object);
        // */
        EntityPlayer entityplayer1 = entityplayer;
        org.bukkit.World fromWorld = entityplayer1.getBukkitEntity().getWorld();
        entityplayer1.viewingCredits = false;
        // CraftBukkit end

        entityplayer1.playerConnection = entityplayer.playerConnection;
        entityplayer1.copyTo(entityplayer, flag);
        entityplayer1.d(entityplayer.getId());
        // WorldServer worldserver = this.server.getWorldServer(entityplayer.dimension); // CraftBukkit - handled later

        // this.a(entityplayer1, entityplayer, worldserver); // CraftBukkit - removed
        ChunkCoordinates chunkcoordinates1;

        // CraftBukkit start - fire PlayerRespawnEvent
        if (location == null) {
            boolean isBedSpawn = false;
            CraftWorld cworld = (CraftWorld) this.cserver.getWorld(entityplayer.spawnWorld);
            if (cworld != null && chunkcoordinates != null) {
                chunkcoordinates1 = EntityHuman.getBed(cworld.getHandle(), chunkcoordinates, flag1);
                if (chunkcoordinates1 != null) {
                    isBedSpawn = true;
                    location = new Location(cworld, chunkcoordinates1.x + 0.5, chunkcoordinates1.y, chunkcoordinates1.z + 0.5);
                } else {
                    entityplayer1.setRespawnPosition(null, true);
                    entityplayer1.playerConnection.sendPacket(new PacketPlayOutGameStateChange(0, 0));
                }
            }

            if (location == null) {
                cworld = (CraftWorld) this.cserver.getWorlds().get(0);
                chunkcoordinates = cworld.getHandle().getSpawn();
                location = new Location(cworld, chunkcoordinates.x + 0.5, chunkcoordinates.y, chunkcoordinates.z + 0.5);
            }

            location = CraftEventFactory.callPlayerRespawnEvent(entityplayer1, location, isBedSpawn).getRespawnLocation();
            entityplayer.reset();
        } else {
            location.setWorld(this.server.getWorldServer(i).getWorld());
        }
        WorldServer worldserver = ((CraftWorld) location.getWorld()).getHandle();
        entityplayer1.setLocation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        // CraftBukkit end

        worldserver.chunkProviderServer.getChunkAt((int) entityplayer1.locX >> 4, (int) entityplayer1.locZ >> 4);

        while (avoidSuffocation && !worldserver.getCubes(entityplayer1, entityplayer1.boundingBox).isEmpty()) { // CraftBukkit
            entityplayer1.setPosition(entityplayer1.locX, entityplayer1.locY + 1.0D, entityplayer1.locZ);
        }

        // CraftBukkit start
        byte actualDimension = (byte) (worldserver.getWorld().getEnvironment().getId());
        // Force the client to refresh their chunk cache.
        entityplayer1.playerConnection.sendPacket(new PacketPlayOutRespawn((byte) (actualDimension >= 0 ? -1 : 0), worldserver.difficulty, worldserver.getWorldData().getType(), entityplayer.playerInteractManager.getGameMode()));
        entityplayer1.playerConnection.sendPacket(new PacketPlayOutRespawn(actualDimension, worldserver.difficulty, worldserver.getWorldData().getType(), entityplayer1.playerInteractManager.getGameMode()));
        entityplayer1.spawnIn(worldserver);
        entityplayer1.dead = false;
        entityplayer1.playerConnection.teleport(new Location(worldserver.getWorld(), entityplayer1.locX, entityplayer1.locY, entityplayer1.locZ, entityplayer1.yaw, entityplayer1.pitch));
        entityplayer1.setSneaking(false);
        chunkcoordinates1 = worldserver.getSpawn();
        // entityplayer1.playerConnection.a(entityplayer1.locX, entityplayer1.locY, entityplayer1.locZ, entityplayer1.yaw, entityplayer1.pitch);
        // CraftBukkit end
        entityplayer1.playerConnection.sendPacket(new PacketPlayOutSpawnPosition(chunkcoordinates1.x, chunkcoordinates1.y, chunkcoordinates1.z));
        entityplayer1.playerConnection.sendPacket(new PacketPlayOutExperience(entityplayer1.exp, entityplayer1.expTotal, entityplayer1.expLevel));
        this.b(entityplayer1, worldserver);
        // CraftBukkit start
        // Don't re-add player to player list if disconnected
        if (!entityplayer.playerConnection.isDisconnected()) {
            worldserver.getPlayerChunkMap().addPlayer(entityplayer1);
            worldserver.addEntity(entityplayer1);
            this.players.add(entityplayer1);
        }
        // Added from changeDimension
        this.updateClient(entityplayer1); // Update health, etc...
        entityplayer1.updateAbilities();
        Iterator iterator = entityplayer1.getEffects().iterator();

        while (iterator.hasNext()) {
            MobEffect mobeffect = (MobEffect) iterator.next();

            entityplayer1.playerConnection.sendPacket(new PacketPlayOutEntityEffect(entityplayer1.getId(), mobeffect));
        }
        // entityplayer1.syncInventory();
        // CraftBukkit end
        entityplayer1.setHealth(entityplayer1.getHealth());

        // CraftBukkit start
        // Don't fire on respawn
        if (fromWorld != location.getWorld()) {
            CraftEventFactory.callPlayerChangedWorldEvent(entityplayer1, fromWorld);
        }

        // Save player file again if they were disconnected
        if (entityplayer.playerConnection.isDisconnected()) {
            this.b(entityplayer1);
        }
        // CraftBukkit end

        return entityplayer1;
    }

    public void changeDimension(EntityPlayer entityplayer, int i) {
        int j = entityplayer.dimension;
        WorldServer worldserver = this.server.getWorldServer(entityplayer.dimension);

        entityplayer.dimension = i;
        WorldServer worldserver1 = this.server.getWorldServer(entityplayer.dimension);

        entityplayer.playerConnection.sendPacket(new PacketPlayOutRespawn(entityplayer.dimension, entityplayer.world.difficulty, entityplayer.world.getWorldData().getType(), entityplayer.playerInteractManager.getGameMode()));
        worldserver.removeEntity(entityplayer);
        entityplayer.dead = false;
        this.a(entityplayer, j, worldserver, worldserver1);
        this.a(entityplayer, worldserver);
        entityplayer.playerConnection.a(entityplayer.locX, entityplayer.locY, entityplayer.locZ, entityplayer.yaw, entityplayer.pitch);
        entityplayer.playerInteractManager.a(worldserver1);
        this.b(entityplayer, worldserver1);
        this.updateClient(entityplayer);
        Iterator iterator = entityplayer.getEffects().iterator();

        while (iterator.hasNext()) {
            MobEffect mobeffect = (MobEffect) iterator.next();

            entityplayer.playerConnection.sendPacket(new PacketPlayOutEntityEffect(entityplayer.getId(), mobeffect));
        }
    }

    public void a(Entity entity, int i, WorldServer worldserver, WorldServer worldserver1) {
        // CraftBukkit start - Split into modular functions
        Location exit = this.calculateTarget(entity.getBukkitEntity().getLocation(), worldserver1);
        this.repositionEntity(entity, exit, true);
    }

    // Copy of original a(Entity, int, WorldServer, WorldServer) method with only location calculation logic
    public Location calculateTarget(Location enter, World target) {
        WorldServer worldserver = ((CraftWorld) enter.getWorld()).getHandle();
        WorldServer worldserver1 = ((CraftWorld) target.getWorld()).getHandle();
        int i = worldserver.dimension;

        double y = enter.getY();
        float yaw = enter.getYaw();
        float pitch = enter.getPitch();
        double d0 = enter.getX();
        double d1 = enter.getZ();
        double d2 = 8.0D;
        /*
        double d3 = entity.locX;
        double d4 = entity.locY;
        double d5 = entity.locZ;
        float f = entity.yaw;

        worldserver.methodProfiler.a("moving");
        */
        if (worldserver1.dimension == -1) {
            d0 /= d2;
            d1 /= d2;
            /*
            entity.setPositionRotation(d0, entity.locY, d1, entity.yaw, entity.pitch);
            if (entity.isAlive()) {
                worldserver.entityJoinedWorld(entity, false);
            }
            */
        } else if (worldserver1.dimension == 0) {
            d0 *= d2;
            d1 *= d2;
            /*
            entity.setPositionRotation(d0, entity.locY, d1, entity.yaw, entity.pitch);
            if (entity.isAlive()) {
                worldserver.entityJoinedWorld(entity, false);
            }
            */
        } else {
            ChunkCoordinates chunkcoordinates;

            if (i == 1) {
                // use default NORMAL world spawn instead of target
                worldserver1 = this.server.worlds.get(0);
                chunkcoordinates = worldserver1.getSpawn();
            } else {
                chunkcoordinates = worldserver1.getDimensionSpawn();
            }

            d0 = (double) chunkcoordinates.x;
            y = (double) chunkcoordinates.y;
            d1 = (double) chunkcoordinates.z;
            yaw = 90.0F;
            pitch = 0.0F;
            /*
            entity.setPositionRotation(d0, entity.locY, d1, 90.0F, 0.0F);
            if (entity.isAlive()) {
                worldserver.entityJoinedWorld(entity, false);
            }
            */
        }

        // worldserver.methodProfiler.b();
        if (i != 1) {
            // worldserver.methodProfiler.a("placing");
            d0 = (double) MathHelper.a((int) d0, -29999872, 29999872);
            d1 = (double) MathHelper.a((int) d1, -29999872, 29999872);
            /*
            if (entity.isAlive()) {
                worldserver1.addEntity(entity);
                entity.setPositionRotation(d0, entity.locY, d1, entity.yaw, entity.pitch);
                worldserver1.entityJoinedWorld(entity, false);
                worldserver1.getTravelAgent().a(entity, d3, d4, d5, f);
            }

            worldserver.methodProfiler.b();
            */
        }

        // entity.spawnIn(worldserver1);
        return new Location(worldserver1.getWorld(), d0, y, d1, yaw, pitch);
    }

    // copy of original a(Entity, int, WorldServer, WorldServer) method with only entity repositioning logic
    public void repositionEntity(Entity entity, Location exit, boolean portal) {
        int i = entity.dimension;
        WorldServer worldserver = (WorldServer) entity.world;
        WorldServer worldserver1 = ((CraftWorld) exit.getWorld()).getHandle();
        /*
        double d0 = entity.locX;
        double d1 = entity.locZ;
        double d2 = 8.0D;
        double d3 = entity.locX;
        double d4 = entity.locY;
        double d5 = entity.locZ;
        float f = entity.yaw;
        */

        worldserver.methodProfiler.a("moving");
        entity.setPositionRotation(exit.getX(), exit.getY(), exit.getZ(), exit.getYaw(), exit.getPitch());
        if (entity.isAlive()) {
            worldserver.entityJoinedWorld(entity, false);
        }
        /*
        if (entity.dimension == -1) {
            d0 /= d2;
            d1 /= d2;
            entity.setPositionRotation(d0, entity.locY, d1, entity.yaw, entity.pitch);
            if (entity.isAlive()) {
                worldserver.entityJoinedWorld(entity, false);
            }
        } else if (entity.dimension == 0) {
            d0 *= d2;
            d1 *= d2;
            entity.setPositionRotation(d0, entity.locY, d1, entity.yaw, entity.pitch);
            if (entity.isAlive()) {
                worldserver.entityJoinedWorld(entity, false);
            }
        } else {
            ChunkCoordinates chunkcoordinates;

            if (i == 1) {
                chunkcoordinates = worldserver1.getSpawn();
            } else {
                chunkcoordinates = worldserver1.getDimensionSpawn();
            }

            d0 = (double) chunkcoordinates.x;
            entity.locY = (double) chunkcoordinates.y;
            d1 = (double) chunkcoordinates.z;
            entity.setPositionRotation(d0, entity.locY, d1, 90.0F, 0.0F);
            if (entity.isAlive()) {
                worldserver.entityJoinedWorld(entity, false);
            }
        }
        */

        worldserver.methodProfiler.b();
        if (i != 1) {
            worldserver.methodProfiler.a("placing");
            /*
            d0 = (double) MathHelper.a((int) d0, -29999872, 29999872);
            d1 = (double) MathHelper.a((int) d1, -29999872, 29999872);
            */
            if (entity.isAlive()) {
                // entity.setPositionRotation(d0, entity.locY, d1, entity.yaw, entity.pitch)
                // worldserver1.getTravelAgent().a(entity, d3, d4, d5, f);
                if (portal) {
                    Vector velocity = entity.getBukkitEntity().getVelocity();
                    worldserver1.getTravelAgent().adjustExit(entity, exit, velocity);
                    entity.setPositionRotation(exit.getX(), exit.getY(), exit.getZ(), exit.getYaw(), exit.getPitch());
                    if (entity.motX != velocity.getX() || entity.motY != velocity.getY() || entity.motZ != velocity.getZ()) {
                        entity.getBukkitEntity().setVelocity(velocity);
                    }
                }
                worldserver1.addEntity(entity);
                worldserver1.entityJoinedWorld(entity, false);
            }

            worldserver.methodProfiler.b();
        }

        entity.spawnIn(worldserver1);
        // CraftBukkit end
    }

    public void tick() {
        if (++this.t > 600) {
            this.t = 0;
        }

        /* CraftBukkit start - Remove updating of lag to players -- it spams way to much on big servers.
        if (this.t < this.players.size()) {
            EntityPlayer entityplayer = (EntityPlayer) this.players.get(this.p);

            this.sendAll(new PacketPlayOutPlayerInfo(entityplayer.getName(), true, entityplayer.ping));
        }
        // CraftBukkit end */
    }

    public void sendAll(Packet packet) {
        for (int i = 0; i < this.players.size(); ++i) {
            ((EntityPlayer) this.players.get(i)).playerConnection.sendPacket(packet);
        }
    }

    public void a(Packet packet, int i) {
        for (int j = 0; j < this.players.size(); ++j) {
            EntityPlayer entityplayer = (EntityPlayer) this.players.get(j);

            if (entityplayer.dimension == i) {
                entityplayer.playerConnection.sendPacket(packet);
            }
        }
    }

    public String b(boolean flag) {
        String s = "";
        ArrayList arraylist = Lists.newArrayList(this.players);

        for (int i = 0; i < arraylist.size(); ++i) {
            if (i > 0) {
                s = s + ", ";
            }

            s = s + ((EntityPlayer) arraylist.get(i)).getName();
            if (flag) {
                s = s + " (" + ((EntityPlayer) arraylist.get(i)).getUniqueID().toString() + ")";
            }
        }

        return s;
    }

    public String[] f() {
        String[] astring = new String[this.players.size()];

        for (int i = 0; i < this.players.size(); ++i) {
            astring[i] = ((EntityPlayer) this.players.get(i)).getName();
        }

        return astring;
    }

    public GameProfile[] g() {
        GameProfile[] agameprofile = new GameProfile[this.players.size()];

        for (int i = 0; i < this.players.size(); ++i) {
            agameprofile[i] = ((EntityPlayer) this.players.get(i)).getProfile();
        }

        return agameprofile;
    }

    public GameProfileBanList getProfileBans() {
        return this.j;
    }

    public IpBanList getIPBans() {
        return this.k;
    }

    public void addOp(GameProfile gameprofile) {
        this.operators.add(new OpListEntry(gameprofile, this.server.l()));

        // CraftBukkit start
        Player player = this.cserver.getPlayer(gameprofile.getId());
        if (player != null) {
            player.recalculatePermissions();
        }
        // CraftBukkit end
    }

    public void removeOp(GameProfile gameprofile) {
        this.operators.remove(gameprofile);

        // CraftBukkit start
        Player player = this.cserver.getPlayer(gameprofile.getId());
        if (player != null) {
            player.recalculatePermissions();
        }
        // CraftBukkit end
    }

    public boolean isWhitelisted(GameProfile gameprofile) {
        return !this.hasWhitelist || this.operators.d(gameprofile) || this.whitelist.d(gameprofile);
    }

    public boolean isOp(GameProfile gameprofile) {
        // CraftBukkit - fix reference to worldserver array
        return this.operators.d(gameprofile) || this.server.N() && this.server.worlds.get(0).getWorldData().allowCommands() && this.server.M().equalsIgnoreCase(gameprofile.getName()) || this.s;
    }

    public EntityPlayer getPlayer(String s) {
        Iterator iterator = this.players.iterator();

        EntityPlayer entityplayer;

        do {
            if (!iterator.hasNext()) {
                return null;
            }

            entityplayer = (EntityPlayer) iterator.next();
        } while (!entityplayer.getName().equalsIgnoreCase(s));

        return entityplayer;
    }

    public List a(ChunkCoordinates chunkcoordinates, int i, int j, int k, int l, int i1, int j1, Map map, String s, String s1, World world) {
        if (this.players.isEmpty()) {
            return Collections.emptyList();
        } else {
            Object object = new ArrayList();
            boolean flag = k < 0;
            boolean flag1 = s != null && s.startsWith("!");
            boolean flag2 = s1 != null && s1.startsWith("!");
            int k1 = i * i;
            int l1 = j * j;

            k = MathHelper.a(k);
            if (flag1) {
                s = s.substring(1);
            }

            if (flag2) {
                s1 = s1.substring(1);
            }

            for (int i2 = 0; i2 < this.players.size(); ++i2) {
                EntityPlayer entityplayer = (EntityPlayer) this.players.get(i2);

                if ((world == null || entityplayer.world == world) && (s == null || flag1 != s.equalsIgnoreCase(entityplayer.getName()))) {
                    if (s1 != null) {
                        ScoreboardTeamBase scoreboardteambase = entityplayer.getScoreboardTeam();
                        String s2 = scoreboardteambase == null ? "" : scoreboardteambase.getName();

                        if (flag2 == s1.equalsIgnoreCase(s2)) {
                            continue;
                        }
                    }

                    if (chunkcoordinates != null && (i > 0 || j > 0)) {
                        float f = chunkcoordinates.e(entityplayer.getChunkCoordinates());

                        if (i > 0 && f < (float) k1 || j > 0 && f > (float) l1) {
                            continue;
                        }
                    }

                    if (this.a((EntityHuman) entityplayer, map) && (l == EnumGamemode.NONE.a() || l == entityplayer.playerInteractManager.getGameMode().a()) && (i1 <= 0 || entityplayer.expLevel >= i1) && entityplayer.expLevel <= j1) {
                        ((List) object).add(entityplayer);
                    }
                }
            }

            if (chunkcoordinates != null) {
                Collections.sort((List) object, new PlayerDistanceComparator(chunkcoordinates));
            }

            if (flag) {
                Collections.reverse((List) object);
            }

            if (k > 0) {
                object = ((List) object).subList(0, Math.min(k, ((List) object).size()));
            }

            return (List) object;
        }
    }

    private boolean a(EntityHuman entityhuman, Map map) {
        if (map != null && map.size() != 0) {
            Iterator iterator = map.entrySet().iterator();

            Entry entry;
            boolean flag;
            int i;

            do {
                if (!iterator.hasNext()) {
                    return true;
                }

                entry = (Entry) iterator.next();
                String s = (String) entry.getKey();

                flag = false;
                if (s.endsWith("_min") && s.length() > 4) {
                    flag = true;
                    s = s.substring(0, s.length() - 4);
                }

                Scoreboard scoreboard = entityhuman.getScoreboard();
                ScoreboardObjective scoreboardobjective = scoreboard.getObjective(s);

                if (scoreboardobjective == null) {
                    return false;
                }

                ScoreboardScore scoreboardscore = entityhuman.getScoreboard().getPlayerScoreForObjective(entityhuman.getName(), scoreboardobjective);

                i = scoreboardscore.getScore();
                if (i < ((Integer) entry.getValue()).intValue() && flag) {
                    return false;
                }
            } while (i <= ((Integer) entry.getValue()).intValue() || flag);

            return false;
        } else {
            return true;
        }
    }

    public void sendPacketNearby(double d0, double d1, double d2, double d3, int i, Packet packet) {
        this.sendPacketNearby((EntityHuman) null, d0, d1, d2, d3, i, packet);
    }

    public void sendPacketNearby(EntityHuman entityhuman, double d0, double d1, double d2, double d3, int i, Packet packet) {
        for (int j = 0; j < this.players.size(); ++j) {
            EntityPlayer entityplayer = (EntityPlayer) this.players.get(j);

            // CraftBukkit start - Test if player receiving packet can see the source of the packet
            if (entityhuman != null && entityhuman instanceof EntityPlayer && !entityplayer.getBukkitEntity().canSee(((EntityPlayer) entityhuman).getBukkitEntity())) {
                continue;
            }
            // CraftBukkit end

            if (entityplayer != entityhuman && entityplayer.dimension == i) {
                double d4 = d0 - entityplayer.locX;
                double d5 = d1 - entityplayer.locY;
                double d6 = d2 - entityplayer.locZ;

                if (d4 * d4 + d5 * d5 + d6 * d6 < d3 * d3) {
                    entityplayer.playerConnection.sendPacket(packet);
                }
            }
        }
    }

    public void savePlayers() {
        for (int i = 0; i < this.players.size(); ++i) {
            this.b((EntityPlayer) this.players.get(i));
        }
    }

    public void addWhitelist(GameProfile gameprofile) {
        this.whitelist.add(new WhiteListEntry(gameprofile));
    }

    public void removeWhitelist(GameProfile gameprofile) {
        this.whitelist.remove(gameprofile);
    }

    public WhiteList getWhitelist() {
        return this.whitelist;
    }

    public String[] getWhitelisted() {
        return this.whitelist.getEntries();
    }

    public OpList getOPs() {
        return this.operators;
    }

    public String[] n() {
        return this.operators.getEntries();
    }

    public void reloadWhitelist() {}

    public void b(EntityPlayer entityplayer, WorldServer worldserver) {
        entityplayer.playerConnection.sendPacket(new PacketPlayOutUpdateTime(worldserver.getTime(), worldserver.getDayTime(), worldserver.getGameRules().getBoolean("doDaylightCycle")));
        if (worldserver.Q()) {
            // CraftBukkit start - handle player weather
            // entityplayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(1, 0.0F));
            // entityplayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(7, worldserver.j(1.0F)));
            // entityplayer.playerConnection.sendPacket(new PacketPlayOutGameStateChange(8, worldserver.h(1.0F)));
            entityplayer.setPlayerWeather(org.bukkit.WeatherType.DOWNFALL, false);
            // CraftBukkit end
        }
    }

    public void updateClient(EntityPlayer entityplayer) {
        entityplayer.updateInventory(entityplayer.defaultContainer);
        entityplayer.getBukkitEntity().updateScaledHealth(); // CraftBukkit - Update scaled health on respawn and worldchange
        entityplayer.playerConnection.sendPacket(new PacketPlayOutHeldItemSlot(entityplayer.inventory.itemInHandIndex));
    }

    public int getPlayerCount() {
        return this.players.size();
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public String[] getSeenPlayers() {
        // CraftBukkit - fix reference to worldserver array
        return this.server.worlds.get(0).getDataManager().getPlayerFileData().getSeenPlayers();
    }

    public boolean getHasWhitelist() {
        return this.hasWhitelist;
    }

    public void setHasWhitelist(boolean flag) {
        this.hasWhitelist = flag;
    }

    public List b(String s) {
        ArrayList arraylist = new ArrayList();
        Iterator iterator = this.players.iterator();

        while (iterator.hasNext()) {
            EntityPlayer entityplayer = (EntityPlayer) iterator.next();

            if (entityplayer.s().equals(s)) {
                arraylist.add(entityplayer);
            }
        }

        return arraylist;
    }

    public int s() {
        return this.q;
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public NBTTagCompound t() {
        return null;
    }

    private void a(EntityPlayer entityplayer, EntityPlayer entityplayer1, World world) {
        if (entityplayer1 != null) {
            entityplayer.playerInteractManager.setGameMode(entityplayer1.playerInteractManager.getGameMode());
        } else if (this.r != null) {
            entityplayer.playerInteractManager.setGameMode(this.r);
        }

        entityplayer.playerInteractManager.b(world.getWorldData().getGameType());
    }

    public void u() {
        for (int i = 0; i < this.players.size(); ++i) {
            ((EntityPlayer) this.players.get(i)).playerConnection.disconnect(this.cserver.getShutdownMessage()); // CraftBukkit - add custom shutdown message
        }
    }

    // CraftBukkit start - Support multi-line messages
    public void sendMessage(IChatBaseComponent[] ichatbasecomponent) {
        for (IChatBaseComponent component : ichatbasecomponent) {
            sendMessage(component, true);
        }
    }
    // CraftBukkit end

    public void sendMessage(IChatBaseComponent ichatbasecomponent, boolean flag) {
        this.server.sendMessage(ichatbasecomponent);
        this.sendAll(new PacketPlayOutChat(ichatbasecomponent, flag));
    }

    public void sendMessage(IChatBaseComponent ichatbasecomponent) {
        this.sendMessage(ichatbasecomponent, true);
    }

    public ServerStatisticManager a(EntityHuman entityhuman) {
        UUID uuid = entityhuman.getUniqueID();
        ServerStatisticManager serverstatisticmanager = uuid == null ? null : (ServerStatisticManager) this.n.get(uuid);

        if (serverstatisticmanager == null) {
            File file1 = new File(this.server.getWorldServer(0).getDataManager().getDirectory(), "stats");
            File file2 = new File(file1, uuid.toString() + ".json");

            if (!file2.exists()) {
                File file3 = new File(file1, entityhuman.getName() + ".json");

                if (file3.exists() && file3.isFile()) {
                    file3.renameTo(file2);
                }
            }

            serverstatisticmanager = new ServerStatisticManager(this.server, file2);
            serverstatisticmanager.a();
            this.n.put(uuid, serverstatisticmanager);
        }

        return serverstatisticmanager;
    }

    public void a(int i) {
        this.q = i;
        if (this.server.worldServer != null) {
            WorldServer[] aworldserver = this.server.worldServer;
            int j = aworldserver.length;

            for (int k = 0; k < j; ++k) {
                WorldServer worldserver = aworldserver[k];

                if (worldserver != null) {
                    worldserver.getPlayerChunkMap().a(i);
                }
            }
        }
    }
}
