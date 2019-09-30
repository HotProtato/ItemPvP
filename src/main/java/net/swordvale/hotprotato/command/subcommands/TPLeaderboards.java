package net.swordvale.hotprotato.command.subcommands;

import net.swordvale.hotprotato.ItemPvP;
import net.swordvale.hotprotato.command.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TPLeaderboards extends SubCommand {
    private ItemPvP plugin;

    public TPLeaderboards(ItemPvP plugin){
        super("tpleaderboards");
        options.playerOnly(true).addPermissions("itempvp.tpleaderboards");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args){
        if(!plugin.hasHolograms()){
            sender.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + "Holographic displays required!");
            return;
        } else if(plugin.getLeaderboards() == null || plugin.getLeaderboards().isDeleted()){
            sender.sendMessage(ChatColor.DARK_RED + "Error: " + ChatColor.RED + "There is no hologram to teleport! Make a new one with /itempvp setleaderboards");
            return;
        }
        plugin.teleportLeaderboards(((Player)sender).getLocation());
        sender.sendMessage(ChatColor.GREEN + "Leaderboards teleported!");
    }
}
