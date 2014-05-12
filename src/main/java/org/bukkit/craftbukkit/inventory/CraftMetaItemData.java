package org.bukkit.craftbukkit.inventory;

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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates any custom data that may be attached to CraftMetaItem.
 */
public class CraftMetaItemData extends MemoryConfiguration {

    /**
     * Create a deep copy of another item's data
     *
     * @param other The data to copy
     */
    protected CraftMetaItemData(CraftMetaItemData other) {
        deepCopy(other.map, map);
    }

    /**
     * Create an empty item data object.
     */
    protected CraftMetaItemData() {
    }

    /**
     * Retrieve a CraftMetaItemData object for a given NBTTagCompound.
     *
     * This will scan the tag for any custom tags, those which are
     * not registered in ItemMetaKey, and copy them if present.
     *
     * If there are no custom tags, this will return null and
     * allocate no memory.
     *
     * @param tag The NBTTagCompound to search for custom data
     * @return A new CraftMetaItemData object, or null if no custom data was found.
     */
    protected static CraftMetaItemData getCustomData(NBTTagCompound tag) {
        Collection<String> customKeys = getCustomKeys(tag);
        if (customKeys == null) return null;

        CraftMetaItemData itemData = new CraftMetaItemData();
        copyFromItem(tag, itemData.map, customKeys);
        return itemData;
    }

    /**
     * Retrieve a CraftMetaItemData object for a given Map of data.
     *
     * This will scan the Map for any custom tags, those which are
     * not registered in ItemMetaKey, and copy them if present.
     *
     * If there are no custom tags, this will return null and
     * allocate no memory.
     *
     * @param map The Map to search for custom data
     * @return A new CraftMetaItemData object, or null if no custom data was found.
     */
    protected static CraftMetaItemData getCustomData(Map<String, Object> map) {
        Collection<String> customKeys = getCustomKeys(map);
        if (customKeys == null) return null;

        CraftMetaItemData itemData = new CraftMetaItemData();
        deepCopy(map, itemData.map, customKeys);

        return itemData;
    }

    /**
     * This will serialize all data into an ImmutableMap.Builder.
     *
     * TODO: Is there a way this can route through MemorySection instead?
     *
     * @param builder The Builder to put this ItemData into.
     * @return The same builder
     */
    ImmutableMap.Builder<String, Object> serialize(ImmutableMap.Builder<String, Object> builder) {
        for (Map.Entry<String, Object> extra : map.entrySet()) {
            // TODO: Test this with complex trees of data
            // Do I need to walk through this and make an
            // ImmutableMap.Builder for each Map in the data, and so on?
            builder.put(extra.getKey(), extra.getValue());
        }
        return builder;
    }

    /**
     * Apply this data to an Item's NBTTag data.
     *
     * @param itemTag The item data to apply our map to.
     */
    protected void applyToItem(NBTTagCompound itemTag) {
        applyToItem(itemTag, map);
    }

    /**
     * Retrieve all keys for a tag.
     *
     * This is a simple wrapper for the obfuscated c() method
     *
     * @param tag The NBTTagCompound to list keys
     * @return A Set of keys from the tag, or null on null input.
     */
    protected static Set<String> getAllKeys(NBTTagCompound tag) {
        if (tag == null) return null;
        // TODO: Deobfuscate this?
        return tag.c();
    }

    /**
     * Return a list of custom tags found on the specified NBTTag.
     *
     * If there are no tags or no custom tags, this will return null.
     *
     * @param tag The NBTTagCompound to search for custom keys
     * @return A Collection of custom keys, or null if none were found
     */
    protected static Collection<String> getCustomKeys(NBTTagCompound tag) {
        Set<String> keys = getAllKeys(tag);
        if (keys == null) return null;

        Collection<String> customKeys = null;
        for (String key : keys) {
            // Skip over auto-registered NBT tags
            if (CraftMetaItem.ItemMetaKey.NBT_TAGS.contains(key)) {
                continue;
            }
            if (customKeys == null) {
                customKeys = new ArrayList<String>();
            }
            customKeys.add(key);
        }

        return customKeys;
    }

    /**
     * Return a list of custom tags found in the specified Map.
     *
     * This filters out Bukkit tags (not NBT tags), they differ
     * in some cases- see ItemMetaKey for details.
     *
     * If there are no tags or no custom tags, this will return null.
     *
     * @param from The Map to search for custom keys
     * @return A Collection of custom keys, or null if none were found
     */
    @SuppressWarnings("unchecked")
    private static Collection<String> getCustomKeys(Map<String, Object> from) {
        if (from == null) return null;

        Collection<String> keys = null;
        for (Map.Entry<String, Object> entry : from.entrySet()) {
            String key = entry.getKey();
            // Skip over well-known tags, but only at the root level.
            // Also filter out some special-case identifiers.
            // This, admittedly, makes me feel like I've done something wrong- it seems like these
            // special class identifiers and other meta info shouldn't make it into the deserialized
            // map in the first place? However, they did in testing.
            if ((CraftMetaItem.ItemMetaKey.BUKKIT_TAGS.contains(key)
                    || key.equals("==")
                    || key.equals(CraftMetaItem.SerializableMeta.TYPE_FIELD))) {
                continue;
            }

            if (keys == null) {
                keys = new ArrayList<String>();
            }
            keys.add(key);
        }

        return keys;
    }

    /**
     * Copy data from an NBTTagCompound to a Map.
     *
     * @param from The Item data to look for custom data
     * @param data The Map to add data to, only custom data will be added
     * @param keys The specific keys to copy
     */
    protected static void copyFromItem(NBTTagCompound from, Map<String, Object> data, Collection<String> keys) {
        if (from == null || data == null || keys == null) return;

        for (String key : keys) {
            data.put(key, convert(from.get(key)));
        }
    }

    /**
     * Apply a Map of data to an item's NBTTag
     *
     * @param itemTag The tag for which to apply data.
     * @param data The data to apply
     */
    private static void applyToItem(NBTTagCompound itemTag, Map<String, Object> data) {
        if (itemTag == null || data == null) return;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            NBTBase copiedValue = convert(entry.getValue());
            if (copiedValue != null) {
                itemTag.set(entry.getKey(), copiedValue);
            } else {
                itemTag.remove(entry.getKey());
            }
        }
    }

    /**
     * Perform a deep copy of one Map to another.
     *
     * @param from The Map to copy from
     * @param to The Map to copy to
     */
    private static void deepCopy(Map<String, Object> from, Map<String, Object> to) {
        if (from == null || to == null) return;

        deepCopy(from, to, from.keySet());
    }

    /**
     * Perform a deep copy of one Map to another.
     *
     * @param from The Map to copy from
     * @param to The Map to copy to
     * @param keys The specific keys to copy, must be provided
     */
    private static void deepCopy(Map<String, Object> from, Map<String, Object> to, Collection<String> keys) {
        if (from == null || to == null || keys == null) return;

        for (String key : keys) {
            Object value = from.get(key);
            if (value != null) {
                if (value instanceof Map) {
                    Map<String, Object> originalMap = (Map<String, Object>)value;
                    value = new HashMap<String, Object>();
                    deepCopy(originalMap, (Map<String, Object>)value);
                } else if (value instanceof List) {
                    value = new ArrayList<Object>((List<Object>) value);
                } else if (value.getClass().isArray()) {
                    Object[] originalArray = (Object[])value;
                    Class arrayType = value.getClass().getComponentType();
                    value = (Object[])java.lang.reflect.Array.newInstance(arrayType, originalArray.length);
                    System.arraycopy(originalArray, 0, value, 0, originalArray.length);
                }
            }
            to.put(key, value);
        }
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
        if (value instanceof ConfigurationSection) {
            NBTTagCompound subtag = new NBTTagCompound();
            ConfigurationSection section = (ConfigurationSection)value;
            Collection<String> keys = section.getKeys(false);
            Map<String, Object> sectionMap = new HashMap<String, Object>(keys.size());
            for (String key : keys) {
                sectionMap.put(key, section.get(key));
            }
            applyToItem(subtag, sectionMap);
            copiedValue = subtag;
        } else if (value instanceof Map) {
            NBTTagCompound subtag = new NBTTagCompound();
            applyToItem(subtag, (Map<String, Object>)value);
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
        }

        return copiedValue;
    }

    /**
     * Convert an NBT tag to an object of the appropriate type
     *
     * @param tag The tag to convert
     * @return A copy of tag's data, or null on failure or bad input
     */
    private static Object convert(NBTBase tag) {
        if (tag == null) return null;

        //  I'll admit, this is getting pretty terrible.
        // Nothing a little extra deobfuscating can't fix, but
        // I understand not wanting to reach into all the extra NBT classes.
        // On the other hand, these are hopefully unlikely to change, yeah?
        if (tag instanceof NBTTagCompound) {
            return convert((NBTTagCompound)tag);
        } else if (tag instanceof NBTTagString) {
            return ((NBTTagString) tag).a_();
        } else if (tag instanceof NBTTagList) {
            NBTTagList list = (NBTTagList)tag;
            int tagSize = list.size();
            List<Object> convertedList = new ArrayList<Object>(tagSize);
            for (int i = 0; i < tagSize; i++) {
                convertedList.add(convert(list.get(i)));
            }
            return convertedList;
        } else if (tag instanceof NBTTagDouble) {
            return ((NBTTagDouble)tag).g();
        } else if (tag instanceof NBTTagInt) {
            return ((NBTTagInt)tag).d();
        } else if (tag instanceof NBTTagLong) {
            return ((NBTTagLong)tag).c();
        } else if (tag instanceof NBTTagFloat) {
            return ((NBTTagFloat)tag).h();
        } else if (tag instanceof NBTTagByte) {
            return ((NBTTagByte)tag).f();
        } else if (tag instanceof NBTTagShort) {
            return ((NBTTagShort)tag).e();
        } else if (tag instanceof NBTTagByteArray) {
            return ((NBTTagByteArray)tag).c();
        } else if (tag instanceof NBTTagIntArray) {
            return ((NBTTagIntArray)tag).c();
        }

        return null;
    }

    /**
     * Converts a compound tag into a Map.
     *
     * @param from The tag to convert
     * @return The converted Map, or null for a null or empty tag.
     */
    private static Map<String, Object> convert(NBTTagCompound from) {
        if (from == null ) return null;

        Set<String> keys = from.c();

        // Create on demand to save memory
        Map<String, Object> converted = null;

        for (String key : keys) {
            if (converted == null) {
                converted = new HashMap<String, Object>();
            }
            converted.put(key, convert(from.get(key)));
        }

        return converted;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof CraftMetaItemData ? ((CraftMetaItemData)other).map.equals(this.map) : false;
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
}
