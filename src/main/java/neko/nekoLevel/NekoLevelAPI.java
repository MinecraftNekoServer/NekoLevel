package neko.nekoLevel;

import org.bukkit.entity.Player;

/**
 * NekoLevel插件的API类，用于其他插件调用猫粮和其他功能
 */
public class NekoLevelAPI {
    private final NekoLevel plugin;
    private final LevelManager levelManager;
    
    public NekoLevelAPI(NekoLevel plugin) {
        this.plugin = plugin;
        this.levelManager = plugin.getLevelManager();
    }
    
    /**
     * 查询玩家猫粮数量
     * 
     * @param player 玩家对象
     * @return 玩家当前拥有的猫粮数量
     */
    public long getCatFood(Player player) {
        LevelManager.PlayerData data = levelManager.getPlayerData(player.getUniqueId(), player.getName());
        return levelManager.getCatFood(data);
    }
    
    /**
     * 设置玩家猫粮数量
     * 
     * @param player 玩家对象
     * @param amount 要设置的猫粮数量
     */
    public void setCatFood(Player player, long amount) {
        LevelManager.PlayerData data = levelManager.getPlayerData(player.getUniqueId(), player.getName());
        levelManager.setCatFood(data, amount);
        levelManager.savePlayerData(data);
    }
    
    /**
     * 增加玩家猫粮
     * 
     * @param player 玩家对象
     * @param amount 要增加的猫粮数量
     */
    public void addCatFood(Player player, long amount) {
        LevelManager.PlayerData data = levelManager.getPlayerData(player.getUniqueId(), player.getName());
        levelManager.addCatFood(data, amount);
        levelManager.savePlayerData(data);
    }
    
    /**
     * 扣除玩家猫粮
     * 
     * @param player 玩家对象
     * @param amount 要扣除的猫粮数量
     * @return 是否成功扣除（如果玩家猫粮不足则返回false）
     */
    public boolean removeCatFood(Player player, long amount) {
        LevelManager.PlayerData data = levelManager.getPlayerData(player.getUniqueId(), player.getName());
        boolean success = levelManager.removeCatFood(data, amount);
        if (success) {
            levelManager.savePlayerData(data);
        }
        return success;
    }
    
    /**
     * 检查玩家是否有足够的猫粮
     * 
     * @param player 玩家对象
     * @param amount 需要检查的猫粮数量
     * @return 是否有足够的猫粮
     */
    public boolean hasEnoughCatFood(Player player, long amount) {
        LevelManager.PlayerData data = levelManager.getPlayerData(player.getUniqueId(), player.getName());
        return levelManager.hasEnoughCatFood(data, amount);
    }
    
    /**
     * 奖励玩家猫粮（带提示消息）
     * 
     * @param player 玩家对象
     * @param amount 要奖励的猫粮数量
     */
    public void rewardCatFood(Player player, long amount) {
        LevelManager.PlayerData data = levelManager.getPlayerData(player.getUniqueId(), player.getName());
        levelManager.rewardPlayerCatFood(data, amount);
    }
    
    /**
     * 获取玩家等级
     * 
     * @param player 玩家对象
     * @return 玩家当前等级
     */
    public int getPlayerLevel(Player player) {
        LevelManager.PlayerData data = levelManager.getPlayerData(player.getUniqueId(), player.getName());
        return levelManager.getPlayerLevel(data);
    }
    
    /**
     * 获取玩家经验
     * 
     * @param player 玩家对象
     * @return 玩家当前经验
     */
    public long getPlayerExperience(Player player) {
        LevelManager.PlayerData data = levelManager.getPlayerData(player.getUniqueId(), player.getName());
        return levelManager.getPlayerExperience(data);
    }
}