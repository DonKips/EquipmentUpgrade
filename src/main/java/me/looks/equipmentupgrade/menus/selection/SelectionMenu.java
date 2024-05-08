package me.looks.equipmentupgrade.menus.selection;

import me.looks.equipmentupgrade.EquipmentUpgrade;
import me.looks.equipmentupgrade.menus.MenuHolder;
import me.looks.equipmentupgrade.menus.upgrade.UpgradeMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class SelectionMenu implements MenuHolder {
    private final EquipmentUpgrade plugin;
    private final Inventory inventory;

    public SelectionMenu(EquipmentUpgrade plugin, Player player) {
        this.plugin = plugin;

        SelectionMenuCache cache = plugin.getLoader().getSelectionMenuCache();
        inventory = Bukkit.createInventory(this, cache.menuCache().size(), cache.menuCache().title());

        for (Map.Entry<ItemStack, List<Integer>> cacheItems : cache.menuCache().cacheItems().entrySet()) {
            for (Integer slot : cacheItems.getValue()) {
                if (slot < 0 | slot >= inventory.getSize()) {
                    plugin.getLogger().severe("В меню selection в items указан слот, выходящий за пределы размера инвентаря! Слот: " + slot);
                    continue;
                }
                inventory.setItem(slot, cacheItems.getKey().clone());
            }
        }

        if (cache.handSlot() >= 0 && cache.handSlot() < inventory.getSize()) {
            ItemStack itemStack = player.getInventory().getItemInMainHand();
            if (cache.availableMaterials().contains(itemStack.getType())) {
                inventory.setItem(cache.handSlot(), itemStack.clone());
            }
        }
        if (cache.helmetSlot() >= 0 && cache.helmetSlot() < inventory.getSize()) {
            ItemStack itemStack = player.getInventory().getHelmet();
            if (itemStack != null && cache.availableMaterials().contains(itemStack.getType())) {
                inventory.setItem(cache.helmetSlot(), itemStack.clone());
            }
        }
        if (cache.chestSlot() >= 0 && cache.chestSlot() < inventory.getSize()) {
            ItemStack itemStack = player.getInventory().getChestplate();
            if (itemStack != null && cache.availableMaterials().contains(itemStack.getType())) {
                inventory.setItem(cache.chestSlot(), itemStack.clone());
            }
        }
        if (cache.legsSlots() >= 0 && cache.legsSlots() < inventory.getSize()) {
            ItemStack itemStack = player.getInventory().getLeggings();
            if (itemStack != null && cache.availableMaterials().contains(itemStack.getType())) {
                inventory.setItem(cache.legsSlots(), itemStack.clone());
            }
        }
        if (cache.bootsSlot() >= 0 && cache.bootsSlot() < inventory.getSize()) {
            ItemStack itemStack = player.getInventory().getBoots();
            if (itemStack != null && cache.availableMaterials().contains(itemStack.getType())) {
                inventory.setItem(cache.bootsSlot(), itemStack.clone());
            }
        }
    }
    @Override
    public void onInventoryClick(InventoryClickEvent event) {

        Player player = (Player) event.getWhoClicked();
        SelectionMenuCache cache = plugin.getLoader().getSelectionMenuCache();

        ItemStack itemStack;
        ItemTypeSelect itemTypeSelect;

        if (event.getSlot() == cache.handSlot()) {

            itemStack = player.getInventory().getItemInMainHand();
            itemTypeSelect = ItemTypeSelect.HAND;

        } else if (event.getSlot() == cache.helmetSlot()) {

            itemStack = player.getInventory().getHelmet();
            itemTypeSelect = ItemTypeSelect.HELMET;

        } else if (event.getSlot() == cache.chestSlot()) {

            itemStack = player.getInventory().getChestplate();
            itemTypeSelect = ItemTypeSelect.CHEST;

        } else if (event.getSlot() == cache.legsSlots()) {

            itemStack = player.getInventory().getLeggings();
            itemTypeSelect = ItemTypeSelect.LEGS;

        } else if (event.getSlot() == cache.bootsSlot()) {

            itemStack = player.getInventory().getBoots();
            itemTypeSelect = ItemTypeSelect.BOOTS;

        } else return;

        if (itemStack == null || !cache.availableMaterials().contains(itemStack.getType())) {
            return;
        }

        UpgradeMenu upgradeMenu = new UpgradeMenu(plugin, itemTypeSelect, itemStack);
        player.openInventory(upgradeMenu.getInventory());

    }
    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
