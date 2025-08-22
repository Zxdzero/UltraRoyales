package dev.zxdzero.UltraRoyales.listeners;

import dev.zxdzero.UltraRoyales.UltraRoyales;
import dev.zxdzero.ZxdzeroEvents.registries.CooldownRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ElectricConchListener implements Listener {

    private final UltraRoyales plugin = UltraRoyales.getPlugin();
    private final double WAVE_RADIUS = 10.0; // Maximum wave radius
    private final int STUN_DURATION = 60; // 3 seconds
    private final double KNOCKBACK_STRENGTH = 1.5;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction().toString().contains("LEFT_CLICK") && player.getInventory().getItemInMainHand().hasItemMeta() && player.getInventory().getItemInMainHand().getItemMeta().hasCustomModelDataComponent()) {
            if (!player.getInventory().getItemInMainHand().getItemMeta().getCustomModelDataComponent().getStrings().contains("ultraroyales:electricconch")) return;

            int cooldown = CooldownRegistry.getCooldown(player, UltraRoyales.conchCooldown);
            if (cooldown > 0) {
                player.sendMessage(Component.text("You must wait another " + cooldown + " seconds to use this again!", NamedTextColor.RED));
                return;
            }

            Location center = player.getLocation();
            center.getWorld().playSound(center, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 2.0f, 0.5f);

            new BukkitRunnable() {
                double radius = 1.0;

                @Override
                public void run() {
                    if (radius > WAVE_RADIUS) {
                        cancel();
                        return;
                    }

                    createTidalWaveEffect(center, radius);

                    for (Entity target : center.getWorld().getLivingEntities()) {
                        if (target.equals(player)) continue;

                        double distance = target.getLocation().distance(center);
                        if (distance <= radius + 0.5 && distance >= radius) {
                            if (target instanceof Player player) {
                                // Apply stun effects
                                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, STUN_DURATION, 3));
                                player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, STUN_DURATION, 2));
                            }

                            // Knockback effect
                            Vector direction = target.getLocation().subtract(center).toVector().normalize();
                            direction.setY(0.4); // Add upward component
                            direction.multiply(KNOCKBACK_STRENGTH);
                            target.setVelocity(direction);

                            target.getWorld().spawnParticle(Particle.SPLASH, target.getLocation().add(0, 1, 0), 15, 0.3, 0.3, 0.3, 0.2);
                        }
                    }

                    radius += 0.6;
                }
            }.runTaskTimer(plugin, 0L, 1L);

            CooldownRegistry.setCooldown(player, UltraRoyales.conchCooldown, 60, false);
        }
    }

    private void createTidalWaveEffect(Location center, double radius) {
        int points = (int) (radius * 12); // More density for fuller wave

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);

            // Create a wall of water 3 blocks tall
            for (double height = 0; height <= 3.0; height += 0.3) {
                Location particleLoc = new Location(center.getWorld(), x, center.getY() + height, z);

                // Dense water splash particles
                center.getWorld().spawnParticle(Particle.SPLASH, particleLoc, 10, 0.2, 0.15, 0.2, 0.4);

                // Water droplets for more density
                center.getWorld().spawnParticle(Particle.UNDERWATER, particleLoc, 5, 0.15, 0.08, 0.15, 0.05);

                // Persistent bubbles at lower heights for foam effect
                if (height <= 1.5) {
                    center.getWorld().spawnParticle(Particle.BUBBLE_POP, particleLoc, 3, 0.2, 0.1, 0.2, 0);
                }

                // Add smaller white spray at the top
                if (height >= 2.0) {
                    center.getWorld().spawnParticle(Particle.CRIT, particleLoc, 1, 0.1, 0.08, 0.2, 0.02);
                }
            }

            // Create inner turbulence for thickness
            if (radius > 2.0) {
                double innerRadius = radius * 0.7;
                double innerX = center.getX() + innerRadius * Math.cos(angle);
                double innerZ = center.getZ() + innerRadius * Math.sin(angle);

                for (double height = 0; height <= 2.0; height += 0.5) {
                    Location innerLoc = new Location(center.getWorld(), innerX, center.getY() + height, innerZ);
                    center.getWorld().spawnParticle(Particle.BUBBLE_POP, innerLoc, 2, 0.2, 0.1, 0.2, 0);
                }
            }
        }
    }
}
