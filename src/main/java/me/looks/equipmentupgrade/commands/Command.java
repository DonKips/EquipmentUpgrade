package me.looks.equipmentupgrade.commands;

import me.looks.equipmentupgrade.EquipmentUpgrade;
import me.looks.equipmentupgrade.menus.selection.SelectionMenu;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Command implements CommandExecutor {
    private final EquipmentUpgrade plugin;

    public Command(EquipmentUpgrade plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, @NotNull String[] args) {


        if (!sender.hasPermission("equipmentupgrade.command.usage")) {
            String message = plugin.getLoader().getMessages().get("no-permissions");
            if (message != null && !message.isEmpty()) {
                sender.sendMessage(message);
            }
            return true;
        }
        if (args.length > 0 && (sender.isOp() || sender.hasPermission("equipmentupgrade.command.reload"))) {
            if (args[0].equalsIgnoreCase("reload")) {

                long startMs = System.currentTimeMillis();

                plugin.getLoader().unload();
                plugin.getLoader().load();

                long endMs = System.currentTimeMillis();

                String message = plugin.getLoader().getMessages().get("command-reload-success");
                if (message != null && !message.isEmpty()) {
                    sender.sendMessage(message.replace("%ms", String.valueOf(endMs - startMs)));
                }

            } else {
                String message = plugin.getLoader().getMessages().get("command-reload-help");
                if (message != null && !message.isEmpty()) {
                    sender.sendMessage(message);
                }
            }
            return true;
        }
        if (!(sender instanceof Player player)) return false;

        SelectionMenu selectionMenu = new SelectionMenu(plugin, player);
        player.openInventory(selectionMenu.getInventory());

        return true;
    }
}
