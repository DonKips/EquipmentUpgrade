package me.looks.equipmentupgrade.hooks;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {
    private final Economy economy;

    public VaultHook() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            throw new NullPointerException("Экономика не найдена!");
        }
        economy = rsp.getProvider();
    }

    public double getBalance(OfflinePlayer player) {
        return economy.getBalance(player);
    }
    public void withdrawPlayer(OfflinePlayer player, double amount) {
        economy.withdrawPlayer(player, amount);
    }


}
