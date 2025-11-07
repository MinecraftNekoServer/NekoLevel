package neko.nekoLevel;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class NekoLevelPlaceholderExpansion extends PlaceholderExpansion {
    private final NekoLevel plugin;

    public NekoLevelPlaceholderExpansion(NekoLevel plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "nekolevel";
    }

    @Override
    public String getAuthor() {
        return "不穿胖次の小奶猫";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) {
            return "";
        }

        LevelManager levelManager = plugin.getLevelManager();
        LevelManager.PlayerData playerData = levelManager.getPlayerData(player.getUniqueId(), player.getName());

        if (playerData == null) {
            return "";
        }

        switch (identifier.toLowerCase()) {
            case "level":
                return String.valueOf(levelManager.getPlayerLevel(playerData));
            case "exp":
                long currentExp = levelManager.getPlayerExperience(playerData);
                long expToNext = levelManager.getExperienceToNextLevel(playerData);
                return ExperienceFormatter.formatExperience(currentExp, expToNext);
            case "next_exp":
                return ExperienceFormatter.formatExperience(levelManager.getExperienceToNextLevel(playerData));
            case "progress":
                // 创建10个字符的进度条，使用方形符号
                double progress = levelManager.getLevelProgress(playerData);
                int filledBlocks = (int) Math.round(progress * 10);
                StringBuilder progressBar = new StringBuilder();
                
                // 添加蓝色整体颜色代码和方形字符表示已完成的部分
                progressBar.append("§b"); // 蓝色整体颜色代码
                for (int i = 0; i < filledBlocks; i++) {
                    progressBar.append("■");
                }
                
                // 添加灰色整体颜色代码和方形字符表示未完成的部分
                progressBar.append("§7"); // 灰色整体颜色代码
                for (int i = filledBlocks; i < 10; i++) {
                    progressBar.append("■");
                }
                
                return progressBar.toString();
            default:
                return null;
        }
    }
}