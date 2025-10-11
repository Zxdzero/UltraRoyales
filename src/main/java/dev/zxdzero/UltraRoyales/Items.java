package dev.zxdzero.UltraRoyales;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import dev.zxdzero.UltraRoyales.listeners.BingoTheClownListener;
import dev.zxdzero.UltraRoyales.listeners.SpongeSaberListener;
import dev.zxdzero.ZxdzeroEvents.ItemHelper;
import dev.zxdzero.ZxdzeroEvents.registries.CooldownRegistry;
import dev.zxdzero.ZxdzeroEvents.registries.ItemActionRegistry;
import dev.zxdzero.ZxdzeroEvents.tooltip.Tooltip;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.inventory.meta.components.EquippableComponent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;

import java.util.EnumSet;
import java.util.List;

public class Items {

    public static final NamespacedKey knightsHorse = NamespacedKey.fromString("knightshorse", UltraRoyales.getPlugin());

    public static void registerBehavior() {

        // Knight's Saddle
        ItemActionRegistry.register(knightsSaddle(), (player, item) -> {
            int cooldown = CooldownRegistry.getCooldown(player, UltraRoyales.saddleCooldown);
            if (cooldown != 0) {
                player.sendMessage(Component.text("You must wait another " + cooldown + " seconds to use this saddle!", NamedTextColor.RED));
                return;
            }

            if (player.getVehicle() != null) {
                player.sendMessage(Component.text("Dismount to use this saddle!", NamedTextColor.RED));
                return;
            }

            ItemStack horseArmor = new ItemStack(Material.IRON_HORSE_ARMOR);
            ItemMeta horseArmorMeta = horseArmor.getItemMeta();
            EquippableComponent horseArmorEquippableComponent = horseArmorMeta.getEquippable();
            horseArmorEquippableComponent.setSlot(EquipmentSlot.BODY);
            horseArmorEquippableComponent.setModel(new NamespacedKey("ultraroyales", "knight_horse"));
            horseArmorMeta.setEquippable(horseArmorEquippableComponent);
            horseArmor.setItemMeta(horseArmorMeta);

            Horse horse = (Horse) player.getWorld().spawnEntity(player.getLocation(), EntityType.HORSE);
            horse.setTamed(true);
            horse.setOwner(player);
            horse.setAdult();
            horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
            horse.getInventory().setArmor(horseArmor);
            horse.setColor(Horse.Color.GRAY);
            horse.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.3375);
            horse.getAttribute(Attribute.MAX_HEALTH).setBaseValue(40);
            horse.setJumpStrength(0.7);
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

        // Heart item
        ItemActionRegistry.register(heartItem(), (player, item) -> {
            if (!player.isSneaking()) return;
            AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
            maxHealth.setBaseValue(maxHealth.getBaseValue() + 2);
            item.setAmount(item.getAmount() - 1);
        });

        // Bingo Spawn Egg
        ItemActionRegistry.register(bingoSpawnEgg(), (player, item) -> {
            RayTraceResult rayTraceResult = player.rayTraceBlocks(5);
            if (rayTraceResult == null) return;

            Skeleton skeleton = (Skeleton) player.getWorld().spawn(rayTraceResult.getHitPosition().toLocation(player.getWorld()), EntityType.SKELETON.getEntityClass());
            skeleton.getEquipment().setItemInMainHand(null);
            EntityEquipment equipment = skeleton.getEquipment();

            ItemStack bingoHelmet = new ItemStack(Material.DIRT);
            ItemMeta bingoHelmetMeta = bingoHelmet.getItemMeta();
            CustomModelDataComponent customModelData = bingoHelmetMeta.getCustomModelDataComponent();
            customModelData.setStrings(List.of("ultraroyales:bingo_head"));
            bingoHelmetMeta.setCustomModelDataComponent(customModelData);
            bingoHelmet.setItemMeta(bingoHelmetMeta);

            ItemStack bingoChestplate = new ItemStack(Material.DIAMOND_CHESTPLATE);
            ItemMeta bingoChestplateMeta = bingoChestplate.getItemMeta();
            EquippableComponent bingoChestplateEquippableComponent = bingoChestplateMeta.getEquippable();
            bingoChestplateEquippableComponent.setSlot(EquipmentSlot.CHEST);
            bingoChestplateEquippableComponent.setModel(new NamespacedKey("ultraroyales", "bingo"));
            bingoChestplateMeta.setEquippable(bingoChestplateEquippableComponent);
            bingoChestplate.setItemMeta(bingoChestplateMeta);

            ItemStack bingoLeggings = new ItemStack(Material.DIAMOND_LEGGINGS);
            ItemMeta bingoLeggingsMeta = bingoLeggings.getItemMeta();
            EquippableComponent bingoLeggingsEquippableComponent = bingoLeggingsMeta.getEquippable();
            bingoLeggingsEquippableComponent.setSlot(EquipmentSlot.LEGS);
            bingoLeggingsEquippableComponent.setModel(new NamespacedKey("ultraroyales", "bingo"));
            bingoLeggingsMeta.setEquippable(bingoLeggingsEquippableComponent);
            bingoLeggings.setItemMeta(bingoLeggingsMeta);

            ItemStack bingoBoots = new ItemStack(Material.DIAMOND_BOOTS);
            ItemMeta bingoBootsMeta = bingoBoots.getItemMeta();
            EquippableComponent bingoBootsEquippableComponent = bingoBootsMeta.getEquippable();
            bingoBootsEquippableComponent.setSlot(EquipmentSlot.FEET);
            bingoBootsEquippableComponent.setModel(new NamespacedKey("ultraroyales", "bingo"));
            bingoBootsMeta.setEquippable(bingoBootsEquippableComponent);
            bingoBoots.setItemMeta(bingoBootsMeta);

            equipment.setHelmet(bingoHelmet);
            equipment.setChestplate(bingoChestplate);
            equipment.setLeggings(bingoLeggings);
            equipment.setBoots(bingoBoots);
            skeleton.setCanPickupItems(false);

            BingoTheClownListener.startMove(skeleton);
            BingoTheClownListener.startFriendly(skeleton);
        });

        // Sponge Saber
        ItemActionRegistry.register(spongeSaber(), (player, item) -> {
            if (CooldownRegistry.getCooldown(player, UltraRoyales.saberCooldown) == 0) {
                SpongeSaberListener.collect(player, item);
            }
        });
        ItemActionRegistry.register(wetSpongeSaber(), (player, item) -> {
            if (CooldownRegistry.getCooldown(player, UltraRoyales.saberCooldown) == 0) {
                SpongeSaberListener.dash(player, item);
            }
        });
    }

    public static ItemStack heartItem() {
        ItemStack heart = new ItemStack(Material.RESIN_BRICK);
        ItemMeta meta = heart.getItemMeta();
        meta.lore(List.of(Tooltip.SHIFT_RIGHT_CLICK.toComponent("to use")));
        meta.displayName(Component.text("Heart").decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
        customModelData.setStrings(List.of("ultraroyales:heart"));
        meta.setCustomModelDataComponent(customModelData);

        heart.setItemMeta(meta);

        return heart;
    }

    public static ItemStack knightsSaddle() {
        ItemStack saddle = new ItemStack(Material.SADDLE);
        ItemMeta meta = saddle.getItemMeta();
        meta.lore(List.of(Tooltip.RIGHT_CLICK.toComponent("to mount the knights horse")));
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
        meta.lore(List.of(Tooltip.RIGHT_CLICK.toComponent("to summon spiders")));
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
        meta.lore(List.of(Tooltip.SHIFT_RIGHT_CLICK.toComponent("to collect water")));
        meta.displayName(Component.text("Sponge Saber").decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
        customModelData.setStrings(List.of("ultraroyales:sponge_saber"));
        meta.setCustomModelDataComponent(customModelData);

        saber.setItemMeta(meta);

        return saber;
    }

    public static ItemStack wetSpongeSaber() {
        ItemStack saber = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = saber.getItemMeta();
        meta.lore(List.of(Tooltip.RIGHT_CLICK.toComponent("to dash")));
        meta.displayName(Component.text("Wet Sponge Saber").decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
        customModelData.setStrings(List.of("ultraroyales:wet_sponge_saber"));
        meta.setCustomModelDataComponent(customModelData);

        saber.setItemMeta(meta);

        return saber;
    }

    public static ItemStack bingoSpawnEgg() {
        ItemStack bingoSpawn = new ItemStack(Material.SNIFFER_SPAWN_EGG);
        ItemMeta meta = bingoSpawn.getItemMeta();
        meta.displayName(Component.text("Bingo the Clown").decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
        customModelData.setStrings(List.of("ultraroyales:bingo_spawn_egg"));
        meta.setCustomModelDataComponent(customModelData);

        bingoSpawn.setItemMeta(meta);

        return bingoSpawn;
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
        conch.addUnsafeEnchantment(Enchantment.RIPTIDE, 3);
        ItemMeta meta = conch.getItemMeta();
        // TODO: Make it Left Click
        meta.lore(List.of(Tooltip.LEFT_CLICK.toComponent("to water burst")));
        meta = ItemHelper.weaponBuilder(meta, 0, 4);
        meta.displayName(Component.text("Electric Conch").decoration(TextDecoration.ITALIC, false).decoration(TextDecoration.BOLD, true));
        meta.setUnbreakable(true);
        CustomModelDataComponent customModelData = meta.getCustomModelDataComponent();
        customModelData.setStrings(List.of("ultraroyales:electricconch"));
        meta.setCustomModelDataComponent(customModelData);

        conch.setItemMeta(meta);

        return conch;
    }
}
