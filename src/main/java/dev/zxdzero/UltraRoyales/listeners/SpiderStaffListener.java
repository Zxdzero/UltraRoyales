package dev.zxdzero.UltraRoyales.listeners;

import dev.zxdzero.UltraRoyales.SpiderAIController;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

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
}
