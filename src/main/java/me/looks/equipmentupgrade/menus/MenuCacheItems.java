package me.looks.equipmentupgrade.menus;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public record MenuCacheItems(List<Integer> slots, ItemStack item, List<String> commands) {}