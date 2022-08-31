package me.youhavetrouble.knockback;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.youhavetrouble.knockback.commands.BanCommand;
import me.youhavetrouble.knockback.commands.KickCommand;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Plugin(
        id = "knockback",
        name = "Knockback",
        version = "1.0",
        description = "Bans for velocity",
        url = "https://youhavetrouble.me",
        authors = {"YouHaveTrouble"}
)
public class Knockback {

    private final Toml config;
    private static DatabaseConnection databaseConnection;
    private final ProxyServer server;

    @Inject
    private Logger logger;

    @Inject
    public Knockback(ProxyServer server, @DataDirectory final Path folder) {
        this.config = loadConfig(folder);
        this.server = server;
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

        server.getCommandManager().register("ban", new BanCommand(this));
        server.getCommandManager().register("kick", new KickCommand(this));
    }

    private Toml loadConfig(Path path) {
        File folder = path.toFile();
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

    public UUID getUUID(String playerName) {
        Optional<Player> optionalPlayer = server.getPlayer(playerName);
        if (optionalPlayer.isEmpty()) {
            return null;
        }
        return optionalPlayer.get().getUniqueId();
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
        Timestamp timestamp = length == null ? null : new Timestamp(Instant.now().getEpochSecond()+length);
        databaseConnection.insertBan(new BanRecord(bannedId, timestamp, reason));
    }

    /**
     * Unbans the player
     */
    public static void unbanPlayer(UUID bannedId, UUID source) throws BanException {
        if (databaseConnection == null) throw new BanException("Database connection not initialized");
        databaseConnection.removeBan(bannedId);
    }

    /**
     * @return Player ban entry. Null if player is not banned.
     */
    public static BanRecord getPlayerBan(UUID uuid) throws BanException {
        if (databaseConnection == null) throw new BanException("Database connection not initialized");
        return databaseConnection.getBan(uuid);
    }
}


