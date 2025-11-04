package neko.nekoLevel;

import neko.nekoLevel.database.DatabaseManager;
import org.bukkit.configuration.file.FileConfiguration;
import java.sql.*;
import java.util.UUID;

public class LevelManager {
    private final NekoLevel plugin;
    private final DatabaseManager databaseManager;
    private final FileConfiguration config;
    
    public LevelManager(NekoLevel plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.config = plugin.getConfig();
        
        // 初始化配置
        plugin.saveDefaultConfig();
    }
    
    public static class PlayerData {
        private int level;
        private long experience;
        private int commandPriority;
        private final String name;
        private final UUID uuid;
        
        public PlayerData(UUID uuid, String name, int level, long experience, int commandPriority) {
            this.uuid = uuid;
            this.name = name;
            this.level = level;
            this.experience = experience;
            this.commandPriority = commandPriority;
        }
        
        // Getters and setters
        public int getLevel() { return level; }
        public long getExperience() { return experience; }
        public int getCommandPriority() { return commandPriority; }
        public String getName() { return name; }
        public UUID getUuid() { return uuid; }
        
        public void setLevel(int level) { this.level = level; }
        public void setExperience(long experience) { this.experience = experience; }
        public void setCommandPriority(int priority) { this.commandPriority = priority; }
        public void addExperience(long exp) { this.experience += exp; }
    }
    
    /**
     * 获取玩家数据
     */
    public PlayerData getPlayerData(UUID uuid, String name) {
        // 直接从数据库加载，不使用内存缓存
        return loadPlayerData(uuid, name);
    }
    
    /**
     * 从数据库加载玩家数据
     */
    private PlayerData loadPlayerData(UUID uuid, String name) {
        String sql = "SELECT level, experience, command_priority FROM player_levels WHERE uuid = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int level = rs.getInt("level");
                long experience = rs.getLong("experience");
                int commandPriority = rs.getInt("command_priority");
                return new PlayerData(uuid, name, level, experience, commandPriority);
            } else {
                // 玩家不存在，创建新记录
                return createNewPlayerData(uuid, name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 创建新玩家数据
     */
    private PlayerData createNewPlayerData(UUID uuid, String name) {
        String sql = "INSERT INTO player_levels (uuid, name, level, experience, command_priority) VALUES (?, ?, 1, 0, 0)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, uuid.toString());
            stmt.setString(2, name);
            stmt.executeUpdate();
            
            return new PlayerData(uuid, name, 1, 0, 0);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 保存玩家数据到数据库
     */
    public void savePlayerData(PlayerData data) {
        String sql = "INSERT INTO player_levels (uuid, name, level, experience) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE name = VALUES(name), level = VALUES(level), experience = VALUES(experience)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, data.getUuid().toString());
            stmt.setString(2, data.getName());
            stmt.setInt(3, data.getLevel());
            stmt.setLong(4, data.getExperience());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取玩家当前等级
     */
    public int getPlayerLevel(PlayerData data) {
        return data.getLevel();
    }
    
    /**
     * 获取玩家当前经验
     */
    public long getPlayerExperience(PlayerData data) {
        return data.getExperience();
    }
    
    /**
     * 获取升级到下一级所需的经验
     */
    public long getExperienceToNextLevel(PlayerData data) {
        int currentLevel = data.getLevel();
        int maxLevel = config.getInt("max-level", 5000);
        
        // 如果已达到最高等级，返回0
        if (currentLevel >= maxLevel) {
            return 0;
        }
        
        // 指数增长公式：下一级所需经验 = 100 * (当前等级^2)
        // 这样随着等级提升，升级所需经验会显著增加
        return 100L * (long) Math.pow(currentLevel + 1, 2);
    }
    
    /**
     * 获取当前经验距离下一级的进度 (0.0 - 1.0)
     */
    public double getLevelProgress(PlayerData data) {
        long currentExp = data.getExperience();
        long expToNextLevel = getExperienceToNextLevel(data);
        
        // 如果已达到最高等级或下一级经验为0，返回1.0
        if (expToNextLevel <= 0) {
            return 1.0;
        }
        
        // 计算进度比例
        return Math.min(1.0, (double) currentExp / expToNextLevel);
    }
    
    /**
     * 设置玩家等级
     */
    public void setPlayerLevel(PlayerData data, int level) {
        int maxLevel = config.getInt("max-level", 5000);
        if (level >= 1 && level <= maxLevel) {
            data.setLevel(level);
        }
    }
    
    /**
     * 设置玩家经验
     */
    public void setPlayerExperience(PlayerData data, long experience) {
        if (experience >= 0) {
            data.setExperience(experience);
        }
    }
}