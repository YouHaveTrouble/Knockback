package me.youhavetrouble.knockback.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.youhavetrouble.knockback.BanException;
import me.youhavetrouble.knockback.Knockback;
import me.youhavetrouble.knockback.util.StringUtil;
import net.kyori.adventure.text.Component;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class KickCommand implements SimpleCommand {

    private final Knockback plugin;
    private final ProxyServer server;

    public KickCommand(Knockback plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        switch (invocation.arguments().length) {
            case 0 -> source.sendMessage(Component.text("Usage: /kick <player> [reason]"));
            case 1 -> {
                Optional<Player> player = server.getPlayer(invocation.arguments()[0]);
                if (player.isEmpty()) {
                    source.sendMessage(Component.text("Player is not online"));
                    return;
                }
                UUID sourceId = source instanceof Player sourcePlayer ? sourcePlayer.getUniqueId() : null;
                try {
                    Knockback.kickPlayer(player.get(), sourceId, null);
                } catch (BanException e) {
                    source.sendMessage(Component.text("Encountered an error during kick process"));
                }
            }
            default -> {
                Optional<Player> player = server.getPlayer(invocation.arguments()[0]);
                if (player.isEmpty()) {
                    source.sendMessage(Component.text("Player is not online"));
                    return;
                }

                List<String> rawMsg = new ArrayList<>(Arrays.stream(invocation.arguments().clone()).toList());
                rawMsg.remove(0);

                String reasonMessage = String.join(" ", rawMsg);

                UUID sourceId = source instanceof Player sourcePlayer ? sourcePlayer.getUniqueId() : null;
                try {
                    Knockback.kickPlayer(player.get(), sourceId, reasonMessage);
                } catch (BanException e) {
                    source.sendMessage(Component.text("Encountered an error during kick process"));
                }
            }
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        if (!hasPermission(invocation)) return CompletableFuture.completedFuture(new ArrayList<>());
        if (invocation.arguments().length < 1) {
            return CompletableFuture.completedFuture(plugin.getOnlinePlayerNames());
        }
        if (invocation.arguments().length == 1) {
            return CompletableFuture.completedFuture(StringUtil.getCompletions(
                    plugin.getOnlinePlayerNames(), invocation.arguments()[0])
            );
        }
        return SimpleCommand.super.suggestAsync(invocation);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("knockback.ban");
    }
}
