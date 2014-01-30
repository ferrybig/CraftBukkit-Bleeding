package org.bukkit.craftbukkit.command;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.CommandAbstract;
import net.minecraft.server.CommandAchievement;
import net.minecraft.server.CommandBan;
import net.minecraft.server.CommandBanIp;
import net.minecraft.server.CommandBanList;
import net.minecraft.server.CommandClear;
import net.minecraft.server.CommandDeop;
import net.minecraft.server.CommandDifficulty;
import net.minecraft.server.CommandEffect;
import net.minecraft.server.CommandEnchant;
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

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.defaults.*;
import org.bukkit.configuration.ConfigurationSection;

public class CraftCommandMap extends SimpleCommandMap {
    private static final String DEFAULT = "default";
    private static final String BUKKIT = "bukkit";
    private static final String MOJANG = "mojang";
    private static final String COMMAND_BLOCKS = "commandblocks";
    protected final Map<String, VanillaCommand> bukkitCommands;
    protected final Map<String, VanillaCommand> vanillaCommands;
    protected final Map<String, VanillaCommand> priorityCommands;
    protected final Map<String, VanillaCommandWrapper> commandBlockCommands;

    public CraftCommandMap(final Server server, ConfigurationSection section) {
        super(server);
        this.bukkitCommands = new HashMap<String, VanillaCommand>();
        this.vanillaCommands = new HashMap<String, VanillaCommand>();
        this.priorityCommands = new HashMap<String, VanillaCommand>();
        this.commandBlockCommands = new HashMap<String, VanillaCommandWrapper>();
        buildVanillaCommands(section);
    }

    @Override
    protected void setDefaultCommands() {
        register("bukkit", new SaveCommand());
        register("bukkit", new SaveOnCommand());
        register("bukkit", new SaveOffCommand());
        register("bukkit", new StopCommand());
        register("bukkit", new VersionCommand("version"));
        register("bukkit", new ReloadCommand("reload"));
        register("bukkit", new PluginsCommand("plugins"));
        register("bukkit", new TimingsCommand("timings"));
    }

    public void buildVanillaCommands(ConfigurationSection section) {
        fallbackCommands.clear();
        vanillaCommands.clear();
        bukkitCommands.clear();
        priorityCommands.clear();
        commandBlockCommands.clear();
        buildCommand(section, new CommandAchievement(), new AchievementCommand(), "/achievement give <stat_name> [player]");
        buildCommand(section, new CommandBan(), new BanCommand(), "/ban <playername> [reason]");
        buildCommand(section, new CommandBanIp(), new BanIpCommand(), "/ban-ip <ip-address|playername>");
        buildCommand(section, new CommandBanList(), new BanListCommand(), "/banlist [ips]");
        buildCommand(section, new CommandBanList(), new BanListCommand(), "/banlist [ips]");
        buildCommand(section, new CommandClear(), new ClearCommand(), "/clear <playername> [item] [metadata]");
        buildCommand(section, new CommandGamemodeDefault(), new DefaultGameModeCommand(), "/defaultgamemode <mode>");
        buildCommand(section, new CommandDeop(), new DeopCommand(), "/deop <playername>");
        buildCommand(section, new CommandDifficulty(), new DifficultyCommand(), "/difficulty <new difficulty>");
        buildCommand(section, new CommandEffect(), new EffectCommand(), "/effect <player> <effect|clear> [seconds] [amplifier]");
        buildCommand(section, new CommandEnchant(), new EnchantCommand(), "/enchant <playername> <enchantment ID> [enchantment level]");
        buildCommand(section, new CommandGamemode(), new GameModeCommand(), "/gamemode <mode> [player]");
        buildCommand(section, new CommandGamerule(), new GameRuleCommand(), "/gamerule <rulename> [true|false]");
        buildCommand(section, new CommandGive(), new GiveCommand(), "/give <playername> <item> [amount] [metadata] [dataTag]");
        buildCommand(section, new CommandHelp(), new HelpCommand(), "/help [page|commandname]");
        buildCommand(section, new CommandIdleTimeout(), new SetIdleTimeoutCommand(), "/setidletimeout <Minutes until kick>");
        buildCommand(section, new CommandKick(), new KickCommand(), "/kick <playername> [reason]");
        buildCommand(section, new CommandKill(), new KillCommand(), "/kill [playername]");
        buildCommand(section, new CommandList(), new ListCommand(), "/list");
        buildCommand(section, new CommandMe(), new MeCommand(), "/me <actiontext>");
        buildCommand(section, new CommandOp(), new OpCommand(), "/op <playername>");
        buildCommand(section, new CommandPardon(), new PardonCommand(), "/pardon <playername>");
        buildCommand(section, new CommandPardonIP(), new PardonIpCommand(), "/pardon-ip <ip-address>");
        buildCommand(section, new CommandPlaySound(), new PlaySoundCommand(), "/playsound <sound> <playername> [x] [y] [z] [volume] [pitch] [minimumVolume]");
        buildCommand(section, new CommandSay(), new SayCommand(), "/say <message>");
        buildCommand(section, new CommandScoreboard(), new ScoreboardCommand(), "/scoreboard");
        buildCommand(section, new CommandSeed(), new SeedCommand(), "/seed");
        buildCommand(section, new CommandSetBlock(), null, "/setblock <x> <y> <z> <tilename> [datavalue] [oldblockHandling] [dataTag]");
        buildCommand(section, new CommandSetWorldSpawn(), new SetWorldSpawnCommand(), "/setworldspawn [x] [y] [z]");
        buildCommand(section, new CommandSpawnpoint(), new SpawnpointCommand(), "/spawnpoint <playername> [x] [y] [z]");
        buildCommand(section, new CommandSpreadPlayers(), new SpreadPlayersCommand(), "/spreadplayers <x> <z> [spreadDistance] [maxRange] [respectTeams] <playernames>");
        buildCommand(section, new CommandSummon(), null, "/summon <EntityName> [x] [y] [z] [dataTag]");
        buildCommand(section, new CommandTp(), new TeleportCommand(), "/tp [player] <target>\n/tp [player] <x> <y> <z>");
        buildCommand(section, new CommandTell(), new TellCommand(), "/tell <playername> <message>");
        buildCommand(section, new CommandTellRaw(), null, "/tellraw <playername> <raw message>");
        buildCommand(section, new CommandTestFor(), new TestForCommand(), "/testfor <playername | selector> [dataTag]");
        buildCommand(section, new CommandTestForBlock(), null, "/testforblock <x> <y> <z> <tilename> [datavalue] [dataTag]");
        buildCommand(section, new CommandTime(), new TimeCommand(), "/time set <value>\n/time add <value>");
        buildCommand(section, new CommandToggleDownfall(), new ToggleDownfallCommand(), "/toggledownfall");
        buildCommand(section, new CommandWeather(), new WeatherCommand(), "/weather <clear/rain/thunder> [duration in seconds]");
        buildCommand(section, new CommandWhitelist(), new WhitelistCommand(), "/whitelist (add|remove) <player>\n/whitelist (on|off|list|reload)");
        buildCommand(section, new CommandXp(), new ExpCommand(), "/xp <amount> [player]\n/xp <amount>L [player]");
    }

    @Override
    protected Command getFallback(String label) {
        Command command = bukkitCommands.get(label);
        if (command != null) {
            return command;
        }
        command = vanillaCommands.get(label);
        if (command != null) {
            return command;
        }
        return super.getFallback(label);
    }

    @Override
    public Command getCommand(String name) {
        Command command = priorityCommands.get(name);
        if (command != null) {
            return command;
        }
        return super.getCommand(name);
    }

    private void buildCommand(ConfigurationSection section, CommandAbstract nmsCommand, VanillaCommand bukkitCommand, String vanillaUsage) {
        String type = section.getString(nmsCommand.c());
        if (type == null) {
            section.set(nmsCommand.c(), DEFAULT);
            type = DEFAULT;
        }
        VanillaCommand vanillaCommand = new VanillaCommandWrapper(nmsCommand, vanillaUsage);
        vanillaCommands.put("minecraft:" + vanillaCommand.getName(), vanillaCommand);
        if (bukkitCommand != null) {
            bukkitCommands.put("bukkit:" + vanillaCommand.getName(),bukkitCommand);
            if (DEFAULT.equals(type)) {
                fallbackCommands.add(bukkitCommand);
                return;
            }
            if (BUKKIT.equals(type)) {
                fallbackCommands.add(bukkitCommand);
                priorityCommands.put(bukkitCommand.getName().toLowerCase(), bukkitCommand);
                return;
            }
            if (COMMAND_BLOCKS.equals(type)) {
                fallbackCommands.add(bukkitCommand);
                commandBlockCommands.put(nmsCommand.c(), (VanillaCommandWrapper) vanillaCommand);
                return;
            }
        }

        if (COMMAND_BLOCKS.equals(type)) {
            commandBlockCommands.put(nmsCommand.c(), (VanillaCommandWrapper) vanillaCommand);
        } else if (!MOJANG.equals(type) && !BUKKIT.equals(type)) {
            section.set(nmsCommand.c(), DEFAULT);
        } else {
            priorityCommands.put(vanillaCommand.getName().toLowerCase(), vanillaCommand);
        }

        fallbackCommands.add(vanillaCommand);
        return;
    }

    public VanillaCommandWrapper getCommandBlockCommand(String key) {
        return commandBlockCommands.get(key.toLowerCase());
    }
}
