package org.bukkit.craftbukkit.inventory;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.support.AbstractTestingBase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
    public void testNestedWellKnownTags() {
        ItemStack testStack = new ItemStack(Material.STICK);
        testStack = CraftItemStack.asCraftCopy(testStack);
        ItemMeta itemMeta = testStack.getItemMeta();
        assertThat(itemMeta.hasCustomData(), is(false));

        // This is a special test case that fires if the safety checks
        // are improperly looking at nested data
        // Adding "myplugin.value.id" or "myplugin.id" is fine,
        // even though "id" is not.
        ConfigurationSection customData = itemMeta.getCustomData();
        ConfigurationSection subSection = customData.createSection("test_sub_section");
        subSection.set("id", TEST_STRING_VALUE);
        testStack.setItemMeta(itemMeta);
    }

    @Test
    public void testAddSerializeableData() {

        ItemStack testStoredStack = new ItemStack(Material.DIAMOND_SWORD);
        testStoredStack = CraftItemStack.asCraftCopy(testStoredStack);
        ItemMeta itemMeta = testStoredStack.getItemMeta();
        itemMeta.setDisplayName(TEST_STRING_VALUE);
        List<String> lore = new ArrayList<String>();
        lore.add(TEST_STRING_VALUE + "_1");
        lore.add(TEST_STRING_VALUE + "_2");
        itemMeta.setLore(lore);
        itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
        itemMeta.getCustomData().set("testing_embedded_tag", TEST_STRING_VALUE);
        testStoredStack.setItemMeta(itemMeta);

        ItemStack testStack = new ItemStack(Material.STICK);
        testStack = CraftItemStack.asCraftCopy(testStack);
        itemMeta = testStack.getItemMeta();
        assertThat(itemMeta.hasCustomData(), is(false));

        SerializeableObject serializeable = new SerializeableObject();
        serializeable.map.put("Test-String", TEST_STRING_VALUE);
        serializeable.map.put("Test-Integer", TEST_INTEGER_VALUE);
        serializeable.map.put("Test-Double", TEST_DOUBLE_VALUE);
        serializeable.map.put("Test-ItemStack", testStoredStack);
        ConfigurationSection customData = itemMeta.getCustomData();
        customData.set("test_serializeable", serializeable);

        assertThat(itemMeta.hasCustomData(), is(true));

        testStack.setItemMeta(itemMeta);

        ItemMeta newMeta = testStack.getItemMeta();
        assertThat(newMeta.hasCustomData(), is(true));
        ConfigurationSection newData = newMeta.getCustomData();
        assertThat(newData, is(not(nullValue())));
        assertThat(newData.contains("test_serializeable"), is(true));
        Object testObject = newData.get("test_serializeable");
        assertThat(testObject instanceof SerializeableObject, is(true));
        SerializeableObject deserialized = (SerializeableObject)testObject;
        assertThat(deserialized.map.size(), is(serializeable.map.size()));
        assertThat(deserialized.map.get("Test-Integer") instanceof Integer, is(true));
        Integer intValue = (Integer)deserialized.map.get("Test-Integer");
        assertThat(intValue, is(TEST_INTEGER_VALUE));
        assertThat(deserialized.map.get("Test-Double") instanceof Double, is(true));
        Double doubleValue = (Double)deserialized.map.get("Test-Double");
        assertThat(doubleValue, is(TEST_DOUBLE_VALUE));
        Object deserializedItem = deserialized.map.get("Test-ItemStack");
        assertThat(deserializedItem instanceof ItemStack, is(true));
        ItemStack deserializedItemStack = (ItemStack)deserializedItem;
        assertThat(deserializedItemStack.getType(), is(Material.DIAMOND_SWORD));
        ItemMeta deserializedMeta = deserializedItemStack.getItemMeta();
        assertThat(deserializedMeta.hasCustomData(), is(true));
        assertThat(deserializedMeta.hasEnchants(), is(true));
    }

    @Test
    public void testSerializeObject() {
        SerializeableObject object = new SerializeableObject();
        List<ItemStack> items = new ArrayList<ItemStack>();
        final int ITEM_COUNT = 64;
        for (int i = 0; i < ITEM_COUNT; i++) {
            ItemStack testStack = new ItemStack(Material.DIAMOND_SWORD);
            testStack = CraftItemStack.asCraftCopy(testStack);
            ItemMeta itemMeta = testStack.getItemMeta();
            ConfigurationSection customData = itemMeta.getCustomData();
            customData.set("testing", TEST_STRING_VALUE);
            itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
            testStack.setItemMeta(itemMeta);

            items.add(testStack);
            object.list.add(testStack.clone());
        }
        object.map.put("items", items);
        object.map.put("item_count", items.size());

        List<String> players = new ArrayList<String>();
        players.add("One Player");
        players.add("Two Player");
        object.map.put("players", players);

        YamlConfiguration yamlConfig = new YamlConfiguration();
        yamlConfig.set("test_object", object);
        String testString = yamlConfig.saveToString();
        assertThat(testString, is(not(isEmptyString())));

        YamlConfiguration loadConfig = new YamlConfiguration();
        try {
            loadConfig.loadFromString(testString);
        } catch (Throwable ex) {
            throw new RuntimeException(testString, ex);
        }

        Object loadedObject = loadConfig.get("test_object");
        assertThat(loadedObject, is(not(nullValue())));
        assertThat(loadedObject instanceof SerializeableObject, is(true));

        SerializeableObject deserialized = (SerializeableObject)loadedObject;

        assertThat(deserialized.map.size(), is(object.map.size()));
        assertThat(deserialized.list.size(), is(object.list.size()));
        assertThat(deserialized.map.containsKey("items"), is(true));
        assertThat(deserialized.map.containsKey("item_count"), is(true));
        assertThat(deserialized.map.containsKey("players"), is(true));
        assertThat(deserialized.map.get("item_count"), is(object.map.get("item_count")));
        assertThat(deserialized.map.get("players"), is(object.map.get("players")));
        assertThat(deserialized.map.get("items"), is(object.map.get("items")));

        Object testList = deserialized.map.get("items");
        assertThat(testList instanceof List, is(true));

        List<ItemStack> itemList = (List<ItemStack>)testList;
        assertThat(itemList.size(), is(ITEM_COUNT));
    }

    @Test
    public void testSerializeItemStack() {
        ItemStack testStack = new ItemStack(Material.DIAMOND_SWORD);
        testStack = CraftItemStack.asCraftCopy(testStack);
        ItemMeta itemMeta = testStack.getItemMeta();
        ConfigurationSection customData = itemMeta.getCustomData();
        customData.set("testing", TEST_STRING_VALUE);
        itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
        testStack.setItemMeta(itemMeta);

        YamlConfiguration yamlConfig = new YamlConfiguration();
        yamlConfig.set("item", testStack);
        String testString = yamlConfig.saveToString();
        assertThat(testString, is(not(isEmptyString())));

        YamlConfiguration loadConfig = new YamlConfiguration();
        try {
            loadConfig.loadFromString(testString);
        } catch (Throwable ex) {
            throw new RuntimeException(testString, ex);
        }

        ItemStack deserializedItem = yamlConfig.getItemStack("item");
        assertThat(deserializedItem, is(not(nullValue())));
        assertThat(itemMeta.hasCustomData(), is(true));
        assertThat(itemMeta.hasEnchants(), is(true));
        assertThat(itemMeta.getCustomData().getString("testing"), is(TEST_STRING_VALUE));
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
