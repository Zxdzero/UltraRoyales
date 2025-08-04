package dev.zxdzero.UltraRoyales;

import com.fractial.codec.api.CodecItemsApi;
import dev.zxdzero.UltraRoyales.listeners.DwarvenBowListener;
import dev.zxdzero.UltraRoyales.listeners.GhlochesterListener;
import dev.zxdzero.UltraRoyales.listeners.KnightsSaddleListener;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class UltraRoyales extends JavaPlugin {

    private static UltraRoyales plugin;

    public static UltraRoyales getPlugin() { return plugin; }

    @Override
    public void onEnable() {
        plugin = this;

        ItemsMenuManager.registerMenus();
        Items.registerBehavior();

        getServer().getPluginManager().registerEvents(new KnightsSaddleListener(), this);
        getServer().getPluginManager().registerEvents(new DwarvenBowListener(), this);
        getServer().getPluginManager().registerEvents(new GhlochesterListener(), this);

        if (Bukkit.getPluginManager().getPlugin("codec") != null) {
            getLogger().info("LOD");
            getLogger().info(String.valueOf(CodecItemsApi.getItem(NamespacedKey.fromString("withergames:item/spider_staff")).isPresent()));
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
