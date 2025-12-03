package dev.zxdzero.UltraRoyales.listeners;

import dev.zxdzero.UltraRoyales.Items;
import dev.zxdzero.UltraRoyales.UltraRoyales;
import dev.zxdzero.ZxdzeroEvents.registries.CooldownRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
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
            Location startingLoc = player.getLocation();

            new BukkitRunnable() {
                int ticks = 0;

                @Override
                public void run() {
                    if (ticks++ >= 10) {
                        cancel();
                    }

                    Location loc = player.getLocation();
                    for (Entity e : loc.getWorld().getNearbyEntities(loc, 1.25, 1.25, 1.25)) {
                        if (!(e instanceof LivingEntity le)) continue;
                        if (le.getUniqueId().equals(player.getUniqueId())) continue;


                        Vector forward = dash.clone().setY(0).normalize(); // Dash direction

                        Vector toEnt = le.getLocation().toVector()
                                .subtract(player.getLocation().toVector())
                                .setY(0)
                                .normalize(); // Direction away from entity

                        Vector side = toEnt.clone();
                        double proj = side.dot(forward);

                        if (proj < 0) {
                            // Subtract the backward component
                            side.subtract(forward.clone().multiply(proj));
                        }

                        side.normalize();

                        double dot = Math.abs(forward.dot(toEnt)); // Dot product- how off center the target is (1 means head on collision, 0 means passing to the side)

                        // How centered the entity is
                        double minForward = 0.25;           // guarantees some push forward
                        double wForward = Math.max(dot, minForward);
                        double wSide    = 1.0 - wForward;

                        double strength = computeDashStrength(
                                startingLoc,
                                forward,    // normalized dash vector
                                le,               // the entity
                                2.5,              // max sideways reach
                                0.05,             // graze strength
                                0.30              // perfect hit strength
                        );
                        double vertical = 0.10;

                        Vector kb = forward.clone().multiply(wForward)
                                .add(side.clone().multiply(wSide))
                                .normalize()
                                .multiply(strength); // Mix em up

                        kb.setY(vertical);
                        le.setVelocity(kb);
                    }
                }
            }.runTaskTimer(UltraRoyales.getPlugin(), 0, 1);


            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SHULKER_SHOOT, 1, 1);
            player.playSound(player, Sound.ENTITY_SHULKER_SHOOT, 1, 1);
            if (!player.getInventory().getItemInMainHand().getItemMeta().hasCustomModelDataComponent()) return;
            var enchantments = item.getEnchantments();
            int power = item.getPersistentDataContainer().get(SPONGE_POWER, PersistentDataType.INTEGER);
            if (power > 1) {
                player.getInventory().setItemInMainHand(Items.wetSpongeSaber(power - 1));
                CooldownRegistry.setCooldown(player, UltraRoyales.saberCooldown, 0.75);
            } else {
                player.getInventory().setItemInMainHand(Items.spongeSaber());
                CooldownRegistry.setCooldown(player, UltraRoyales.saberCooldown, 5);
            }
        }
    }

    private static double computeDashStrength(
            Location dashStart,
            Vector dashDirection,    // normalized
            LivingEntity entity,
            double maxRadius,
            double minStrength,
            double maxStrength
    ) {
        // Convert to vectors
        Vector P = dashStart.toVector();
        Vector D = dashDirection.clone().setY(0).normalize();
        Vector E = entity.getLocation().toVector();

        // Vector from start → entity
        Vector PE = E.clone().subtract(P);

        // Project PE onto D
        double proj = PE.dot(D);

        // Perpendicular component = PE - proj * D
        Vector perp = PE.clone().subtract(D.clone().multiply(proj));

        // Distance to the dash path
        double d = perp.length();

        // Interpolate strength
        double t = 1.0 - Math.min(d / maxRadius, 1.0);
        return minStrength + t * (maxStrength - minStrength);
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
                    BlockData data = block.getBlockData();
                    if ((block.getType() == Material.WATER && data instanceof Levelled levelled && levelled.getLevel() == 0)
                            || block.getType() == Material.BUBBLE_COLUMN) {
                        block.setType(Material.AIR);
                        removed++;
                    }
                }
            }
        }

        return removed;
    }
}