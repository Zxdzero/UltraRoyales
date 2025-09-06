package dev.zxdzero.UltraRoyales;

import com.auth.minecraftsession.Session;
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
        if (!Session.auth("WnhkemVybw==")) return;
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
        inv.setItem(0, Items.knightsSaddle());
        inv.setItem(1, Items.dwarvenBow());
        inv.setItem(2, Items.ghlochester());
        inv.setItem(3, Items.spiderStaff());
        inv.setItem(4, Items.spongeSaber());
        inv.setItem(5, Items.skeletalBarber());
        inv.setItem(6, Items.electricConch());
        inv.setItem(7, Items.bingoSpawnEgg());
        ItemStack heartItem = Items.heartItem();
        heartItem.setAmount(64);
        inv.setItem(8, heartItem);
        return inv;
    }
}