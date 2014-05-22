package org.bukkit.craftbukkit.util;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.NBTBase;
import net.minecraft.server.NBTTagByte;
import net.minecraft.server.NBTTagByteArray;
import net.minecraft.server.NBTTagCompound;
import net.minecraft.server.NBTTagDouble;
import net.minecraft.server.NBTTagFloat;
import net.minecraft.server.NBTTagInt;
import net.minecraft.server.NBTTagIntArray;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.NBTTagLong;
import net.minecraft.server.NBTTagShort;
import net.minecraft.server.NBTTagString;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.PersistentMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.NumberConversions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates any custom data that may be stored in an NBTTagCompound.
 * <p>
 * Handles serialization of ConfigurationSerialization objects and
 * basic data types to and from a persistent data store.
 * <p>
 * This is used by CraftMetaItem to implement the Metadatable interface
 * by mapping Plugin ownership to a standard tag structure.
 */
public class NBTMetadataStore implements Cloneable {
    // Theses are key strings used to store and check for custom data
    // Bukkit may store custom internal data under CUSTOM_DATA_KEY.
    // Custom Plugin data (from the Metadatable interface)
    // is stored under BUKKIT_DATA_KEY.PLUGIN_DATA_KEY
    public final static String BUKKIT_DATA_KEY = "bukkit";
    public final static String PLUGIN_DATA_KEY = "plugins";

    // This is copied from CraftMetaItem.SerializableMeta
    // It is needed for filtering, and I wasn't sure how to cleanly share
    // this without a lot of refactoring.
    static final String TYPE_FIELD = "meta-type";

    protected NBTTagCompound tag;

    /**
     * Returns a filtered store, such that it may not contain any of
     * the specified keys.
     * <p>
     * If there is no unfiltered data in the given tag, this method will
     * not create a new store, and will return null.
     *
     * @param tag The tag to scan for data
     * @param filterKeys A Set of keys to filter out
     * @return A new NBTMetadataStore containing a copy of the data in
     *   tag, minus what was filtered. If the end result is empty,
     *   null will be returned.
     */
    public static NBTMetadataStore getFilteredStore(NBTTagCompound tag, Set<String> filterKeys) {
        NBTTagCompound filteredTag = null;
        Set<String> keys = getAllKeys(tag);
        for (String key : keys) {
            if (filterKeys.contains(key)) {
                continue;
            }

            if (filteredTag == null) {
                filteredTag = new NBTTagCompound();
            }
            filteredTag.set(key, tag.get(key).clone());
        }
        return filteredTag == null ? null : new NBTMetadataStore(filteredTag);
    }

    /**
     * Returns a filtered store, such that it may not contain any of
     * the specified keys.
     * <p>
     * If there is no unfiltered data in the given Map, this method will
     * not create a new store, and will return null.
     *
     * @param dataMap The Ma[ to scan for data
     * @param filterKeys A Set of keys to filter out
     * @return A new NBTMetadataStore containing a copy of the data in
     *   dataMap, minus what was filtered. If the end result is empty,
     *   null will be returned.
     */
    public static NBTMetadataStore getFilteredStore(Map<String, Object> dataMap, Set<String> filterKeys) {
        NBTTagCompound filteredTag = null;
        Set<String> keys = dataMap.keySet();
        for (String key : keys) {
            if (filterKeys.contains(key)) {
                continue;
            }

            // Filter out special ConfigurationSerialization and
            // SerializeableMeta tags.
            // These seem like they shouldn't make it this far down the pipeline,
            // but it seems like they will be in the root after deserialization.
            if (key.equals(ConfigurationSerialization.SERIALIZED_TYPE_KEY) || key.equals(TYPE_FIELD)) {
                continue;
            }

            if (filteredTag == null) {
                filteredTag = new NBTTagCompound();
            }
            filteredTag.set(key, convert(dataMap.get(key)));
        }
        return filteredTag == null ? null : new NBTMetadataStore(filteredTag);
    }

    /**
     * Check to see if a tag has a specific key
     * registered to any Plugin.
     *
     * @param tag The tag to scan for data
     * @param key The key to check for
     * @return True if the tag has a non-empty
     *   BUKKIT_DATA_KEY.PLUGIN_DATA_KEY compound.
     */
    public static boolean hasPluginData(NBTTagCompound tag, String key) {
        NBTTagCompound bukkitRoot = tag.getCompound(BUKKIT_DATA_KEY);
        if (bukkitRoot == null) return false;
        NBTTagCompound pluginRoot = bukkitRoot.getCompound(PLUGIN_DATA_KEY);

        return pluginRoot != null && pluginRoot.hasKey(key);
    }

    /**
     * Check to see if a tag has any plugin data on it.
     *
     * @param tag The tag to scan for data
     * @param key The key to check for
     * @param owningPlugin The plugin to check for data
     * @return True if the tag has a non-empty
     *   BUKKIT_DATA_KEY.PLUGIN_DATA_KEY compound.
     */
    public static boolean hasPluginData(NBTTagCompound tag, String key, Plugin owningPlugin) {
        NBTTagCompound bukkitRoot = tag.getCompound(BUKKIT_DATA_KEY);
        if (bukkitRoot == null) return false;
        NBTTagCompound pluginRoot = bukkitRoot.getCompound(PLUGIN_DATA_KEY);
        if (pluginRoot == null || !pluginRoot.hasKey(key)) return false;
        NBTTagCompound dataRoot = pluginRoot.getCompound(key);
        String pluginName = owningPlugin.getName();
        return dataRoot != null && dataRoot.hasKey(pluginName);
    }

    /**
     * Create an empty data store.
     */
    public NBTMetadataStore() {
        this.tag = new NBTTagCompound();
    }

    /**
     * Wrap a data store around an existing NBTTagCompound.
     * <p>
     * It is expected that this tag will contain a structure
     * that follows the "<datakey>.<plugin> = <value>" format.
     *
     * @param tag The root of this datastore.
     */
    private NBTMetadataStore(NBTTagCompound tag) {
        this.tag = tag;
    }

    /**
     * Retrieve the NBTTagCompound that holds all of the Plugin metadata.
     *
     * @param create If True, the path to the root will be created if it does not exist.
     * @return The NBTTagCompound that holds this data
     */
    protected NBTTagCompound getPluginMetadataRoot(boolean create) {
        NBTTagCompound bukkitRoot = getBukkitDataRoot(create);
        NBTTagCompound pluginsRoot = bukkitRoot.getCompound(PLUGIN_DATA_KEY);
        if (create) {
            bukkitRoot.set(PLUGIN_DATA_KEY, pluginsRoot);
        }
        return pluginsRoot;
    }

    /**
     * Store a MetadataValue.
     * <p>
     * Throws an IllegalArgumentException if setting anything
     * other than a PersistentMetadataValue value.
     *
     * @param metadataKey The metadata key to store
     * @param newMetadataValue The value to store, must be PersistentMetadataValue
     */
    public void setPluginMetadata(String metadataKey, MetadataValue newMetadataValue) {
        if (!(newMetadataValue instanceof PersistentMetadataValue)) {
            throw new IllegalArgumentException("This store can only hold PersistentMetadataValue");
        }

        NBTTagCompound metadataRoot = getPluginMetadataRoot(true);
        NBTTagCompound dataTag = metadataRoot.getCompound(metadataKey);
        dataTag.set(newMetadataValue.getOwningPlugin().getName(), convert(newMetadataValue.value()));
        metadataRoot.set(metadataKey, dataTag);
    }

    /**
     * Retrieve all stored metadata for all plugins.
     *
     * @param metadataKey The metadata to look up
     * @return A List of values found, or an empty List.
     */
    public List<MetadataValue> getPluginMetadata(String metadataKey) {
        NBTTagCompound metadataRoot = getPluginMetadataRoot(false);
        if (!metadataRoot.hasKey(metadataKey)) {
            return Collections.emptyList();
        }

        PluginManager pm = Bukkit.getPluginManager();
        List<MetadataValue> metadata = new ArrayList<MetadataValue>();
        NBTTagCompound dataTag = metadataRoot.getCompound(metadataKey);
        Set<String> pluginKeys = getAllKeys(dataTag);
        for (String pluginKey : pluginKeys) {
            Plugin plugin = pm.getPlugin(pluginKey);
            if (plugin != null) {
                metadata.add(new PersistentMetadataValue(plugin, convert(dataTag.get(pluginKey))));
            }
        }
        return Collections.unmodifiableList(metadata);
    }

    /**
     * Retrieve a single key of stored metadata for a specific Plugin
     *
     * @param metadataKey The metadata to look up
     * @param owningPlugin The Plugin to look for data
     * @return A List of values found, or an empty List.
     */
    public MetadataValue getPluginMetadata(String metadataKey, Plugin owningPlugin) {
        NBTTagCompound metadataRoot = getPluginMetadataRoot(false);
        if (!metadataRoot.hasKey(metadataKey)) {
            return null;
        }
        NBTTagCompound dataTag = metadataRoot.getCompound(metadataKey);
        String pluginName = owningPlugin.getName();
        if (!dataTag.hasKey(pluginName)) {
            return null;
        }
        return new PersistentMetadataValue(owningPlugin, convert(dataTag.get(pluginName)));
    }

    /**
     * Check for existing metadata.
     *
     * @param metadataKey The key to check for
     * @return True if the key is present in this store
     */
    public boolean hasPluginMetadata(String metadataKey) {
        NBTTagCompound metadataRoot = getPluginMetadataRoot(false);
        return metadataRoot.hasKey(metadataKey);
    }

    /**
     * Check for existing metadata registered to a specific Plugin.
     *
     * @param metadataKey The key to remove
     * @param owningPlugin the plugin that owns the data
     */
    public boolean hasPluginMetadata(String metadataKey, Plugin owningPlugin) {
        NBTTagCompound metadataRoot = getPluginMetadataRoot(false);
        if (!metadataRoot.hasKey(metadataKey)) return false;

        NBTTagCompound dataTag = metadataRoot.getCompound(metadataKey);
        return dataTag.hasKey(owningPlugin.getName());
    }

    /**
     * Remove data from this store.
     *
     * @param metadataKey The key to remove
     * @param owningPlugin The Plugin that owns this data.
     */
    public void removePluginMetadata(String metadataKey, Plugin owningPlugin) {
        NBTTagCompound metadataRoot = getPluginMetadataRoot(false);
        if (!metadataRoot.hasKey(metadataKey)) return;

        NBTTagCompound dataTag = metadataRoot.getCompound(metadataKey);
        dataTag.remove(owningPlugin.getName());
        if (dataTag.isEmpty()) {
            metadataRoot.remove(metadataKey);
            if (metadataRoot.isEmpty()) {
                NBTTagCompound bukkitRoot = tag.getCompound(BUKKIT_DATA_KEY);
                bukkitRoot.remove(PLUGIN_DATA_KEY);
                if (bukkitRoot.isEmpty()) {
                    tag.remove(BUKKIT_DATA_KEY);
                }
            }
        }
    }

    /**
     * Retrieve the NBTTagCompound that holds all of the custom Bukkit data.
     *
     * @param create If True, the path to the root will be created if it does not exist.
     * @return The NBTTagCompound that holds this data
     */
    protected NBTTagCompound getBukkitDataRoot(boolean create) {
        NBTTagCompound bukkitRoot = tag.getCompound(BUKKIT_DATA_KEY);
        if (create) {
            tag.set(BUKKIT_DATA_KEY, bukkitRoot);
        }
        return bukkitRoot;
    }

    /**
     * Check for a specific key of custom Bukkit data in this store.
     *
     * @param key The key to look for
     * @return True if the key is in this store under the bukkit root.
     */
    public boolean hasBukkitData(String key) {
        NBTTagCompound bukkitRoot = getBukkitDataRoot(false);
        return bukkitRoot.hasKey(key);
    }

    /**
     * Store a raw Object in the custom Bukkit data store
     *
     * @param key The key to store
     * @param value The data to store
     */
    public void setBukkitData(String key, Object value) {
        NBTTagCompound bukkitRoot = getBukkitDataRoot(true);
        bukkitRoot.set(key, convert(value));
    }

    /**
     * Retrieve a raw Object stored in the Bukkit custom data.
     *
     * @param key The key to retrieve
     * @return The object, if it exists in the store, else null.
     */
    public Object getBukkitData(String key) {
        NBTTagCompound bukkitRoot = getBukkitDataRoot(false);
        return convert(bukkitRoot.get(key));
    }

    public int getBukkitDataAsInt(String key) {
        return NumberConversions.toInt(getBukkitData(key));
    }

    public float getBukkitDataAsFloat(String key) {
        return NumberConversions.toFloat(getBukkitData(key));
    }

    public double getBukkitDataAsDouble(String key) {
        return NumberConversions.toDouble(getBukkitData(key));
    }

    public long getBukkitDataAsLong(String key) {
        return NumberConversions.toLong(getBukkitData(key));
    }

    public short getBukkitDataAsShort(String key) {
        return NumberConversions.toShort(getBukkitData(key));
    }

    public byte getBukkitDataAsByte(String key) {
        return NumberConversions.toByte(getBukkitData(key));
    }

    public boolean getBukkitDataAsBoolean(String key) {
        Object value = getBukkitData(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }

        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }

        return value != null;
    }

    public String asString(String key) {
        Object value = getBukkitData(key);

        if (value == null) {
            return "";
        }
        return value.toString();
    }

    /**
     * Remove a specific key of Bukkit data from this store.
     *
     * @param key The key to remove
     */
    public void removeBukkitData(String key) {
        NBTTagCompound bukkitRoot = getBukkitDataRoot(false);
        bukkitRoot.remove(key);
        if (bukkitRoot.isEmpty()) {
            tag.remove(BUKKIT_DATA_KEY);
        }
    }

    /**
     * Apply the contents of this data store on top of an item tag.
     * <p>
     * This will overwrite any keys in the tag that are also in this
     * store.
     *
     * @param other The tag to write this data store to
     */
    public void applyToTag(NBTTagCompound other) {
        if (other == null) return;

        Set<String> keys = getAllKeys(tag);
        for (String key : keys) {
            other.set(key, tag.get(key).clone());
        }
    }

    /**
     * This will serialize all data into an ImmutableMap.Builder.
     *
     * @param builder An ImmutableMap builder to serialize this data store.
     * @return The same Map Builder
     */
    public ImmutableMap.Builder<String, Object> serialize(ImmutableMap.Builder<String, Object> builder) {
        Set<String> pluginKeys = getAllKeys(tag);
        for (String pluginKey : pluginKeys) {
            builder.put(pluginKey, convert(tag.get(pluginKey)));
        }
        return builder;
    }

    /**
     * Convert an NBTBase object to an object of the appropriate type for
     * inclusion in our data map.
     * <p>
     * This will convert a compound tag into either a Map (used for object
     * deserialization) or a ConfigurationSection.
     * <p>
     * It is not possible to store a Map directly.
     *
     * @param tag The tag to convert and store
     * @return The converted object, or null if nothing was stored
     */
    private static Object convert(NBTBase tag) {
        if (tag == null) return null;

        Object value = null;
        // This adds some extra reaching into NBT internals.
        if (tag instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound)tag;
            Collection<String> keys = getAllKeys(compound);

            // Check for Map, ConfigurationSection or SerliazebleObject creation
            boolean isSerializedObject = compound.hasKey(ConfigurationSerialization.SERIALIZED_TYPE_KEY);
            Map<String, Object> dataMap = new HashMap<String, Object>();
            for (String tagKey : keys) {
                dataMap.put(tagKey, convert(compound.get(tagKey)));
            }
            if (isSerializedObject) {
                try {
                    value = ConfigurationSerialization.deserializeObject(dataMap);
                    if (value == null) {
                        throw new IllegalArgumentException("Failed to deserialize object of class " + compound.get(ConfigurationSerialization.SERIALIZED_TYPE_KEY));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new IllegalArgumentException("Failed to deserialize object of class " + compound.get(ConfigurationSerialization.SERIALIZED_TYPE_KEY) + ", " + ex.getMessage());
                }
            } else {
                value = dataMap;
            }
        } else if (tag instanceof NBTTagString) {
            value = ((NBTTagString) tag).a_();
        } else if (tag instanceof NBTTagList) {
            NBTTagList list = (NBTTagList)tag;
            int tagSize = list.size();
            List<Object> convertedList = new ArrayList<Object>(tagSize);
            int listType = list.d();
            for (int i = 0; i < tagSize; i++) {
                // Convert to appropriate NBT object type
                Object listValue = null;
                switch (listType) {
                    case 10: // TagCompound
                        listValue = convert(list.get(i));
                        break;
                    case 1: // Byte
                    case 2: // Short
                    case 3: // Int
                    case 4: // Long
                        // I don't think this is going to work.
                        listValue = list.e(i);
                        break;
                    case 6: // Double
                    case 5: // Float
                        listValue = list.e(i);
                        break;
                    case 7: // Byte array
                        int[] intArray = list.c(i);
                        byte[] byteArray = new byte[intArray.length];
                        for (int arrayIndex = 0; arrayIndex < intArray.length; arrayIndex++) {
                            byteArray[arrayIndex] = (byte)intArray[arrayIndex];
                        }
                        listValue = byteArray;
                        break;
                    case 8: // String;
                        listValue = list.f(i);
                        break;
                    case 9: // List;
                        // We don't support nested lists.
                        listValue = null;
                        break;
                    case 11: // Int array
                        listValue = list.c(i);
                        break;
                }
                if (listValue != null) {
                    convertedList.add(listValue);
                }
            }
            value = convertedList;
        } else if (tag instanceof NBTTagDouble) {
            value = ((NBTTagDouble)tag).g();
        } else if (tag instanceof NBTTagInt) {
            value = ((NBTTagInt)tag).d();
        } else if (tag instanceof NBTTagLong) {
            value = ((NBTTagLong)tag).c();
        } else if (tag instanceof NBTTagFloat) {
            value = ((NBTTagFloat)tag).h();
        } else if (tag instanceof NBTTagByte) {
            value = ((NBTTagByte)tag).f();
        } else if (tag instanceof NBTTagShort) {
            return ((NBTTagShort)tag).e();
        } else if (tag instanceof NBTTagByteArray) {
            value = ((NBTTagByteArray)tag).c();
        } else if (tag instanceof NBTTagIntArray) {
            value = ((NBTTagIntArray)tag).c();
        }

        return value;
    }

    /**
     * Convert an object to an NBTBase object. This creates a copy of the
     * input and wraps it in the appropriate NBT class.
     *
     * @param value The value to copy and wrap
     * @return An NBTBase representation of the input
     */
    @SuppressWarnings("unchecked")
    private static NBTBase convert(Object value) {
        if (value == null) return null;

        NBTBase copiedValue = null;
        if (value instanceof Map) {
            NBTTagCompound subtag = new NBTTagCompound();
            applyToTag(subtag, (Map<String, Object>)value);
            copiedValue = subtag;
        } else if (value instanceof String) {
            copiedValue = new NBTTagString((String)value);
        } else if (value instanceof Integer) {
            copiedValue = new NBTTagInt((Integer)value);
        } else if (value instanceof Float) {
            copiedValue = new NBTTagFloat((Float)value);
        } else if (value instanceof Double) {
            copiedValue = new NBTTagDouble((Double)value);
        } else if (value instanceof Byte) {
            copiedValue = new NBTTagByte((Byte)value);
        } else if (value instanceof Boolean) {
            copiedValue = new NBTTagByte((Boolean)value ? (byte)1 : (byte)0);
        } else if (value instanceof Short) {
            copiedValue = new NBTTagShort((Short)value);
        } else if (value instanceof List) {
            NBTTagList tagList = new NBTTagList();
            List<Object> list = (List<Object>)value;
            for (Object listValue : list) {
                tagList.add(convert(listValue));
            }
            copiedValue = tagList;
        } else if (value.getClass().isArray()) {
            Class<?> arrayType = value.getClass().getComponentType();
            // I suppose you could convert Byte[], Integer[] here ... Long, Float, etc for that matter.
            if (arrayType == Byte.TYPE) {
                copiedValue = new NBTTagByteArray((byte[]) value);
            } else if (arrayType == Integer.TYPE) {
                copiedValue = new NBTTagIntArray((int[]) value);
            }
        } else if (value instanceof ConfigurationSerializable) {
            ConfigurationSerializable serializable = (ConfigurationSerializable)value;
            Map<String, Object> serializedMap = new HashMap<String, Object>();
            serializedMap.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(serializable.getClass()));
            serializedMap.putAll(serializable.serialize());
            NBTTagCompound subtag = new NBTTagCompound();
            applyToTag(subtag, serializedMap);
            copiedValue = subtag;
        } else {
            throw new IllegalArgumentException("Can't store objects of type " + value.getClass().getName());
        }

        return copiedValue;
    }

    /**
     * Return a raw copy of this store's data.
     *
     * @return A copy of this store's data.
     */
    public NBTTagCompound getTag()
    {
        return (NBTTagCompound)tag.clone();
    }

    /**
     * Apply a Map of data to an NBTTagCompound
     *
     * @param tag The tag for which to apply data.
     * @param data The data to apply
     */
    private static void applyToTag(NBTTagCompound tag, Map<String, Object> data) {
        if (tag == null || data == null) return;

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            NBTBase copiedValue = convert(entry.getValue());
            if (copiedValue != null) {
                tag.set(entry.getKey(), copiedValue);
            } else {
                tag.remove(entry.getKey());
            }
        }
    }

    /**
     * Retrieve all keys for a tag.
     * <p>
     * This is a simple wrapper for the obfuscated c() method
     *
     * @param tag The NBTTagCompound to list keys
     * @return A Set of keys from the tag, or null on null input.
     */
    @SuppressWarnings("unchecked")
    protected static Set<String> getAllKeys(NBTTagCompound tag) {
        if (tag == null) return null;
        // TODO: Deobfuscate c() and remove the wrapper?
        return tag.c();
    }

    @Override
    public boolean equals(Object other) {
        // This seems to work as expected in testing, though I am suspicious.
        return other instanceof NBTMetadataStore && ((NBTMetadataStore) other).tag.equals(this.tag);
    }

    @Override
    public int hashCode() {
        // TODO: Is this sufficient?
        return tag.hashCode();
    }

    @Override
    public Object clone() {
        return new NBTMetadataStore((NBTTagCompound)tag.clone());
    }

    public boolean isEmpty() {
        return tag.isEmpty();
    }
}
