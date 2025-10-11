package dev.zxdzero.UltraRoyales.listeners;

import dev.zxdzero.UltraRoyales.SpiderAIController;
import dev.zxdzero.UltraRoyales.UltraRoyales;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SpiderStaffListener implements Listener {

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player player && e.getEntity() instanceof LivingEntity victim && SpiderAIController.getPlayerSpiderCount(player) > 0 && player.getInventory().getItemInMainHand().getItemMeta().hasCustomModelDataComponent()) {
            if (player.getInventory().getItemInMainHand().getItemMeta().getCustomModelDataComponent().getStrings().contains("ultraroyales:spiderstaff")) {
                SpiderAIController.setPlayerSpidersTarget(player, victim);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        SpiderAIController.removePlayerSpiders(player);
    }

    @EventHandler
    public void onSpiderHit(EntityDamageByEntityEvent e){
        if (e.getEntity() instanceof Player player && e.getDamager().getType() == EntityType.CAVE_SPIDER) {
            Map<UUID, Set<UUID>> playerSpiders = SpiderAIController.getPlayerSpiders();
            if (playerSpiders.containsKey(player.getUniqueId()) && playerSpiders.get(player.getUniqueId()).contains(e.getDamager().getUniqueId())) {
                e.setCancelled(true);
                UltraRoyales.getPlugin().getLogger().warning("Spider staff backup measure activated");
            }
        }
    }
}
