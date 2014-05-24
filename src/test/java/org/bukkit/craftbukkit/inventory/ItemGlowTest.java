package org.bukkit.craftbukkit.inventory;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.PersistentMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.support.AbstractTestingBase;
import org.bukkit.support.DummyPlugin;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class ItemGlowTest extends AbstractTestingBase {

    @Test
    public void testAddRemoveGlow() {
        ItemStack testStack = new ItemStack(Material.STICK);
        testStack = CraftItemStack.asCraftCopy(testStack);
        ItemMeta itemMeta = testStack.getItemMeta();
        assertThat(itemMeta.hasGlowEffect(), is(false));
        itemMeta.setGlowEffect(true);
        assertThat(itemMeta.hasGlowEffect(), is(true));
        testStack.setItemMeta(itemMeta);

        ItemMeta newMeta = testStack.getItemMeta();
        assertThat(newMeta.hasGlowEffect(), is(true));
        newMeta.setGlowEffect(false);
        assertThat(newMeta.hasGlowEffect(), is(false));
        testStack.setItemMeta(newMeta);
        newMeta = testStack.getItemMeta();
        assertThat(newMeta.hasGlowEffect(), is(false));
    }

    @Test
    public void testAddRemoveEnchants() {
        ItemStack testStack = new ItemStack(Material.DIAMOND_SWORD);
        testStack = CraftItemStack.asCraftCopy(testStack);
        ItemMeta itemMeta = testStack.getItemMeta();
        itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);

        // Note that a glow effect flag only reacts to custom glow being
        // set, not enchantment glow.
        assertThat(itemMeta.hasGlowEffect(), is(false));
        testStack.setItemMeta(itemMeta);

        ItemMeta newMeta = testStack.getItemMeta();
        assertThat(newMeta.hasGlowEffect(), is(false));
        assertThat(newMeta.hasEnchants(), is(true));
        assertThat(newMeta.hasEnchant(Enchantment.DURABILITY), is(true));

        newMeta.removeEnchant(Enchantment.DURABILITY);
        assertThat(newMeta.hasGlowEffect(), is(false));
        assertThat(newMeta.hasEnchants(), is(false));
        assertThat(newMeta.hasEnchant(Enchantment.DURABILITY), is(false));

        testStack.setItemMeta(newMeta);
        newMeta = testStack.getItemMeta();
        assertThat(newMeta.hasGlowEffect(), is(false));
        assertThat(newMeta.hasEnchants(), is(false));
        assertThat(newMeta.hasEnchant(Enchantment.DURABILITY), is(false));
    }

    @Test
    public void testAddRemoveGlowAndEnchant() {
        ItemStack testStack = new ItemStack(Material.DIAMOND_SWORD);
        testStack = CraftItemStack.asCraftCopy(testStack);
        ItemMeta itemMeta = testStack.getItemMeta();
        itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
        assertThat(itemMeta.hasGlowEffect(), is(false));
        itemMeta.setGlowEffect(true);
        assertThat(itemMeta.hasGlowEffect(), is(true));
        testStack.setItemMeta(itemMeta);

        ItemMeta newMeta = testStack.getItemMeta();
        assertThat(newMeta.hasGlowEffect(), is(true));
        assertThat(newMeta.hasEnchants(), is(true));
        assertThat(newMeta.hasEnchant(Enchantment.DURABILITY), is(true));

        newMeta.removeEnchant(Enchantment.DURABILITY);
        assertThat(newMeta.hasGlowEffect(), is(true));
        assertThat(newMeta.hasEnchants(), is(false));
        assertThat(newMeta.hasEnchant(Enchantment.DURABILITY), is(false));

        testStack.setItemMeta(newMeta);
        newMeta = testStack.getItemMeta();
        assertThat(newMeta.hasGlowEffect(), is(true));
        assertThat(newMeta.hasEnchants(), is(false));
        assertThat(newMeta.hasEnchant(Enchantment.DURABILITY), is(false));
    }

    @Test
    public void testAddRemoveGlowAndMetadata() {
        Plugin pluginX = new DummyPlugin("pluginx");

        ItemStack testStack = new ItemStack(Material.DIAMOND_SWORD);
        testStack = CraftItemStack.asCraftCopy(testStack);
        ItemMeta itemMeta = testStack.getItemMeta();
        itemMeta.setMetadata("testing", new PersistentMetadataValue(pluginX, "foo"));
        assertThat(itemMeta.hasMetadata(), is(true));
        assertThat(itemMeta.hasGlowEffect(), is(false));
        itemMeta.setGlowEffect(true);
        assertThat(itemMeta.hasGlowEffect(), is(true));
        testStack.setItemMeta(itemMeta);

        ItemMeta newMeta = testStack.getItemMeta();
        assertThat(newMeta.hasGlowEffect(), is(true));
        assertThat(newMeta.hasMetadata(), is(true));

        newMeta.removeMetadata("testing", pluginX);
        assertThat(newMeta.hasGlowEffect(), is(true));

        // Note that "glow" counts as metadata.
        // This, admittedly, feels a little inconsistent,
        // and causes me to consider removing "hasMetadata()" from the
        // API.
        assertThat(newMeta.hasMetadata(), is(true));

        testStack.setItemMeta(newMeta);
        newMeta = testStack.getItemMeta();
        assertThat(newMeta.hasGlowEffect(), is(true));
        assertThat(newMeta.hasMetadata(), is(true));
    }
}
