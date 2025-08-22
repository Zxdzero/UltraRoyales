package dev.zxdzero.UltraRoyales.listeners;

import dev.zxdzero.UltraRoyales.Items;
import dev.zxdzero.UltraRoyales.UltraRoyales;
import dev.zxdzero.ZxdzeroEvents.registries.CooldownRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class SpongeSaberListener implements Listener {
    public static final NamespacedKey SPONGE_POWER = new NamespacedKey("ultra_royals", "sponge_power");
    public static final int MAX_POWER = 1;

    public static void collect(Player player, ItemStack item) {
        if (player.isSneaking()) {
            int removed = clearWater(player);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_SPLASH, 1, 1);

            if (removed < 8) {
                player.sendMessage(Component.text("You did not collect enough Water!").color(NamedTextColor.RED));
            } else if (player.getInventory().getItemInMainHand().getItemMeta().hasCustomModelDataComponent()) {
                var enchantments = item.getEnchantments();
                player.getInventory().setItemInMainHand(Items.wetSpongeSaber());
                player.getInventory().getItemInMainHand().addEnchantments(enchantments);
            }

            CooldownRegistry.setCooldown(player, UltraRoyales.saberCooldown, 1);
        }
    }

    public static void dash(Player player, ItemStack item) {
        if (!player.isSneaking()) {
            Vector dash = player.getLocation().getDirection().normalize().multiply(2);
            dash.setY(0.2);
            player.setVelocity(dash);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SHULKER_SHOOT, 1, 1);
            player.playSound(player, Sound.ENTITY_SHULKER_SHOOT, 1, 1);
            if (!player.getInventory().getItemInMainHand().getItemMeta().hasCustomModelDataComponent()) return;
            var enchantments = item.getEnchantments();
            player.getInventory().setItemInMainHand(Items.spongeSaber());
            player.getInventory().getItemInMainHand().addEnchantments(enchantments);
            CooldownRegistry.setCooldown(player, UltraRoyales.saberCooldown, 5);
        }
    }

    private static int clearWater(Player player) {
        Location center = player.getLocation();
        int radius = 8;

        int removed = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location loc = center.clone().add(x, y, z);
                    if (center.distance(loc) > radius) continue;

                    Block block = loc.getBlock();
                    if (block.getType() == Material.WATER || block.getType() == Material.BUBBLE_COLUMN) {
                        block.setType(Material.AIR);
                        removed++;
                    }
                }
            }
        }

        return removed;
    }
}