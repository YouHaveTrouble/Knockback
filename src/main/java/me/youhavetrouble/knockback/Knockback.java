package me.youhavetrouble.knockback;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.youhavetrouble.knockback.command.BanCommand;
import me.youhavetrouble.knockback.command.KickCommand;
import me.youhavetrouble.knockback.command.TembanCommand;
import me.youhavetrouble.knockback.command.UnbanCommand;
import me.youhavetrouble.knockback.listener.JoinListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.slf4j.Logger;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Plugin(
        id = "knockback",
        name = "Knockback",
        version = "1.0",
        description = "Bans for velocity",
        url = "https://youhavetrouble.me",
        authors = {"YouHaveTrouble"}
)
public class Knockback {

    private static Knockback plugin;
    private final Toml config;
    private static DatabaseConnection databaseConnection;
    private final ProxyServer server;

    @Inject
    private Logger logger;

    private final Path configFolder;

    @Inject
    public Knockback(ProxyServer server, @DataDirectory final Path folder) {
        this.configFolder = folder;
        this.config = loadConfig();
        this.server = server;
        plugin = this;
        PluginMessage.reloadFromConfig();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) throws ClassNotFoundException {

        Toml database = config.getTable("Database");

        databaseConnection = new DatabaseConnection(
                database.getString("host"),
                Integer.parseInt(database.getLong("port").toString()),
                database.getString("username"),
                database.getString("password"),
                database.getString("database")
        );

        server.getEventManager().register(this, new JoinListener());

        server.getCommandManager().register("ban", new BanCommand(this));
        server.getCommandManager().register("tempban", new TembanCommand(this));
        server.getCommandManager().register("kick", new KickCommand(this));

        server.getCommandManager().register("unban", new UnbanCommand(this));
    }

    public static Knockback getPlugin() {
        return plugin;
    }

    protected Toml loadConfig() {
        File folder = configFolder.toFile();
        File file = new File(folder, "config.toml");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try (InputStream input = getClass().getResourceAsStream("/" + file.getName())) {
                if (input != null) {
                    Files.copy(input, file.toPath());
                } else {
                    file.createNewFile();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
                return null;
            }
        }
        return new Toml().read(file);
    }

    public ProxyServer getServer() {
        return server;
    }

    /**
     * @param length Length in milliseconds
     * Bans the player
     */
    public static void banPlayer(UUID bannedId, Long length, String reason, UUID source) throws BanException {
        if (databaseConnection == null) throw new BanException("Database connection not initialized");
        Timestamp timestamp = length == null ? null : new Timestamp((Instant.now().getEpochSecond()+length)*1000);
        BanRecord banRecord = new BanRecord(bannedId, timestamp, reason);
        databaseConnection.insertBan(banRecord);
        Optional<Player> optionalPlayer = Knockback.plugin.server.getPlayer(bannedId);
        if (optionalPlayer.isPresent()) {
            Player player = optionalPlayer.get();
            player.disconnect(PluginMessage.getBannedMessage(banRecord));
        }
        databaseConnection.insertBanLog(
                new ArchievedBanEntry(
                        ArchievedBanEntry.Action.BAN,
                        banRecord.getUuid(),
                        source,
                        banRecord.getReason(),
                        banRecord.getTimestamp()
                )
        );
    }

    public static void unbanPlayer(UUID bannedId, UUID source, boolean shouldLog) throws BanException {
        if (databaseConnection == null) throw new BanException("Database connection not initialized");
        databaseConnection.removeBan(bannedId);
        if (!shouldLog) return;
        databaseConnection.insertBanLog(
                new ArchievedBanEntry(
                        ArchievedBanEntry.Action.UNBAN,
                        bannedId,
                        source,
                        null,
                        null
                )
        );
    }

    /**
     * Unbans the player
     */
    public static void unbanPlayer(UUID bannedId, UUID source) throws BanException {
        unbanPlayer(bannedId, source, true);
    }

    public static void kickPlayer(Player player, UUID source, String reason) throws BanException {
        if (databaseConnection == null) throw new BanException("Database connection not initialized");
        if (!player.isActive()) return;

        Component reasonComponent = reason != null ? Component.text(reason) : PluginMessage.NO_REASON.getMessage();

        player.disconnect(PluginMessage.KICK_FORMAT.getMessage().replaceText(TextReplacementConfig.builder()
                .match("%reason%")
                .replacement(reasonComponent).build())
        );
        databaseConnection.insertBanLog(
                new ArchievedBanEntry(
                        ArchievedBanEntry.Action.KICK,
                        player.getUniqueId(),
                        source,
                        reason,
                        null
                )
        );
    }

    /**
     * @return Player ban entry. Null if player is not banned.
     */
    public static BanRecord getPlayerBan(UUID uuid) throws BanException {
        if (databaseConnection == null) throw new BanException("Database connection not initialized");
        return databaseConnection.getBan(uuid);
    }

    /**
     * Asks mojang API for player's UUID
     * @return Player's UUID. Null if player not found or on error
     */
    private UUID askMojangForUuid(String playerName) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + playerName).openStream()));
            String stringUuid = (((JsonObject)JsonParser.parseReader(in)).get("id")).toString().replaceAll("\"", "");
            stringUuid = stringUuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
            in.close();
            return UUID.fromString(stringUuid);
        } catch (IOException | IllegalArgumentException | JsonParseException | NullPointerException e) {
            return null;
        }
    }

    public ArrayList<String> getOnlinePlayerNames() {
        ArrayList<String> suggestions = new ArrayList<>();
        server.getAllPlayers().forEach(player -> suggestions.add(player.getUsername()));
        return suggestions;
    }

    public Toml getConfig() {
        return config;
    }

    /**
     * Supplies player's UUID. Returns null if player is not on the server and wasn't found in mojang services.
     * @return Player's UUID or null if not found.
     */
    public CompletableFuture<UUID> getUuidFromName(String playerName) {
        Optional<Player> optionalPlayer = server.getPlayer(playerName);
        return optionalPlayer
                .map(player -> CompletableFuture.completedFuture(player.getUniqueId()))
                .orElseGet(() -> CompletableFuture.supplyAsync(() -> askMojangForUuid(playerName)));
    }
}


