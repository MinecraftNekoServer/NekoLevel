package neko.nekoLevel;

import neko.nekoLevel.database.DatabaseManager;
import org.bukkit.configuration.file.FileConfiguration;
import java.sql.*;
import java.util.UUID;

public class LevelManager {
    private final NekoLevel plugin;
    private final DatabaseManager databaseManager;
    private final FileConfiguration config;
    private final PlayerCache playerCache;
    
    public LevelManager(NekoLevel plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
        this.config = plugin.getConfig();
        this.playerCache = new PlayerCache(plugin);
        
        // 初始化配置
        plugin.saveDefaultConfig();
    }
    
    public static class PlayerData {

        private int level;

        private long experience;

        private long catFood;

        private int commandPriority;

        private final String name;

        private final UUID uuid;
        
        private long lastAccessTime; // 记录最后访问时间用于缓存管理

        

        public PlayerData(UUID uuid, String name, int level, long experience, long catFood, int commandPriority) {

            this.uuid = uuid;

            this.name = name;

            this.level = level;

            this.experience = experience;

            this.catFood = catFood;

            this.commandPriority = commandPriority;

            this.lastAccessTime = System.currentTimeMillis(); // 初始化最后访问时间

        }

        

        // Getters and setters

        public int getLevel() { return level; }

        public long getExperience() { return experience; }

        public long getCatFood() { return catFood; }

        public int getCommandPriority() { return commandPriority; }

        public String getName() { return name; }

        public UUID getUuid() { return uuid; }

        

        public void setLevel(int level) { this.level = level; }

        public void setExperience(long experience) { this.experience = experience; }

        public void setCatFood(long catFood) { this.catFood = catFood; }

        public void setCommandPriority(int priority) { this.commandPriority = priority; }

        public void addExperience(long exp) { this.experience += exp; }

        public void addCatFood(long food) { this.catFood += food; }

        public void removeCatFood(long food) { this.catFood = Math.max(0, this.catFood - food); }
        
        // 以下方法用于缓存管理
        public long getLastAccessTime() {
            return lastAccessTime;
        }
        
        public void updateLastAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }
    }
    
    /**
     * 获取玩家数据
     */
    public PlayerData getPlayerData(UUID uuid, String name) {
        // 首先检查缓存中是否存在玩家数据
        PlayerData cachedData = playerCache.getPlayerData(uuid);
        if (cachedData != null) {
            return cachedData;
        }
        
        // 缓存中没有，从数据库加载
        PlayerData data = loadPlayerData(uuid, name);
        if (data != null) {
            // 将数据添加到缓存
            playerCache.addPlayerData(uuid, data);
        }
        return data;
    }
    
    /**
     * 从缓存中获取玩家数据（不访问数据库）
     */
    public PlayerData getCachedPlayerData(UUID uuid) {
        return playerCache.getPlayerData(uuid);
    }
    
    /**
     * 将玩家数据从缓存中移除
     */
    public void removePlayerDataFromCache(UUID uuid) {
        playerCache.removePlayerData(uuid);
    }
    
    /**
     * 清空所有缓存
     */
    public void clearCache() {
        playerCache.clearCache();
    }
    
    /**

     * 从数据库加载玩家数据

     */

    private PlayerData loadPlayerData(UUID uuid, String name) {

        String sql = "SELECT level, experience, cat_food, command_priority FROM player_levels WHERE uuid = ?";

        try (Connection conn = databaseManager.getConnection();

             PreparedStatement stmt = conn.prepareStatement(sql)) {

            

            stmt.setString(1, uuid.toString());

            ResultSet rs = stmt.executeQuery();

            

            if (rs.next()) {

                int level = rs.getInt("level");

                long experience = rs.getLong("experience");

                long catFood = rs.getLong("cat_food");

                int commandPriority = rs.getInt("command_priority");

                return new PlayerData(uuid, name, level, experience, catFood, commandPriority);

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

        String sql = "INSERT INTO player_levels (uuid, name, level, experience, cat_food, command_priority) VALUES (?, ?, 1, 0, 0, 0)";

        try (Connection conn = databaseManager.getConnection();

             PreparedStatement stmt = conn.prepareStatement(sql)) {

            

            stmt.setString(1, uuid.toString());

            stmt.setString(2, name);

            stmt.executeUpdate();

            

            return new PlayerData(uuid, name, 1, 0, 0, 0);

        } catch (SQLException e) {

            e.printStackTrace();

            return null;

        }

    }
    
    /**

     * 保存玩家数据到数据库

     */

    public void savePlayerData(PlayerData data) {

        String sql = "INSERT INTO player_levels (uuid, name, level, experience, cat_food) VALUES (?, ?, ?, ?, ?) " +

                     "ON DUPLICATE KEY UPDATE name = VALUES(name), level = VALUES(level), experience = VALUES(experience), cat_food = VALUES(cat_food)";

        try (Connection conn = databaseManager.getConnection();

             PreparedStatement stmt = conn.prepareStatement(sql)) {

            

            stmt.setString(1, data.getUuid().toString());

            stmt.setString(2, data.getName());

            stmt.setInt(3, data.getLevel());

            stmt.setLong(4, data.getExperience());

            stmt.setLong(5, data.getCatFood());

            stmt.executeUpdate();
            
            // 同时更新缓存中的数据
            playerCache.addPlayerData(data.getUuid(), data);

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
        
        // 参考Hypixel的升级系统，使用平滑的多项式函数来平衡低等级和高等级的升级难度
        if (currentLevel < 100) {
            // 前100级：平滑的线性增长，逐渐增加难度
            return 500L + (long)currentLevel * 100L;
        } else if (currentLevel < 500) {
            // 100-500级：平滑的多项式增长
            double base = currentLevel - 99;
            return (long)(base * base * 8.0) + 15000;
        } else {
            // 500级以上：更平滑的指数增长
            double base = currentLevel - 499;
            return (long)(base * base * 20.0) + 100000;
        }
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

    

    /**

     * 获取玩家猫粮数量

     */

    public long getPlayerCatFood(PlayerData data) {

        return data.getCatFood();

    }

    

    /**

     * 设置玩家猫粮数量

     */

    public void setPlayerCatFood(PlayerData data, long catFood) {

        data.setCatFood(catFood);

    }

    

    /**

     * 增加玩家猫粮

     */

    public void addPlayerCatFood(PlayerData data, long catFood) {

        data.addCatFood(catFood);

    }

    

    /**

     * 减少玩家猫粮

     */

    public void removePlayerCatFood(PlayerData data, long catFood) {

        data.removeCatFood(catFood);

    }
    
    /**
     * 奖励玩家猫粮（带提示消息）
     */
    public void rewardPlayerCatFood(PlayerData data, long catFood) {
        data.addCatFood(catFood);
        // 在游戏中发送提示消息
        org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(data.getUuid());
        if (player != null && player.isOnline()) {
            player.sendMessage("§a恭喜你获得 §e" + catFood + " §a个猫粮！当前猫粮: §e" + data.getCatFood());
        }
    }
    
    /**
     * 查询玩家猫粮数量
     * 
     * @param data 玩家数据
     * @return 玩家当前拥有的猫粮数量
     */
    public long getCatFood(PlayerData data) {
        return data.getCatFood();
    }
    
    /**
     * 设置玩家猫粮数量
     * 
     * @param data 玩家数据
     * @param amount 要设置的猫粮数量
     */
    public void setCatFood(PlayerData data, long amount) {
        data.setCatFood(amount);
    }
    
    /**
     * 增加玩家猫粮
     * 
     * @param data 玩家数据
     * @param amount 要增加的猫粮数量
     */
    public void addCatFood(PlayerData data, long amount) {
        data.addCatFood(amount);
    }
    
    /**
     * 扣除玩家猫粮
     * 
     * @param data 玩家数据
     * @param amount 要扣除的猫粮数量
     * @return 是否成功扣除（如果玩家猫粮不足则返回false）
     */
    public boolean removeCatFood(PlayerData data, long amount) {
        if (data.getCatFood() >= amount) {
            data.removeCatFood(amount);
            return true;
        }
        return false;
    }
    
    /**
     * 检查玩家是否有足够的猫粮
     * 
     * @param data 玩家数据
     * @param amount 需要检查的猫粮数量
     * @return 是否有足够的猫粮
     */
    public boolean hasEnoughCatFood(PlayerData data, long amount) {
        return data.getCatFood() >= amount;
    }

}