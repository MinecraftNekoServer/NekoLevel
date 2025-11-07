package neko.nekoLevel;

import neko.nekoLevel.command.LevelCommand;
import neko.nekoLevel.database.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class NekoLevel extends JavaPlugin {
    private DatabaseManager databaseManager;
    private LevelManager levelManager;
    private NekoLevelPlaceholderExpansion placeholderExpansion;
    private NekoLevelAPI nekoLevelAPI;

    @Override
    public void onEnable() {
        // Plugin startup logic
        // 初始化数据库管理器
        databaseManager = new DatabaseManager(this);
        
        // 初始化等级管理器
        levelManager = new LevelManager(this, databaseManager);
        
        // 初始化API
        nekoLevelAPI = new NekoLevelAPI(this);
        
        // 注册指令
        getCommand("nekolevel").setExecutor(new LevelCommand(this));
        
        // 注册变量占位符
        registerPlaceholderExpansion();
        
        // 保存默认配置
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        
        // 注销变量占位符
        if (placeholderExpansion != null) {
            placeholderExpansion.unregister();
        }
    }
    
    private void registerPlaceholderExpansion() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderExpansion = new NekoLevelPlaceholderExpansion(this);
            placeholderExpansion.register();
        }
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public LevelManager getLevelManager() {
        return levelManager;
    }
    
    /**
     * 获取NekoLevel API实例
     * 
     * @return NekoLevelAPI实例
     */
    public NekoLevelAPI getNekoLevelAPI() {
        return nekoLevelAPI;
    }
}
