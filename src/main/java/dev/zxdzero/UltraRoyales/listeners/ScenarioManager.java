package dev.zxdzero.UltraRoyales.listeners;

import dev.zxdzero.UltraRoyales.UltraRoyales;
import dev.zxdzero.UltraRoyales.listeners.scenarios.Scenario;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ScenarioManager implements CommandExecutor, TabExecutor, Listener {
    private static UltraRoyales plugin = UltraRoyales.getPlugin();

    public static String activeScenario = null;
    private static HashMap<String, Scenario> scenarios = new HashMap<>();
    private static BukkitTask tickTask;

    public static NamespacedKey relogMarker = new NamespacedKey(plugin, "relog_marker");


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            return false;
        }

        if (args[0].equalsIgnoreCase("start")) {
            if (activeScenario != null)  {
                sender.sendMessage(Component.text("There is already an active scenario!", NamedTextColor.RED));
                return true;
            }
            if (args.length == 1) {
                sender.sendMessage(Component.text("Please specify a scenario!", NamedTextColor.RED));
                return true;
            }
            if (!scenarios.containsKey(args[1])) {
                sender.sendMessage(Component.text("Invalid scenario!", NamedTextColor.RED));
                return true;
            }

            Scenario newScenario = scenarios.get(args[1]);
            activeScenario = args[1];

            List<Location> pods = PodRecorder.getPods();
            List<Player> players = Bukkit.getOnlinePlayers().stream()
                    .filter(p -> p.getGameMode() == GameMode.SURVIVAL)
                    .collect(Collectors.toList());
            Collections.shuffle(players);

            for (int i = 0; i < players.size() && i < pods.size(); i++) {
                Location pod = pods.get(i);
                Location facingPod = faceTowards(pod, new Location(pod.getWorld(), 0, pod.getY() + 1, 0));
                players.get(i).teleport(facingPod);
            }

            newScenario.start();
            plugin.getServer().getPluginManager().registerEvents(newScenario, plugin);
            tickTask = Bukkit.getScheduler().runTaskTimer(plugin, newScenario::tick, 0L, 5L);

            sender.sendMessage(Component.text("Starting scenario " + activeScenario, NamedTextColor.GREEN));
        }

        else if (args[0].equalsIgnoreCase("end")) {
            if (activeScenario != null) {
                sender.sendMessage(Component.text("Ending scenario " + activeScenario, NamedTextColor.YELLOW));
                endScenario();
            } else {
                sender.sendMessage(Component.text("There is no scenario active!", NamedTextColor.RED));
                return true;
            }
        }

        else {
            return false;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("start", "end");
        } else if (args.length == 2 && args[0].equals("start")) {
            return scenarios.keySet().stream().toList();
        }
        return Collections.emptyList();
    }

    public static void registerScenario(String id, Scenario scenario) {
        scenarios.put(id,scenario);
    }

    public static void endScenario() {
        tickTask.cancel();
        HandlerList.unregisterAll(scenarios.get(activeScenario));
        scenarios.get(activeScenario).end();
        activeScenario = null;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.SURVIVAL) {
                player.getInventory().clear();
            }
        }
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        if (activeScenario != null && e.getPlayer().getGameMode() == GameMode.SURVIVAL) {
            e.getPlayer().getPersistentDataContainer().set(relogMarker, PersistentDataType.BOOLEAN, true);
        }
    }

    @EventHandler
    public void onConnect(PlayerJoinEvent e) {
        if (activeScenario == null && e.getPlayer().getPersistentDataContainer().getOrDefault(relogMarker, PersistentDataType.BOOLEAN, false)) {
            e.getPlayer().getInventory().clear();
        }
        e.getPlayer().getPersistentDataContainer().remove(relogMarker);
    }

    public Location faceTowards(Location from, Location to) {
        Location result = from.clone();

        // Calculate differences
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();

        // Math.atan2 returns radians, so convert to degrees
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));

        result.setYaw(yaw);
        result.setPitch(0f); // Look straight ahead (optional)

        return result;
    }
}
