package me.youhavetrouble.knockback.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import me.youhavetrouble.knockback.BanException;
import me.youhavetrouble.knockback.Knockback;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UnbanCommand implements SimpleCommand {

    private final Knockback plugin;

    public UnbanCommand(Knockback plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        switch (invocation.arguments().length) {
            case 1 -> plugin.getUuidFromName(invocation.arguments()[0]).thenAccept(uuid -> {
                if (uuid == null) {
                    source.sendMessage(Component.text("Cannot get UUID"));
                    return;
                }
                UUID sourceId = source instanceof Player player ? player.getUniqueId() : null;
                try {
                    Knockback.unbanPlayer(uuid, sourceId);
                    source.sendMessage(Component.text(String.format("Unbanned %s", invocation.arguments()[0])));
                } catch (BanException e) {
                    source.sendMessage(Component.text("Encountered an error during ban process"));
                }
            });
            default -> source.sendMessage(Component.text("Usage: /unban <player>"));
        }
    }

    @Override
    public CompletableFuture<List<String>> suggestAsync(Invocation invocation) {
        return CompletableFuture.completedFuture(new ArrayList<>());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("knockback.unban");
    }
}
