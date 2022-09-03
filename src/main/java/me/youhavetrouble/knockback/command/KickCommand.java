package me.youhavetrouble.knockback.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.youhavetrouble.knockback.Knockback;
import me.youhavetrouble.knockback.PluginMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;

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
                player.get().disconnect(PluginMessage.KICK_FORMAT.getMessage().replaceText(TextReplacementConfig.builder()
                        .match("%reason%")
                        .replacement(PluginMessage.NO_REASON.getMessage()).build())
                );
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
                player.get().disconnect(PluginMessage.KICK_FORMAT.getMessage().replaceText(TextReplacementConfig.builder()
                        .match("%reason%")
                        .replacement(Component.text(reasonMessage))
                        .build())
                );
            }
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        if (!hasPermission(invocation)) return CompletableFuture.completedFuture(new ArrayList<>());
        if (invocation.arguments().length <= 1) {
            return plugin.getOnlinePlayerNames();
        }
        return SimpleCommand.super.suggestAsync(invocation);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("knockback.ban");
    }
}
