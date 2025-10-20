package neko.nekoLevel;

import neko.nekoLevel.command.LevelCommand;
import neko.nekoLevel.database.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class NekoLevel extends JavaPlugin {
    private DatabaseManager databaseManager;
    private LevelManager levelManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        // 初始化数据库管理器
        databaseManager = new DatabaseManager(this);
        
        // 初始化等级管理器
        levelManager = new LevelManager(this, databaseManager);
        
        // 注册指令
        getCommand("nekolevel").setExecutor(new LevelCommand(this));
        
        // 保存默认配置
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public LevelManager getLevelManager() {
        return levelManager;
    }
}
