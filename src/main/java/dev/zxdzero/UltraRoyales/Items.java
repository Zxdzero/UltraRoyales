package dev.zxdzero.UltraRoyales;

import dev.zxdzero.ZxdzeroEvents.registries.ItemActionRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

public class Items {

    public static void registerBehavior() {

        // Knight's Saddle
        ItemActionRegistry.register(knightsSaddle(), (player, item) -> {
            player.getWorld().spawnParticle(
                    Particle.FLAME,
                    player.getLocation().add(0, 1, 0), // slightly above ground
                    100, // count
                    0.5, 1, 0.5, // x, y, z offset
                    0.05 // speed
            );
            player.getWorld().spawnParticle(
                    Particle.LARGE_SMOKE,
                    player.getLocation().add(0, 1, 0),
                    40,
                    0.3, 0.8, 0.3,
                    0.01
            );
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.0f);
        });
    }

    public static ItemStack knightsSaddle() {
        ItemStack amulet = new ItemStack(Material.SADDLE);
        ItemMeta meta = amulet.getItemMeta();
        meta.displayName(Component.text("Knight's Saddle").decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));

        CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
        customModelData.setStrings(List.of("ultraroyales:knightssaddle"));
        meta.setCustomModelDataComponent(customModelData);

        amulet.setItemMeta(meta);

        return amulet;
    }
}
