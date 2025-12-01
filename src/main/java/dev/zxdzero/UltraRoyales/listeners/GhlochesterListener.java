package dev.zxdzero.UltraRoyales.listeners;

import dev.zxdzero.UltraRoyales.UltraRoyales;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class GhlochesterListener implements Listener {

    private NamespacedKey grappled = new NamespacedKey(UltraRoyales.getPlugin(), "grappled");

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

        if ((player.getInventory().getItemInMainHand().hasItemMeta() && player.getInventory().getItemInMainHand().getItemMeta().hasCustomModelDataComponent()
                && player.getInventory().getItemInMainHand().getItemMeta().getCustomModelDataComponent().getStrings().contains("ultraroyales:ghlochester")) ||
                (player.getInventory().getItemInOffHand().hasItemMeta() && player.getInventory().getItemInOffHand().getItemMeta().hasCustomModelDataComponent()
                && player.getInventory().getItemInOffHand().getItemMeta().getCustomModelDataComponent().getStrings().contains("ultraroyales:ghlochester"))) {

            switch (event.getState()) {
                case CAUGHT_ENTITY -> {
                    Entity caught = event.getCaught();
                    if (caught instanceof LivingEntity target) {

                        // Pull target towards shooter
                        Vector pull = player.getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(-1.2);
                        target.setVelocity(pull);
                    }
                }
                case IN_GROUND, FAILED_ATTEMPT -> {
                    if (event.getHook() != null) {
                        grapple(event.getHook(), player);
                    }
                }
                case REEL_IN -> {
                    FishHook hook = event.getHook();
                    if (hook == null) return;

                    Location base = hook.getLocation();
                    World world = base.getWorld();
                    double d = 0.3;

                    Location[] tests = new Location[] {
                            base.clone().add( d, 0, 0),   // +X
                            base.clone().add(-d, 0, 0),   // -X
                            base.clone().add(0, 0,  d),   // +Z
                            base.clone().add(0, 0, -d)    // -Z
                    };


                    for (Location test : tests) {
                        if (world.getBlockAt(test).getType().isSolid()) {
                            grapple(event.getHook(), player);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void grapple(FishHook hook, Player player) {
        Vector pull = hook.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(1.2);
        pull.setY(0.5);
        player.setVelocity(pull);
        player.getPersistentDataContainer().set(
                grappled,
                PersistentDataType.BYTE,
                (byte) 1
        );
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player player) {
            if (e.getCause().equals(EntityDamageEvent.DamageCause.FALL)) {
                if (player.getPersistentDataContainer().has(grappled)) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (player.getPersistentDataContainer().has(grappled) && player.isOnGround()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.getPersistentDataContainer().remove(grappled);
                }
            }.runTaskLater(UltraRoyales.getPlugin(), 1);
        }
    }

    @EventHandler
    public void onHookHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof FishHook hook)) return;
        if (!(hook.getShooter() instanceof Player player)) return;
        if (player.getInventory().getItemInMainHand().hasItemMeta() && player.getInventory().getItemInMainHand().getItemMeta().hasCustomModelDataComponent()
                && player.getInventory().getItemInMainHand().getItemMeta().getCustomModelDataComponent().getStrings().contains("ultraroyales:ghlochester")) {

            if (event.getHitEntity() instanceof LivingEntity target) {
                double finalDamage = 4.0;
                target.damage(finalDamage, player);
            }

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
