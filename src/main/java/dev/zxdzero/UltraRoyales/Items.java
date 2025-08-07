package dev.zxdzero.UltraRoyales;

import com.fractial.codec.api.CodecItemsApi;
import dev.zxdzero.ZxdzeroEvents.registries.CooldownRegistry;
import dev.zxdzero.ZxdzeroEvents.registries.ItemActionRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

public class Items {

    public static final NamespacedKey knightsHorse = NamespacedKey.fromString("knightshorse", UltraRoyales.getPlugin());

    public static void registerBehavior() {

        // Knight's Saddle
        ItemActionRegistry.register(knightsSaddle(), (player, item) -> {
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            Objective objective = scoreboard.getObjective("knightshorse");
            int cooldown = 0;
            if (objective != null) {
                cooldown = objective.getScore(player.getName()).getScore();
            }

            if (cooldown != 0) {
                player.sendMessage(Component.text("You must wait another " + cooldown/20 + " seconds to use this saddle!", NamedTextColor.RED));
                return;
            }

            if (player.getVehicle() != null) {
                player.sendMessage(Component.text("Dismount to use this saddle!", NamedTextColor.RED));
                return;
            }
            Horse horse = (Horse) player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);
            horse.setTamed(true);
            horse.setOwner(player);
            horse.setAdult();
            horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            horse.setInvulnerable(true);
            horse.setColor(Horse.Color.GRAY);
            horse.getPersistentDataContainer().set(knightsHorse, PersistentDataType.BOOLEAN, true);
            horse.addPassenger(player);

            player.getWorld().spawnParticle(
                    Particle.HEART,
                    player.getLocation().add(0, 1, 0),
                    100,
                    0.5, 1, 0.5,
                    0.05
            );
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_HORSE_SADDLE, 1.0f, 1.0f);
        });

        // Spider Staff
        ItemActionRegistry.register(spiderStaff(), (player, item) -> {
            if (CooldownRegistry.getCooldown(player, UltraRoyales.staffCooldown) == 0) {
                SpiderAIController.spawnPlayerSpiders(player);
                CooldownRegistry.setCooldown(player, UltraRoyales.staffCooldown, 180);
            }
        });
    }

    public static ItemStack knightsSaddle() {
        ItemStack saddle = new ItemStack(Material.SADDLE);
        ItemMeta meta = saddle.getItemMeta();
        meta.displayName(Component.text("Knight's Saddle").decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
        customModelData.setStrings(List.of("ultraroyales:knightssaddle"));
        meta.setCustomModelDataComponent(customModelData);

        saddle.setItemMeta(meta);

        return saddle;
    }

    public static ItemStack dwarvenBow() {
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta meta = bow.getItemMeta();
        meta.displayName(Component.text("Dwarven Bow").decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
        customModelData.setStrings(List.of("ultraroyales:dwarvenbow"));
        meta.setCustomModelDataComponent(customModelData);

        bow.setItemMeta(meta);

        return bow;
    }

    public static ItemStack ghlochester() {
        ItemStack rod = new ItemStack(Material.FISHING_ROD);
        ItemMeta meta = rod.getItemMeta();
        meta.displayName(Component.text("Ghlochester").decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
        customModelData.setStrings(List.of("ultraroyales:ghlochester"));
        meta.setCustomModelDataComponent(customModelData);
        meta.setUnbreakable(true);

        rod.setItemMeta(meta);

        return rod;
    }

    public static ItemStack spiderStaff() {
        ItemStack staff = new ItemStack(Material.NAUTILUS_SHELL);
        ItemMeta meta = staff.getItemMeta();
        meta.displayName(Component.text("Spider Staff").decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
        customModelData.setStrings(List.of("ultraroyales:spiderstaff"));
        meta.setCustomModelDataComponent(customModelData);
        meta.setUnbreakable(true);

        staff.setItemMeta(meta);

        return staff;
    }
}
