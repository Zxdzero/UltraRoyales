package dev.zxdzero.UltraRoyales.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.util.Vector;

public class GhlochesterListener implements Listener {

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

//        if (!isSpecialRod(player.getInventory().getItemInMainHand())) return;

        switch (event.getState()) {
            case CAUGHT_ENTITY -> {
                Entity caught = event.getCaught();
                if (caught instanceof Player target) {

                    // Pull target towards shooter
                    Vector pull = player.getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(1.5);
                    target.setVelocity(pull);

                    // Diamond axe crit damage
                    double diamondAxeDamage = 9.0; // base diamond axe damage
                    double critMultiplier = 1.5; // crit multiplier
                    double finalDamage = diamondAxeDamage * critMultiplier;

//                    target.damage(finalDamage, player); // attribute damage to shooter
                }
            }
            case IN_GROUND, FAILED_ATTEMPT -> {
                // Grapple yourself towards the hook
                if (event.getHook() != null) {
                    Vector pull = event.getHook().getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(1.5);
                    pull.setY(0.5);
                    player.setVelocity(pull);
                }
            }
        }
    }

    @EventHandler
    public void onHookHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof FishHook hook)) return;
        if (!(hook.getShooter() instanceof Player player)) return;

//        if (!isSpecialRod(player.getInventory().getItemInMainHand())) return;

        if (event.getHitEntity() instanceof Player target) {
            double finalDamage = 9.0 * 1.5;
            target.damage(finalDamage, player);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerRespawnEvent event) {
        Player dead = event.getPlayer();

        // Loop all entities in the same world and remove hooks targeting the dead player
        dead.getWorld().getEntitiesByClass(FishHook.class).forEach(hook -> {
            if (hook.getHookedEntity() != null && hook.getHookedEntity().equals(dead)) {
                hook.remove(); // Remove the fishing hook
            }
        });
    }

}
