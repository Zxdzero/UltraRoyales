package dev.zxdzero.UltraRoyales.listeners;

import dev.zxdzero.UltraRoyales.UltraRoyales;
import dev.zxdzero.UltraRoyales.listeners.scenarios.Scenario;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ScenarioManager implements CommandExecutor, TabExecutor, Listener {
    private UltraRoyales plugin = UltraRoyales.getPlugin();

    public static String activeScenario = null;
    private static HashMap<String, Scenario> scenarios = new HashMap<>();
    private static BukkitTask tickTask;


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
            newScenario.start();
            plugin.getServer().getPluginManager().registerEvents(newScenario, plugin);
            tickTask = Bukkit.getScheduler().runTaskTimer(plugin, newScenario::tick, 0L, 5L);

            sender.sendMessage(Component.text("Starting scenario " + activeScenario, NamedTextColor.GREEN));
        }

        else if (args[0].equalsIgnoreCase("end")) {
            if (activeScenario != null) {
                sender.sendMessage(Component.text("Ending scenario " + activeScenario, NamedTextColor.YELLOW));
                shutdown();
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

    public static void shutdown() {
        tickTask.cancel();
        HandlerList.unregisterAll(scenarios.get(activeScenario));
        scenarios.get(activeScenario).end();
        activeScenario = null;
    }
}
