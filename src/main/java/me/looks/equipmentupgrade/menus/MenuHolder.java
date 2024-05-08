package me.looks.equipmentupgrade.menus;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public interface MenuHolder extends InventoryHolder {

    default void onInventoryClick(InventoryClickEvent event) {}
}
