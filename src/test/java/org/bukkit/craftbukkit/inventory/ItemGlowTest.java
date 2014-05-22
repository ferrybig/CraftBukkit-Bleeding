package org.bukkit.craftbukkit.inventory;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.support.AbstractTestingBase;
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
        assertThat(itemMeta.hasGlowEffect(), is(true));
        itemMeta.setGlowEffect(false);
        assertThat(itemMeta.hasGlowEffect(), is(false));
        testStack.setItemMeta(itemMeta);
        newMeta = testStack.getItemMeta();
        assertThat(itemMeta.hasGlowEffect(), is(false));
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
        assertThat(itemMeta.hasGlowEffect(), is(false));
        assertThat(itemMeta.hasEnchants(), is(true));
        assertThat(itemMeta.hasEnchant(Enchantment.DURABILITY), is(true));

        itemMeta.removeEnchant(Enchantment.DURABILITY);
        assertThat(itemMeta.hasGlowEffect(), is(false));
        assertThat(itemMeta.hasEnchants(), is(false));
        assertThat(itemMeta.hasEnchant(Enchantment.DURABILITY), is(false));

        testStack.setItemMeta(itemMeta);
        newMeta = testStack.getItemMeta();
        assertThat(itemMeta.hasGlowEffect(), is(false));
        assertThat(itemMeta.hasEnchants(), is(false));
        assertThat(itemMeta.hasEnchant(Enchantment.DURABILITY), is(false));
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
        assertThat(itemMeta.hasGlowEffect(), is(true));
        assertThat(itemMeta.hasEnchants(), is(true));
        assertThat(itemMeta.hasEnchant(Enchantment.DURABILITY), is(true));

        itemMeta.removeEnchant(Enchantment.DURABILITY);
        assertThat(itemMeta.hasGlowEffect(), is(true));
        assertThat(itemMeta.hasEnchants(), is(false));
        assertThat(itemMeta.hasEnchant(Enchantment.DURABILITY), is(false));

        testStack.setItemMeta(itemMeta);
        newMeta = testStack.getItemMeta();
        assertThat(itemMeta.hasGlowEffect(), is(true));
        assertThat(itemMeta.hasEnchants(), is(false));
        assertThat(itemMeta.hasEnchant(Enchantment.DURABILITY), is(false));
    }
}
