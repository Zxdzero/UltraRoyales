package dev.zxdzero.UltraRoyales;

import dev.zxdzero.UltraRoyales.listeners.*;
import dev.zxdzero.ZxdzeroEvents.registries.CooldownRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class UltraRoyales extends JavaPlugin {

    private static UltraRoyales plugin;

    public static UltraRoyales getPlugin() { return plugin; }

    public static NamespacedKey staffCooldown;
    public static NamespacedKey saberCooldown;
    public static NamespacedKey conchCooldown;

    @Override
    public void onEnable() {
        plugin = this;
        staffCooldown = new NamespacedKey(plugin, "spider_staff_cooldown");
        saberCooldown = new NamespacedKey(plugin, "sponge_saber_cooldown");
        conchCooldown = new NamespacedKey(plugin, "electric_conch_cooldown");

        ItemsMenuManager.registerMenus();
        Items.registerBehavior();

        getServer().getPluginManager().registerEvents(new KnightsSaddleListener(), this);
        getServer().getPluginManager().registerEvents(new DwarvenBowListener(), this);
        getServer().getPluginManager().registerEvents(new GhlochesterListener(), this);
        getServer().getPluginManager().registerEvents(new SpiderStaffListener(), this);
        getServer().getPluginManager().registerEvents(new FractialDwarvenBowListener(), this);
        getServer().getPluginManager().registerEvents(new SkeletalBarberListener(), this);
        getServer().getPluginManager().registerEvents(new ElectricConchListener(), this);

        getCommand("arrowaura").setExecutor(new ArrowAuraCommand());

        CooldownRegistry.registerCooldown(staffCooldown, Material.NAUTILUS_SHELL);
        CooldownRegistry.registerCooldown(saberCooldown, Material.DIAMOND_SWORD);
        CooldownRegistry.registerCooldown(conchCooldown, Material.TRIDENT);
    }

    @Override
    public void onDisable() {
        SpiderAIController.shutdown();
    }
}
