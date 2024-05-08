package me.looks.equipmentupgrade.menus.upgrade;

import me.looks.equipmentupgrade.EquipmentUpgrade;
import me.looks.equipmentupgrade.menus.MenuHolder;
import me.looks.equipmentupgrade.menus.selection.ItemTypeSelect;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpgradeMenu implements MenuHolder {
    private final EquipmentUpgrade plugin;
    private final Inventory inventory;
    private final ItemTypeSelect itemTypeSelect;
    private ItemStack itemSelect;
    private final HashMap<Integer, UpgradeRecordsData.UpgradeLevelData> levels = new HashMap<>();

    public UpgradeMenu(EquipmentUpgrade plugin, ItemTypeSelect itemTypeSelect, ItemStack itemSelect) {
        this.plugin = plugin;
        this.itemTypeSelect = itemTypeSelect;
        this.itemSelect = itemSelect.clone();

        UpgradeRecordsData.UpgradeMenuCache cache = plugin.getLoader().getUpgradeMenuCache();
        inventory = Bukkit.createInventory(this, cache.menuCache().size(), cache.menuCache().title());

        for (Map.Entry<ItemStack, List<Integer>> cacheItems : cache.menuCache().cacheItems().entrySet()) {
            for (Integer slot : cacheItems.getValue()) {
                inventory.setItem(slot, cacheItems.getKey().clone());
            }
        }

        for (UpgradeRecordsData.UpgradeData upgradeData : cache.upgrades()) {
            updateUpgrade(upgradeData);
        }
    }

    private void updateUpgrade(UpgradeRecordsData.UpgradeData upgradeData) {
        if (!upgradeData.availableMaterials().contains(itemSelect.getType())) return;

        UpgradeRecordsData.UpgradeLevelData level = getLevel(itemSelect, upgradeData.levels());

        ItemStack itemStack;
        if (level == null) {

            itemStack = upgradeData.itemMaxLevel().clone();

        } else {
            itemStack = upgradeData.item().clone();
            ItemMeta itemMeta = itemStack.getItemMeta();

            if (itemMeta.hasDisplayName()) {
                itemMeta.setDisplayName(itemMeta.getDisplayName()
                        .replace("%level-name%", level.name())
                        .replace("%price-vault%", String.valueOf(level.vaultPrice()))
                        .replace("%price-player-points%", String.valueOf(level.playerPointsPrice())));
            }

            if (itemMeta.getLore() != null) {
                List<String> lore = new ArrayList<>();

                for (String str : itemMeta.getLore()) {
                    if (str.contains("%level-lore%")) {

                        for (String string : level.lore()) {
                            lore.add(string
                                    .replace("%level-name%", level.name())
                                    .replace("%price-vault%", String.valueOf(level.vaultPrice()))
                                    .replace("%price-player-points%", String.valueOf(level.playerPointsPrice())));
                        }

                    } else {
                        lore.add(str
                                .replace("%level-name%", level.name())
                                .replace("%price-vault%", String.valueOf(level.vaultPrice()))
                                .replace("%price-player-points%", String.valueOf(level.playerPointsPrice())));
                    }
                }
                itemMeta.setLore(lore);
            }
            itemStack.setItemMeta(itemMeta);
            levels.put(upgradeData.slot(), level);
        }

        inventory.setItem(upgradeData.slot(), itemStack);
    }

    private UpgradeRecordsData.UpgradeLevelData getLevel(ItemStack itemStack, List<UpgradeRecordsData.UpgradeLevelData> levels) {

        for (UpgradeRecordsData.UpgradeLevelData levelData : levels) {
            for (String str : levelData.performanceConditions()) {

                String[] args = str.split(" ");

                if (args[0].equalsIgnoreCase("enchant")) {
                    if (args[1].equalsIgnoreCase("has")) {

                        Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(args[2].toLowerCase()));
                        if (enchantment == null) {
                            plugin.getLogger().severe("Зачарование указано неверно: " + str);
                            continue;
                        }

                        int level = Integer.parseInt(args[3]);

                        if (itemStack.getEnchantmentLevel(enchantment) < level) {
                            return levelData;
                        }
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {

        if (levels.containsKey(event.getSlot())) {

            UpgradeRecordsData.UpgradeLevelData levelData = levels.get(event.getSlot());

            if (levelData.vaultPrice() > 0 && plugin.getLoader().getVaultHook() != null) {

                double balance = plugin.getLoader().getVaultHook().getBalance((OfflinePlayer) event.getWhoClicked());

                if (balance < levelData.vaultPrice()) {
                    String message = plugin.getLoader().getMessages().get("level-buy-no-money");
                    if (message != null && !message.isEmpty()) {
                        event.getWhoClicked().sendMessage(message
                                .replace("%need-moneys%", String.valueOf(levelData.vaultPrice()))
                                .replace("%remainder-moneys%", String.valueOf(levelData.vaultPrice() - balance))
                                .replace("%moneys%", String.valueOf(balance)));
                    }
                    return;
                }
            }
            if (levelData.playerPointsPrice() > 0 && plugin.getLoader().getPlayerPointsHook() != null) {
                int points = plugin.getLoader().getPlayerPointsHook().look(event.getWhoClicked().getUniqueId());

                if (points < levelData.playerPointsPrice()) {
                    String message = plugin.getLoader().getMessages().get("level-buy-no-player-points");
                    if (message != null && !message.isEmpty()) {
                        event.getWhoClicked().sendMessage(message
                                .replace("%need-points%", String.valueOf(levelData.playerPointsPrice()))
                                .replace("%remainder-points%", String.valueOf(levelData.playerPointsPrice() - points))
                                .replace("%points%", String.valueOf(points)));
                    }
                    return;
                }
            }

            ItemStack itemStack;
            switch (itemTypeSelect) {
                case HAND -> itemStack = event.getWhoClicked().getInventory().getItemInMainHand();
                case HELMET -> itemStack = event.getWhoClicked().getInventory().getHelmet();
                case CHEST -> itemStack = event.getWhoClicked().getInventory().getChestplate();
                case LEGS -> itemStack = event.getWhoClicked().getInventory().getLeggings();
                case BOOTS -> itemStack = event.getWhoClicked().getInventory().getBoots();
                default -> itemStack = null;
            }

            if (itemStack == null || !itemStack.equals(itemSelect)) {
                String message = plugin.getLoader().getMessages().get("invalid-select-item");
                if (message != null && !message.isEmpty()) {
                    event.getWhoClicked().sendMessage(message);
                }
                inventory.close();
                return;
            }

            if (plugin.getLoader().getPlayerPointsHook() != null) {
                plugin.getLoader().getPlayerPointsHook().take(event.getWhoClicked().getUniqueId(), levelData.playerPointsPrice());
            }
            if (plugin.getLoader().getVaultHook() != null) {
                plugin.getLoader().getVaultHook().withdrawPlayer((OfflinePlayer) event.getWhoClicked(), levelData.vaultPrice());
            }

            ItemStack result = applyActions(itemStack, levelData.actions());
            switch (itemTypeSelect) {
                case HAND -> event.getWhoClicked().getInventory().setItemInMainHand(result);
                case HELMET -> event.getWhoClicked().getInventory().setHelmet(result);
                case CHEST -> event.getWhoClicked().getInventory().setChestplate(result);
                case LEGS -> event.getWhoClicked().getInventory().setLeggings(result);
                case BOOTS -> event.getWhoClicked().getInventory().setBoots(result);
            }
            itemSelect = result;

            for (UpgradeRecordsData.UpgradeData upgradeData : plugin.getLoader().getUpgradeMenuCache().upgrades()) {
                if (upgradeData.slot() == event.getSlot()) {
                    updateUpgrade(upgradeData);
                }
            }

            String message = plugin.getLoader().getMessages().get("level-buy-success");
            if (message != null && !message.isEmpty()) {
                event.getWhoClicked().sendMessage(message);
            }

        }

    }

    private ItemStack applyActions(ItemStack itemStack, List<String> actions) {
        for (String str : actions) {

            String[] args = str.split(" ");

            if (args[0].equalsIgnoreCase("enchant")) {

                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(args[2].toLowerCase()));
                if (enchantment == null) {
                    plugin.getLogger().severe("Зачарование указано неверно: " + str);
                    continue;
                }
                int level = Integer.parseInt(args[3]);

                switch (args[1].toLowerCase()) {
                    case "add" -> {
                        int prevLevel = itemStack.removeEnchantment(enchantment);
                        if (prevLevel + level > 0) {
                            itemStack.addEnchantment(enchantment, prevLevel + level);
                        }
                    }
                    case "set" -> {
                        itemStack.removeEnchantment(enchantment);
                        if (level > 0) {
                            itemStack.addEnchantment(enchantment, level);
                        }
                    }
                    case "remove" -> {
                        int prevLevel = itemStack.removeEnchantment(enchantment);
                        if (prevLevel - level > 0) {
                            itemStack.addEnchantment(enchantment, prevLevel - level);
                        }
                    }
                }
            }
        }
        return itemStack;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

}
