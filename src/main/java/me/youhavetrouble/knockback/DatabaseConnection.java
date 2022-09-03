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
        Class.forName("com.mysql.cj.jdbc.Driver");
        HikariConfig config = new HikariConfig();
        String url = String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s", server, port, database, username, password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl(url);
        config.setMaximumPoolSize(10);
        dataSource = new HikariDataSource(config);
        createBansTable();
        createArchivedBansTable();
    }

    private void createBansTable() {
        String sql = "CREATE TABLE IF NOT EXISTS `bans` (uuid varchar(36) NOT NULL PRIMARY KEY, expires timestamp NULL, reason varchar(512));";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createArchivedBansTable() {
        String sql = "CREATE TABLE IF NOT EXISTS `ban_log` (id int NOT NULL AUTO_INCREMENT PRIMARY KEY, ban_action varchar(32) NOT NULL, ban_time timestamp, target_uuid varchar(36) NOT NULL, source_uuid varchar(36), expires timestamp NULL, reason varchar(512));";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void insertBanLog(ArchievedBanEntry archievedBanEntry) {
        String sql = "INSERT INTO `ban_log` (ban_action, ban_time, target_uuid, source_uuid, expires, reason) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, archievedBanEntry.getAction().toString());
            statement.setTimestamp(2, archievedBanEntry.getTimestamp());
            statement.setString(3, archievedBanEntry.getTargetId().toString());
            statement.setString(4, archievedBanEntry.getSourceId() != null ? archievedBanEntry.getSourceId().toString() : null);
            statement.setTimestamp(5, archievedBanEntry.getTimestamp());
            statement.setString(6, archievedBanEntry.getExtraData());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void insertBan(BanRecord banRecord) {
        System.out.println(banRecord.getTimestamp());
        String sql = "INSERT INTO `bans` (uuid, expires, reason) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE expires = ?, reason = ?;";
        try (Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, banRecord.getUuid().toString());
            statement.setTimestamp(2, banRecord.getTimestamp());
            statement.setString(3, banRecord.getReason());
            statement.setTimestamp(4, banRecord.getTimestamp());
            statement.setString(5, banRecord.getReason());
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
