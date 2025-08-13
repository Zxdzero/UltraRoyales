package dev.zxdzero.UltraRoyales.listeners;

import dev.zxdzero.UltraRoyales.UltraRoyales;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkeletalBarberListener implements Listener {

    private final Map<UUID, Long> firstCriticalHit = new HashMap<>();
    private static final long TIME_WINDOW = 2000;

    private static UltraRoyales plugin = UltraRoyales.getPlugin();

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof LivingEntity entity)) return;

        Player player = (Player) event.getDamager();

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (!weapon.hasItemMeta() || !weapon.getItemMeta().hasCustomModelDataComponent() || !event.isCritical()) return;
        if (!weapon.getItemMeta().getCustomModelDataComponent().getStrings().contains("ultraroyales:skeletalbarber")) return;

        UUID playerUUID = player.getUniqueId();
        long currentTime = System.currentTimeMillis();

        if (!firstCriticalHit.containsKey(playerUUID)) {
            firstCriticalHit.put(playerUUID, currentTime);
        } else {
            long firstHitTime = firstCriticalHit.get(playerUUID);
            long timeDifference = currentTime - firstHitTime;
            if (timeDifference <= TIME_WINDOW) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    entity.setVelocity(entity.getVelocity().add(new Vector(0, 0.7, 0)));
                }, 1L);
                entity.addPotionEffect(new PotionEffect(
                        PotionEffectType.WITHER,
                        40,
                        1
                ));
                barberEffects(entity.getLocation());
                firstCriticalHit.remove(playerUUID);

            } else {
                firstCriticalHit.put(playerUUID, currentTime);
            }
        }
    }

    public static void barberEffects(Location center) {
        World world = center.getWorld();
        if (world == null) return;

        // Get ground block for realistic debris
        Block groundBlock = world.getBlockAt(center.getBlockX(), center.getBlockY() - 1, center.getBlockZ());
        Material groundMaterial = groundBlock.getType();

        world.spawnParticle(Particle.BLOCK, center, 100, 1.5, 0.1, 1.5, 0.3, groundMaterial.createBlockData());
        world.spawnParticle(Particle.BLOCK_CRUMBLE, center, 100, 1.5, 0.1, 1.5, 0.3, groundMaterial.createBlockData());
        world.spawnParticle(Particle.CRIT, center, 20, 2, 0.5, 2, 0.2);
        world.spawnParticle(Particle.SWEEP_ATTACK, center, 3, 2, 0, 2, 0);

        world.playSound(center, Sound.ITEM_MACE_SMASH_GROUND_HEAVY, 1.0f, 1.0f);
    }
}
