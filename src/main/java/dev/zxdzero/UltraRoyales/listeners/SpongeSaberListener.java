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
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class SpongeSaberListener implements Listener {
    public static final NamespacedKey SPONGE_POWER = new NamespacedKey("ultra_royals", "sponge_power");
    public static final int MAX_POWER = 3;

    public static void run(Player player, ItemStack item) {
        if (player.isSneaking() && getCounter(item) <= MAX_POWER) {
            int removed = clearWater(player);

            if (removed >= 8) {
                int power = increaseCounter(item);

                if (power == MAX_POWER) {
                    setAttribute(item);
                }
            } else {
                player.sendMessage(Component.text("You did not collect enough Water!").color(NamedTextColor.RED));
            }

            CooldownRegistry.setCooldown(player, UltraRoyales.saberCooldown, 1);
        } else if (getCounter(item) >= MAX_POWER) {
            dash(player);
            resetCounter(item);
            resetAttribute(player, item);
            CooldownRegistry.setCooldown(player, UltraRoyales.saberCooldown, 5);
        }
    }

    private static void setAttribute(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta.hasAttributeModifiers()) return;
        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(SPONGE_POWER, 8, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HAND));
        item.setItemMeta(meta);
    }

    private static void resetAttribute(Player player, ItemStack item) {
        if (!player.getInventory().getItemInMainHand().getItemMeta().hasCustomModelDataComponent()) return;
        player.getInventory().setItemInMainHand(Items.spongeSaber());
    }

    private static void dash(Player player) {
        Vector dash = player.getLocation().getDirection().normalize().multiply(2);
        dash.setY(0.2);
        player.setVelocity(dash);
    }

    private static void displayPower(ItemStack item, int power) {
        ItemMeta meta = item.getItemMeta();
        String text = power == 3 ? "Wet Sponge Katana" : "Sponge Katana";
        meta.displayName(Component.text(text).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true).append(Component.text(" (" + power + "/" + MAX_POWER + ")").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.BOLD, false)));
        item.setItemMeta(meta);
    }

    private static int getCounter(ItemStack item) {
        return item.getItemMeta().getPersistentDataContainer().getOrDefault(SPONGE_POWER, PersistentDataType.INTEGER, 0);
    }

    private static int getCounter(ItemMeta meta) {
        return meta.getPersistentDataContainer().getOrDefault(SPONGE_POWER, PersistentDataType.INTEGER, 0);
    }

    private static int increaseCounter(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        int power = getCounter(meta);
        if (power >= MAX_POWER) return power;
        power++;
        meta.getPersistentDataContainer().set(SPONGE_POWER, PersistentDataType.INTEGER, power);
        item.setItemMeta(meta);
        displayPower(item, power);
        return power;
    }

    public static void resetCounter(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(SPONGE_POWER, PersistentDataType.INTEGER, 0);
        item.setItemMeta(meta);
        displayPower(item, 0);
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
