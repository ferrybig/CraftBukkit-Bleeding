package org.bukkit.support;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.*;

import java.io.File;
import java.util.*;

/**
 * Minimally implements the PluginManager interface using
 * DummyPlugin instances.
 * <p>
 * This facilitates testing PersistentMetadataValue loading,
 * which requires being able to look up a Plugin by name.
 * <p>
 * This class is a Singleton and should be referenced via
 * instance().
 */
public class DummyPluginManager implements PluginManager {
    private static DummyPluginManager instance;
    private Map<String, DummyPlugin> plugins = new HashMap<String, DummyPlugin>();

    public static DummyPluginManager instance() {
        if (instance == null) {
            instance = new DummyPluginManager();
        }

        return instance;
    }

    private DummyPluginManager() {
    }

    protected void registerPlugin(DummyPlugin plugin) {
        plugins.put(plugin.getName(), plugin);
    }

    @Override
    public void registerInterface(Class<? extends PluginLoader> aClass) throws IllegalArgumentException {

    }

    @Override
    public Plugin getPlugin(String s) {
        return plugins.get(s);
    }

    @Override
    public Plugin[] getPlugins() {
        return (new ArrayList<Plugin>(plugins.values())).toArray(new Plugin[0]);
    }

    @Override
    public boolean isPluginEnabled(String s) {
        return isPluginEnabled(getPlugin(s));
    }

    @Override
    public boolean isPluginEnabled(Plugin plugin) {
        return plugin != null && plugin.isEnabled();
    }

    @Override
    public Plugin loadPlugin(File file) throws InvalidPluginException, InvalidDescriptionException, UnknownDependencyException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Plugin[] loadPlugins(File file) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void disablePlugins() {
        for (Plugin plugin : plugins.values()) {
            plugin.onDisable();
        }
    }

    @Override
    public void clearPlugins() {
        plugins.clear();
    }

    @Override
    public void callEvent(Event event) throws IllegalStateException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void registerEvents(Listener listener, Plugin plugin) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void registerEvent(Class<? extends Event> aClass, Listener listener, EventPriority eventPriority, EventExecutor eventExecutor, Plugin plugin) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void registerEvent(Class<? extends Event> aClass, Listener listener, EventPriority eventPriority, EventExecutor eventExecutor, Plugin plugin, boolean b) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void enablePlugin(Plugin plugin) {
        if (plugin != null) plugin.onEnable();
    }

    @Override
    public void disablePlugin(Plugin plugin) {
        if (plugin != null) plugin.onDisable();
    }

    @Override
    public Permission getPermission(String s) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void addPermission(Permission permission) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void removePermission(Permission permission) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void removePermission(String s) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Set<Permission> getDefaultPermissions(boolean b) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void recalculatePermissionDefaults(Permission permission) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void subscribeToPermission(String s, Permissible permissible) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void unsubscribeFromPermission(String s, Permissible permissible) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Set<Permissible> getPermissionSubscriptions(String s) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void subscribeToDefaultPerms(boolean b, Permissible permissible) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void unsubscribeFromDefaultPerms(boolean b, Permissible permissible) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Set<Permissible> getDefaultPermSubscriptions(boolean b) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Set<Permission> getPermissions() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean useTimings() {
        return false;
    }
}
