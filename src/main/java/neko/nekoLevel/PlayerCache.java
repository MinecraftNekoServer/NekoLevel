package neko.nekoLevel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * 玩家数据缓存管理器
 */
public class PlayerCache {
    private final Map<UUID, CachedPlayerData> cache = new HashMap<>();
    private final NekoLevel plugin;
    private final long cacheTimeout; // 缓存超时时间（毫秒）

    public PlayerCache(NekoLevel plugin) {
        this.plugin = plugin;
        // 从配置获取缓存超时时间，默认为1小时(3600000毫秒)，以防止内存泄漏
        this.cacheTimeout = plugin.getConfig().getLong("cache.timeout", 3600000L);
        
        // 启动定时任务清理过期缓存
        startCacheCleanupTask();
    }

    /**
     * 获取缓存中的玩家数据
     */
    public LevelManager.PlayerData getPlayerData(UUID uuid) {
        CachedPlayerData cachedData = cache.get(uuid);
        if (cachedData != null) {
            // 检查缓存是否过期
            if (System.currentTimeMillis() - cachedData.getLastAccessTime() > cacheTimeout) {
                cache.remove(uuid);
                return null;
            }
            // 更新最后访问时间
            cachedData.updateLastAccessTime();
            return cachedData.getPlayerData();
        }
        return null;
    }

    /**
     * 将玩家数据添加到缓存
     */
    public void addPlayerData(UUID uuid, LevelManager.PlayerData data) {
        cache.put(uuid, new CachedPlayerData(data));
    }

    /**
     * 从缓存中移除玩家数据
     */
    public void removePlayerData(UUID uuid) {
        cache.remove(uuid);
    }

    /**
     * 检查玩家数据是否在缓存中
     */
    public boolean isCached(UUID uuid) {
        CachedPlayerData cachedData = cache.get(uuid);
        if (cachedData != null) {
            // 检查缓存是否过期
            if (System.currentTimeMillis() - cachedData.getLastAccessTime() > cacheTimeout) {
                cache.remove(uuid);
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 清理所有缓存
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * 启动定时清理过期缓存的任务
     */
    private void startCacheCleanupTask() {
        // 每10分钟检查一次过期缓存
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            Iterator<Map.Entry<UUID, CachedPlayerData>> iterator = cache.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, CachedPlayerData> entry = iterator.next();
                if (currentTime - entry.getValue().getLastAccessTime() > cacheTimeout) {
                    iterator.remove();
                }
            }
        }, 20 * 60 * 10L, 20 * 60 * 10L); // 每10分钟执行一次
    }

    /**
     * 缓存中的玩家数据包装类
     */
    private static class CachedPlayerData {
        private final LevelManager.PlayerData playerData;
        private long lastAccessTime;

        public CachedPlayerData(LevelManager.PlayerData playerData) {
            this.playerData = playerData;
            updateLastAccessTime();
        }

        public LevelManager.PlayerData getPlayerData() {
            return playerData;
        }

        public void updateLastAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }

        public long getLastAccessTime() {
            return lastAccessTime;
        }
    }
}