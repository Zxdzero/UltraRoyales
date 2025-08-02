package dev.zxdzero.UltraRoyales;

import dev.zxdzero.ZxdzeroEvents.registries.ItemMenuRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ItemsMenuManager implements Listener {

    private static final UltraRoyales plugin = UltraRoyales.getPlugin();

    public static void registerMenus() {
        // Register the main WitherGames items menu (the original menu functionality)
        ItemMenuRegistry.registerItemMenu(
                plugin,
                "ultraroyales_items",
                new ItemStack(Material.DIAMOND),
                Component.text("Ultra Royales Items", NamedTextColor.LIGHT_PURPLE),
                ItemsMenuManager::createMenu
        );
    }
    private static Inventory createMenu() {
        Inventory inv = Bukkit.createInventory(null, 9, Component.text("Ultra Royales Items"));
        inv.setItem(0, new ItemStack(Material.STICK));
        return inv;
    }
}