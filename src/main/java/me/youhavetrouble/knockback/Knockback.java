package me.youhavetrouble.knockback;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

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
    private DatabaseConnection databaseConnection;

    @Inject
    private Logger logger;

    @Inject
    public Knockback(ProxyServer server, @DataDirectory final Path folder) {
        this.config = loadConfig(folder);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) throws ClassNotFoundException {
        Toml database = config.getTable("Database");
        this.databaseConnection = new DatabaseConnection(
                database.getString("host"),
                Integer.parseInt(database.getLong("port").toString()),
                database.getString("username"),
                database.getString("password"),
                database.getString("database")
        );
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
}


