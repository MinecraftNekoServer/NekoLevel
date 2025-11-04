package neko.nekoLevel.command;

import neko.nekoLevel.LevelManager;
import neko.nekoLevel.NekoLevel;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LevelCommand implements CommandExecutor {
    private final NekoLevel plugin;
    private final LevelManager levelManager;

    public LevelCommand(NekoLevel plugin) {
        this.plugin = plugin;
        this.levelManager = plugin.getLevelManager();
    }
    
    /**
     * 检查玩家是否可以升级
     */
    private void checkLevelUp(LevelManager.PlayerData playerData) {
        long currentExp = levelManager.getPlayerExperience(playerData);
        long expToNextLevel = levelManager.getExperienceToNextLevel(playerData);
        int currentLevel = levelManager.getPlayerLevel(playerData);
        int maxLevel = plugin.getConfig().getInt("max-level", 5000);
        
        // 如果已达到最高等级，不升级
        if (currentLevel >= maxLevel) {
            return;
        }
        
        // 如果当前经验大于等于升级所需经验，则升级
        if (currentExp >= expToNextLevel && expToNextLevel > 0) {
            // 升级并清空经验
            levelManager.setPlayerLevel(playerData, currentLevel + 1);
            levelManager.setPlayerExperience(playerData, currentExp - expToNextLevel);
            
            // 获取玩家对象
            Player player = plugin.getServer().getPlayer(playerData.getUuid());
            if (player != null && player.isOnline()) {
                // 发送升级消息
                player.sendMessage("§a恭喜你升级了！当前等级: §e" + (currentLevel + 1));
                
                // 播放经验音效
                player.playSound(player.getLocation(), "entity.player.levelup", 1.0f, 1.0f);
            }
            
            // 递归检查是否还能继续升级
            checkLevelUp(playerData);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查是否为玩家或控制台
        Player targetPlayer = null;
        String targetPlayerName = "";
        
        if (sender instanceof Player) {
            Player player = (Player) sender;
            
            if (args.length == 0) {
                // 显示当前等级和经验
                LevelManager.PlayerData playerData = levelManager.getPlayerData(player.getUniqueId(), player.getName());
                int level = levelManager.getPlayerLevel(playerData);
                long experience = levelManager.getPlayerExperience(playerData);
                player.sendMessage("§a当前等级: §e" + level);
                player.sendMessage("§a当前经验: §e" + experience);
                return true;
            }
            
            // 检查是否有指定玩家名
            if (args.length >= 2 && player.hasPermission("nekolevel.admin")) {
                targetPlayer = plugin.getServer().getPlayer(args[1]);
                if (targetPlayer != null) {
                    targetPlayerName = targetPlayer.getName();
                }
            }
            
            // 如果没有指定玩家或玩家不在线，则操作自己
            if (targetPlayer == null) {
                targetPlayer = player;
                targetPlayerName = player.getName();
            }
        } else {
            // 控制台命令必须指定玩家名
            if (args.length < 2) {
                sender.sendMessage("§c控制台使用此命令必须指定玩家名");
                sender.sendMessage("§c用法: /nekolevel <指令> <玩家名> [参数]");
                return true;
            }
            
            targetPlayer = plugin.getServer().getPlayer(args[1]);
            if (targetPlayer == null) {
                sender.sendMessage("§c玩家不在线或不存在");
                return true;
            }
            targetPlayerName = targetPlayer.getName();
        }

        LevelManager.PlayerData targetPlayerData = levelManager.getPlayerData(targetPlayer.getUniqueId(), targetPlayerName);
        boolean isSelf = sender instanceof Player && ((Player) sender).getUniqueId().equals(targetPlayer.getUniqueId());

        switch (args[0].toLowerCase()) {
            case "setlevel":
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /nekolevel setlevel <玩家名> <等级>");
                    return true;
                }
                
                if (sender instanceof Player && !((Player) sender).hasPermission("nekolevel.admin")) {
                    sender.sendMessage("§c你没有权限使用此命令");
                    return true;
                }
                
                try {
                    int level = Integer.parseInt(args[args.length == 2 ? 1 : 2]);
                    levelManager.setPlayerLevel(targetPlayerData, level);
                    levelManager.savePlayerData(targetPlayerData);
                    sender.sendMessage("§a已将玩家 " + targetPlayerName + " 的等级设置为: §e" + level);
                    if (!isSelf) {
                        targetPlayer.sendMessage("§a管理员已将你的等级设置为: §e" + level);
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c无效的等级数值");
                }
                break;
                
            case "addlevel":
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /nekolevel addlevel <玩家名> <等级>");
                    return true;
                }
                
                if (sender instanceof Player && !((Player) sender).hasPermission("nekolevel.admin")) {
                    sender.sendMessage("§c你没有权限使用此命令");
                    return true;
                }
                
                try {
                    int level = Integer.parseInt(args[args.length == 2 ? 1 : 2]);
                    int currentLevel = levelManager.getPlayerLevel(targetPlayerData);
                    levelManager.setPlayerLevel(targetPlayerData, currentLevel + level);
                    levelManager.savePlayerData(targetPlayerData);
                    sender.sendMessage("§a已将玩家 " + targetPlayerName + " 的等级增加了: §e" + level);
                    sender.sendMessage("§a当前等级: §e" + levelManager.getPlayerLevel(targetPlayerData));
                    if (!isSelf) {
                        targetPlayer.sendMessage("§a管理员已将你的等级增加了: §e" + level);
                        targetPlayer.sendMessage("§a当前等级: §e" + levelManager.getPlayerLevel(targetPlayerData));
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c无效的等级数值");
                }
                break;
                
            case "setexp":
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /nekolevel setexp <玩家名> <经验>");
                    return true;
                }
                
                if (sender instanceof Player && !((Player) sender).hasPermission("nekolevel.admin")) {
                    sender.sendMessage("§c你没有权限使用此命令");
                    return true;
                }
                
                try {
                    long exp = Long.parseLong(args[args.length == 2 ? 1 : 2]);
                    levelManager.setPlayerExperience(targetPlayerData, exp);
                    // 检查是否可以升级
                    checkLevelUp(targetPlayerData);
                    levelManager.savePlayerData(targetPlayerData);
                    sender.sendMessage("§a已将玩家 " + targetPlayerName + " 的经验设置为: §e" + exp);
                    
                    // 显示当前等级
                    int currentLevel = levelManager.getPlayerLevel(targetPlayerData);
                    sender.sendMessage("§a当前等级: §e" + currentLevel);
                    if (!isSelf) {
                        targetPlayer.sendMessage("§a管理员已将你的经验设置为: §e" + exp);
                        targetPlayer.sendMessage("§a当前等级: §e" + currentLevel);
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c无效的经验数值");
                }
                break;
                
            case "addexp":
                if (args.length < 2) {
                    sender.sendMessage("§c用法: /nekolevel addexp <玩家名> <经验>");
                    return true;
                }
                
                if (sender instanceof Player && !((Player) sender).hasPermission("nekolevel.admin")) {
                    sender.sendMessage("§c你没有权限使用此命令");
                    return true;
                }
                
                try {
                    long exp = Long.parseLong(args[args.length == 2 ? 1 : 2]);
                    long currentExp = levelManager.getPlayerExperience(targetPlayerData);
                    levelManager.setPlayerExperience(targetPlayerData, currentExp + exp);
                    // 检查是否可以升级
                    checkLevelUp(targetPlayerData);
                    levelManager.savePlayerData(targetPlayerData);
                    sender.sendMessage("§a已将玩家 " + targetPlayerName + " 的经验增加了: §e" + exp);
                    
                    // 显示当前等级和经验
                    int currentLevel = levelManager.getPlayerLevel(targetPlayerData);
                    long newExp = levelManager.getPlayerExperience(targetPlayerData);
                    sender.sendMessage("§a当前等级: §e" + currentLevel);
                    sender.sendMessage("§a当前经验: §e" + newExp);
                    if (!isSelf) {
                        targetPlayer.sendMessage("§a你获得了 §e" + exp + " §a点经验");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c无效的经验数值");
                }
                break;
                
            case "reload":
                if (sender instanceof Player && !((Player) sender).hasPermission("nekolevel.admin")) {
                    sender.sendMessage("§c你没有权限使用此命令");
                    return true;
                }
                
                plugin.reloadConfig();
                sender.sendMessage("§a配置文件已重新加载");
                break;
                
            default:
                sender.sendMessage("§c未知子命令");
                sender.sendMessage("§a可用命令:");
                sender.sendMessage("§e/nekolevel - 查看当前等级和经验");
                sender.sendMessage("§e/nekolevel setlevel <玩家名> <等级> - 设置等级");
                sender.sendMessage("§e/nekolevel addlevel <玩家名> <等级> - 增加等级");
                sender.sendMessage("§e/nekolevel setexp <玩家名> <经验> - 设置经验");
                sender.sendMessage("§e/nekolevel addexp <玩家名> <经验> - 增加经验");
                sender.sendMessage("§e/nekolevel reload - 重新加载配置文件");
                break;
        }
        
        return true;
    }
}