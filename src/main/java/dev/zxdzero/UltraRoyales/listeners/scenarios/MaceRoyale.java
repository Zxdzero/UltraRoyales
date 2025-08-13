package dev.zxdzero.UltraRoyales.listeners.scenarios;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class MaceRoyale extends Scenario {
    @EventHandler
    public void onPlayerKill(EntityDeathEvent e) {
        if (! (e.getDamageSource().getCausingEntity() instanceof Player killer)) {
            return;
        }

        if (killer.getInventory().getItemInMainHand() instanceof ItemStack item) {
            if (item.getType() == Material.MACE && !item.getItemMeta().hasCustomModelDataComponent()) {
                item.addUnsafeEnchantment(Enchantment.WIND_BURST, item.getEnchantmentLevel(Enchantment.WIND_BURST) + 1);
            }
        }

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!e.getPlayer().getInventory().contains(Material.MACE)) {
            e.getPlayer().give(new ItemStack(Material.MACE));
        }
    }

    @Override
    public void start() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.give(new ItemStack(Material.MACE));
        }
    }

    @Override
    public void end() {
    }
}
