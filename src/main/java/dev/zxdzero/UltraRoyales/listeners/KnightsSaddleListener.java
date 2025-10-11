package dev.zxdzero.UltraRoyales.listeners;

import dev.zxdzero.UltraRoyales.Items;
import dev.zxdzero.UltraRoyales.UltraRoyales;
import dev.zxdzero.ZxdzeroEvents.registries.CooldownRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class KnightsSaddleListener implements Listener {

    private int cooldown = 180;

    @EventHandler
    public void onDismount(EntityDismountEvent e) {
        if (e.getEntity() instanceof Player player && e.getDismounted() instanceof Horse horse) {
            if (Boolean.TRUE.equals(horse.getPersistentDataContainer().get(Items.knightsHorse, PersistentDataType.BOOLEAN))) {
                horse.remove();
                player.getWorld().spawnParticle(
                        Particle.LARGE_SMOKE,
                        player.getLocation().add(0, -1, 0),
                        100,
                        0.5, 1, 0.5,
                        0.05
                );
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_HORSE_BREATHE, 1.0f, 1.0f);
                player.setCooldown(Material.SADDLE, cooldown);
                CooldownRegistry.setCooldown(player, UltraRoyales.saddleCooldown, cooldown);
            }
        }
    }
}
