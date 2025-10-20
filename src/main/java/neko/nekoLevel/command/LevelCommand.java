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

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("只有玩家可以使用此命令");
            return true;
        }

        Player player = (Player) sender;
        var playerData = levelManager.getPlayerData(player.getUniqueId(), player.getName());

        if (args.length == 0) {
            // 显示当前等级和经验
            int level = levelManager.getPlayerLevel(playerData);
            long experience = levelManager.getPlayerExperience(playerData);
            player.sendMessage("§a当前等级: §e" + level);
            player.sendMessage("§a当前经验: §e" + experience);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setlevel":
                if (args.length < 2) {
                    player.sendMessage("§c用法: /nekolevel setlevel <等级>");
                    return true;
                }
                
                if (!player.hasPermission("nekolevel.admin")) {
                    player.sendMessage("§c你没有权限使用此命令");
                    return true;
                }
                
                try {
                    int level = Integer.parseInt(args[1]);
                    levelManager.setPlayerLevel(playerData, level);
                    levelManager.savePlayerData(playerData);
                    player.sendMessage("§a已将你的等级设置为: §e" + level);
                } catch (NumberFormatException e) {
                    player.sendMessage("§c无效的等级数值");
                }
                break;
                
            case "setexp":
                if (args.length < 2) {
                    player.sendMessage("§c用法: /nekolevel setexp <经验>");
                    return true;
                }
                
                if (!player.hasPermission("nekolevel.admin")) {
                    player.sendMessage("§c你没有权限使用此命令");
                    return true;
                }
                
                try {
                    long exp = Long.parseLong(args[1]);
                    levelManager.setPlayerExperience(playerData, exp);
                    levelManager.savePlayerData(playerData);
                    player.sendMessage("§a已将你的经验设置为: §e" + exp);
                } catch (NumberFormatException e) {
                    player.sendMessage("§c无效的经验数值");
                }
                break;
                
            case "reload":
                if (!player.hasPermission("nekolevel.admin")) {
                    player.sendMessage("§c你没有权限使用此命令");
                    return true;
                }
                
                plugin.reloadConfig();
                player.sendMessage("§a配置文件已重新加载");
                break;
                
            default:
                player.sendMessage("§c未知子命令");
                player.sendMessage("§a可用命令:");
                player.sendMessage("§e/nekolevel - 查看当前等级和经验");
                player.sendMessage("§e/nekolevel setlevel <等级> - 设置等级");
                player.sendMessage("§e/nekolevel setexp <经验> - 设置经验");
                player.sendMessage("§e/nekolevel reload - 重新加载配置文件");
                break;
        }
        
        return true;
    }
}