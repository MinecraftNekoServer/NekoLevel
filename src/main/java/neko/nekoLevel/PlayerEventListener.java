package neko.nekoLevel;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.UUID;

/**
 * 玩家事件监听器 - 用于管理玩家数据缓存
 */
public class PlayerEventListener implements Listener {
    private final NekoLevel plugin;

    public PlayerEventListener(NekoLevel plugin) {
        this.plugin = plugin;
    }

    /**
     * 玩家加入服务器时，加载玩家数据到缓存
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        String playerName = event.getPlayer().getName();
        
        // 从数据库加载或获取缓存的玩家数据
        LevelManager.PlayerData playerData = plugin.getLevelManager().getPlayerData(playerUUID, playerName);
        
        // 如果数据加载成功且不在缓存中，将其加载到缓存
        if (playerData != null) {
            // 这一步已在getPlayerData方法中完成
        }
    }

    /**
     * 玩家离开服务器时，从缓存中移除玩家数据
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        
        // 从缓存中移除玩家数据
        plugin.getLevelManager().removePlayerDataFromCache(playerUUID);
    }
}