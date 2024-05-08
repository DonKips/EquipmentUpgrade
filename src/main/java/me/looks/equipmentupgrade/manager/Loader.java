package me.looks.equipmentupgrade.manager;

import me.looks.equipmentupgrade.EquipmentUpgrade;
import me.looks.equipmentupgrade.commands.Command;
import me.looks.equipmentupgrade.handlers.InventoryClick;
import me.looks.equipmentupgrade.hooks.PlayerPointsHook;
import me.looks.equipmentupgrade.hooks.VaultHook;
import me.looks.equipmentupgrade.menus.MenuCache;
import me.looks.equipmentupgrade.menus.MenuHolder;
import me.looks.equipmentupgrade.menus.upgrade.UpgradeRecordsData;
import me.looks.equipmentupgrade.menus.selection.SelectionMenuCache;
import me.looks.equipmentupgrade.utils.ColorUtil;
import me.looks.equipmentupgrade.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Loader {
    private final EquipmentUpgrade plugin;
    private SelectionMenuCache selectionMenuCache;
    private UpgradeRecordsData.UpgradeMenuCache upgradeMenuCache;

    private VaultHook vaultHook;
    private PlayerPointsHook playerPointsHook;
    private final HashMap<String, String> messages = new HashMap<>();

    public Loader(EquipmentUpgrade plugin) {
        this.plugin = plugin;
    }


    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        ConfigurationSection messagesSection = plugin.getConfig().getConfigurationSection("messages");
        if (messagesSection != null) {
            for (String str : messagesSection.getKeys(false)) {
                messages.put(str, ColorUtil.color(messagesSection.getString(str)));
            }
        }

        List<Material> availableMaterials = new ArrayList<>();
        for (String str : plugin.getConfig().getStringList("selection-menu.equipments.available-materials")) {
            try {
                Material material = Material.valueOf(str.toUpperCase());
                availableMaterials.add(material);
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().severe("selection-menu.equipments.available-materials");
            }
        }
        selectionMenuCache = new SelectionMenuCache(
                createMenuCache("selection-menu."),
                plugin.getConfig().getInt("selection-menu.equipments.hand-slot"),
                plugin.getConfig().getInt("selection-menu.equipments.helmet-slot"),
                plugin.getConfig().getInt("selection-menu.equipments.chest-slot"),
                plugin.getConfig().getInt("selection-menu.equipments.legs-slot"),
                plugin.getConfig().getInt("selection-menu.equipments.boots-slot"),
                availableMaterials);

        List<UpgradeRecordsData.UpgradeData> upgrades = new ArrayList<>();

        ConfigurationSection upgradesSection = plugin.getConfig().getConfigurationSection("upgrade-menu.upgrades");
        if (upgradesSection != null) {
            for (String key : upgradesSection.getKeys(false)) {

                List<UpgradeRecordsData.UpgradeLevelData> levels = new ArrayList<>();
                ConfigurationSection levelsSection = upgradesSection.getConfigurationSection(key + ".levels");
                if (levelsSection != null) {
                    for (String str : levelsSection.getKeys(false)) {

                        List<String> lore = new ArrayList<>();
                        for (String string : levelsSection.getStringList(str + ".lore")) {
                            lore.add(ColorUtil.color(string));
                        }

                        levels.add(new UpgradeRecordsData.UpgradeLevelData(
                                levelsSection.getInt(str + ".price.vault"),
                                levelsSection.getInt(str + ".price.player-points"),
                                levelsSection.getStringList(str + ".actions"),
                                levelsSection.getStringList(str + ".performance-conditions"),
                                levelsSection.getString(str + ".name"),
                                lore
                        ));
                    }
                }

                if (!levels.isEmpty()) {
                    List<Material> upgradeAvailableMaterials = new ArrayList<>();
                    for (String str : upgradesSection.getStringList(key + ".available-materials")) {
                        try {
                            Material material = Material.valueOf(str.toUpperCase());
                            upgradeAvailableMaterials.add(material);
                        } catch (IllegalArgumentException ex) {
                            plugin.getLogger().severe(upgradesSection.getCurrentPath() + "." + key + ".available-materials " + str);
                        }
                    }

                    upgrades.add(new UpgradeRecordsData.UpgradeData(
                            upgradesSection.getInt(key + ".slot"),
                            ItemBuilder.createItemStack(upgradesSection, key + ".item."),
                            ItemBuilder.createItemStack(upgradesSection, key + ".item-max-level."),
                            levels,
                            upgradeAvailableMaterials));
                }
            }
        }

        upgradeMenuCache = new UpgradeRecordsData.UpgradeMenuCache(
                createMenuCache("upgrade-menu."),
                upgrades);

        PluginCommand command = Bukkit.getPluginCommand("equipmentupgrade");
        if (command != null) {
            command.setExecutor(new Command(plugin));
        }
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new InventoryClick(), plugin);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (pluginManager.isPluginEnabled("PlayerPoints")) {
                playerPointsHook = new PlayerPointsHook();
            }

            if (pluginManager.isPluginEnabled("Vault")) {
                vaultHook = new VaultHook();
            }
        });

    }

    private @NotNull MenuCache createMenuCache(String key) {
        String title = ColorUtil.color(plugin.getConfig().getString(key + "title", ""));
        int size = plugin.getConfig().getInt(key + "size");

        HashMap<ItemStack, List<Integer>> cacheItems = new HashMap<>();
        ConfigurationSection itemsSection = plugin.getConfig().getConfigurationSection(key + "items");

        if (itemsSection != null) {
            for (String item : itemsSection.getKeys(false)) {

                List<Integer> slots = new ArrayList<>();
                try {
                    for (String str : itemsSection.getStringList(item + ".slots")) {
                        if (str.contains("-")) {
                            String[] strings = str.split("-");
                            int min = Integer.parseInt(strings[0]);
                            int max = Integer.parseInt(strings[1]);

                            for (int s = min; s <= max; s++) {
                                slots.add(s);
                            }
                        } else {
                            slots.add(Integer.parseInt(str));
                        }
                    }
                } catch (NumberFormatException e) {
                    plugin.getLogger().severe("Ошибка в слотах: " + key + "items." + item + ".slots");
                    continue;
                }
                ItemStack itemStack = ItemBuilder.createItemStack(itemsSection, item + ".");

                cacheItems.put(itemStack, slots);
            }
        }

        return new MenuCache(title, size, cacheItems);
    }

    public void unload() {
        HandlerList.unregisterAll(plugin);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory().getHolder() instanceof MenuHolder) {
                player.closeInventory();
            }
        }
        playerPointsHook = null;
        vaultHook = null;
        messages.clear();
    }

    public SelectionMenuCache getSelectionMenuCache() {
        return selectionMenuCache;
    }

    public UpgradeRecordsData.UpgradeMenuCache getUpgradeMenuCache() {
        return upgradeMenuCache;
    }

    public PlayerPointsHook getPlayerPointsHook() {
        return playerPointsHook;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public HashMap<String, String> getMessages() {
        return messages;
    }
}
