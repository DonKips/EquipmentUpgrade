package me.looks.equipmentupgrade.menus.upgrade;

import me.looks.equipmentupgrade.menus.MenuCache;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public class UpgradeRecordsData {
    public record UpgradeMenuCache(MenuCache menuCache, List<UpgradeData> upgrades) {}
    public record UpgradeData(int slot, ItemStack item, ItemStack itemMaxLevel, List<UpgradeLevelData> levels, List<Material> availableMaterials) {}
    public record UpgradeLevelData(int vaultPrice, int playerPointsPrice, List<String> actions,
                                   List<String> performanceConditions, String name, List<String> lore) {}

}