package net.swordvale.hotprotato.command.subcommands;

import net.swordvale.hotprotato.ItemPvP;
import net.swordvale.hotprotato.command.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.UUID;

public class Leaderboards extends SubCommand {
    private ItemPvP plugin;

    public Leaderboards(ItemPvP plugin){
        super("leaderboards");
        options.setDescription("View the players with the highest killstreaks!").addPermissions("itempvp.leaderboards");
        this.plugin = plugin;
    }

    public void execute(CommandSender sender, String[] args){
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("leaderboards-title")));
        for(Map.Entry<UUID, Integer> e : plugin.getTopKillstreaks()){
            String format = plugin.getConfig().getString("leaderboards-entry-format");
            format = format.replace("{player}", Bukkit.getOfflinePlayer(e.getKey()).getName());
            if(e.getValue() == null || e.getValue() < 1){
                format = format.replace("{killstreak}", 0 + "");
            } else {
                format = format.replace("{killstreak}", e.getValue() + "");
            }
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', format));
        }
    }
}