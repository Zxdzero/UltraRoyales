package dev.zxdzero;

import org.bukkit.plugin.java.JavaPlugin;

public final class UltraRoyales extends JavaPlugin {

    private static UltraRoyales plugin;

    public static UltraRoyales getPlugin() { return plugin; }

    @Override
    public void onEnable() {
        plugin = this;

        ItemsMenuManager.registerMenus();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
