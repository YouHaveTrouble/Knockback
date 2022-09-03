package me.youhavetrouble.knockback.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import me.youhavetrouble.knockback.BanException;
import me.youhavetrouble.knockback.Knockback;
import me.youhavetrouble.knockback.util.StringUtil;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BanCommand implements SimpleCommand {

    private final Knockback plugin;

    public BanCommand(Knockback plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        switch (invocation.arguments().length) {
            case 0 -> source.sendMessage(Component.text("Usage: /ban <player> [reason]"));
            case 1 -> plugin.getUuidFromName(invocation.arguments()[0]).thenAccept(uuid -> {
                if (uuid == null) {
                    source.sendMessage(Component.text("Cannot get UUID"));
                    return;
                }
                UUID sourceId = source instanceof Player player ? player.getUniqueId() : null;
                try {
                    Knockback.banPlayer(uuid, null, null, sourceId);
                    source.sendMessage(Component.text(String.format("Banned %s", invocation.arguments()[0])));
                } catch (BanException e) {
                    source.sendMessage(Component.text("Encountered an error during ban process"));
                }
            });
            default -> plugin.getUuidFromName(invocation.arguments()[0]).thenAccept(uuid -> {
                if (uuid == null) {
                    source.sendMessage(Component.text("Cannot get UUID"));
                    return;
                }

                List<String> rawMsg = new ArrayList<>(Arrays.stream(invocation.arguments().clone()).toList());
                rawMsg.remove(0);

                String reasonMessage = String.join(" ", rawMsg);

                UUID sourceId = source instanceof Player player ? player.getUniqueId() : null;
                try {
                    Knockback.banPlayer(uuid, null, reasonMessage, sourceId);
                    source.sendMessage(Component.text(String.format("Banned %s", invocation.arguments()[0])));
                } catch (BanException e) {
                    source.sendMessage(Component.text("Encountered an error during ban process"));
                }
            });
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
