package me.looks.equipmentupgrade.menus;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public record MenuCache(String title, int size, HashMap<ItemStack, List<Integer>> cacheItems) {}