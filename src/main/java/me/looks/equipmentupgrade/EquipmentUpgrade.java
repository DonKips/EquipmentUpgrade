package me.looks.equipmentupgrade;

import me.looks.equipmentupgrade.manager.Loader;
import org.bukkit.plugin.java.JavaPlugin;

public final class EquipmentUpgrade extends JavaPlugin {
    private Loader loader;

    @Override
    public void onEnable() {

        long startMs = System.currentTimeMillis();

        loader = new Loader(this);
        loader.load();

        long endMs = System.currentTimeMillis();

        getLogger().info("Плагин включен! (" + (endMs - startMs) + "ms)");

    }

    @Override
    public void onDisable() {
        if (loader != null) {
            loader.unload();
        }
    }

    public Loader getLoader() {
        return loader;
    }
}
