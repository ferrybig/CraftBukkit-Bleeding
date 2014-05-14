package org.bukkit.craftbukkit.inventory;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.support.AbstractTestingBase;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ItemMetaDataTest extends AbstractTestingBase {
    private static final String TEST_STRING_VALUE = "MY_TEST string 123";
    private static final int TEST_INTEGER_VALUE = 12345;
    private static final double TEST_DOUBLE_VALUE = 12345.1234;

    private class LinkedObject
    {
        public LinkedObject next;

        public LinkedObject()
        {
            // Create a circular reference
            this.next = new LinkedObject(this);
        }

        public LinkedObject(LinkedObject next)
        {
            this.next = next;
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testAddInvalidData() {
        ItemStack testStack = new ItemStack(Material.STICK);
        testStack = CraftItemStack.asCraftCopy(testStack);
        ItemMeta itemMeta = testStack.getItemMeta();
        assertThat(itemMeta.hasCustomData(), is(false));

        LinkedObject objectInstance = new LinkedObject();
        ConfigurationSection customData = itemMeta.getCustomData();
        customData.set("test_object", objectInstance);

        testStack.setItemMeta(itemMeta);
    }


    @Test(expected=IllegalArgumentException.class)
    public void testOverrideBuiltinData() {
        ItemStack testStack = new ItemStack(Material.STICK);
        testStack = CraftItemStack.asCraftCopy(testStack);
        ItemMeta itemMeta = testStack.getItemMeta();
        assertThat(itemMeta.hasCustomData(), is(false));

        ConfigurationSection customData = itemMeta.getCustomData();
        customData.set("ench", "I'm going to mess up your enchants!");

        testStack.setItemMeta(itemMeta);
    }

    @Test
    public void testAddSerializeableData() {
        ItemStack testStack = new ItemStack(Material.STICK);
        testStack = CraftItemStack.asCraftCopy(testStack);
        ItemMeta itemMeta = testStack.getItemMeta();
        assertThat(itemMeta.hasCustomData(), is(false));

        SerializeableObject serializeable = new SerializeableObject();
        serializeable.data.put("Test-String", TEST_STRING_VALUE);
        serializeable.data.put("Test-Integer", TEST_INTEGER_VALUE);
        serializeable.data.put("Test-Double", TEST_DOUBLE_VALUE);
        ConfigurationSection customData = itemMeta.getCustomData();
        customData.set("test_serializeable", serializeable);

        assertThat(itemMeta.hasCustomData(), is(true));

        testStack.setItemMeta(itemMeta);

        ItemMeta newMeta = testStack.getItemMeta();
        assertThat(newMeta.hasCustomData(), is(true));
        ConfigurationSection newData = newMeta.getCustomData();
        assertThat(newData, is(not(nullValue())));
        assertThat(newData.contains("test_serializeable"), is(true));
        Object deserialized = newData.get("test_serializeable");
        assertThat(deserialized instanceof SerializeableObject, is(true));
        serializeable = (SerializeableObject)deserialized;

        // This is kind of a hack- I think ConfigurationSerialization ought to remove
        // this key when passing to deserialize. But, it doesn't.
        // I'm guessing this isn't a problem for most objects since they look for specific keys
        serializeable.data.remove(ConfigurationSerialization.SERIALIZED_TYPE_KEY);
        assertThat(serializeable.data.size(), is(3));
        assertThat(serializeable.data.get("Test-Integer") instanceof Integer, is(true));
        Integer intValue = (Integer)serializeable.data.get("Test-Integer");
        assertThat(intValue, is(TEST_INTEGER_VALUE));
        assertThat(serializeable.data.get("Test-Double") instanceof Double, is(true));
        Double doubleValue = (Double)serializeable.data.get("Test-Double");
        assertThat(doubleValue, is(TEST_DOUBLE_VALUE));
    }

    @Test
    public void testAddRemoveData() {
        ItemStack testStack = new ItemStack(Material.STICK);
        testStack = CraftItemStack.asCraftCopy(testStack);
        ItemMeta itemMeta = testStack.getItemMeta();
        assertThat(itemMeta.hasCustomData(), is(false));

        ConfigurationSection customData = itemMeta.getCustomData();
        customData.set("testing", TEST_STRING_VALUE);

        assertThat(itemMeta.hasCustomData(), is(true));

        testStack.setItemMeta(itemMeta);

        ItemMeta newMeta = testStack.getItemMeta();
        assertThat(newMeta.hasCustomData(), is(true));

        ConfigurationSection newData = newMeta.getCustomData();
        assertThat(newData, is(not(nullValue())));
        assertThat(newData.contains("testing"), is(true));
        assertThat(newData.getString("testing"), is(TEST_STRING_VALUE));

        newData.set("testing", null);
        testStack.setItemMeta(newMeta);

        ItemMeta cleanedMeta = testStack.getItemMeta();
        assertThat(cleanedMeta.hasCustomData(), is(false));
        ConfigurationSection cleanedData = cleanedMeta.getCustomData();
        assertThat(cleanedData, is(not(nullValue())));
        assertThat(cleanedData.contains("testing"), is(false));

        // Re-check that querying for data did not add data
        // Internally, the data object is now non-null but should be empty
        assertThat(cleanedMeta.hasCustomData(), is(false));
    }

    @Test
    public void testDataAndEnchants() {
        ItemStack testStack = new ItemStack(Material.DIAMOND_SWORD);
        testStack = CraftItemStack.asCraftCopy(testStack);
        ItemMeta itemMeta = testStack.getItemMeta();
        assertThat(itemMeta.hasCustomData(), is(false));
        assertThat(itemMeta.hasEnchants(), is(false));

        ConfigurationSection customData = itemMeta.getCustomData();
        customData.set("testing", TEST_STRING_VALUE);

        assertThat(itemMeta.hasCustomData(), is(true));
        assertThat(itemMeta.hasEnchants(), is(false));

        itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);

        assertThat(itemMeta.hasCustomData(), is(true));
        assertThat(itemMeta.hasEnchants(), is(true));

        testStack.setItemMeta(itemMeta);

        ItemMeta newMeta = testStack.getItemMeta();
        assertThat(newMeta.hasCustomData(), is(true));
        assertThat(newMeta.hasEnchants(), is(true));
        assertThat(newMeta.hasEnchant(Enchantment.DURABILITY), is(true));

        ConfigurationSection newData = newMeta.getCustomData();
        assertThat(newData, is(not(nullValue())));
        assertThat(newData.contains("testing"), is(true));
        assertThat(newData.getString("testing"), is(TEST_STRING_VALUE));

        newData.set("testing", null);
        testStack.setItemMeta(newMeta);

        ItemMeta cleanedMeta = testStack.getItemMeta();
        assertThat(cleanedMeta.hasCustomData(), is(false));
        ConfigurationSection cleanedData = cleanedMeta.getCustomData();
        assertThat(cleanedData, is(not(nullValue())));
        assertThat(cleanedData.contains("testing"), is(false));
        assertThat(cleanedMeta.hasCustomData(), is(false));
        assertThat(newMeta.hasEnchants(), is(true));
        assertThat(newMeta.hasEnchant(Enchantment.DURABILITY), is(true));
    }
}
