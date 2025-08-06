package dev.zxdzero.UltraRoyales;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.bukkit.Bukkit.getServer;

/**
 * Controls spider minions for players. Each player can spawn up to 10 spiders
 * that will never attack them and will follow them when not targeting enemies.
 */
public class SpiderAIController {

    private static final Plugin plugin = UltraRoyales.getPlugin();
    private static final int MAX_SPIDERS_PER_PLAYER = 10;

    // Maps player UUID to their set of spider UUIDs
    private static final Map<UUID, Set<UUID>> playerSpiders = new ConcurrentHashMap<>();
    // Maps spider UUID to their current target UUID
    private static final Map<UUID, UUID> spiderTargets = new ConcurrentHashMap<>();
    // Maps spider UUID to their control task
    private static final Map<UUID, BukkitRunnable> spiderTasks = new ConcurrentHashMap<>();

    private SpiderAIController() {}

    /**
     * Spawns 10 spiders for a player at their location.
     * Removes any existing spiders first.
     *
     * @param player The player to spawn spiders for
     * @return Number of spiders successfully spawned
     */
    public static int spawnPlayerSpiders(Player player) {
        // Clean up existing spiders first
        removePlayerSpiders(player);

        UUID playerUUID = player.getUniqueId();
        Location spawnLocation = player.getLocation();
        Set<UUID> newSpiders = ConcurrentHashMap.newKeySet();
        int spawnedCount = 0;

        for (int i = 0; i < MAX_SPIDERS_PER_PLAYER; i++) {
            try {
                // Spawn spider near player with slight offset
                Location spiderLoc = spawnLocation.clone().add(
                        (Math.random() - 0.5) * 4, // Random X offset (-2 to +2)
                        0,
                        (Math.random() - 0.5) * 4  // Random Z offset (-2 to +2)
                );

                spawnLocation.getWorld().spawnParticle(
                        Particle.LARGE_SMOKE,
                        spiderLoc,
                        100,
                        0.5, 1, 0.5,
                        0.05
                );


                Spider spider = (Spider) spawnLocation.getWorld().spawnEntity(spiderLoc, EntityType.SPIDER);
                spider.customName(Component.text(player.getName() + "'s Spider", NamedTextColor.RED));
                spider.setCustomNameVisible(true);
                spider.setRemoveWhenFarAway(false);

                UUID spiderUUID = spider.getUniqueId();
                newSpiders.add(spiderUUID);
                startSpiderControl(spider, player);
                spawnedCount++;

            } catch (Exception e) {
                plugin.getLogger().warning("Failed to spawn spider for player " + player.getName() + ": " + e.getMessage());
            }
        }

        playerSpiders.put(playerUUID, newSpiders);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.0f, 1.0f);
        return spawnedCount;
    }

    /**
     * Sets a target for all of a player's spiders.
     *
     * @param player The player whose spiders should target the entity
     * @param target The entity to target (null to remove targeting and return to following)
     */
    public static void setPlayerSpidersTarget(Player player, LivingEntity target) {
        Set<UUID> spiderUUIDs = playerSpiders.get(player.getUniqueId());
        if (spiderUUIDs == null) return;

        for (UUID spiderUUID : spiderUUIDs) {
            Entity entity = plugin.getServer().getEntity(spiderUUID);
            if (entity instanceof Spider && entity.isValid()) {
                setSpiderTarget((Spider) entity, target);
            }
        }
    }

    /**
     * Removes all spiders belonging to a player.
     *
     * @param player The player whose spiders should be removed
     */
    public static void removePlayerSpiders(Player player) {
        UUID playerUUID = player.getUniqueId();
        Set<UUID> spiderUUIDs = playerSpiders.remove(playerUUID);

        if (spiderUUIDs != null) {
            for (UUID spiderUUID : spiderUUIDs) {
                cleanupSpider(spiderUUID);

                // Remove the actual spider entity
                Entity entity = plugin.getServer().getEntity(spiderUUID);
                if (entity != null && entity.isValid()) {
                    entity.remove();
                }
            }
        }
    }

    /**
     * Gets the number of valid spiders a player currently has.
     *
     * @param player The player
     * @return Number of valid spiders
     */
    public static int getPlayerSpiderCount(Player player) {
        Set<UUID> spiderUUIDs = playerSpiders.get(player.getUniqueId());
        if (spiderUUIDs == null) return 0;

        int count = 0;
        Iterator<UUID> iterator = spiderUUIDs.iterator();

        while (iterator.hasNext()) {
            UUID spiderUUID = iterator.next();
            Entity entity = plugin.getServer().getEntity(spiderUUID);

            if (entity instanceof Spider && entity.isValid() && !entity.isDead()) {
                count++;
            } else {
                // Clean up invalid spider
                iterator.remove();
                cleanupSpider(spiderUUID);
            }
        }

        return count;
    }

    /**
     * Shuts down the controller and cleans up all resources.
     */
    public static void shutdown() {
        for (BukkitRunnable task : spiderTasks.values()) {
            task.cancel();
        }
        spiderTasks.clear();
        spiderTargets.clear();
        playerSpiders.clear();

        for (World world : getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Spider) {
                    Spider spider = (Spider) entity;
                    // Check if this is one of our controlled spiders by the custom name
                    Component customName = spider.customName();
                    if (customName != null) {
                        String nameText = PlainTextComponentSerializer.plainText().serialize(customName);
                        if (nameText.endsWith("'s Spider")) {
                            entity.remove();
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets a specific target for a spider.
     */
    private static void setSpiderTarget(Spider spider, LivingEntity target) {
        UUID spiderUUID = spider.getUniqueId();

        if (target == null) {
            spiderTargets.remove(spiderUUID);
        } else {
            spiderTargets.put(spiderUUID, target.getUniqueId());
        }
    }

    /**
     * Starts the AI control task for a spider.
     */
    private static void startSpiderControl(Spider spider, Player owner) {
        UUID spiderUUID = spider.getUniqueId();
        UUID ownerUUID = owner.getUniqueId();

        BukkitRunnable controlTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Check if spider is still valid
                if (!spider.isValid() || spider.isDead()) {
                    cancel();
                    cleanupDeadSpider(spiderUUID, ownerUUID);
                    return;
                }

                // Check if owner is still online
                Player currentOwner = plugin.getServer().getPlayer(ownerUUID);
                if (currentOwner == null || !currentOwner.isOnline()) {
                    return; // Wait for owner to come back online
                }

                // Never target the owner
                if (spider.getTarget() == currentOwner) {
                    spider.setTarget(null);
                }

                UUID targetUUID = spiderTargets.get(spiderUUID);

                if (targetUUID != null) {
                    // Spider has a specific target
                    Entity targetEntity = plugin.getServer().getEntity(targetUUID);

                    if (targetEntity instanceof LivingEntity target && targetEntity.isValid() && !targetEntity.isDead()) {
                        // Make sure spider isn't targeting owner instead
                        if (target != currentOwner && spider.getTarget() != target) {
                            spider.setTarget(target);
                            spider.setAggressive(true);
                        }
                    } else {
                        // Target is dead/invalid, remove it and start following owner
                        spiderTargets.remove(spiderUUID);
                    }
                } else {
                    // No specific target, follow owner
                    followOwner(spider, currentOwner);
                }
            }
        };

        controlTask.runTaskTimer(plugin, 0L, 10L); // Run every 0.5 seconds
        spiderTasks.put(spiderUUID, controlTask);
    }

    /**
     * Makes a spider follow its owner.
     */
    private static void followOwner(Spider spider, Player owner) {
        double distance = spider.getLocation().distance(owner.getLocation());

        // If spider is too far from owner, teleport it closer
        if (distance > 20) {
            Location teleportLoc = owner.getLocation().add(
                    (Math.random() - 0.5) * 6,
                    0,
                    (Math.random() - 0.5) * 6
            );
            spider.teleport(teleportLoc);
        } else if (distance > 5) {
            // Spider is moderately far, make it pathfind to owner
            spider.setTarget(null); // Clear any combat target
            spider.setAggressive(false);

            // Use navigation to move towards owner
            try {
                spider.getPathfinder().moveTo(owner.getLocation(), 1.2);
            } catch (Exception e) {
                // Fallback: set owner as target briefly to encourage movement
                spider.setTarget(owner);
                // Immediately clear it on next tick
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (spider.isValid() && spider.getTarget() == owner) {
                        spider.setTarget(null);
                    }
                }, 1L);
            }
        }
    }

    /**
     * Cleans up a spider's control task and targeting data.
     */
    private static void cleanupSpider(UUID spiderUUID) {
        BukkitRunnable task = spiderTasks.remove(spiderUUID);
        if (task != null) {
            task.cancel();
        }
        spiderTargets.remove(spiderUUID);
    }

    /**
     * Cleans up a dead spider from player's spider set.
     */
    private static void cleanupDeadSpider(UUID spiderUUID, UUID ownerUUID) {
        cleanupSpider(spiderUUID);

        Set<UUID> spiders = playerSpiders.get(ownerUUID);
        if (spiders != null) {
            spiders.remove(spiderUUID);
        }
    }
}