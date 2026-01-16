package dev.zxdzero.UltraRoyales.listeners;

import dev.zxdzero.UltraRoyales.Items;
import dev.zxdzero.UltraRoyales.UltraRoyales;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class LifeElixirListener implements Listener {
    public static final NamespacedKey ELIXIR_POWER = new NamespacedKey(UltraRoyales.getPlugin(), "elixir_power");

    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent e) {
        ItemStack item = e.getItem();

        if (!(item.getItemMeta() instanceof PotionMeta meta)) return;
        if (!meta.getPersistentDataContainer().has(ELIXIR_POWER)) return;
        int power = item.getPersistentDataContainer().get(ELIXIR_POWER, PersistentDataType.INTEGER);

        e.getPlayer().giveExpLevels(5);
        if (power == 1) {
            ItemStack empty = new ItemStack(Material.GLASS_BOTTLE);
            ItemMeta emptyMeta = empty.getItemMeta();

            emptyMeta.getPersistentDataContainer().set(LifeElixirListener.ELIXIR_POWER, PersistentDataType.INTEGER, 0);
            emptyMeta.displayName(Component.text("Empty Life Elixir").decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
            List<Component> lore = new ArrayList<>();
            lore.add(0, Component.text("30 seconds of Absorption 2").color(TextColor.color(0x0099db)).decoration(TextDecoration.ITALIC, false));
            lore.add(1, Component.text("Can be refilled once with player head").color(TextColor.color(0x0099db)).decoration(TextDecoration.ITALIC, false));
            emptyMeta.lore(lore);
            CustomModelDataComponent customModelData = emptyMeta.getCustomModelDataComponent();
            customModelData.setStrings(List.of("ultraroyales:lifeelixir"));
            emptyMeta.setCustomModelDataComponent(customModelData);

            empty.setItemMeta(emptyMeta);
            e.setReplacement(empty);
        } else if (power == 0) {
            e.setReplacement(
                    new ItemStack(Material.AIR)
            );
        }
    }

    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        ItemStack slot = e.getCurrentItem();
        if (slot == null) return;
        if (!(slot.getType() == Material.GLASS_BOTTLE)) return;
        if (!slot.getItemMeta().getPersistentDataContainer().has(ELIXIR_POWER)) return;
        if (!(e.getCursor().getType() == Material.PLAYER_HEAD)) return;


        e.setCurrentItem(Items.lifeElixir(0));
        e.getView().setCursor(null);
        e.setCancelled(true);
    }
}
