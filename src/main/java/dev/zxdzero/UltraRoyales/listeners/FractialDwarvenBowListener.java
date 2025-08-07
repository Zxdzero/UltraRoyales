package dev.zxdzero.UltraRoyales.listeners;

import dev.zxdzero.UltraRoyales.UltraRoyales;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class FractialDwarvenBowListener implements Listener {

    @EventHandler
    public void onBowFire(EntityShootBowEvent e) {
        if (e.getBow().getItemMeta().hasCustomModelDataComponent() && e.getProjectile() instanceof Arrow arrow) {
            if (e.getBow().getItemMeta().getCustomModelDataComponent().getStrings().contains("ultraroyales:fractial_dwarvenbow")) {
                arrow.addCustomEffect(new PotionEffect(
                        PotionEffectType.MINING_FATIGUE,
                        600,  // 30 seconds duration
                        0     // Mining Fatigue I (amplifier 0 = level I)
                ), true);
                new BukkitRunnable() {
                    double radius = 2;

                    @Override
                    public void run() {
                        if (arrow.isDead() || arrow.isInBlock()) {
                            cancel();
                            return;
                        }

                        LivingEntity target = getNearestTarget(arrow, radius);
                        if (target != null) {
                            Location arrowLocation = arrow.getLocation();
                            Location targetLocation = target.getLocation().add(0, 1, 0);
                            Vector direction = targetLocation.toVector().subtract(arrowLocation.toVector()).normalize();
                            arrow.setVelocity(arrow.getVelocity().add(direction).normalize().multiply(2));
                            return;
                        } else {
                            radius = arrow.getLocation().distance(e.getEntity().getLocation());
                        }
                    }
                }.runTaskTimer(UltraRoyales.getPlugin(), 1, 1);
            }
        }
    }

    private LivingEntity getNearestTarget(Arrow arrow, double radius) {
        LivingEntity nearestEntity = null;
        double minDistance = radius;

        for (Entity entity : arrow.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof LivingEntity target && !target.equals(arrow.getShooter()) && !target.isDead()) {
                if (target instanceof Player player) {
                    if (!player.getGameMode().equals(GameMode.SURVIVAL)) return null;
                }

                double distance = arrow.getLocation().distance(target.getLocation());
                if (distance < minDistance) {
                    nearestEntity = target;
                    minDistance = distance;
                }
            }
        }
        return nearestEntity;
    }
}
