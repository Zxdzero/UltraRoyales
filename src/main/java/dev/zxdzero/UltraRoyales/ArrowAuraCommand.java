package dev.zxdzero.UltraRoyales;

import dev.zxdzero.UltraRoyales.listeners.DwarvenBowListener;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class ArrowAuraCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        int totalArrows = 20;
        double sphereRadius = 4.0;
        double backwardOffset = 1.5; // 5 feet = ~1.5 blocks
        Location center = player.getLocation().add(0, 1, 0); // Slightly above feet

        for (int i = 0; i < totalArrows; i++) {
            // Random point in unit sphere
            Vector offset;
            do {
                offset = new Vector(
                        (Math.random() * 2 - 1),
                        (Math.random() * 2 - 1),
                        (Math.random() * 2 - 1)
                );
            } while (offset.lengthSquared() > 1);

            offset.multiply(sphereRadius);
            Location arrowLoc = center.clone().add(offset);

            // Vector from player to arrow
            Vector toArrow = offset.clone().normalize();

            // Find a perpendicular direction
            Vector arbitrary = Math.abs(toArrow.getY()) < 0.9 ? new Vector(0, 1, 0) : new Vector(1, 0, 0);
            Vector perpendicular = toArrow.getCrossProduct(arbitrary).normalize();

            // Rotate that perpendicular vector randomly around the radial vector
            double theta = Math.random() * 2 * Math.PI;
            Vector orbitVel = rotateAroundAxis(perpendicular, toArrow, theta);

            // ✅ ADD: extra random rotation for variety
            double extraRotation = (Math.random() - 0.5) * Math.PI / 6; // ±15 degrees
            orbitVel = rotateAroundAxis(orbitVel, toArrow, extraRotation);

            // ✅ Adjust vertical velocity if downward
            if (orbitVel.getY() < 0) {
                orbitVel.setY(orbitVel.getY() * 0.3);
            }

            // ✅ Increase speed
            double speed = 1.1 + Math.random() * 0.4; // 1.2–1.3
            orbitVel.multiply(speed);

            // ✅ Move arrow 1.5 blocks *backwards* along its velocity vector
            Location spawnLoc = arrowLoc.clone().subtract(orbitVel.clone().normalize().multiply(backwardOffset));

            // Spawn arrow
            Arrow arrow = (Arrow) player.getWorld().spawnEntity(spawnLoc, EntityType.ARROW);
            arrow.setVelocity(orbitVel);
            arrow.setShooter(player);
            arrow.setGravity(true);
            arrow.setPersistent(false);
            arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            arrow.addCustomEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 600, 0), true);

            startHoming(arrow, player);
        }

        return true;
    }

    private Vector rotateAroundAxis(Vector vec, Vector axis, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return vec.clone().multiply(cos)
                .add(axis.clone().crossProduct(vec).multiply(sin))
                .add(axis.clone().multiply(axis.dot(vec) * (1 - cos)));
    }

    private void startHoming(Arrow arrow, Player shooter) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (arrow.isDead() || arrow.isOnGround()) {
                    cancel();
                    return;
                }
                steerArrowOrbit(arrow, shooter.getLocation().add(0,1,0), 0.4);

                arrow.getWorld().spawnParticle(Particle.ASH, arrow.getLocation(), 2);
            }
        }.runTaskTimer(UltraRoyales.getPlugin(), 1, 1);
    }

    public static void steerArrowOrbit(AbstractArrow arrow, Location targetLoc, double baseTurnSpeed) {
        Vector currentVel = arrow.getVelocity();
        double currentSpeed = currentVel.length();

        // Define a reference speed at which turnSpeed is max (tweak as needed)
        double maxSpeed = 1.5;

        // Scale turn speed from 0 (at speed=0) to baseTurnSpeed (at or above maxSpeed)
        double speedFactor = Math.min(currentSpeed / maxSpeed, 1.0);
        double turnSpeed = baseTurnSpeed * speedFactor;

        if (turnSpeed <= 0) return;

        // Vector from arrow to target (still the basis for desiredDir)
        Vector toTarget = targetLoc.clone().add(0, 0.5, 0).toVector()
                .subtract(arrow.getLocation().toVector());

        double distance = toTarget.length();

        // ✅ Repelling push if arrow is too close (below 1.5 blocks)
        if (distance < 1.5) {
            // Add small push away from the target
            Vector repel = toTarget.clone().normalize().multiply(-0.5 * (1.5 - distance));
            currentVel.add(repel);
            currentSpeed = currentVel.length(); // Recalculate speed after push
        }

        // ✅ Recalculate desired direction after repelling
        Vector desiredDir = toTarget.normalize();
        Vector currentDir = currentVel.clone().normalize();

        // Blend toward desired direction
        Vector newDir = currentDir.multiply(1 - turnSpeed).add(desiredDir.multiply(turnSpeed)).normalize();

        // ✅ Reduce deceleration to keep tighter orbit
        double adjustedSpeed = Math.max(currentSpeed, 0.5); // minimum speed clamp if needed
        Vector newVelocity = newDir.multiply(adjustedSpeed);

        arrow.setVelocity(newVelocity);
    }

}
