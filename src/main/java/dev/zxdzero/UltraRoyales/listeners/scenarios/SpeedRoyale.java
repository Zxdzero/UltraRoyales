package dev.zxdzero.UltraRoyales.listeners.scenarios;

import dev.zxdzero.UltraRoyales.UltraRoyales;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class SpeedRoyale extends Scenario {

    private HashMap<UUID, Integer> speedRoyaleLevels = new HashMap<>();

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent e) {
        if (! (e.getDamageSource().getCausingEntity() instanceof Player killer)) {
            return;
        }

        Integer level = speedRoyaleLevels.get(killer.getUniqueId());

        if (level == null) {
            speedRoyaleLevels.put(killer.getUniqueId(), 1);
        } else if (level < 5) {
            speedRoyaleLevels.replace(killer.getUniqueId(), level + 1);
        }

        killer.sendMessage("Test working");

    }

    public void tick() {
        for (UUID uuid : speedRoyaleLevels.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.SPEED,
                        30,
                        speedRoyaleLevels.get(uuid) - 1,
                        true,
                        false
                ));
            }
        }
    }

    public void end() {
        speedRoyaleLevels.clear();
    }

}
