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

public class TembanCommand implements SimpleCommand {

    private final Knockback plugin;

    public TembanCommand(Knockback plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        switch (invocation.arguments().length) {
            case 0, 1 -> source.sendMessage(Component.text("Usage: /tempban <player> <time> [reason]"));
            case 2 -> plugin.getUuidFromName(invocation.arguments()[0]).thenAccept(uuid -> {
                if (uuid == null) {
                    source.sendMessage(Component.text("Cannot get UUID"));
                    return;
                }

                Long duration = getDurationInMillis(invocation.arguments()[1]);
                System.out.println("Duration: "+ duration);

                if (duration == null) {
                    source.sendMessage(Component.text("Invalid duration"));
                    return;
                }

                UUID sourceId = source instanceof Player player ? player.getUniqueId() : null;
                try {
                    Knockback.banPlayer(uuid, duration, null, sourceId);
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

                Long duration = getDurationInMillis(invocation.arguments()[1]);

                if (duration == null) {
                    source.sendMessage(Component.text("Invalid duration"));
                    return;
                }

                List<String> rawMsg = new ArrayList<>(Arrays.stream(invocation.arguments().clone()).toList());
                rawMsg.remove(0);
                rawMsg.remove(1);

                String reasonMessage = String.join(" ", rawMsg);

                UUID sourceId = source instanceof Player player ? player.getUniqueId() : null;
                try {
                    Knockback.banPlayer(uuid, duration, reasonMessage, sourceId);
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
        if (invocation.arguments().length == 2 && invocation.arguments()[1].length() >= 1) {
            ArrayList<String> suggestions = new ArrayList<>();
            if (!invocation.arguments()[1].matches("([0-9]+)")) return CompletableFuture.completedFuture(suggestions);
            suggestions.add(invocation.arguments()[1]+"s");
            suggestions.add(invocation.arguments()[1]+"m");
            suggestions.add(invocation.arguments()[1]+"h");
            suggestions.add(invocation.arguments()[1]+"d");
            suggestions.add(invocation.arguments()[1]+"mo");
            suggestions.add(invocation.arguments()[1]+"y");
            return CompletableFuture.completedFuture(StringUtil.getCompletions(suggestions, invocation.arguments()[1]));
        }
        return SimpleCommand.super.suggestAsync(invocation);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("knockback.tempban");
    }

    private Long getDurationInMillis(String durationString) {
        try {
            if (durationString.endsWith("s")) {
                long duration = Long.parseLong(durationString.replace("s", ""));
                return duration;
            }
            if (durationString.endsWith("m")) {
                long duration = Long.parseLong(durationString.replace("m", ""));
                return duration * 60;
            }
            if (durationString.endsWith("h")) {
                long duration = Long.parseLong(durationString.replace("h", ""));
                return duration * 60 * 60;
            }
            if (durationString.endsWith("d")) {
                long duration = Long.parseLong(durationString.replace("d", ""));
                return duration * 60 * 60 * 24;
            }
            if (durationString.endsWith("mo")) {
                long duration = Long.parseLong(durationString.replace("mo", ""));
                return duration * 60 * 60 * 24 * 30;
            }
            if (durationString.endsWith("y")) {
                long duration = Long.parseLong(durationString.replace("y", ""));
                return duration * 60 * 60 * 24 * 30 * 365;
            }
            return Long.parseLong(durationString);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
