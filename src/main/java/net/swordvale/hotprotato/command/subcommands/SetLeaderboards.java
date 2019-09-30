package net.swordvale.hotprotato.command.subcommands;

import net.swordvale.hotprotato.ItemPvP;
import net.swordvale.hotprotato.command.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetLeaderboards extends SubCommand {
    private ItemPvP plugin;

    public SetLeaderboards(ItemPvP plugin){
        super("setleaderboards", "sleaderboards", "setleaderboard", "slb");
        options.playerOnly(true).addPermissions("itempvp.setleaderboards");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args){
        if(!plugin.hasHolograms()){
            sender.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + "Holographic displays required!");
            return;
        } else if(plugin.getConfig().getInt("max-leaderboard-entries") <= 0){
            sender.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + "Max entries are 0. Change this in the configuration!");
            return;
        } else if(plugin.getLeaderboards() != null && !plugin.getLeaderboards().isDeleted()){
            sender.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + "You already have leaderboards set up! To move it, you can use /itempvp tpleaderboards");
        }
        plugin.createHologram(((Player)sender).getLocation());
        sender.sendMessage(ChatColor.GREEN + "Leaderboards created!");
    }
}