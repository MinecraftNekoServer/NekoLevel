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

        var levelManager = plugin.getLevelManager();
        var playerData = levelManager.getPlayerData(player.getUniqueId(), player.getName());

        if (playerData == null) {
            return "";
        }

        switch (identifier.toLowerCase()) {
            case "level":
                return String.valueOf(levelManager.getPlayerLevel(playerData));
            case "exp":
                return String.valueOf(levelManager.getPlayerExperience(playerData));
            default:
                return null;
        }
    }
}