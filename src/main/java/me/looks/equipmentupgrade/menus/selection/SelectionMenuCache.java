package me.looks.equipmentupgrade.menus.selection;

import me.looks.equipmentupgrade.menus.MenuCache;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public record SelectionMenuCache(MenuCache menuCache, int handSlot, int helmetSlot, int chestSlot,
                                 int legsSlots, int bootsSlot, List<Material> availableMaterials) {}