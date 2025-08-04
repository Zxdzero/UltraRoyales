package dev.zxdzero.UltraRoyales.listeners;

import dev.zxdzero.UltraRoyales.Items;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class KnightsSaddleListener implements Listener {

    private int cooldown = 2400;

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
                horse.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.3375);
                horse.getAttribute(Attribute.MAX_HEALTH).setBaseValue(30);
                horse.setJumpStrength(1.0);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_HORSE_BREATHE, 1.0f, 1.0f);
                player.setCooldown(Material.SADDLE, cooldown);
                Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                Objective objective = scoreboard.getObjective("knightshorse");
                if (objective != null) {
                    objective.getScore(player.getName()).setScore(cooldown);
                }
            }
        }
    }
}
