package me.youhavetrouble.knockback;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class DatabaseConnection {

    private final HikariDataSource dataSource;

    public DatabaseConnection(String server, int port, String username, String password, String database) throws ClassNotFoundException {
        synchronized (this) {
            Class.forName("com.mysql.cj.jdbc.Driver");
            HikariConfig config = new HikariConfig();
            String url = String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s", server, port, database, username, password);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setJdbcUrl(url);
            config.setMaximumPoolSize(10);
            dataSource = new HikariDataSource(config);
        }
    }

    public void insertBan(BanRecord banRecord) {
        String insertBan = "INSERT INTO `bans` (uuid, expires, reason) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(insertBan)) {
            statement.setString(1, banRecord.uuid().toString());
            statement.setLong(2, banRecord.endsAt());
            statement.setString(3, banRecord.reason());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public BanRecord getBan(UUID uuid) {
        String getBan = "SELECT expires, reason FROM `bans` WHERE uuid = ?";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(getBan)) {
            statement.setString(1, uuid.toString());
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return new BanRecord(uuid, result.getLong("expires"), result.getString("reason"));
            }
            return null;
        } catch (SQLException e) {
            return null;
        }
    }

    public BanRecord removeBan(UUID uuid) {
        String getBan = "DELETE FROM `bans` WHERE uuid = ?";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(getBan)) {
            statement.setString(1, uuid.toString());
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return new BanRecord(uuid, result.getLong("expires"), result.getString("reason"));
            }
            return null;
        } catch (SQLException e) {
            return null;
        }
    }


}
