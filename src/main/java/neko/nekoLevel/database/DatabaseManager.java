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
    
    // 指令优先级常量
    public static final int COMMAND_PRIORITY_SETLEVEL = 100;
    public static final int COMMAND_PRIORITY_ADDLEVEL = 90;
    public static final int COMMAND_PRIORITY_SETEXP = 80;
    public static final int COMMAND_PRIORITY_ADDEXP = 70;

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
            
            // 创建玩家等级表（添加指令优先级字段和猫粮字段）
            String createTableSQL = "CREATE TABLE IF NOT EXISTS player_levels (" +
                    "uuid VARCHAR(36) PRIMARY KEY, " +
                    "name VARCHAR(16) NOT NULL, " +
                    "level INTEGER NOT NULL DEFAULT 1, " +
                    "experience BIGINT NOT NULL DEFAULT 0, " +
                    "cat_food BIGINT NOT NULL DEFAULT 0, " +
                    "command_priority INTEGER NOT NULL DEFAULT 0" +
                    ");";
            
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTableSQL);
            }
            
            // 检查并添加command_priority列（如果不存在）
            String checkPriorityColumnSQL = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = '" + database + "' AND TABLE_NAME = 'player_levels' " +
                    "AND COLUMN_NAME = 'command_priority'";
            
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(checkPriorityColumnSQL)) {
                
                if (!rs.next()) {
                    // 添加command_priority列
                    String alterTableSQL = "ALTER TABLE player_levels ADD COLUMN command_priority INTEGER NOT NULL DEFAULT 0";
                    stmt.execute(alterTableSQL);
                }
            }
            
            // 检查并添加cat_food列（如果不存在）
            String checkCatFoodColumnSQL = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = '" + database + "' AND TABLE_NAME = 'player_levels' " +
                    "AND COLUMN_NAME = 'cat_food'";
            
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(checkCatFoodColumnSQL)) {
                
                if (!rs.next()) {
                    // 添加cat_food列
                    String alterTableSQL = "ALTER TABLE player_levels ADD COLUMN cat_food BIGINT NOT NULL DEFAULT 0";
                    stmt.execute(alterTableSQL);
                }
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
    
    /**
     * 设置玩家指令优先级
     */
    public void setCommandPriority(String uuid, int priority) {
        String sql = "UPDATE player_levels SET command_priority = ? WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, priority);
            stmt.setString(2, uuid);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取玩家指令优先级
     */
    public int getCommandPriority(String uuid) {
        String sql = "SELECT command_priority FROM player_levels WHERE uuid = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, uuid);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("command_priority");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // 默认优先级
    }
}