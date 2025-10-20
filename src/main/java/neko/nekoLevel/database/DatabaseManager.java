package neko.nekoLevel.database;

import neko.nekoLevel.NekoLevel;
import java.sql.*;

public class DatabaseManager {
    private final NekoLevel plugin;
    private Connection connection;
    
    // MySQL连接配置
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;

    public DatabaseManager(NekoLevel plugin) {
        this.plugin = plugin;
        loadConfig();
        initializeDatabase();
    }
    
    private void loadConfig() {
        // 从配置文件加载数据库配置
        host = plugin.getConfig().getString("database.host", "localhost");
        port = plugin.getConfig().getInt("database.port", 3306);
        database = plugin.getConfig().getString("database.database", "neko_level");
        username = plugin.getConfig().getString("database.username", "root");
        password = plugin.getConfig().getString("database.password", "");
    }

    private void initializeDatabase() {
        try {
            // 使用MySQL数据库
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC";
            connection = DriverManager.getConnection(url, username, password);
            
            // 创建玩家等级表
            String createTableSQL = "CREATE TABLE IF NOT EXISTS player_levels (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "name VARCHAR(16) NOT NULL, " +
                    "level INTEGER NOT NULL DEFAULT 1, " +
                    "experience BIGINT NOT NULL DEFAULT 0" +
                    ");";
            
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTableSQL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initializeDatabase();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}