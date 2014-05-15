package org.bukkit.craftbukkit.inventory;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple serializeable object with a public data map and list to test
 * ConfigurationSerialization.
 */
public class SerializeableObject implements ConfigurationSerializable
{
    {
        ConfigurationSerialization.registerClass(SerializeableObject.class);
    }

    public Map<String, Object> map = new HashMap<String, Object>();
    public List<Object> list = new ArrayList<Object>();

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("map", map);
        data.put("list", list);
        return data;
    }

    public SerializeableObject() {

    }

    public SerializeableObject(Map<String, Object> data) {
        // This is kind of a hack- I think ConfigurationSerialization ought to remove
        // this key when passing it in. But, it doesn't.
        // I'm guessing this isn't a problem for most objects since they look for specific keys,
        // but here we're lazily coping them all over.
        // Shallow copies, even.
        Map<String, Object> map = (Map<String, Object>)data.get("map");
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            if (key.equals(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) continue;
            this.map.put(key, entry.getValue());
        }
        // Shallow copies again
        List<Object> list = (List<Object>)data.get("list");
        if (list != null) {
            for (Object o : list) {
                this.list.add(o);
            }
        }
    }
}
