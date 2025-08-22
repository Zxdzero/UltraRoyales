package dev.zxdzero.UltraRoyales.listeners;

import dev.zxdzero.UltraRoyales.UltraRoyales;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.Random;

public class DwarvenBowListener implements Listener {

    @EventHandler
    public void onBowFire(EntityShootBowEvent e) {
        if (e.getBow().getItemMeta().hasCustomModelDataComponent() && e.getProjectile() instanceof Arrow arrow) {
            if (e.getBow().getItemMeta().getCustomModelDataComponent().getStrings().contains("ultraroyales:dwarvenbow")) {
                arrow.addCustomEffect(new PotionEffect(
                        PotionEffectType.MINING_FATIGUE,
                        200,  // 10 seconds duration
                        0     // Mining Fatigue I
                ), true);

                // 50% of homing effect
                Random rand = new Random();
                boolean win = rand.nextBoolean();

                if (win) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (arrow.isDead() || arrow.isOnGround()) {
                                cancel();
                                return;
                            }

                            LivingEntity target = findClosestTarget(arrow, e.getEntity(), 20); // 20 block search radius
                            if (target != null) {
                                steerArrow(arrow, target.getLocation(), 0.3); // 0.3 = turning speed
                            }

                            arrow.getWorld().spawnParticle(Particle.ASH, arrow.getLocation(), 2);
                        }

                    }.runTaskTimer(UltraRoyales.getPlugin(), 1, 1);
                }
            }
        }
    }

    private LivingEntity findClosestTarget(Arrow arrow, Entity shooter, double radius) {
        LivingEntity closest = null;
        double closestDist = Double.MAX_VALUE;

        // Parameters for trajectory simulation
        int ticksToSimulate = 20;
        double gravity = 0.05;
        double drag = 0.99;

        Vector pos = arrow.getLocation().toVector();
        Vector velocity = arrow.getVelocity();

        Scoreboard scoreboard = arrow.getServer().getScoreboardManager().getMainScoreboard();
        Team shooterTeam = scoreboard.getEntryTeam(shooter.getUniqueId().toString());

        for (Entity entity : arrow.getNearbyEntities(radius, radius, radius)) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.equals(shooter)) continue;

            Team entityTeam = scoreboard.getEntryTeam(living.getUniqueId().toString());
            if (shooterTeam != null && shooterTeam.equals(entityTeam)) continue;

            Vector entityPos = living.getLocation().toVector();

            double minDistToTrajectorySq = Double.MAX_VALUE;
            Vector simPos = pos.clone();
            Vector simVel = velocity.clone();

            for (int tick = 0; tick < ticksToSimulate; tick++) {
                // Calculate squared distance from entity to simulated point on trajectory
                double distSq = entityPos.distanceSquared(simPos);
                if (distSq < minDistToTrajectorySq) {
                    minDistToTrajectorySq = distSq;
                }

                // Simulate next position & velocity (simple physics)
                simVel.setY(simVel.getY() - gravity);        // gravity pulls down
                simVel.multiply(drag);                        // drag slows arrow
                simPos.add(simVel);                           // move arrow forward
            }

            // Prefer players over other LivingEntities if distance ties
            if (minDistToTrajectorySq < closestDist || (living instanceof Player && !(closest instanceof Player))) {
                closestDist = minDistToTrajectorySq;
                closest = living;
            }
        }

        return closest;
    }

    private void steerArrow(AbstractArrow arrow, Location targetLoc, double baseTurnSpeed) {
        Vector currentVel = arrow.getVelocity();
        double currentSpeed = currentVel.length();

        // Define a reference speed at which turnSpeed is max
        double maxSpeed = 1.5;

        // Scale turn speed from 0 (at speed=0) to baseTurnSpeed (at or above maxSpeed)
        double speedFactor = Math.min(currentSpeed / maxSpeed, 1.0);
        double turnSpeed = baseTurnSpeed * speedFactor;

        if (turnSpeed <= 0) return;

        Vector desiredDir = targetLoc.clone()
                .add(0, 0.5, 0) // aim a bit above target center
                .toVector()
                .subtract(arrow.getLocation().toVector())
                .normalize();

        Vector currentDir = currentVel.clone().normalize();

        Vector newDir = currentDir.multiply(1 - turnSpeed).add(desiredDir.multiply(turnSpeed)).normalize();

        Vector newVelocity = newDir.multiply(currentSpeed);

        arrow.setVelocity(newVelocity);
    }

}
