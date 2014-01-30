package org.bukkit.craftbukkit.command;

import java.util.List;

import net.minecraft.server.CommandAbstract;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.VanillaCommand;

public final class CommandBlockVanillaCommandWrapper extends VanillaCommandWrapper {
    private VanillaCommand bukkitCommand;

    public CommandBlockVanillaCommandWrapper(CommandAbstract nmsCommand, VanillaCommand bukkitCommand) {
        super(nmsCommand);
        this.description = bukkitCommand.getDescription();
        this.usageMessage = bukkitCommand.getUsage();
        this.setPermission(bukkitCommand.getPermission());
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        return bukkitCommand.execute(sender, commandLabel, args);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        return bukkitCommand.tabComplete(sender, alias, args);
    }
}
