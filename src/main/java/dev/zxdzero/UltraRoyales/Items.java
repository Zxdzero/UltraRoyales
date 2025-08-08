package dev.zxdzero.UltraRoyales;

import com.fractial.codec.api.CodecItemsApi;
import dev.zxdzero.ZxdzeroEvents.ItemHelper;
import dev.zxdzero.ZxdzeroEvents.registries.CooldownRegistry;
import dev.zxdzero.ZxdzeroEvents.registries.ItemActionRegistry;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class Items {

    public static final NamespacedKey knightsHorse = NamespacedKey.fromString("knightshorse", UltraRoyales.getPlugin());

    public enum SpongeSaberName {
        WET(Component.text("Wet Sponge Katana").decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true)),
        DRY(Component.text("Sponge Katana").decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));

        public final Component component;

        SpongeSaberName(@NotNull Component component) {
            this.component = component;
        }
    }

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

        // Sponge Saber
        ItemActionRegistry.register(spongeSaber(), (player, item) -> {
            NamespacedKey spongePower = new NamespacedKey("ultraroyales", "sponge_power");

            if (CooldownRegistry.getCooldown(player, UltraRoyales.saberCooldown) == 0) {
                ItemMeta meta = item.getItemMeta();

                if (player.isSneaking()) {
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
                    if (removed > 10) {
                        meta.displayName(SpongeSaberName.WET.component);
                        meta.getPersistentDataContainer().set(spongePower, PersistentDataType.BOOLEAN, true);
                        meta.addAttributeModifier(Attribute.ATTACK_DAMAGE, new AttributeModifier(spongePower, 8D, AttributeModifier.Operation.ADD_NUMBER));
                        item.setItemMeta(meta);
                    }
                    CooldownRegistry.setCooldown(player, UltraRoyales.saberCooldown, 3);
                } else if (Boolean.TRUE.equals(meta.getPersistentDataContainer().get(spongePower, PersistentDataType.BOOLEAN))) {
                    Vector dash = player.getLocation().getDirection().normalize().multiply(2);
                    dash.setY(0.2);
                    player.setVelocity(dash);
                    meta.displayName(SpongeSaberName.DRY.component);
                    meta.getPersistentDataContainer().set(spongePower, PersistentDataType.BOOLEAN, false);
                    item.setItemMeta(meta);
                    CooldownRegistry.setCooldown(player, UltraRoyales.saberCooldown, 5);
                }
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

    public static ItemStack fractialDwarvenBow() {
        ItemStack bow = new ItemStack(Material.BOW);
        ItemMeta meta = bow.getItemMeta();
        meta.displayName(Component.text("Fractial's Dwarven Bow").decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
        customModelData.setStrings(List.of("ultraroyales:fractial_dwarvenbow"));
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
        ItemStack saber = new ItemStack(Material.NAUTILUS_SHELL);
        ItemMeta meta = saber.getItemMeta();
        meta.displayName(Component.text("Spider Staff", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
        customModelData.setStrings(List.of("ultraroyales:spiderstaff"));
        meta.setCustomModelDataComponent(customModelData);
        meta.setUnbreakable(true);

        saber.setItemMeta(meta);

        return saber;
    }

    public static ItemStack spongeSaber() {
        ItemStack saber = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = saber.getItemMeta();
        meta.displayName(SpongeSaberName.DRY.component);
        CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
        customModelData.setStrings(List.of("ultraroyales:sponge_saber"));
        meta.setCustomModelDataComponent(customModelData);

        saber.setItemMeta(meta);

        return saber;
    }

    public static ItemStack skeletalBarber() {
        ItemStack staff = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = staff.getItemMeta();
        meta.displayName(Component.text("Skeletal Barber").decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
        customModelData.setStrings(List.of("ultraroyales:skeletalbarber"));
        meta.setCustomModelDataComponent(customModelData);

        staff.setItemMeta(meta);

        return staff;
    }

    static ItemStack electricConch() {
        ItemStack conch = new ItemStack(Material.TRIDENT);
        conch.addUnsafeEnchantment(Enchantment.RIPTIDE, 4);
        ItemMeta meta = conch.getItemMeta();
        meta = ItemHelper.weaponBuilder(meta, 0, 4);
        meta.displayName(Component.text("Electric Conch").decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
        customModelData.setStrings(List.of("ultraroyales:electricconch"));
        meta.setCustomModelDataComponent(customModelData);

        conch.setItemMeta(meta);

        return conch;
    }
}
