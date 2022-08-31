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

    protected DatabaseConnection(String server, int port, String username, String password, String database) throws ClassNotFoundException {
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

    protected void insertBan(BanRecord banRecord) {
        String sql = "INSERT INTO `bans` (uuid, expires, reason) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE expires = ?, reason = ?;";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, banRecord.uuid().toString());
            statement.setTimestamp(2, banRecord.endsAt());
            statement.setString(3, banRecord.reason());
            statement.setTimestamp(4, banRecord.endsAt());
            statement.setString(5, banRecord.reason());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected BanRecord getBan(UUID uuid) {
        String sql = "SELECT expires, reason FROM `bans` WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                return new BanRecord(uuid, result.getTimestamp("expires"), result.getString("reason"));
            }
            return null;
        } catch (SQLException e) {
            return null;
        }
    }

    protected void removeBan(UUID uuid) {
        String sql = "DELETE FROM `bans` WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
