package me.youhavetrouble.knockback;

import com.moandjiezana.toml.Toml;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public enum PluginMessage {

    NO_REASON("no-reason", Component.text("No reason provided.")),
    KICK_FORMAT("kick-format", Component.text("You were kicked.<newline>Reason: %reason%")),
    BAN_FORMAT("ban-format", Component.text("You were permanently banned.<newline>Reason: %reason%")),
    TEMPBAN_FORMAT("tempban-format", Component.text("You were banned until %time%.<newline>Reason: %reason%"))
    ;

    private final String path;
    private Component message;

    PluginMessage(String path, Component message) {
        this.path = path;
        this.message = message;
    }

    public void setMessage(Component message) {
        this.message = message;
    }

    public Component getMessage() {
        return message;
    }

    public static void reloadFromConfig() {
        Knockback.getPlugin().loadConfig();
        Toml messages = Knockback.getPlugin().getConfig().getTable("Messages");
        for (PluginMessage pluginMessage : PluginMessage.values()) {
            pluginMessage.setMessage(MiniMessage.miniMessage().deserialize(messages.getString(pluginMessage.path)));
        }
    }

    public static Component getBannedMessage(BanRecord banRecord) {
        Component bannedMessage;
        if (banRecord.getTimestamp() == null) {
            bannedMessage = PluginMessage.BAN_FORMAT.getMessage().replaceText(builder -> {
                builder.match("%reason%");
                builder.replacement(banRecord.getReason());
            });
        } else {
            bannedMessage = PluginMessage.TEMPBAN_FORMAT.getMessage();
            bannedMessage = bannedMessage.replaceText(builder -> {
                builder.match("%reason%");
                builder.replacement(banRecord.getReason());
            });
            bannedMessage = bannedMessage.replaceText(builder -> {
                builder.match("%time%");
                builder.replacement(banRecord.getTimestamp().toString());
            });
        }
        return bannedMessage;
    }
}
