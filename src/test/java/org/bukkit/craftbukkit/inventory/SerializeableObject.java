package org.bukkit.craftbukkit.inventory;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple serializeable object with a public data map to test
 * ConfigurationSerialization.
 */
public class SerializeableObject implements ConfigurationSerializable
{
    {
        ConfigurationSerialization.registerClass(SerializeableObject.class);
    }

    public Map<String, Object> data = new HashMap<String, Object>();

    @Override
    public Map<String, Object> serialize() {
        return data;
    }

    public SerializeableObject() {

    }

    public SerializeableObject(Map<String, Object> data) {
        this.data = data;
    }
}
