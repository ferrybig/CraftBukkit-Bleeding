package org.bukkit.craftbukkit.command;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.server.ChatMessage;
import net.minecraft.server.CommandAbstract;
import net.minecraft.server.CommandAchievement;
import net.minecraft.server.CommandBan;
import net.minecraft.server.CommandBanIp;
import net.minecraft.server.CommandBanList;
import net.minecraft.server.CommandBlockListenerAbstract;
import net.minecraft.server.CommandClear;
import net.minecraft.server.CommandDeop;
import net.minecraft.server.CommandDifficulty;
import net.minecraft.server.CommandEffect;
import net.minecraft.server.CommandEnchant;
import net.minecraft.server.CommandException;
import net.minecraft.server.CommandGamemode;
import net.minecraft.server.CommandGamemodeDefault;
import net.minecraft.server.CommandGamerule;
import net.minecraft.server.CommandGive;
import net.minecraft.server.CommandHelp;
import net.minecraft.server.CommandIdleTimeout;
import net.minecraft.server.CommandKick;
import net.minecraft.server.CommandKill;
import net.minecraft.server.CommandList;
import net.minecraft.server.CommandMe;
import net.minecraft.server.CommandOp;
import net.minecraft.server.CommandPardon;
import net.minecraft.server.CommandPardonIP;
import net.minecraft.server.CommandPlaySound;
import net.minecraft.server.CommandSay;
import net.minecraft.server.CommandScoreboard;
import net.minecraft.server.CommandSeed;
import net.minecraft.server.CommandSetBlock;
import net.minecraft.server.CommandSetWorldSpawn;
import net.minecraft.server.CommandSpawnpoint;
import net.minecraft.server.CommandSpreadPlayers;
import net.minecraft.server.CommandSummon;
import net.minecraft.server.CommandTell;
import net.minecraft.server.CommandTellRaw;
import net.minecraft.server.CommandTestFor;
import net.minecraft.server.CommandTestForBlock;
import net.minecraft.server.CommandTime;
import net.minecraft.server.CommandToggleDownfall;
import net.minecraft.server.CommandTp;
import net.minecraft.server.CommandWeather;
import net.minecraft.server.CommandWhitelist;
import net.minecraft.server.CommandXp;
import net.minecraft.server.EntityMinecartCommandBlock;
import net.minecraft.server.EntityMinecartCommandBlockListener;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.EnumChatFormat;
import net.minecraft.server.ExceptionUsage;
import net.minecraft.server.ICommandListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerSelector;
import net.minecraft.server.RemoteControlCommandListener;
import net.minecraft.server.TileEntityCommandListener;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.Level;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.command.defaults.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftMinecartCommand;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;

public class VanillaCommandWrapper extends VanillaCommand {
    public static final String DEFAULT = "default";
    public static final String BUKKIT = "bukkit";
    public static final String MOJANG = "mojang";
    public static final String COMMAND_BLOCK = "command-block";
    private static final Set<VanillaCommand> vanillaCommands = new HashSet<VanillaCommand>();
    protected final CommandAbstract vanillaCommand;

    public VanillaCommandWrapper(CommandAbstract vanillaCommand) {
        super(vanillaCommand.c());
        this.vanillaCommand = vanillaCommand;
    }

    public VanillaCommandWrapper(CommandAbstract vanillaCommand, String usage) {
        super(vanillaCommand.c());
        this.vanillaCommand = vanillaCommand;
        this.description = "A Mojang provided command.";
        this.usageMessage = usage;
        this.setPermission("vanilla.command." + vanillaCommand.c());
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!testPermission(sender)) return true;
        ICommandListener icommandlistener = getListener(sender);
        try {
            vanillaCommand.b(icommandlistener, args);
            return true;
        } catch (ExceptionUsage exceptionusage) {
            return false;
        } catch (CommandException commandexception) {
            ChatMessage chatmessage = new ChatMessage(commandexception.getMessage(), commandexception.a());
            chatmessage.b().setColor(EnumChatFormat.RED);
            icommandlistener.sendMessage(chatmessage);
            return false;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        Validate.notNull(sender, "Sender cannot be null");
        Validate.notNull(args, "Arguments cannot be null");
        Validate.notNull(alias, "Alias cannot be null");
        return (List<String>) vanillaCommand.a(getListener(sender), args);
    }

    public int dispatchVanillaCommandBlock(CommandBlockListenerAbstract icommandlistener, String s) {
        // Copied from net.minecraft.server.CommandHandler
        s = s.trim();
        if (s.startsWith("/")) {
            s = s.substring(1);
        }
        String as[] = s.split(" ");
        as = dropFirstArgument(as);
        int i = getPlayerListSize(as);
        int j = 0;
        try {
            if (vanillaCommand.a(icommandlistener)) {
                if (i > -1) {
                    EntityPlayer aentityplayer[] = PlayerSelector.getPlayers(icommandlistener, as[i]);
                    String s2 = as[i];
                    EntityPlayer aentityplayer1[] = aentityplayer;
                    int k = aentityplayer1.length;
                    for (int l = 0; l < k;) {
                        EntityPlayer entityplayer = aentityplayer1[l];
                        as[i] = entityplayer.getName();
                        try {
                            vanillaCommand.b(icommandlistener, as);
                            j++;
                            continue;
                        } catch (CommandException commandexception1) {
                            ChatMessage chatmessage4 = new ChatMessage(commandexception1.getMessage(), commandexception1.a());
                            chatmessage4.b().setColor(EnumChatFormat.RED);
                            icommandlistener.sendMessage(chatmessage4);
                            l++;
                        }
                    }

                    as[i] = s2;
                } else {
                    vanillaCommand.b(icommandlistener, as);
                    j++;
                }
            } else {
                ChatMessage chatmessage = new ChatMessage("commands.generic.permission", new Object[0]);
                chatmessage.b().setColor(EnumChatFormat.RED);
                icommandlistener.sendMessage(chatmessage);
            }
        } catch (ExceptionUsage exceptionusage) {
            ChatMessage chatmessage1 = new ChatMessage("commands.generic.usage", new Object[] { new ChatMessage(exceptionusage.getMessage(), exceptionusage.a()) });
            chatmessage1.b().setColor(EnumChatFormat.RED);
            icommandlistener.sendMessage(chatmessage1);
        } catch (CommandException commandexception) {
            ChatMessage chatmessage2 = new ChatMessage(commandexception.getMessage(), commandexception.a());
            chatmessage2.b().setColor(EnumChatFormat.RED);
            icommandlistener.sendMessage(chatmessage2);
        } catch (Throwable throwable) {
            ChatMessage chatmessage3 = new ChatMessage("commands.generic.exception", new Object[0]);
            chatmessage3.b().setColor(EnumChatFormat.RED);
            icommandlistener.sendMessage(chatmessage3);
            if(icommandlistener instanceof TileEntityCommandListener) {
                TileEntityCommandListener listener = (TileEntityCommandListener) icommandlistener;
                MinecraftServer.av().log(Level.WARN, String.format("CommandBlock at (%d,%d,%d) failed to handle command", listener.getChunkCoordinates().x, listener.getChunkCoordinates().y, listener.getChunkCoordinates().z), throwable);
            } else if (icommandlistener instanceof EntityMinecartCommandBlockListener) {
                EntityMinecartCommandBlockListener listener = (EntityMinecartCommandBlockListener) icommandlistener;
                MinecraftServer.av().log(Level.WARN, String.format("MinecartCommandBlock at (%d,%d,%d) failed to handle command", listener.getChunkCoordinates().x, listener.getChunkCoordinates().y, listener.getChunkCoordinates().z), throwable);
            } else {
                MinecraftServer.av().log(Level.WARN, String.format("Unknown CommandBlock failed to handle command"), throwable);
            }
        }
        return j;
    }

    private ICommandListener getListener(CommandSender sender) {
        if (sender instanceof Player) {
            return ((CraftPlayer) sender).getHandle();
        }
        if (sender instanceof BlockCommandSender) {
            return ((CraftBlockCommandSender) sender).getTileEntity();
        }
        if (sender instanceof CommandMinecart) {
            return ((EntityMinecartCommandBlock) ((CraftMinecartCommand) sender).getHandle()).e();
        }
        if (sender instanceof RemoteConsoleCommandSender) {
            return RemoteControlCommandListener.instance;
        }
        if (sender instanceof ConsoleCommandSender) {
            return ((CraftServer) sender.getServer()).getServer();
        }
        return null;
    }

    private int getPlayerListSize(String as[]) {
        for (int i = 0; i < as.length; i++) {
            if (vanillaCommand.a(as, i) && PlayerSelector.isList(as[i])) {
                return i;
            }
        }
        return -1;
    }

    private String[] dropFirstArgument(String as[]) {
        String as1[] = new String[as.length - 1];
        for (int i = 1; i < as.length; i++) {
            as1[i - 1] = as[i];
        }

        return as1;
    }

    public static Set<VanillaCommand> buildCommands(ConfigurationSection section) {
        vanillaCommands.clear();
        vanillaCommands.add(buildCommand(section, new CommandAchievement(), new AchievementCommand(), "/achievement give <stat_name> [player]"));
        vanillaCommands.add(buildCommand(section, new CommandBan(), new BanCommand(), "/ban <playername> [reason]"));
        vanillaCommands.add(buildCommand(section, new CommandBanIp(), new BanIpCommand(), "/ban-ip <ip-address|playername>"));
        vanillaCommands.add(buildCommand(section, new CommandBanList(), new BanListCommand(), "/banlist [ips]"));
        vanillaCommands.add(buildCommand(section, new CommandBanList(), new BanListCommand(), "/banlist [ips]"));
        vanillaCommands.add(buildCommand(section, new CommandClear(), new ClearCommand(), "/clear <playername> [item] [metadata]"));
        vanillaCommands.add(buildCommand(section, new CommandGamemodeDefault(), new DefaultGameModeCommand(), "/defaultgamemode <mode>"));
        vanillaCommands.add(buildCommand(section, new CommandDeop(), new DeopCommand(), "/deop <playername>"));
        vanillaCommands.add(buildCommand(section, new CommandDifficulty(), new DifficultyCommand(), "/difficulty <new difficulty>"));
        vanillaCommands.add(buildCommand(section, new CommandEffect(), new EffectCommand(), "/effect <player> <effect|clear> [seconds] [amplifier]"));
        vanillaCommands.add(buildCommand(section, new CommandEnchant(), new EnchantCommand(), "/enchant <playername> <enchantment ID> [enchantment level]"));
        vanillaCommands.add(buildCommand(section, new CommandGamemode(), new GameModeCommand(), "/gamemode <mode> [player]"));
        vanillaCommands.add(buildCommand(section, new CommandGamerule(), new GameRuleCommand(), "/gamerule <rulename> [true|false]"));
        vanillaCommands.add(buildCommand(section, new CommandGive(), new GiveCommand(), "/give <playername> <item> [amount] [metadata] [dataTag]"));
        vanillaCommands.add(buildCommand(section, new CommandHelp(), new HelpCommand(), "/help [page|commandname]"));
        vanillaCommands.add(buildCommand(section, new CommandIdleTimeout(), new SetIdleTimeoutCommand(), "/setidletimeout <Minutes until kick>"));
        vanillaCommands.add(buildCommand(section, new CommandKick(), new KickCommand(), "/kick <playername> [reason]"));
        vanillaCommands.add(buildCommand(section, new CommandKill(), new KillCommand(), "/kill [playername]"));
        vanillaCommands.add(buildCommand(section, new CommandList(), new ListCommand(), "/list"));
        vanillaCommands.add(buildCommand(section, new CommandMe(), new MeCommand(), "/me <actiontext>"));
        vanillaCommands.add(buildCommand(section, new CommandOp(), new OpCommand(), "/op <playername>"));
        vanillaCommands.add(buildCommand(section, new CommandPardon(), new PardonCommand(), "/pardon <playername>"));
        vanillaCommands.add(buildCommand(section, new CommandPardonIP(), new PardonIpCommand(), "/pardon-ip <ip-address>"));
        vanillaCommands.add(buildCommand(section, new CommandPlaySound(), new PlaySoundCommand(), "/playsound <sound> <playername> [x] [y] [z] [volume] [pitch] [minimumVolume]"));
        vanillaCommands.add(buildCommand(section, new CommandSay(), new SayCommand(), "/say <message>"));
        vanillaCommands.add(buildCommand(section, new CommandScoreboard(), new ScoreboardCommand(), "/scoreboard"));
        vanillaCommands.add(buildCommand(section, new CommandSeed(), new SeedCommand(), "/seed"));
        vanillaCommands.add(buildCommand(section, new CommandSetBlock(), null, "/setblock <x> <y> <z> <tilename> [datavalue] [oldblockHandling] [dataTag]"));
        vanillaCommands.add(buildCommand(section, new CommandSetWorldSpawn(), new SetWorldSpawnCommand(), "/setworldspawn [x] [y] [z]"));
        vanillaCommands.add(buildCommand(section, new CommandSpawnpoint(), new SpawnpointCommand(), "/spawnpoint <playername> [x] [y] [z]"));
        vanillaCommands.add(buildCommand(section, new CommandSpreadPlayers(), new SpreadPlayersCommand(), "/spreadplayers <x> <z> [spreadDistance] [maxRange] [respectTeams] <playernames>"));
        vanillaCommands.add(buildCommand(section, new CommandSummon(), null, "/summon <EntityName> [x] [y] [z] [dataTag]"));
        vanillaCommands.add(buildCommand(section, new CommandTp(), new TeleportCommand(), "/tp [player] <target>\n/tp [player] <x> <y> <z>"));
        vanillaCommands.add(buildCommand(section, new CommandTell(), new TellCommand(), "/tell <playername> <message>"));
        vanillaCommands.add(buildCommand(section, new CommandTellRaw(), null, "/tellraw <playername> <raw message>"));
        vanillaCommands.add(buildCommand(section, new CommandTestFor(), new TestForCommand(), "/testfor <playername | selector> [dataTag]"));
        vanillaCommands.add(buildCommand(section, new CommandTestForBlock(), null, "/testforblock <x> <y> <z> <tilename> [datavalue] [dataTag]"));
        vanillaCommands.add(buildCommand(section, new CommandTime(), new TimeCommand(), "/time set <value>\n/time add <value>"));
        vanillaCommands.add(buildCommand(section, new CommandToggleDownfall(), new ToggleDownfallCommand(), "/toggledownfall"));
        vanillaCommands.add(buildCommand(section, new CommandWeather(), new WeatherCommand(), "/weather <clear/rain/thunder> [duration in seconds]"));
        vanillaCommands.add(buildCommand(section, new CommandWhitelist(), new WhitelistCommand(), "/whitelist (add|remove) <player>\n/whitelist (on|off|list|reload)"));
        vanillaCommands.add(buildCommand(section, new CommandXp(), new ExpCommand(), "/xp <amount> [player]\n/xp <amount>L [player]"));
        return vanillaCommands;
    }

    private static VanillaCommand buildCommand(ConfigurationSection section, CommandAbstract nmsCommand, VanillaCommand bukkitCommand, String vanillaUsage) {
        String type = section.getString(nmsCommand.c());
        if (type == null) {
            section.set(nmsCommand.c(), DEFAULT);
            type = DEFAULT;
        }
        if ((DEFAULT.equals(type) || BUKKIT.equals(type)) && bukkitCommand != null) {
            return bukkitCommand;
        } else if (COMMAND_BLOCK.equals(type) && bukkitCommand != null) {
            return new CommandBlockVanillaCommandWrapper(nmsCommand, bukkitCommand);
        } else {
            if (!MOJANG.equals(type)) {
                section.set(nmsCommand.c(), DEFAULT);
            }
            return new VanillaCommandWrapper(nmsCommand, vanillaUsage);
        }
    }
}
