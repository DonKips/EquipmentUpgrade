package me.looks.equipmentupgrade.handlers;

import me.looks.equipmentupgrade.menus.MenuHolder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClick implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof MenuHolder) {

            event.setCancelled(true);
            if (event.getClickedInventory() == null) return;
            if (event.getCurrentItem() == null) return;

            if (event.getClickedInventory().getHolder() instanceof MenuHolder menuHolder) {
                menuHolder.onInventoryClick(event);
            }
        }
    }

}
