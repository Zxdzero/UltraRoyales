package dev.zxdzero.UltraRoyales.commands;

import dev.zxdzero.UltraRoyales.listeners.BingoTheClownListener;
import dev.zxdzero.ZxdzeroEvents.registries.ItemMenuRegistry;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class BingoResetCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(Component.text("Only players can use this command!"));
            return true;
        }

        player.getPersistentDataContainer().set(BingoTheClownListener.BINGO_TRIES, PersistentDataType.INTEGER, 0);
        return true;
    }
}