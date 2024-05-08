package me.looks.equipmentupgrade.utils;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {

    public static ItemStack createItemStack(ConfigurationSection section, String key){
        String material = section.getString(key + "material", "STONE");

        ItemStack item = new ItemStack(Material.valueOf(material.toUpperCase()));
        ItemMeta meta = item.getItemMeta();

        String displayName = section.getString(key + "name", "");
        if (!displayName.isEmpty()) {
            meta.setDisplayName(ColorUtil.color(displayName));
        }
        List<String> lore = new ArrayList<>();
        section.getStringList(key + "lore").forEach(str -> lore.add(ColorUtil.color(str)));
        meta.setLore(lore);

        boolean glow = section.getBoolean(key + "glow");
        if (glow){
            meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
        }
        boolean hide = section.getBoolean(key + "hide");
        if (!section.contains(key + "hide") || hide){
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE,
                    ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_DYE, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS);
        }
        item.setItemMeta(meta);
        return item;
    }

}
