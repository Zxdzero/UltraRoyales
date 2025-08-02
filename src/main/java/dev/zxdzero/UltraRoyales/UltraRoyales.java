package dev.zxdzero.UltraRoyales;

import com.fractial.codec.api.CodecItemsApi;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class UltraRoyales extends JavaPlugin {

    private static UltraRoyales plugin;

    public static UltraRoyales getPlugin() { return plugin; }

    @Override
    public void onEnable() {
        plugin = this;

        ItemsMenuManager.registerMenus();
        Items.registerBehavior();

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
