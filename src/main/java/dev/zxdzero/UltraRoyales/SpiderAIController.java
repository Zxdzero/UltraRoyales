package dev.zxdzero.UltraRoyales;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.bukkit.Bukkit.getServer;

public class SpiderAIController {

    private static final Plugin plugin = UltraRoyales.getPlugin();
    private static final int MAX_SPIDERS_PER_PLAYER = 10;

    private static final NamespacedKey SPIDER_TAG = new NamespacedKey(plugin, "minion_spider");

    private static final Map<UUID, Set<UUID>> playerSpiders = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> spiderTargets = new ConcurrentHashMap<>();
    private static final Map<UUID, BukkitRunnable> spiderTasks = new ConcurrentHashMap<>();

    private SpiderAIController() {}

    public static int spawnPlayerSpiders(Player player) {
        removePlayerSpiders(player);

        UUID playerUUID = player.getUniqueId();
        Location spawnLocation = player.getLocation();
        Set<UUID> newSpiders = ConcurrentHashMap.newKeySet();
        int spawnedCount = 0;

        for (int i = 0; i < MAX_SPIDERS_PER_PLAYER; i++) {
            try {
                Location spiderLoc = spawnLocation.clone().add(
                        (Math.random() - 0.5) * 4,
                        0,
                        (Math.random() - 0.5) * 4
                );

                spawnLocation.getWorld().spawnParticle(
                        Particle.LARGE_SMOKE,
                        spiderLoc,
                        100,
                        0.5, 1, 0.5,
                        0.05
                );

                CaveSpider spider = (CaveSpider) spawnLocation.getWorld().spawnEntity(spiderLoc, EntityType.CAVE_SPIDER);
                spider.setRemoveWhenFarAway(false);
                spider.getPersistentDataContainer().set(SPIDER_TAG, PersistentDataType.BYTE, (byte) 1);

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

    public static void setPlayerSpidersTarget(Player player, LivingEntity target) {
        Set<UUID> spiderUUIDs = playerSpiders.get(player.getUniqueId());
        if (spiderUUIDs == null) return;

        for (UUID spiderUUID : spiderUUIDs) {
            Entity entity = plugin.getServer().getEntity(spiderUUID);
            if (entity instanceof CaveSpider && entity.isValid()) {
                setSpiderTarget((CaveSpider) entity, target);
            }
        }
    }

    public static void removePlayerSpiders(Player player) {
        UUID playerUUID = player.getUniqueId();
        Set<UUID> spiderUUIDs = playerSpiders.remove(playerUUID);

        if (spiderUUIDs != null) {
            for (UUID spiderUUID : spiderUUIDs) {
                cleanupSpider(spiderUUID);

                Entity entity = plugin.getServer().getEntity(spiderUUID);
                if (entity != null && entity.isValid()) {
                    entity.remove();
                }
            }
        }
    }

    public static int getPlayerSpiderCount(Player player) {
        Set<UUID> spiderUUIDs = playerSpiders.get(player.getUniqueId());
        if (spiderUUIDs == null) return 0;

        int count = 0;
        Iterator<UUID> iterator = spiderUUIDs.iterator();

        while (iterator.hasNext()) {
            UUID spiderUUID = iterator.next();
            Entity entity = plugin.getServer().getEntity(spiderUUID);

            if (entity instanceof CaveSpider && entity.isValid() && !entity.isDead()) {
                count++;
            } else {
                iterator.remove();
                cleanupSpider(spiderUUID);
            }
        }

        return count;
    }

    public static void shutdown() {
        for (BukkitRunnable task : spiderTasks.values()) {
            task.cancel();
        }
        spiderTasks.clear();
        spiderTargets.clear();
        playerSpiders.clear();

        for (World world : getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof CaveSpider) {
                    if (entity.getPersistentDataContainer().has(SPIDER_TAG, PersistentDataType.BYTE)) {
                        entity.remove();
                    }
                }
            }
        }
    }

    private static void setSpiderTarget(CaveSpider spider, LivingEntity target) {
        UUID spiderUUID = spider.getUniqueId();

        if (target == null) {
            spiderTargets.remove(spiderUUID);
        } else {
            spiderTargets.put(spiderUUID, target.getUniqueId());
        }
    }

    private static void startSpiderControl(CaveSpider spider, Player owner) {
        UUID spiderUUID = spider.getUniqueId();
        UUID ownerUUID = owner.getUniqueId();

        BukkitRunnable controlTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!spider.isValid() || spider.isDead()) {
                    cancel();
                    cleanupDeadSpider(spiderUUID, ownerUUID);
                    return;
                }

                Player currentOwner = plugin.getServer().getPlayer(ownerUUID);
                if (currentOwner == null || !currentOwner.isOnline()) {
                    return;
                }

                if (spider.getTarget() == currentOwner) {
                    spider.setTarget(null);
                }

                UUID targetUUID = spiderTargets.get(spiderUUID);

                if (targetUUID != null) {
                    Entity targetEntity = plugin.getServer().getEntity(targetUUID);
                    if (targetEntity instanceof LivingEntity target && target.isValid() && !target.isDead()) {
                        if (target != currentOwner && spider.getTarget() != target) {
                            spider.setTarget(target);
                            spider.setAggressive(true);
                        }
                    } else {
                        spiderTargets.remove(spiderUUID);
                    }
                } else {
                    followOwner(spider, currentOwner);
                }
            }
        };

        controlTask.runTaskTimer(plugin, 0L, 10L);
        spiderTasks.put(spiderUUID, controlTask);
    }

    private static void followOwner(CaveSpider spider, Player owner) {
        double distance = spider.getLocation().distance(owner.getLocation());

        if (distance > 20) {
            Location teleportLoc = owner.getLocation().add(
                    (Math.random() - 0.5) * 6,
                    0,
                    (Math.random() - 0.5) * 6
            );
            spider.teleport(teleportLoc);
        } else if (distance > 5) {
            spider.setTarget(null);
            spider.setAggressive(false);

            try {
                spider.getPathfinder().moveTo(owner.getLocation(), 1.2);
            } catch (Exception e) {
                spider.setTarget(owner);
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (spider.isValid() && spider.getTarget() == owner) {
                        spider.setTarget(null);
                    }
                }, 1L);
            }
        }
    }

    private static void cleanupSpider(UUID spiderUUID) {
        BukkitRunnable task = spiderTasks.remove(spiderUUID);
        if (task != null) {
            task.cancel();
        }
        spiderTargets.remove(spiderUUID);
    }

    private static void cleanupDeadSpider(UUID spiderUUID, UUID ownerUUID) {
        cleanupSpider(spiderUUID);

        Set<UUID> spiders = playerSpiders.get(ownerUUID);
        if (spiders != null) {
            spiders.remove(spiderUUID);
        }
    }
}
